package com.zwh.fileview;

import java.io.File;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainViewActivity extends Activity {
	private String[] mPlanetTitles = {"device","network","cloud"};
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mMainListView;
	private ListView mIndexListView;

	private ListViewAdapter mMainListViewAdapter;
	private Menu mOptionsMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mainview);
		mMainListView = (ListView) findViewById(R.id.main_view);
		mMainListViewAdapter = new ListViewAdapter(this, File.listRoots()[0].listFiles());
		mMainListView.setAdapter(mMainListViewAdapter);
		mMainListView.setOnItemClickListener(new MainListOnItemClickListener());
		mMainListView.setOnItemLongClickListener(new MainListOnItemLongClickListener());


		mIndexListView = (ListView) findViewById(R.id.index_view);
		mIndexListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mPlanetTitles));
		mIndexListView.setOnItemClickListener(new DrawerItemClickListener());

		setDrawerToggle();
		setContextualAction();
		initTab();
	}

	private void initTab(){
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowHomeEnabled(true);
		bar.setDisplayShowTitleEnabled(false);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		addTab(File.listRoots()[0]);
	}

	private void addTab(File tabDir){
		ActionBar bar = getActionBar();
		ActionBar.Tab tab = bar.newTab();
		String name = tabDir.getName();
		if( null == name || name.isEmpty()){
			name = "Home";
		}

		tab.setText(name);
		//tab.setIcon(R.drawable.ic_launcher);
		tab.setTag(tabDir);
		tab.setTabListener(tabListener);
		bar.addTab(tab);

		bar.selectTab(tab);
	}

	private TabListener tabListener = new TabListener() {

		@Override
		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
			// TODO Auto-generated method stub
			tabSelected(tab);
		}

		@Override
		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
			// TODO Auto-generated method stub

		}
	};

	private void tabSelected(Tab tab){
		File currentFile = (File) tab.getTag();
		mMainListViewAdapter.setFileData(currentFile.listFiles());
		mMainListViewAdapter.notifyDataSetChanged();
		mMainListView.invalidate();

		ActionBar bar = getActionBar();
		int index = bar.getSelectedNavigationIndex();
		for (int i = bar.getTabCount() - 1; i > index; i--) {
			bar.removeTabAt(i);
		}
		if( null != mOptionsMenu ){
			mOptionsMenu.findItem(R.id.add_folder).setVisible(currentFile.canWrite());
		}
	}

	private void setDrawerToggle(){
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer_light, 
				R.string.drawer_open, 
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			@SuppressLint("NewApi")
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				invalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void setContextualAction(){
		mMainListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mMainListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				// Here you can do something when items are selected/de-selected,
				// such as update the title in the CAB
				//mMainViewList.setItemChecked(position, checked);
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// Respond to clicks on the actions in the CAB
				switch (item.getItemId()) {
				case R.id.delete:
					deleteSelectedItems();
					mode.finish(); // Action picked, so close the CAB
					return true;
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// Inflate the menu for the CAB
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.contextual_action, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// Here you can make any necessary updates to the activity when
				// the CAB is removed. By default, selected items are deselected/unchecked.
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// Here you can perform updates to the CAB due to
				// an invalidate() request
				return false;
			}
		});
	}

	private void deleteSelectedItems(){

	}

	private class MainListOnItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			File currentFile = mMainListViewAdapter.getItem(position);
			Log.i("MainListOnItemClickListener", "position:"+position+",currentFile:"+currentFile.getName());
			Log.v("v","isDirectory: "+ currentFile.isDirectory() );
			if( currentFile.isDirectory() ){
				mMainListViewAdapter.setFileData(currentFile.listFiles());
				mMainListViewAdapter.notifyDataSetChanged();
				mMainListView.invalidate();

				addTab(currentFile);
			}
			else{
				//open
			}
		}
	}
	
	private class MainListOnItemLongClickListener implements OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			view.setBackgroundResource(android.R.color.white);
			return false;
		}
	}

	public class ListViewAdapter extends BaseAdapter {
		private Context mContext;
		private File[] fileList;

		public ListViewAdapter(Context context, File[] dataList) {  
			this.mContext = context;  
			setFileData(dataList);
		}  

		public void setFileData(File[] dataList){
			if( null != dataList ){
				Arrays.sort(dataList, fileComparator);
			}

			this.fileList = dataList;
		}

		@Override  
		public int getCount() {
			if( null == fileList ){
				return 0;
			}
			return fileList.length; 
		}  

		@Override  
		public File getItem(int position) {  
			return fileList[position];
		}  

		@Override  
		public long getItemId(int position) {  
			return position;
		}  

		@Override  
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;  
			if (convertView == null) {  
				holder = new ViewHolder();  
				convertView = LayoutInflater.from(mContext).inflate(  
						R.layout.main_listview_item, null);
				holder.fileNameTextView = (TextView) convertView.findViewById(R.id.file_name_textview);  

				convertView.setTag(holder);
			} else {  
				holder = (ViewHolder) convertView.getTag();
			}  

			holder.fileNameTextView.setText(fileList[position].getName());  
			return convertView;  
		}  

		/** 
		 * ViewHolder�����Դ���item�пؼ������� 
		 */  
		final class ViewHolder {  
			TextView fileNameTextView;
		}
	}  

	@Override  
	public boolean onCreateOptionsMenu(Menu menu){  
		MenuInflater inflater = getMenuInflater();  
		inflater.inflate(R.menu.action, menu);
		mOptionsMenu = menu;
		return true;
	}  

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mIndexListView);
		//menu.findItem(R.id.action_bar_title).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle your other action bar items...
		switch (item.getItemId()) {
		case R.id.add_folder:
			makeNewDirDialog();
			break;
		case R.id.search:

			break;
		case R.id.paste:

			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void makeNewDirDialog() {
		final EditText inputEditText = new EditText(this);
		inputEditText.setSingleLine();

		new AlertDialog.Builder(this)
		.setTitle(R.string.str_newdir_text)  
		.setView(inputEditText)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton) {
				String modName = inputEditText.getText().toString();
				if (isLegitimateFileName(modName)){
					mkdirs(modName);
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, null).show();
	}

	private boolean isLegitimateFileName(String fileName){
		boolean result = true;
		if (null == fileName
				|| fileName.isEmpty()
				|| fileName.contains("\\") 
				|| fileName.contains("/")
				|| fileName.contains(":")
				|| fileName.contains("*")
				|| fileName.contains("?")
				|| fileName.contains("\"")
				|| fileName.contains("<")
				|| fileName.contains(">")
				|| fileName.contains("|")) {

			Toast.makeText(MainViewActivity.this,
					getString(R.string.wrong_file_name),
					Toast.LENGTH_SHORT).show();
			result = false;
		}
		return result;
	}

	private void mkdirs(String fileName){
		ActionBar bar = getActionBar();
		Tab tab = bar.getTabAt( bar.getTabCount() - 1 );
		final File currentFile = (File) tab.getTag();
		final String path = currentFile.getPath();
		String filePath;
		if(path.equals(File.separator)){
			filePath = fileName;
		}
		else{
			filePath = path + File.separator + fileName;
			filePath = filePath.substring(1);
		}

		File newFloder = new File(filePath);
		
		if(!newFloder.exists()){
			boolean created = newFloder.mkdirs();
			if(created){
				mMainListViewAdapter.setFileData(currentFile.listFiles());
				mMainListViewAdapter.notifyDataSetChanged();
				mMainListView.invalidate();
			}
			Log.e("path", filePath+","+created);
		}
		else{
			Toast.makeText(MainViewActivity.this,
					getString(R.string.directory_is_exist_already),
					Toast.LENGTH_SHORT).show();
		}
	}

	private Comparator<File> fileComparator = new Comparator<File>() {

		private Collator collator = Collator.getInstance(); //��������ǽ�������������� 
		private Map<File, CollationKey> map = new HashMap<File, CollationKey>();
		private CollationKey lkey; 
		private CollationKey rkey; 
		private String label;
		private Object object;
		@Override
		public int compare(File lhs, File rhs) {
			// TODO Auto-generated method stub

			object = map.get(lhs);
			if( null != object ){
				lkey = (CollationKey) object;
			}
			else{
				label = lhs.getName();
				lkey = collator.getCollationKey(label.toLowerCase(Locale.CHINESE));
				map.put(lhs, lkey);
			}

			object = map.get(rhs);
			if( null != object ){
				rkey = (CollationKey) object;
			}
			else{
				label = rhs.getName();
				rkey = collator.getCollationKey(label.toLowerCase(Locale.CHINESE));
				map.put(rhs, rkey);
			}

			/*if(rhs.isDirectory() && lhs.isFile()){
				return 1;
			}

			if(rhs.isFile() && lhs.isDirectory()){
				return -1;
			}*/

			return lkey.compareTo(rkey);
		}
	};

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		mIndexListView.setItemChecked(position, true);
		setTitle(mPlanetTitles[position]);
		mDrawerLayout.closeDrawer(mIndexListView);
	}
}
