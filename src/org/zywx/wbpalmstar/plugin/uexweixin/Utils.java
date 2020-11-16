package org.zywx.wbpalmstar.plugin.uexweixin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.platform.certificates.Http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Utils {

	private static final String TAG = "Utils";
	private static final int OK = 200;

	public static byte[] httpGet(final String url) {
		if (url == null || url.length() == 0) {
			Log.e(TAG, "httpGet, url is null");
			return null;
		}
		HttpURLConnection httpURLConnection;
		try {
			httpURLConnection=Http.getHttpsURLConnection(url);
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.connect();
			int responseCode = httpURLConnection.getResponseCode();
			if (responseCode != OK) {
				Log.e(TAG, "httpGet fail, status code = " +responseCode);
				return null;
			}
			return toByteArray(httpURLConnection);
		} catch (Exception e) {
			Log.e(TAG, "httpGet exception, e = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private static byte[] toByteArray(HttpURLConnection conn) throws Exception {
		if (null == conn) {
			return new byte[]{};
		}
		InputStream mInStream = conn.getInputStream();
		if (mInStream == null) {
			return new byte[]{};
		}
		long len = conn.getContentLength();
		if (len > Integer.MAX_VALUE) {
			throw new Exception(
					"HTTP entity too large to be buffered in memory");
		}
		String contentEncoding = conn.getContentEncoding();
		if (null != contentEncoding) {
			if ("gzip".equalsIgnoreCase(contentEncoding)) {
				mInStream = new GZIPInputStream(mInStream, 2048);
			}
		}
		return IOUtils.toByteArray(mInStream);
	}
	public static byte[] httpPost(String url, String entity) {
		if (url == null || url.length() == 0) {
			Log.e(TAG, "httpPost, url is null");
			return null;
		}
		OutputStream out = null; //写
		HttpURLConnection httpURLConnection;
		try {
			httpURLConnection = Http.getHttpsURLConnection(url);
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Accept", "application/json");
			httpURLConnection.setRequestProperty("Content-type", "application/json");
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setConnectTimeout(30000);
			httpURLConnection.setReadTimeout(30000);
			out = httpURLConnection.getOutputStream();
			out.write(entity.getBytes());
			out.flush();
			out.close();
			httpURLConnection.connect();
			int responseCode = httpURLConnection.getResponseCode();
			if (responseCode != OK) {
				BDebug.w(TAG, "httpURLConnection fail, status code = " + responseCode);
				return null;
			}
			return toByteArray(httpURLConnection);
		} catch (Exception e) {
			BDebug.w(TAG, "httpURLConnection exception, e = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] httpPostForm(String url, Map<String, String> formParams) {
		if (url == null || url.length() == 0) {
			Log.e(TAG, "httpPostForm, url is null");
			return null;
		}
		OutputStream out = null; //写
		HttpURLConnection httpURLConnection;
		try {
			httpURLConnection = Http.getHttpsURLConnection(url);
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Accept", "application/json");
			httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setConnectTimeout(30000);
			httpURLConnection.setReadTimeout(30000);
			out = httpURLConnection.getOutputStream();
			String entity = mapToFormString(formParams);
			out.write(entity.getBytes());
			out.flush();
			out.close();
			httpURLConnection.connect();
			int responseCode = httpURLConnection.getResponseCode();
			if (responseCode != OK) {
				BDebug.w(TAG, "httpURLConnection fail, status code = " + responseCode);
				return null;
			}
			return toByteArray(httpURLConnection);
		} catch (Exception e) {
			BDebug.w(TAG, "httpURLConnection exception, e = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private static String mapToFormString(Map<String, String> formParams){
		String result = "";
		if (formParams == null){
			return result;
		}
		StringBuilder strb = new StringBuilder();
		for (String key : formParams.keySet()){
			String value = formParams.get(key);
			strb.append(key);
			strb.append("=");
			strb.append(value);
			strb.append("&");
		}
		if (strb.length() > 0){
			result = strb.substring(0, strb.length() - 1);
		}
		return result;
	}

	public static byte[] streamToByteArray(InputStream in) {
		if (in == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int read;
		byte[] byteArray = null;
		try {
			while ((read = in.read()) != 1) {
				baos.write(read);
			}
			byteArray = baos.toByteArray();
			baos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray;
	}

	public static byte[] bmpToByteArray(Bitmap bmp, boolean needRecycle) {
		if(bmp==null){
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, baos);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = baos.toByteArray();
		try {
			baos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static AlertDialog showAlert(Context ctx, final String title, final String message, final String label, DialogInterface.OnClickListener listener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton(label, listener);
		AlertDialog alert =builder.create();
		alert.show();
		return alert;
	}

	public static String getAppId(Context ctx){
		SharedPreferences prefs = ctx.getSharedPreferences("appId", Context.MODE_PRIVATE);
		return prefs.getString("appId", null);
	}

	public static void setAppId(Context ctx, String appId)
	{
		SharedPreferences prefs = ctx.getSharedPreferences("appId", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("appId", appId);
		editor.commit();
	}

	public static String sha1(List<String> stringList) {
		if (stringList == null || stringList.size() == 0) {
			return null;
		}
		Collections.sort(stringList); // 按照字典顺序排序
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
			for (String data : stringList) {
				if (!TextUtils.isEmpty(data)) {
					mdTemp.update(data.getBytes());
				}
			}
			byte[] md = mdTemp.digest();
			int j = md.length;
			char[] buf = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String sha1(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}

		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

		try {
			MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
			mdTemp.update(str.getBytes());

			byte[] md = mdTemp.digest();
			int j = md.length;
			char buf[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
