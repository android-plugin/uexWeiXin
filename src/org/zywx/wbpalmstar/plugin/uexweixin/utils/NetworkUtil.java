package org.zywx.wbpalmstar.plugin.uexweixin.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetworkUtil {

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
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
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

	public static AlertDialog showAlert(Context ctx, final String title, final String message, final String label, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton(label, listener);
		AlertDialog alert = builder.create();
		alert.show();
		return alert;
	}

}
