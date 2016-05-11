package org.zywx.wbpalmstar.plugin.uexweixin.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

	/**
	 * 文件名
	 */
	private static final String SP_FILE_NAME_APPID = "appId";

	/**
	 * 字段名
	 */
	private static final String SP_FIELD_NAME_APPID = "appId";

	/**
	 * 把AppId存入SP中
	 * 
	 * @param context
	 * @param appId
	 */
	public static void saveAppIdInSP(Context context, String appId) {
		SharedPreferences prefs = context.getSharedPreferences(SP_FILE_NAME_APPID, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SP_FIELD_NAME_APPID, appId);
		editor.commit();
	}

	/**
	 * 从SP中取出appId
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getAppIdFrom(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(SP_FILE_NAME_APPID, Context.MODE_PRIVATE);
		return prefs.getString(SP_FIELD_NAME_APPID, null);
	}

}
