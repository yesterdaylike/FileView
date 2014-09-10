package com.zwh.fileview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

public class FileOpertion {
	private static String TAG = "FileOpertion";

	public static final int SUCCESS = 0x0800;
	public static final int FILE_IS_ALREADY_EXITS = 0x0801;
	public static final int FAILED = 0x0802;

	public static final String SHORTCUT_PATH = "shortcut_path";

	public static final String str_audio_type = "audio/*";
	public static final String str_video_type = "video/*";
	public static final String str_image_type = "image/*";
	public static final String str_txt_type = "text/plain";
	public static final String str_pdf_type = "application/pdf";
	public static final String str_epub_type = "application/epub+zip";
	public static final String str_apk_type = "application/vnd.android.package-archive";

	public static final String str_flash_path = Environment.getExternalStorageDirectory().getPath();

	public static boolean DelFile(File delFile){
		Log.i(TAG, "DelFile: "+delFile.getPath());
		boolean ret = true;

		if(delFile.isDirectory()){
			if( null!= delFile.listFiles()){
				for (File file : delFile.listFiles()) {
					ret &= DelFile(file);
				}
			}
		}
		ret = delFile.delete();
		if(!ret) Log.e(TAG, "Delete file " + delFile.getPath() + " fail");
		Log.v(TAG, "DelFile: "+delFile.getPath());
		return ret;
	}
	public static int RenameFile(File file,String newName){
		Log.i(TAG, "RenameFile: "+file.getPath()+" to: "+newName);

		int ret = SUCCESS;

		String oldPath = file.getPath();
		int lastSeparator = oldPath.lastIndexOf(File.separator);
		String newPath = oldPath.substring(0,lastSeparator+1).concat(newName);

		File newFile = new File(newPath);

		if(newFile.exists()){
			ret = FILE_IS_ALREADY_EXITS;
		}
		else{
			if(file.renameTo(newFile)){
				ret = SUCCESS;
			}
			else{
				ret = FAILED;
			}
		}
		return ret;
	}

	public static int cutPasteFile(File sourceFile , File newDir){
		Log.i(TAG, "cutPasteFile: "+sourceFile.getPath()+" to: "+newDir.getPath());
		int ret = SUCCESS;
		File newFile = new File(newDir, sourceFile.getName());

		if(newFile.exists()){
			ret = FILE_IS_ALREADY_EXITS;
		}

		else{
			if(sourceFile.renameTo(newFile)){
				ret = SUCCESS;
			}
			else{
				ret = FAILED;
			}
		}
		return ret;
	}

