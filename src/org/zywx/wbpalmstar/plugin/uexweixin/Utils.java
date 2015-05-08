package org.zywx.wbpalmstar.plugin.uexweixin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class Utils {
	
	private static final String TAG = "Utils";
	
	public static byte[] httpGet(final String url) {
		if (url == null || url.length() == 0) {
			Log.e(TAG, "httpGet, url is null");
			return null;
		}

		HttpClient httpClient = getNewHttpClient();
		HttpGet httpGet = new HttpGet(url);

		try {
			HttpResponse resp = httpClient.execute(httpGet);
			if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				Log.e(TAG, "httpGet fail, status code = " + resp.getStatusLine().getStatusCode());
				return null;
			}

			return EntityUtils.toByteArray(resp.getEntity());

		} catch (Exception e) {
			Log.e(TAG, "httpGet exception, e = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] httpPost(String url, String entity) {
		if (url == null || url.length() == 0) {
			Log.e(TAG, "httpPost, url is null");
			return null;
		}
		
		HttpClient httpClient = getNewHttpClient();
		
		HttpPost httpPost = new HttpPost(url);
		
		try {
			httpPost.setEntity(new StringEntity(entity));
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			
			HttpResponse resp = httpClient.execute(httpPost);
			if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				Log.e(TAG, "httpGet fail, status code = " + resp.getStatusLine().getStatusCode());
				return null;
			}

			return EntityUtils.toByteArray(resp.getEntity());
		} catch (Exception e) {
			Log.e(TAG, "httpPost exception, e = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private static HttpClient getNewHttpClient() { 
		   try { 
		       KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType()); 
		       trustStore.load(null, null); 

		       SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore); 
		       sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 

		       HttpParams params = new BasicHttpParams(); 
		       HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); 
		       HttpProtocolParams.setContentCharset(params, HTTP.UTF_8); 

		       SchemeRegistry registry = new SchemeRegistry(); 
		       registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80)); 
		       registry.register(new Scheme("https", sf, 443)); 

		       ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry); 

		       return new DefaultHttpClient(ccm, params); 
		   } catch (Exception e) { 
		       return new DefaultHttpClient(); 
		   } 
		}
	
	private static class SSLSocketFactoryEx extends SSLSocketFactory {      
	      
	    SSLContext sslContext = SSLContext.getInstance("TLS");      
	      
	    public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {      
	        super(truststore);      
	      
	        TrustManager tm = new X509TrustManager() {      
	      
	            public X509Certificate[] getAcceptedIssuers() {      
	                return null;      
	            }      
	      
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain,	String authType) throws java.security.cert.CertificateException {
				}  
	        };      
	      
	        sslContext.init(null, new TrustManager[] { tm }, null);      
	    }      
	      
		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,	port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		} 
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
			return null;
		}
	}
	
}
