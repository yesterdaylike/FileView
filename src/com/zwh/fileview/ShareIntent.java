package com.zwh.fileview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.widget.Toast;
/**
 * �������ڴ������ļ��ǹ�����  gmail ��Ӧ��
 */

public class ShareIntent {
	
	static String [] filterList = {"com.google.android.gm"};
	
	/**
	 * ����Ҫ���˵Ĺؼ��֣���԰������й���
	 */
	public static void SetFilter(String [] mList){
		filterList = mList;
	}
			
	/**
    *	һ�η��Ͷ���ļ�
    *	�ļ�����һ��ʹ�ã�x-mixmedia/*
    */	
	public static void shareMultFile(Context mcontext,String mFileType,ArrayList<Uri> uris){
		List<Intent> appList= getShareApps(mcontext,mFileType,Intent.ACTION_SEND_MULTIPLE);
		//System.out.println("appList:" + appList);
		if(appList !=null && appList.size()>0){
			for(Intent i : appList){
				i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			}
			Sending(mcontext, appList);
		} else {
			Toast.makeText(mcontext, mcontext.getString(R.string.no_apps_to_send),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
    *	һ�η���һ���ļ�����Ҫָ�ļ�����
    *	 ���磺image/* audio/* text/plain
    */	
	public static void shareFile(Context mcontext,String mFileType,Uri uri){
		List<Intent> appList= getShareApps(mcontext,mFileType,Intent.ACTION_SEND);
		//System.out.println("appList:" + appList);
		if(appList !=null && appList.size()>0){
			for(Intent i : appList){
				i.putExtra(Intent.EXTRA_STREAM, uri);
			}
			Sending(mcontext, appList);
		} else {
			Toast.makeText(mcontext, mcontext.getString(R.string.no_apps_to_send),
					Toast.LENGTH_SHORT).show();
		}
	} 
	
	/**
    *	ͳһ�ķ��ͽӿ�
    */
	private static void Sending(Context mcontext,List<Intent> appList){
		Intent chooserIntent =Intent.createChooser(appList.remove(0),mcontext.getText(R.string.send));
        if(chooserIntent ==null){
            return ;
        }
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, appList.toArray(new Parcelable[]{}));
        try{
        	chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	mcontext.startActivity(chooserIntent);
        }catch(android.content.ActivityNotFoundException ex){
            Toast.makeText(mcontext,"Can't find share component to share",Toast.LENGTH_SHORT).show();
        }
	}
	
    /**
    *	��ȡ���˺��app�б�
    *	ֱ�ӹ��˰���
    */
	private static List<Intent> getShareApps(Context mcontext,String mFileType,String mSendIntentType){
	    Intent it =new Intent(mSendIntentType);
	    it.setType(mFileType);
	    List<ResolveInfo> sendActiviryInfo = mcontext.getPackageManager().queryIntentActivities(it,0);
	    
	    if(!sendActiviryInfo.isEmpty()){
	        List<Intent> targetedShareIntents =new ArrayList<Intent>();
	        for(ResolveInfo info : sendActiviryInfo){
	            Intent targeted =new Intent(mSendIntentType);
	            targeted.setType(mFileType);
	            ActivityInfo activityInfo = info.activityInfo;
	            //�ڴ˴����˰���
	            if(containKeyword(activityInfo.packageName)){
	                continue;
	            }
	            targeted.setPackage(activityInfo.packageName);
	            targetedShareIntents.add(targeted);
	        }
	        return targetedShareIntents;
	    }
		return null;
	}
	
	/**
	 * �������Ƿ�����ؼ���
	 */
	private static boolean containKeyword(String packageName){
		for(String name : filterList){
			if(packageName.contains(name) && !("").equals(name)){
				return true;
			}
		}
		return false;
	}
}