	public static boolean isLegitimateFileName(String fileName){
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

			result = false;
		}
		return result;
	}

	/**
	 * 为当前应用添加桌面快捷方式
	 * 
	 * @param context
	 * @param fileInfo 
	 *            
	 */
	public static void addShortcut(Context context , File fileInfo) {
		String file_type = getMIMEType(context, fileInfo);

		// 创建快捷方式的Intent
		Intent shortcutIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		// 不允许重复创建
		shortcutIntent.putExtra("duplicate", false);
		// 需要现实的名称
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				fileInfo.getName());

		Intent intent = null;
		Parcelable icon = null;
		if(fileInfo.isDirectory()) { // 若点击的是文件夹
			intent = new Intent(context, MainViewActivity.class);
			intent.setAction("android.intent.action.MAIN");
			intent.addCategory("android.intent.category.LAUNCHER");
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.setDataAndType(
					Uri.fromFile(fileInfo), file_type);

			intent.putExtra(SHORTCUT_PATH, fileInfo.getPath());
			icon = Intent.ShortcutIconResource.fromContext(
					context, R.drawable.ic_drive_floder);
		} else {
			intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(fileInfo),
					file_type);
			// 快捷图片--文件类型的图标
			icon = Intent.ShortcutIconResource.fromContext(
					context, getDrawableId(file_type));
		}

		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		context.sendBroadcast(shortcutIntent);
	}


	/* 判断文件MimeType的方法 */
	public static String getMIMEType(Context context, File file)
	{ 
		String type="";
		String fileName=file.getName();
		/* 取得扩展名 */
		String end=fileName.substring(fileName.lastIndexOf(".")+1,
				fileName.length()).toLowerCase(); 

		/* 依附档名的类型决定MimeType */
		if(end.equalsIgnoreCase("mp3")||end.equalsIgnoreCase("wma")
				||end.equalsIgnoreCase("mp1")||end.equalsIgnoreCase("mp2")
				||end.equalsIgnoreCase("ogg")||end.equalsIgnoreCase("oga")
				||end.equalsIgnoreCase("flac")||end.equalsIgnoreCase("ape")
				||end.equalsIgnoreCase("wav")||end.equalsIgnoreCase("aac")
				||end.equalsIgnoreCase("m4a")||end.equalsIgnoreCase("m4r")
				||end.equalsIgnoreCase("amr")||end.equalsIgnoreCase("mid")
				||end.equalsIgnoreCase("asx")
				/*
		         ||end.equalsIgnoreCase("mid")||end.equalsIgnoreCase("amr")
		    	 ||end.equalsIgnoreCase("awb")||end.equalsIgnoreCase("midi")
		    	 ||end.equalsIgnoreCase("xmf")||end.equalsIgnoreCase("rtttl")
		    	 ||end.equalsIgnoreCase("smf")||end.equalsIgnoreCase("imy")
		    	 ||end.equalsIgnoreCase("rtx")||end.equalsIgnoreCase("ota")*/
				)
		{
			type = str_audio_type; 
		}
		else if(end.equalsIgnoreCase("3gp")||end.equalsIgnoreCase("mp4")
				||end.equalsIgnoreCase("rmvb")||end.equalsIgnoreCase("3gpp")
				||end.equalsIgnoreCase("avi")||end.equalsIgnoreCase("rm")
				||end.equalsIgnoreCase("mov")||end.equalsIgnoreCase("flv")
				||end.equalsIgnoreCase("mkv")||end.equalsIgnoreCase("wmv")
				||end.equalsIgnoreCase("divx")||end.equalsIgnoreCase("bob")
				||end.equalsIgnoreCase("mpg")||end.equalsIgnoreCase("dat")
				||end.equalsIgnoreCase("vob")||end.equalsIgnoreCase("asf"))
		{
			type = str_video_type;
			if(end.equalsIgnoreCase("3gpp")){
				if(isVideoFile(context, file)){
					type = str_video_type;
				}else{
					type = str_audio_type; 
				}
			}
		}
		else if(end.equalsIgnoreCase("jpg")||end.equalsIgnoreCase("gif")
				||end.equalsIgnoreCase("png")||end.equalsIgnoreCase("jpeg")
				||end.equalsIgnoreCase("bmp"))
		{
			type = str_image_type;
		}
		else if(end.equalsIgnoreCase("txt"))
		{
			type = str_txt_type;
		}
		else if(end.equalsIgnoreCase("epub") || end.equalsIgnoreCase("pdb") || end.equalsIgnoreCase("fb2") || end.equalsIgnoreCase("rtf"))
		{
			type = str_epub_type;
		}
		else if(end.equalsIgnoreCase("pdf"))
		{
			type = str_pdf_type;
		}
		else if(end.equalsIgnoreCase("apk"))
		{
			type = str_apk_type;  
		}
		else
		{
			/* 如果无法直接打开，就跳出软件列表给用户选择 */
			type="*/*";
		}

		return type; 
	}

	public static boolean isVideoFile(Context context, File tmp_file){
		String path = tmp_file.getPath();
		ContentResolver resolver = context.getContentResolver();
		String[] audiocols = new String[] {
				MediaStore.Video.Media._ID,
				MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.TITLE
		};  
		Log.i(TAG,"in getFileUri --- path = " + path);
		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Video.Media.DATA + "=" + "'" + path + "'");
		Cursor cur = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				audiocols,
				where.toString(), null, null);
		if(cur.moveToFirst()){
			return true;
		}
		return false;
	}

	public static int getDrawableId(String tmp_type){
		int drawableId = R.drawable.ic_drive_unknown;
		if(tmp_type.equals(str_audio_type)){
			drawableId = R.drawable.ic_drive_audio;
		}else if(tmp_type.equals(str_video_type)){
			drawableId = R.drawable.ic_drive_video;
		}else if(tmp_type.equals(str_image_type)){
			drawableId = R.drawable.ic_drive_image;
		}else if(tmp_type.equals(str_txt_type)){
			drawableId = R.drawable.ic_drive_docs;
		}else if(tmp_type.equals(str_pdf_type)){
			drawableId = R.drawable.ic_drive_pdf;
		}else if(tmp_type.equals(str_epub_type)){
			drawableId = R.drawable.ic_drive_unknown;
		}else if(tmp_type.equals(str_apk_type)){
			drawableId = R.drawable.ic_drive_apk;
		}

		return drawableId;
	}

	public static Drawable getDrawable(Context context,File file){
		int drawableId;
		Drawable drawable;

		if(file.isDirectory()){
			drawableId = R.drawable.ic_drive_floder;
		}
		else{
			String tmp_type = getMIMEType(context, file);
			drawableId = getDrawableId(tmp_type);
		}


		if( drawableId == R.drawable.ic_drive_apk ){
			drawable = getApkIcon(context, file.getPath());
		}
		else{
			drawable = context.getResources().getDrawable(drawableId);
		}

		return drawable;
	}

	public static Drawable getApkIcon(Context context, String Path) {
		// 未安装的程序通过apk文件获取icon
		String apkPath = Path; // apk 文件所在的路径
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			typeArgs = new Class<?>[] { File.class, String.class,
					DisplayMetrics.class, int.class };
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);
			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
					"applicationInfo");
			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);
			Class<?> assetMagCls = Class.forName(PATH_AssetManager);
			Object assetMag = assetMagCls.newInstance();
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					"addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
			Resources res = context.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor<Resources> resCt = Resources.class
					.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);
			if (info != null) {
				if (info.icon != 0) {
					Drawable icon = res.getDrawable(info.icon);
					return icon;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static int Copy(File file,String newName){
		Log.i(TAG, "RenameFile: "+file.getPath()+" to: "+newName);

		int ret = SUCCESS;

		String oldPath = file.getPath();
		int lastSeparator = oldPath.lastIndexOf(File.separator);
		String newPath = oldPath.substring(0,lastSeparator+1).concat(newName);

		File newFile = new File(newPath);

		if(newFile.exists()){
			ret = FILE_IS_ALREADY_EXITS;
		}
		else{
			if(file.renameTo(new File(newPath))){
				ret = SUCCESS;
			}
			else{
				ret = FAILED;
			}
		}
		return ret;
	}

	public static int copyFile(File sourceLocation , File targetLocation) {
		Log.i(TAG, "copy "+sourceLocation.getPath()+" to "+targetLocation.getPath());
		int ret = SUCCESS;

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdirs();
			}

			for (File file : sourceLocation.listFiles()) {
				copyFile(file,	new File(targetLocation, file.getName()));
			}
		} else {
			try {
				if(targetLocation.exists()){
					ret = FILE_IS_ALREADY_EXITS;
					return ret;
				}

				InputStream in = new FileInputStream(sourceLocation);
				OutputStream out = new FileOutputStream(targetLocation);

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

				ret = SUCCESS;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ret = FAILED;
				Log.e("zheng", e.getMessage());
			}
		}
		return ret;
	}
	public static void openFile(Context context, File file){
		String fileType = getMIMEType(context, file);

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		intent.setDataAndType(Uri.fromFile(file),fileType);

		try {
			context.startActivity(intent);
		} catch (android.content.ActivityNotFoundException e) {
			Toast.makeText(context,
					context.getString(R.string.no_apps_to_send), Toast.LENGTH_SHORT)
					.show();
		}
	}

	/** 转换文件大小 **/
	public static String formetFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = fileS + " B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + " K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + " M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + " G";
		}
		return fileSizeString;
	}


	public static ArrayList<Uri> getUris(ArrayList<File> files) {
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (File mfile : files) {
			addFileUri(mfile, uris);
		}
		return uris;
	}

	public static void addFileUri(File mfile, ArrayList<Uri> uris) {
		if (mfile.isDirectory()) {
			File[] files = mfile.listFiles();
			if( null != files && files.length > 0){
				for (File file : files) {
					addFileUri(file, uris);
				}
			}
		} else {
			uris.add(Uri.fromFile(mfile));
		}
	}

	private static final String [] filterList = {"com.google.android.gm"};
	public static void shareFiles(Context context, File currentFile) {

		ArrayList<File> multi_path = new ArrayList<File>();
		multi_path.add(currentFile);

		String currenPath = currentFile.getPath();

		if(currenPath == null ) {
			return;
		}
		if(currenPath.startsWith(str_flash_path)) {
			ShareIntent.SetFilter(new String[]{});
		} else {
			ShareIntent.SetFilter(filterList);
		}

		ArrayList<Uri> srcSends = getUris(multi_path);
		if (srcSends.size() == 1) {
			File file = multi_path.get(0);
			String fileType = getMIMEType(context, file);

			if("application/vnd.android.package-archive".equals(fileType)) {
				fileType = "application/zip";
			}
			ShareIntent.shareFile(context, fileType, srcSends.get(0));
		} else if (srcSends.size() > 1) {
			ShareIntent.shareMultFile(context, "x-mixmedia/*", srcSends);
		}
		/*if (is_multi_choice) { // 若是在多选取状态下的话，则去除多选状态
			is_multi_choice = !is_multi_choice;
			multi_choice_process(is_multi_choice);
		}
		multi_path = new ArrayList<FileInfo>();*/
	}

	public void Detail(Context context,File file){

		getMIMEType(context, file);
		file.getPath();

		long size	= file.length();
		formetFileSize(size);

		long modify	= file.lastModified();
		System.out.println(new Timestamp(modify).toString());

	}

	public static long getFileNumInDirectory(File file){
		long size = 0;
		File fileList[] = file.listFiles();
		if(fileList!=null){
			for (File fileTemp : fileList) {
				if (fileTemp.isDirectory()) {
					size += getFileNumInDirectory(fileTemp);
				} else {
					size++;
				}
			}
		}
		return size;
	}

	public static long getFileSize(File file){
		long size = 0;
		if(file.isFile()){
			size = file.length();
		}
		else{
			File fileList[] = file.listFiles();
			if( fileList != null){
				for (File fileTemp : fileList) {
					size += getFileSize(fileTemp);
				}
			}
		}
		return size;
	}
}
