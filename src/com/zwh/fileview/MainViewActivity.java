package com.zwh.fileview;

import java.io.File;
import java.sql.Timestamp;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
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
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainViewActivity extends Activity {
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mMainListView;
	private ListView mIndexListView;
	private TextView mPromptTextView;

	private ListViewAdapter mMainListViewAdapter;
	private Menu mOptionsMenu;

	private static String TAG = "FileView";
	private File currentFile;
	private File mClipboard;
	private boolean mCut = false;

	private String[] mIndexDirectory= {
			File.listRoots()[0].getPath(),
			Environment.getExternalStorageDirectory().getPath(),
			"/mnt/external_sd",
			"/mnt/usb_storage",
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mainview);
		mPromptTextView = (TextView) findViewById(R.id.prompt_view);

		mMainListView = (ListView) findViewById(R.id.main_view);
		mMainListViewAdapter = new ListViewAdapter(this, File.listRoots()[0].listFiles());
		mMainListView.setAdapter(mMainListViewAdapter);
		mMainListView.setOnItemClickListener(new MainListOnItemClickListener());
		mMainListView.setOnItemLongClickListener(new MainListOnItemLongClickListener());


		mIndexListView = (ListView) findViewById(R.id.index_view);
		mIndexListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.general_dir)));
		mIndexListView.setOnItemClickListener(new DrawerItemClickListener());

		setDrawerToggle();
		setContextualAction();
		initTab();

		String path = getIntent().getStringExtra(FileOpertion.SHORTCUT_PATH);
		if( null != path ){
			refrestTab(path);
		}
	}

	private void initTab(){
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowHomeEnabled(true);
		bar.setDisplayShowTitleEnabled(false);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		addTab(new File(mIndexDirectory[0]));
	}

	private void refrestTab(String path){
		ActionBar bar = getActionBar();
		bar.removeAllTabs();

		Log.i("refrestTab", ""+path);
		InvertedAddTab(new File(path));
	}

	private void InvertedAddTab(File file){
		if( null != file ){
			InvertedAddTab( file.getParentFile() );
			addTab(file);
		}
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
		currentFile = (File) tab.getTag();
		refreshList(currentFile.listFiles());

		ActionBar bar = getActionBar();
		int index = bar.getSelectedNavigationIndex();
		for (int i = bar.getTabCount() - 1; i > index; i--) {
			bar.removeTabAt(i);
		}
		if( null != mOptionsMenu ){
			mOptionsMenu.findItem(R.id.add_folder).setVisible(currentFile.canWrite());

			if( currentFile.canWrite() && null != mClipboard){
				mOptionsMenu.findItem(R.id.paste).setVisible(true);
			}
			else{
				mOptionsMenu.findItem(R.id.paste).setVisible(false);
			}
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
			private File selected;
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				// Here you can do something when items are selected/de-selected,
				// such as update the title in the CAB
				//mMainListView.setItemChecked(position, checked);
				Log.i(TAG, "onItemCheckedStateChanged position:"+ position+ ",id:"+id+",checked:"+checked);
				//View itemView = mMainListView.getChildAt(position);
				//itemView.setBackgroundResource(android.R.color.white);

				selected = (File) mMainListView.getItemAtPosition(position);
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// Respond to clicks on the actions in the CAB
				switch (item.getItemId()) {
				case R.id.copy:
					copyFile(selected);
					break;
				case R.id.cut:
					cutFile(selected);
					break;
				case R.id.delete:
					deleteSelectedItems(selected);
					break;
				case R.id.rename:
					renameSelectedItems(selected);
					break;
				case R.id.shortcut:
					FileOpertion.addShortcut(MainViewActivity.this, selected);
					break;
				case R.id.share:
					FileOpertion.shareFiles(MainViewActivity.this, selected);
					break;
				case R.id.detail:
					showDetailSelectedItems(selected);
					break;

				default:
					return false;
				}
				mode.finish();
				return true;
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
				Log.i(TAG, "onDestroyActionMode");
				if( null != mOptionsMenu && currentFile.canWrite() && null != mClipboard){
					mOptionsMenu.findItem(R.id.paste).setVisible(true);
				}
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// Here you can perform updates to the CAB due to
				// an invalidate() request
				return false;
			}
		});
	}

	private void deleteSelectedItems(final File file){
		new AlertDialog.Builder(this)
		.setTitle(R.string.sure_delete_file)
		//.setMessage(R.string.sure_delete_file)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton) {
				if(FileOpertion.DelFile(file)){
					Log.i(TAG, "delete");
					refreshList(currentFile.listFiles());
				}
				else{
					Toast.makeText(MainViewActivity.this,
							getString(R.string.delete_file_failed),
							Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, null).show();
	}

	private void renameSelectedItems(final File file){
		final EditText inputEditText = new EditText(this);
		inputEditText.setSingleLine();

		new AlertDialog.Builder(this)
		.setTitle(R.string.str_rename_text)
		.setView(inputEditText)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton) {
				String modName = inputEditText.getText().toString();
				if (FileOpertion.isLegitimateFileName(modName)){
					int ret = FileOpertion.RenameFile(file, modName);

					switch (ret) {
					case FileOpertion.SUCCESS:
						refreshList(currentFile.listFiles());
						break;
					case FileOpertion.FILE_IS_ALREADY_EXITS:
						Toast.makeText(MainViewActivity.this,
								getString(R.string.file_is_exist_already),
								Toast.LENGTH_SHORT).show();
						break;
					case FileOpertion.FAILED:
						Toast.makeText(MainViewActivity.this,
								getString(R.string.rename_file_failed),
								Toast.LENGTH_SHORT).show();
						break;

					default:
						break;
					}
				}
				else{
					Toast.makeText(MainViewActivity.this,
							getString(R.string.wrong_file_name),
							Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, null).show();
	}

	private void showDetailSelectedItems(final File file){
		ListView listView = new ListView(this);
		ArrayList<HashMap<String, String>> dataArrayList = new ArrayList<HashMap<String, String>>();  

		HashMap<String, String> map = new HashMap<String, String>();  
		map.put("name", getString(R.string.path));
		map.put("value", file.getPath());
		dataArrayList.add(map);

		map = new HashMap<String, String>();  
		map.put("name", getString(R.string.lastModified));
		long modify	= file.lastModified();
		map.put("value", new Timestamp(modify).toString());
		dataArrayList.add(map);

		map = new HashMap<String, String>();  
		map.put("name", getString(R.string.size));
		long size	= FileOpertion.getFileSize(file);
		map.put("value", FileOpertion.formetFileSize(size));
		dataArrayList.add(map);

		if(file.isDirectory()){
			map = new HashMap<String, String>();  
			map.put("name", getString(R.string.file_num));
			map.put("value", String.valueOf(FileOpertion.getFileNumInDirectory(file)));
			dataArrayList.add(map);
		}

		map = new HashMap<String, String>();  
		map.put("name", getString(R.string.can_read));
		map.put("value", file.canRead()? getString(R.string.yes):getString(R.string.no));
		dataArrayList.add(map);

		map = new HashMap<String, String>();  
		map.put("name", getString(R.string.can_write));
		map.put("value", file.canWrite()? getString(R.string.yes):getString(R.string.no));
		dataArrayList.add(map);

		map = new HashMap<String, String>();  
		map.put("name", getString(R.string.can_execute));
		map.put("value", file.canExecute()? getString(R.string.yes):getString(R.string.no));
		dataArrayList.add(map);

		SimpleAdapter mSchedule = new SimpleAdapter(this,
				dataArrayList, 
				R.layout.simple_list_item_2,
				new String[] {"name", "value"},   
				new int[] {R.id.text1, R.id.text2});
		listView.setAdapter(mSchedule);


		new AlertDialog.Builder(this)
		.setTitle(file.getName())
		.setIcon(FileOpertion.getDrawable(this, file))
		.setView(listView).show();
		//.setMessage(stringBuilder.toString()).show();
	}
	private void copyFile(File selected){
		mClipboard = selected;
		Log.i(TAG, "onActionItemClicked copy");
		Toast.makeText(MainViewActivity.this,
				getString(R.string.copy_file_to_clipboard),
				Toast.LENGTH_SHORT).show();
	}

	private void pasteCopyFile(){
		File targetFile = new File(currentFile, mClipboard.getName());
		int ret = FileOpertion.copyFile(mClipboard, targetFile);

		switch (ret) {
		case FileOpertion.SUCCESS:
			refreshList(currentFile.listFiles());
			break;
		case FileOpertion.FILE_IS_ALREADY_EXITS:
			Toast.makeText(MainViewActivity.this,
					getString(R.string.file_is_exist_already),
					Toast.LENGTH_SHORT).show();
			break;
		case FileOpertion.FAILED:
			Toast.makeText(MainViewActivity.this,
					getString(R.string.paste_file_failed),
					Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}

	private void cutFile(File selected){
		mClipboard = selected;
		mCut = true;
		Toast.makeText(MainViewActivity.this,
				getString(R.string.cut_file_to_clipboard),
				Toast.LENGTH_SHORT).show();
		Log.i(TAG, "onActionItemClicked cut");
	}

	private void pasteCutFile(){
		int ret = FileOpertion.cutPasteFile(mClipboard, currentFile);

		switch (ret) {
		case FileOpertion.SUCCESS:
			refreshList(currentFile.listFiles());
			mCut = false;
			mClipboard = null;
			break;
		case FileOpertion.FILE_IS_ALREADY_EXITS:
			Toast.makeText(MainViewActivity.this,
					getString(R.string.file_is_exist_already),
					Toast.LENGTH_SHORT).show();
			break;
		case FileOpertion.FAILED:
			Toast.makeText(MainViewActivity.this,
					getString(R.string.cut_file_failed),
					Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}

	private class MainListOnItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			File currentFile = mMainListViewAdapter.getItem(position);
			Log.i("MainListOnItemClickListener", "position:"+position+",currentFile:"+currentFile.getName());
			Log.v("v","isDirectory: "+ currentFile.isDirectory() );
			if( currentFile.isDirectory() ){
				//refreshList(currentFile.listFiles());
				addTab(currentFile);
			}
			else{
				FileOpertion.openFile(MainViewActivity.this, currentFile);
			}
		}
	}

	private class MainListOnItemLongClickListener implements OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			//view.setBackgroundResource(android.R.color.black);
			/*if (mActionMode != null) {
				return false;
			}

			// Start the CAB using the ActionMode.Callback defined above
			mActionMode = startActionMode(mActionModeCallback);
			view.setSelected(true);*/
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
				holder.fileIconImageView = (ImageView) convertView.findViewById(R.id.file_icon_iamgeview);  

				convertView.setTag(holder);
			} else {  
				holder = (ViewHolder) convertView.getTag();
			}  

			holder.fileNameTextView.setText(fileList[position].getName());
			Drawable drawableLeft = FileOpertion.getDrawable(mContext, fileList[position]);
			//drawableLeft.setBounds(1, 1, 50, 1);
			//holder.fileNameTextView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
			holder.fileIconImageView.setImageDrawable(drawableLeft);
			return convertView;
		}  

		/** 
		 * ViewHolder类用以储存item中控件的引用 
		 */  
		final class ViewHolder {  
			TextView fileNameTextView;
			ImageView fileIconImageView;
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
			if(mCut){
				pasteCutFile();
			}
			else{
				pasteCopyFile();
			}
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
				if (FileOpertion.isLegitimateFileName(modName)){
					mkdirs(modName);
				}
				else{
					Toast.makeText(MainViewActivity.this,
							getString(R.string.wrong_file_name),
							Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, null).show();
	}

	private void mkdirs(String fileName){
		/*ActionBar bar = getActionBar();
		Tab tab = bar.getTabAt( bar.getTabCount() - 1 );
		final File currentFile = (File) tab.getTag();*/
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
				refreshList(currentFile.listFiles());
			}
			Log.e("path", filePath+","+created);
		}
		else{
			Toast.makeText(MainViewActivity.this,
					getString(R.string.directory_is_exist_already),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void refreshList(File[] dataList){
		if( null == dataList || dataList.length < 1){
			Log.e(TAG, "wow this floder is empty!");
			mPromptTextView.setVisibility(View.VISIBLE);
		}
		else if(mPromptTextView.getVisibility()==View.VISIBLE){
			mPromptTextView.setVisibility(View.INVISIBLE);
		}
		mMainListViewAdapter.setFileData(dataList);
		mMainListViewAdapter.notifyDataSetChanged();
		mMainListView.invalidate();
	}

	private Comparator<File> fileComparator = new Comparator<File>() {

		private Collator collator = Collator.getInstance(); //调入这个是解决中文排序问题 
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

			if(rhs.isDirectory() && !lhs.isDirectory()){
				return 1;
			}

			if(!rhs.isDirectory() && lhs.isDirectory()){
				return -1;
			}

			return lkey.compareTo(rkey);
		}
	};

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//selectItem(position);
			mDrawerLayout.closeDrawer(mIndexListView);
			refrestTab(mIndexDirectory[position]);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		String path = intent.getStringExtra(FileOpertion.SHORTCUT_PATH);
		refrestTab(path);
		super.onNewIntent(intent);
	}
}
