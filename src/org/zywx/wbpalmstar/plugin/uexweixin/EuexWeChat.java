package org.zywx.wbpalmstar.plugin.uexweixin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.CallbackWindowNameVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.LoginAccessTokenVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.LoginCheckTokenVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.LoginRefreshTokenVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.LoginResultVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.LoginVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.PayDataVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.PrePayDataVO;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.JsConst;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.WXPayGetPrepayIdTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@SuppressLint("SdCardPath")
public class EuexWeChat extends EUExBase {

	private static final String TAG = "EuexWeChat";
	
	public final static String PARAMS_JSON_KEY_TEXT = "text";
	public final static String PARAMS_JSON_KEY_SCENE = "scene";
	public final static String PARAMS_JSON_KEY_THUMBIMG = "thumbImg";
	public final static String PARAMS_JSON_KEY_IMAGE = "image";
	public final static String PARAMS_JSON_KEY_WEDPAGEURL = "wedpageUrl";
	public final static String PARAMS_JSON_KEY_TITLE = "title";
	public final static String PARAMS_JSON_KEY_DESCRIPTION = "description";

	public static final String CB_REGISTER_WXAPP_RESULT = "uexWeiXin.cbRegisterApp";//注册回调
	public static final String CB_IS_WXAPP_INSTALLIED = "uexWeiXin.cbIsWXAppInstalled";//
	public static final String CB_SEND_TEXT_CONTENT = "uexWeiXin.cbSendTextContent";//
	public static final String CB_SEND_IMAGE_CONTENT = "uexWeiXin.cbSendImageContent";
	public static final String CB_IS_PAY_SUPPORTED = "uexWeiXin.cbIsSupportPay";
	public static final String CB_GET_ACCESS_TOKEN = "uexWeiXin.cbGetAccessToken";
	public static final String CB_GET_PREPAY_ID = "uexWeiXin.cbGenerateAdvanceOrder";
	public static final String CB_GET_PAY_RESULT = "uexWeiXin.cbGotoPay";
	public static final String CB_GET_ACCESS_TOKEN_LOCAL = "uexWeiXin.cbGetAccessTokenLocal";
	public static final String CB_SHARE_TEXT_CONTENT = "uexWeiXin.cbShareTextContent";
	public static final String CB_SHARE_IMAGE_CONTENT = "uexWeiXin.cbShareImageContent";
	public static final String CB_SHARE_LINK_CONTENT = "uexWeiXin.cbShareLinkContent";

	// 微信登陆
	public static final String CB_LOGIN_WEIXIN = "uexWeiXin.cbWeiXinLogin";
	public static final String CB_GETWEIXINLOGINACCESSTOKEN = "uexWeiXin.cbGetWeiXinLoginAccessToken";
	public static final String CB_GETWEIXINLOGINREFRESHACCESSTOKEN = "uexWeiXin.cbGetWeiXinLoginRefreshAccessToken";
	public static final String CB_GETWEIXINLOGINCHECKACCESSTOKEN = "uexWeiXin.cbGetWeiXinLoginCheckAccessToken";
	public static final String CB_GETWEIXINLOGINUNIONID = "uexWeiXin.cbGetWeiXinLoginUnionID";

	private static final int THUMB_SIZE = 100;

	private static final int TEXT_CODE = 1;
	private static final int IMAGE_CODE = 2;
	private static final int SHARE_TEXT_CONTENT_CODE = 3;
	private static final int SHARE_IMAGE_CONTENT_CODE = 4;
	private static final int SHARE_LINK_CONTENT_CODE = 5;

	private static IWXAPI api;
	public static WeChatCallBack weChatCallBack;
	private String appId;
	private long timeStamp;
	private String nonceStr;

	private int countCode;

	private static String accessCode;

    private static final String BUNDLE_DATA = "data";
    private static final int MSG_GET_PREPAY_ID = 1;
    private static final int MSG_START_PAY = 2;

    private static final int MSG_LOGIN = 6;
    private static final int MSG_GET_LOGIN_ACCESS_TOKEN = 7;
    private static final int MSG_GET_LOGIN_REFRESH_ACCESS_TOKEN = 8;
    private static final int MSG_GET_LOGIN_CHECK_ACCESS_TOKEN = 9;
    private static final int MSG_GET_LOGIN_UNION_I_D = 10;
    private static boolean isLoginNew = false;
    private static int code;
    private static String mWindowName = null;

	public EuexWeChat(Context context, EBrowserView parent) {
		super(context, parent);
		init();
	}

	public String getJson(String errCode, String errStr) {
		String myString;
		if (errStr == null) {
			errStr = "";
		}
		try {
			myString = new JSONStringer().object().key("errCode")
					.value(errCode).key("errStr").value(errStr).endObject()
					.toString();
			Log.d("path", myString);
			return myString;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void init() {
		weChatCallBack = new WeChatCallBack() {
			@Override
			public void callBackPayResult(BaseResp msg) {
                callbackOldInterface(CB_GET_PAY_RESULT, 0, EUExCallback.F_C_JSON,
                        getJson(msg.errCode + "", msg.errStr));
                callBackPluginJsAsync(JsConst.CALLBACK_START_PAY,
                        getJson(msg.errCode + "", msg.errStr));
			}

			@Override
			public void callBackShareResult(int message) {
				Log.i("WXEntryActivity", "callBackShareResult->errorCode="
						+ message);
				if (code == 1) {// 类型代表是文字类型
                    callbackOldInterface(CB_SEND_TEXT_CONTENT, 0,
                            EUExCallback.F_C_INT, message == 0 ? "0" : "1");
				} else if (code == 2) {// 类型代表是图片类型
                    callbackOldInterface(CB_SEND_IMAGE_CONTENT, 0,
                            EUExCallback.F_C_INT, message == 0 ? "0" : "1");
				} else if (code == SHARE_TEXT_CONTENT_CODE) {
					shareCallBack(CB_SHARE_TEXT_CONTENT, message == 0 ? "0"
							: "1");
				} else if (code == SHARE_IMAGE_CONTENT_CODE) {
					shareCallBack(CB_SHARE_IMAGE_CONTENT, message == 0 ? "0"
							: "1");
				} else if (code == SHARE_LINK_CONTENT_CODE) {
					shareCallBack(CB_SHARE_LINK_CONTENT, message == 0 ? "0"
							: "1");
				}
			}

			@Override
			public void backLoginResult(BaseResp msg) {
				if (msg.errCode == 0) {
					String code = ((SendAuth.Resp) msg).code;// 需要转换一下才可以
					accessCode = code;
				}
                if (isLoginNew) {
                    LoginResultVO resultVO = new LoginResultVO();
                    resultVO.setErrCode(msg.errCode + "");
                    SendAuth.Resp resp = (SendAuth.Resp) msg;
                    if (resp != null){
                        if (msg.errCode == 0) {
                            resultVO.setCode(resp.code);
                        }
                        resultVO.setState(resp.state);
                        resultVO.setLanguage(resp.lang);
                        resultVO.setCountry(resp.country);
                        String resultStr = DataHelper.gson.toJson(resultVO);
                        shareCallBack(JsConst.CALLBACK_LOGIN, resultStr);
                    }
                    isLoginNew = false;
                }else{
                    callbackOldInterface(CB_LOGIN_WEIXIN, 0, EUExCallback.F_C_TEXT,
                            msg.errCode + "");
                }
			}
		};
	}

	/**
	 * 注册应用到微信
	 * 
	 * @param appId
	 *            从微信开放平台申请的appId
	 * @return true 注册成功， false 注册失败
	 */
	public boolean registerApp(String[] data) {
		Log.d(TAG, "registerApp");
		if (data == null || data.length < 1) {
			return false;
		}
		appId = data[0];
		if (appId == null || appId.length() == 0) {
			return false;
		}
		api = WXAPIFactory.createWXAPI(mContext, appId, false);
		boolean regOk = api.registerApp(appId);
		if (regOk) {
			Utils.setAppId(mContext, appId);
		}
		jsCallback(CB_REGISTER_WXAPP_RESULT, 0, EUExCallback.F_C_INT, regOk ? 0
				: 1);// 注册回调 0-成功 1-失败
		return regOk;
	}

	// 微信登陆接口
	public void weiXinLogin(String[] parms) {
		Log.d(TAG, "weiXinLogin");
		if (parms.length < 2) {
			return;
		}
		SendAuth.Req req = new SendAuth.Req();
		req.scope = parms[0];// "snsapi_userinfo"
		req.state = parms[1];// "wechat_sdk_demo_test"
		Log.d(TAG, req.scope + "=======>" + req.state + "======appId====>"
				+ appId);
		api.sendReq(req);

	}

	// 微信登陆获取accessToken
	public void getWeiXinLoginAccessToken(String[] parms) {
		Log.d(TAG, "weiXinLogin");
		if (parms.length < 2) {
			return;
		}
		try {
			countCode = Constants.token;
			String secret = parms[0];
			String grant_type = parms[1];

			Log.i("EuexWeChat", "grant_type=====>" + grant_type);

			String url = String
					.format(JsConst.URL_LOGIN_GET_ACCESS_TOKEN,
							appId, secret, accessCode, grant_type);
			Log.i("EuexWeChat", "url=====>" + url);
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 微信登陆获取refreshToken
	public void getWeiXinLoginRefreshAccessToken(String[] parms) {
		Log.d(TAG, "weiXinLogin");
		if (parms.length < 2) {
			return;
		}
		try {
			countCode = Constants.refresh;
			String grant_type = parms[0];
			String refresh_token = parms[1];

			String url = String
					.format(JsConst.URL_LOGIN_REFRESH_ACCESS_TOKEN,
							appId, grant_type, refresh_token);
			Log.i("EuexWeChat", "url=====>" + url);
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 检测微信登陆的token
	public void getWeiXinLoginCheckAccessToken(String[] parms) {
		Log.d(TAG, "weiXinLogin");
		if (parms.length < 2) {
			return;
		}
		try {
			countCode = Constants.check;
			String access_token = parms[0];
			String openid = parms[1];
			String url = String
					.format(JsConst.URL_LOGIN_CHECK_ACCESS_TOKEN,
							access_token, openid);
			Log.i("EuexWeChat", "url=====>" + url);
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 获取个人信息
	public void getWeiXinLoginUnionID(String[] parms) {
		Log.d(TAG, "weiXinLogin");
		if (parms.length < 2) {
			return;
		}
		try {
			countCode = Constants.union;
			String access_token = parms[0];
			String openid = parms[1];

			String url = String
					.format(JsConst.URL_LOGIN_UNIONID,
							access_token, openid);
			Log.i("EuexWeChat", "url=====>" + url);
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	class NetWorkAsyncTaskToken extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
            Log.i("EuexWeChat", "url->" + url);
			byte[] buf = Utils.httpGet(url);
			String callBack = new String(buf);
			return callBack;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i("countCode", "countCode=====>" + countCode);
            if (isLoginNew){
                switch (countCode){
                    case Constants.token:
                        callBackPluginJs(JsConst.CALLBACK_GET_LOGIN_ACCESS_TOKEN, result);
                        break;
                    case Constants.refresh:
                        callBackPluginJs(JsConst.CALLBACK_GET_LOGIN_REFRESH_ACCESS_TOKEN, result);
                        break;
                    case Constants.check:
                        callBackPluginJs(JsConst.CALLBACK_GET_LOGIN_CHECK_ACCESS_TOKEN, result);
                        break;
                    case Constants.union:
                        callBackPluginJs(JsConst.CALLBACK_GET_LOGIN_UNION_I_D, result);
                        break;
                    default:
                        break;
                }
                isLoginNew = false;
            }else{
                if (countCode == 1) {
                    Log.i("EuexWeChat", "result=====>" + result);
                    jsCallback(CB_GETWEIXINLOGINACCESSTOKEN, 0,
                            EUExCallback.F_C_JSON, result);
                } else if (countCode == 2) {
                    Log.i("EuexWeChat", "result=====>" + result);
                    jsCallback(CB_GETWEIXINLOGINREFRESHACCESSTOKEN, 0,
                            EUExCallback.F_C_JSON, result);
                } else if (countCode == 3) {

                    CheckModel model = AnalJson.getJson(result);
                    String re = "";
                    if (!("0".equals(model.errcode))) {
                        re = "1";
                    } else {
                        re = "0";
                    }
                    Log.i("CheckModel", "re=====>"+re);

                    jsCallback(CB_GETWEIXINLOGINCHECKACCESSTOKEN, 0,
                            EUExCallback.F_C_JSON, re);
                } else if (countCode == 4) {
                    jsCallback(CB_GETWEIXINLOGINUNIONID, 0, EUExCallback.F_C_JSON,
                            result);
                }
            }

		}
	}

	public boolean sendTextContent(String[] params) {
		Log.d(TAG, "sendTextContent");
		if (params == null || params.length < 2) {
			return false;
		}
		code = TEXT_CODE;
		try {
			int scene = Integer.parseInt(params[0]);
			String text = params[1];
			return sendTextContent(scene, text);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误： " + params[0], Toast.LENGTH_SHORT)
					.show();
		}
		return false;
	}

	public boolean sendImageContent(String[] params) {
		Log.d(TAG, "sendImageContent");
		if (params == null || params.length < 3) {
			return false;
		}
		code = IMAGE_CODE;
		try {
			int scene = Integer.parseInt(params[0]);
			String thumbPath = params[1];
			String imgPath = params[2];
			String imageUrl = params[3];
			String imageTitle = params[4];
			String imageDescription = params[5];
			return sendImageContent(scene, thumbPath, imgPath, imageTitle,
					imageDescription, imageUrl);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	/**
	 * 判断是否安装微信应用，
	 * 
	 * @return true 已安装， false 未安装
	 */
	public boolean isWXAppInstalled(String[] args) {
		Log.d(TAG, "isWXAppInstalled");
		// 判断是否安装微信，安装返回true。
		boolean isWXInstalled = api.isWXAppInstalled();
		jsCallback(CB_IS_WXAPP_INSTALLIED, 0, EUExCallback.F_C_TEXT,
				isWXInstalled ? 0 : 1);
		return isWXInstalled;
	}

	/**
	 * 
	 * @param scene
	 * @param text
	 * @return
	 */
	public boolean sendTextContent(int scene, String text) {
		if (text == null || text.length() == 0) {
			return false;
		}

		WXTextObject textObj = new WXTextObject();
		textObj.text = text;

		WXMediaMessage msg = new WXMediaMessage();
		msg.description = text;
		msg.mediaObject = textObj;

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.message = msg;
		req.scene = scene;
		req.transaction = "text";
		Log.i(TAG, scene + " " + text);
		return api.sendReq(req);

	}

	/**
	 * 
	 * @param scene
	 *            发送场景，0 微信， 1 朋友圈
	 * @param thumImgPath
	 *            缩略图地址
	 * @param realImgPath
	 *            图片地址
	 * @return true 发送成功， false 发送失败
	 */
	public boolean sendImageContent(int scene, String thumbImgPath,
			String realImgPath, String imageTitle, String imageDescription,
			String imageUrl) {

		if (realImgPath == null || realImgPath.length() == 0) {
			return false;
		}

        String imgPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), realImgPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        String thumbPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), thumbImgPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
		WXImageObject imgObj = createImageObject(imgPath);

		// 如果没有缩略图地址，图片地址赋值给缩略图地址，生成缩略图
		if (thumbPath == null || thumbPath.length() == 0) {
			thumbPath = imgPath;
		}
		Bitmap thumbBmp = createThumbBitmap(thumbPath);

		WXMediaMessage msg = new WXMediaMessage();
		msg.thumbData = Utils.bmpToByteArray(thumbBmp, true);
		msg.title = imageTitle;
		msg.description = imageDescription;
		if (imageUrl == null || imageUrl.length() == 0) {
			msg.mediaObject = imgObj;
		} else {
			WXWebpageObject webObj = new WXWebpageObject();
			webObj.webpageUrl = imageUrl;
			msg.mediaObject = webObj;
		}

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = scene;
		Log.i(TAG, scene + " " + realImgPath);
		return api.sendReq(req);
	}


	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	public void isSupportPay(String[] params) {
		// getWXAppSupportSAPI是否支持微信版本。
		boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
		// isPaySupported判断是否支持已安装的微信版本。
		if (isPaySupported) {
			jsCallback(CB_IS_PAY_SUPPORTED, 0, EUExCallback.F_C_INT, 0);
		} else {
			jsCallback(CB_IS_PAY_SUPPORTED, 0, EUExCallback.F_C_INT, 1);
		}
	}

	public void getAccessToken(String[] params) {
		new GetAccessTokenTask(params[0], params[1]).execute();
	}

	String contentAccess = "";

	private class GetAccessTokenTask extends
			AsyncTask<Void, Void, GetAccessTokenResult> {

		private ProgressDialog dialog;
		private String appId, appSecret;

		public GetAccessTokenTask(String mAppId, String mAppSecret) {
			this.appId = mAppId;
			this.appSecret = mAppSecret;
		}

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(mContext, "提示", "正在获取access token...");
		}

		@Override
		protected void onPostExecute(GetAccessTokenResult result) {
			if (dialog != null) {
				dialog.dismiss();
			}

			if (result.localRetCode == LocalRetCode.ERR_OK) {
				jsCallback(CB_GET_ACCESS_TOKEN, 0, EUExCallback.F_C_TEXT,
						contentAccess);

			} else {
				jsCallback(CB_GET_ACCESS_TOKEN, 0, EUExCallback.F_C_TEXT,
						result.localRetCode.name());
			}
		}

		@Override
		protected GetAccessTokenResult doInBackground(Void... params) {
			GetAccessTokenResult result = new GetAccessTokenResult();
			// String.format用与替换路径后面的%s。
			String url = String
					.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
							appId, appSecret);
			byte[] buf = Utils.httpGet(url);
			if (buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}
			contentAccess = new String(buf);
			// *****************************
			// 用于将获取到的Token数据，存储到本地。
			SharedPreferences.Editor token = mContext.getSharedPreferences(
					"token", 0).edit();
			token.putString("token", contentAccess);
			token.commit();
			System.out.println(contentAccess);

			result.parseFrom(contentAccess);
			return result;
		}
	}

	// 获取本地存储的Token数据。
	public void getAccessTokenLocal(String[] params) {
		setCallBackData();
	}

	String contentAccessLocal = "";

	public void setCallBackData() {
		if (getLocalData().localRetCode == LocalRetCode.ERR_OK) {
			jsCallback(CB_GET_ACCESS_TOKEN_LOCAL, 0, EUExCallback.F_C_TEXT,
					contentAccessLocal);

		} else {
			jsCallback(CB_GET_ACCESS_TOKEN_LOCAL, 0, EUExCallback.F_C_TEXT,
					getLocalData().localRetCode.name());
		}
	}

	public GetAccessTokenResult getLocalData() {
		GetAccessTokenResult result = new GetAccessTokenResult();
		// 获取本地存储的Token数据
		SharedPreferences sharedata = mContext.getSharedPreferences("token", 0);
		String data = sharedata.getString("token", null);
		System.out.println(data + "-------");
		byte[] buf = data.getBytes();
		if (buf == null || buf.length == 0) {
			result.localRetCode = LocalRetCode.ERR_OTHER;
			return result;
		}
		contentAccessLocal = new String(buf);
		System.out.println(contentAccessLocal);

		result.parseFrom(contentAccessLocal);
		return result;
	}

	public void generateAdvanceOrder(String[] params) {
		System.out.println("params[0]=" + params[0]);
		System.out.println("params[1]=" + params[1]);
		GetPrepayIdTask getPrepayId = new GetPrepayIdTask(params[0], params[1]);
		getPrepayId.execute();
	}

    public void generatePrepayID(String[] params) {
        GeneratePrepayID getPrepayId = new GeneratePrepayID(params);
        getPrepayId.execute();
    }

	private static class GetPrepayIdResult {
		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;

		public void parseFrom(String content) {

			if (content == null || content.length() <= 0) {
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}
			try {
				JSONObject json = new JSONObject(content);
				if (json.has("prepayid")) { // success case
					localRetCode = LocalRetCode.ERR_OK;
				} else {
					localRetCode = LocalRetCode.ERR_JSON;
				}
			} catch (Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}

	String jsonPrepayData = "";

	private class GetPrepayIdTask extends
			AsyncTask<Void, Void, GetPrepayIdResult> {

		private ProgressDialog dialog;
		private String accessToken, entity;

		public GetPrepayIdTask(String mAccessToken, String mEntity) {
			this.accessToken = mAccessToken;
			this.entity = mEntity;
		}

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(mContext, "提示", "正在获取预支付订单...");
		}

		@Override
		protected void onPostExecute(GetPrepayIdResult result) {
			if (dialog != null) {
				dialog.dismiss();
			}

			if (result.localRetCode == LocalRetCode.ERR_OK) {
				jsCallback(CB_GET_PREPAY_ID, 0, EUExCallback.F_C_TEXT,
						jsonPrepayData);
			} else {
				jsCallback(CB_GET_PREPAY_ID, 0, EUExCallback.F_C_TEXT,
						result.localRetCode.name());
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected GetPrepayIdResult doInBackground(Void... params) {

			String url = String.format(
					"https://api.weixin.qq.com/pay/genprepay?access_token=%s",
					accessToken);

			Log.d(TAG, "doInBackground, url = " + url);
			Log.d(TAG, "doInBackground, entity = " + entity);

			GetPrepayIdResult result = new GetPrepayIdResult();

			byte[] buf = Utils.httpPost(url, entity);

			// ****************************************

			if (buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}
			// 获取预付网络Json数据，
			jsonPrepayData = new String(buf);
			String content = new String(buf);
			Log.d(TAG, "doInBackground, content = " + content);
			result.parseFrom(content);
			return result;
		}
	}

    private class GeneratePrepayID extends
            AsyncTask<Void, Void, GetPrepayIdResult> {
        
        private ProgressDialog dialog;
        private String accessToken, appKey, packageValue, traceId;
        
        public GeneratePrepayID(String[] params) {
            this.accessToken = params[0];
            this.appKey = params[1];
            packageValue = params[2];
            this.traceId = getTraceId();
            if(params.length > 3){
                this.traceId = params[3];
            }
        }
        
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(mContext, "提示", "正在获取预支付订单...");
        }
        
        @Override
        protected void onPostExecute(GetPrepayIdResult result) {
            if (dialog != null) {
                dialog.dismiss();
            }
        
            if (result.localRetCode == LocalRetCode.ERR_OK) {
                jsCallback(CB_GET_PREPAY_ID, 0, EUExCallback.F_C_TEXT,
                        jsonPrepayData);
            } else {
                jsCallback(CB_GET_PREPAY_ID, 0, EUExCallback.F_C_TEXT,
                        result.localRetCode.name());
            }
        }
        
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
        
        @Override
        protected GetPrepayIdResult doInBackground(Void... params) {
        
            String url = String.format(
                    "https://api.weixin.qq.com/pay/genprepay?access_token=%s",
                    accessToken);
        
            Log.d(TAG, "doInBackground, url = " + url);
            String entity = genProductArgs(appKey, packageValue, traceId);
            GetPrepayIdResult result = new GetPrepayIdResult();
        
            byte[] buf = Utils.httpPost(url, entity);
        
            // ****************************************
        
            if (buf == null || buf.length == 0) {
                result.localRetCode = LocalRetCode.ERR_HTTP;
                return result;
            }
            // 获取预付网络Json数据，
            jsonPrepayData = new String(buf);
            String content = new String(buf);
            Log.d(TAG, "doInBackground, content = " + content);
            result.parseFrom(content);
            return result;
        }
    }

    private String genProductArgs(String appKey, String packageValue, String traceId) {
        JSONObject json = new JSONObject();
        
        try {
            json.put("appid", appId);
            if(!TextUtils.isEmpty(traceId)){
                json.put("traceid", traceId); 
            }
            nonceStr = genNonceStr();
            json.put("noncestr", nonceStr);
            json.put("package", packageValue);
            timeStamp = genTimeStamp();
            json.put("timestamp", timeStamp);
            
            List<NameValuePair> signParams = new LinkedList<NameValuePair>();
            signParams.add(new BasicNameValuePair("appid", appId));
            signParams.add(new BasicNameValuePair("appkey", appKey));
            signParams.add(new BasicNameValuePair("noncestr", nonceStr));
            signParams.add(new BasicNameValuePair("package", packageValue));
            signParams.add(new BasicNameValuePair("timestamp", String.valueOf(timeStamp)));
            signParams.add(new BasicNameValuePair("traceid", traceId));
            json.put("app_signature", genSign(signParams));
            
            json.put("sign_method", "sha1");
        } catch (Exception e) {
            Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
            return null;
        }
        
        return json.toString();
    }

    public void sendPay(String[] params) {
        PayReq req = new PayReq();
        req.appId = appId;
        req.partnerId = params[0];
        req.prepayId = params[1];
        req.nonceStr = nonceStr;
        req.timeStamp = String.valueOf(timeStamp);
        req.packageValue = "Sign=" + params[3];
        
        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("appkey", params[2]));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
        req.sign = genSign(signParams);

        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        api.sendReq(req);
    }  
    
	public void gotoPay(String[] params) {
	    PayReq req = new PayReq();
		req.appId = Utils.getAppId(mContext);
		req.partnerId = params[0];
		req.prepayId = params[1];
		req.packageValue = params[2];
		req.nonceStr = params[3];
		req.timeStamp = params[4];
        req.sign = params[5];
        api.sendReq(req);
	}

	private String genSign(final List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (; i < params.size() - 1; i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append(params.get(i).getName());
		sb.append('=');
		sb.append(params.get(i).getValue());

		String sha1 = Utils.sha1(sb.toString());
		return sha1;
	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

	/**
	 * 建议 traceid 字段包含用户信息及订单信息，方便后续对订单状态的查询和跟踪
	 */
	private String getTraceId() {
		return "crestxu_" + genTimeStamp();
	}

	private String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	private static class GetAccessTokenResult {

		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;

		public void parseFrom(String content) {

			if (content == null || content.length() <= 0) {
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {
				JSONObject json = new JSONObject(content);
				if (json.has("access_token")) { // success case
					localRetCode = LocalRetCode.ERR_OK;
				} else {
					localRetCode = LocalRetCode.ERR_JSON;
				}

			} catch (Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}

	private static enum LocalRetCode {
		ERR_OK, ERR_HTTP, ERR_JSON, ERR_OTHER
	}

	@Override
	protected boolean clean() {
		return false;
	}

	public interface WeChatCallBack {
		void callBackPayResult(BaseResp msg);

		void callBackShareResult(int message);

		void backLoginResult(BaseResp msg);

	}

	public boolean shareTextContent(String[] params) {
		Log.d(TAG, "shareText");
		code = SHARE_TEXT_CONTENT_CODE;
		try {
			JSONObject jsonObject = new JSONObject(params[0]);
			int scene = jsonObject.getInt(PARAMS_JSON_KEY_SCENE);
			String text = jsonObject.getString(PARAMS_JSON_KEY_TEXT);
			return sendTextContent(scene, text);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误： " + params[0], Toast.LENGTH_SHORT)
					.show();
		}
		return false;
	}

	public boolean shareImageContent(String[] params) {
		Log.d(TAG, "shareImage");
		code = SHARE_IMAGE_CONTENT_CODE;
		try {
			JSONObject jsonObject = new JSONObject(params[0]);
			int scene = jsonObject.getInt(PARAMS_JSON_KEY_SCENE);
			String thumbImg = jsonObject.getString(PARAMS_JSON_KEY_THUMBIMG);
			String image = jsonObject.getString(PARAMS_JSON_KEY_IMAGE);
			shareImage(scene, thumbImg, image);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	/**
	 * 
	 * @param scene
	 *            发送场景，0 微信， 1 朋友圈
	 * @param thumImgPath
	 *            缩略图地址
	 * @param realImgPath
	 *            图片地址
	 * @return true 发送成功， false 发送失败
	 */
	public boolean shareImage(int scene, String thumbImgPath,
			String realImgPath) {
		if (realImgPath == null || realImgPath.length() == 0) {
			return false;
		}

        String imgPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), realImgPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
		WXImageObject imgObj = createImageObject(imgPath);

		String thumbPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), thumbImgPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		// 如果没有缩略图地址，图片地址赋值给缩略图地址，生成缩略图
		if (thumbPath == null || thumbPath.length() == 0) {
			thumbPath = imgPath;
		}
		Bitmap thumbBmp = createThumbBitmap(thumbPath);

		WXMediaMessage msg = new WXMediaMessage();
		msg.thumbData = Utils.bmpToByteArray(thumbBmp, true);
		msg.mediaObject = imgObj;

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = scene;
		return api.sendReq(req);
	}

	public boolean shareLinkContent(String[] params) {
		Log.d(TAG, "shareLink");
		code = SHARE_LINK_CONTENT_CODE;
		try {
			JSONObject jsonObject = new JSONObject(params[0]);
			int scene = jsonObject.getInt(PARAMS_JSON_KEY_SCENE);
			String thumbImg = jsonObject.getString(PARAMS_JSON_KEY_THUMBIMG);
			String wedpageUrl = jsonObject.getString(PARAMS_JSON_KEY_WEDPAGEURL);
			String title = jsonObject.getString(PARAMS_JSON_KEY_TITLE);
			String description = jsonObject.getString(PARAMS_JSON_KEY_DESCRIPTION);
			shareLink(scene, thumbImg, title, description, wedpageUrl);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	/**
	 * 
	 * @param scene
	 *            发送场景，0 微信， 1 朋友圈
	 * @param thumImgPath
	 *            缩略图地址
	 * @param title
	 *            网页标题
	 * @param description
	 *            网页描述
	 * @param wedpageUrl
	 *            网页url
	 * @return true 发送成功， false 发送失败
	 */
	public boolean shareLink(int scene, String thumbImgPath,
			String title, String description, String wedpageUrl) {
		if (wedpageUrl == null || wedpageUrl.length() == 0) {
			return false;
		}

        String thumbPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), thumbImgPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
		Bitmap thumbBmp = createThumbBitmap(thumbPath);

		WXMediaMessage msg = new WXMediaMessage();
		msg.thumbData = Utils.bmpToByteArray(thumbBmp, true);
		msg.title = title;
		msg.description = description;
		WXWebpageObject webObj = new WXWebpageObject();
		webObj.webpageUrl = wedpageUrl;
		msg.mediaObject = webObj;

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("link");
		req.message = msg;
		req.scene = scene;
		return api.sendReq(req);
	}

	private WXImageObject createImageObject(String imgPath) {
		WXImageObject imgObj = new WXImageObject();
		if (imgPath.startsWith("http://")) {
			imgObj.imageUrl = imgPath;
		} else {
			if (imgPath.startsWith("/")) {
				imgObj.imagePath = imgPath;
			} else {
				try {
					InputStream in = mContext.getAssets().open(imgPath);
					int lenght = in.available();
					BitmapFactory.Options opts = null;
					int count = 0;
					count = lenght / 100000 + 1;
					opts = new BitmapFactory.Options();
					if (lenght > 100000) {
						opts.inSampleSize = count;
					} else {
						opts.inSampleSize = 1;
					}
					Bitmap bmp = BitmapFactory.decodeStream(mContext
							.getAssets().open(imgPath));
					imgObj.imageData = Utils.bmpToByteArray(bmp, true);
				} catch (Exception e) {
					Toast.makeText(mContext, "图片不存在： " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}
		}
		return imgObj;
	}

	private Bitmap createThumbBitmap(String thumbPath) {
		// 缩略图地址临时变量
		Bitmap bmp = null;
		if (thumbPath.startsWith("http://")) {
			try {
				bmp = BitmapFactory.decodeStream(new URL(thumbPath)
						.openStream());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (thumbPath.startsWith("/")) {// sd卡路径时
				File f = new File(thumbPath);
				if (!f.exists()) {
					Toast.makeText(mContext, "File is not exist!",
							Toast.LENGTH_SHORT).show();
				}
				bmp = BitmapFactory.decodeFile(thumbPath);
			} else {
				try {
					bmp = BitmapFactory.decodeStream(mContext.getAssets().open(thumbPath));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE,
				THUMB_SIZE, true);
			
		if (bmp != null && bmp != thumbBmp) {
			bmp.recycle();
		}
		return thumbBmp;
	}

    public void getPrepayId(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_GET_PREPAY_ID;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void getPrepayIdMsg(String[] params) {
        String json = params[0];
        PrePayDataVO dataVO = DataHelper.gson.fromJson(json, PrePayDataVO.class);
        WXPayGetPrepayIdTask task = new WXPayGetPrepayIdTask(mContext, dataVO, listener);
        task.getPrepayId();
    }

    public void startPay(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_START_PAY;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void startPayMsg(String[] params) {
        String json = params[0];
        PayDataVO dataVO = DataHelper.gson.fromJson(json, PayDataVO.class);
        try {
            JSONObject jsonObject = new JSONObject(json);
            dataVO.setPackageValue(jsonObject.getString(JsConst.PACKAGE_VALUE));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WXPayGetPrepayIdTask task = new WXPayGetPrepayIdTask(mContext, dataVO);
        task.pay();
    }
    public void login(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_LOGIN;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void loginMsg(String[] params) {
        String json = params[0];
        if (TextUtils.isEmpty(appId)){
            errorCallback(0, 0, "please register first!");
            return;
        }
        isLoginNew = true;
        LoginVO dataVO = DataHelper.gson.fromJson(json, LoginVO.class);
        dataVO.setAppid(appId);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = dataVO.getScope();// "snsapi_userinfo"
        req.state = dataVO.getState();// "wechat_sdk_demo_test"
        Log.d(TAG, req.scope + "=======>" + req.state + "======appId====>"
                + appId);
        api.sendReq(req);
    }

    public void getLoginAccessToken(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_GET_LOGIN_ACCESS_TOKEN;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void getLoginAccessTokenMsg(String[] params) {
        Log.i("EuexWeChat", "getLoginAccessTokenMsg->" + Arrays.toString(params));
        String json = params[0];
        if (TextUtils.isEmpty(appId)){
            errorCallback(0, 0, "please register first!");
            return;
        }
        LoginAccessTokenVO dataVO = DataHelper.gson.fromJson(json, LoginAccessTokenVO.class);
        dataVO.setAppid(appId);
        isLoginNew = true;
        try {
            countCode = Constants.token;
            String url = String
                    .format(JsConst.URL_LOGIN_GET_ACCESS_TOKEN,
                            dataVO.getAppid(), dataVO.getSecret(),
                            dataVO.getCode(), dataVO.getGrant_type());
            NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
            token.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLoginRefreshAccessToken(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_GET_LOGIN_REFRESH_ACCESS_TOKEN;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void getLoginRefreshAccessTokenMsg(String[] params) {
        Log.i("EuexWeChat", "getLoginRefreshAccessTokenMsg->" + Arrays.toString(params));
        String json = params[0];
        if (TextUtils.isEmpty(appId)){
            errorCallback(0, 0, "please register first!");
            return;
        }
        LoginRefreshTokenVO dataVO = DataHelper.gson.fromJson(json, LoginRefreshTokenVO.class);
        dataVO.setAppid(appId);
        isLoginNew = true;
        try {
            countCode = Constants.refresh;
            String url = String
                    .format(JsConst.URL_LOGIN_REFRESH_ACCESS_TOKEN, dataVO.getAppid(),
                            dataVO.getGrant_type(), dataVO.getRefresh_token());
            NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
            token.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLoginCheckAccessToken(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_GET_LOGIN_CHECK_ACCESS_TOKEN;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void getLoginCheckAccessTokenMsg(String[] params) {
        Log.i("EuexWeChat", "getLoginCheckAccessTokenMsg->" + Arrays.toString(params));
        String json = params[0];
        LoginCheckTokenVO dataVO = DataHelper.gson.fromJson(json, LoginCheckTokenVO.class);
        isLoginNew = true;
        try {
            countCode = Constants.check;
            String url = String
                    .format(JsConst.URL_LOGIN_CHECK_ACCESS_TOKEN,
                            dataVO.getAccess_token(), dataVO.getOpenid());
            NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
            token.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLoginUnionID(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_GET_LOGIN_UNION_I_D;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void getLoginUnionIDMsg(String[] params) {
        Log.i("EuexWeChat", "getLoginUnionIDMsg->" + Arrays.toString(params));
        String json = params[0];
        LoginCheckTokenVO dataVO = DataHelper.gson.fromJson(json, LoginCheckTokenVO.class);
        isLoginNew = true;
        try {
            countCode = Constants.union;
            String url = String
                    .format(JsConst.URL_LOGIN_UNIONID,
                            dataVO.getAccess_token(), dataVO.getOpenid());
            NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
            token.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCallbackWindowName(String[] params){
        if (params == null || params.length < 1) return;
        CallbackWindowNameVO dataVO = DataHelper.gson.fromJson(params[0],
                CallbackWindowNameVO.class);
        if (dataVO != null){
            mWindowName = dataVO.getWindowName();
        }
    }

    @Override
    public void onHandleMessage(Message message) {
        if(message == null){
            return;
        }
        Bundle bundle=message.getData();
        switch (message.what) {

            case MSG_GET_PREPAY_ID:
                getPrepayIdMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_START_PAY:
                startPayMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_LOGIN:
                loginMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_GET_LOGIN_ACCESS_TOKEN:
                getLoginAccessTokenMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_GET_LOGIN_REFRESH_ACCESS_TOKEN:
                getLoginRefreshAccessTokenMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_GET_LOGIN_CHECK_ACCESS_TOKEN:
                getLoginCheckAccessTokenMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_GET_LOGIN_UNION_I_D:
                getLoginUnionIDMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            default:
                super.onHandleMessage(message);
        }
    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        Log.i(TAG, "callBackPluginJs:" + js);
        onCallback(js);
    }

    private void callBackPluginJsAsync(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        Log.i(TAG, "callBackPluginJsAsync:" + js);
        evaluateScript(mWindowName, 0, js);
        //mBrwView.addUriTaskAsyn(js);
    }
    private void callbackOldInterface(String functionName, int opId, int type, String content) {
        String js = SCRIPT_HEADER + "if(" + functionName + "){"
                + functionName + "(" + opId + "," + type + ",'"
                + content + "'" + SCRIPT_TAIL;
        evaluateScript(mWindowName, 0, js);
    }

    private void shareCallBack(String funName, String code) {
        String js = SCRIPT_HEADER + "if(" + funName + "){" + funName + "('"
                + code + "')}";
        //mBrwView.addUriTaskAsyn(js);
        evaluateScript(mWindowName, 0, js);
    }
    OnPayResultListener listener = new OnPayResultListener() {
        @Override
        public void onGetPrepayResult(String json) {
            callBackPluginJs(JsConst.CALLBACK_GET_PREPAY_ID, json);
        }
    };

    public interface OnPayResultListener{
        public void onGetPrepayResult(String jsonData);
    }


}
