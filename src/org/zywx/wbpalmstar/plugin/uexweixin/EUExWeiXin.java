package org.zywx.wbpalmstar.plugin.uexweixin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
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
import org.zywx.wbpalmstar.plugin.uexweixin.utils.BitmapUtil;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.SecurityUtil;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.MLog;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.SharedPreferencesUtil;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.NetworkUtil;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.WXPayGetPrepayIdTask;
import org.zywx.wbpalmstar.plugin.uexweixin.vo.CallbackWindowNameVO;
import org.zywx.wbpalmstar.plugin.uexweixin.vo.LoginAccessTokenVO;
import org.zywx.wbpalmstar.plugin.uexweixin.vo.LoginCheckTokenVO;
import org.zywx.wbpalmstar.plugin.uexweixin.vo.LoginRefreshTokenVO;
import org.zywx.wbpalmstar.plugin.uexweixin.vo.LoginResultVO;
import org.zywx.wbpalmstar.plugin.uexweixin.vo.LoginVO;
import org.zywx.wbpalmstar.plugin.uexweixin.vo.PayDataVO;
import org.zywx.wbpalmstar.plugin.uexweixin.vo.PrePayDataVO;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class EUExWeiXin extends EUExBase {

	public final static String PARAMS_JSON_KEY_TEXT = "text";
	public final static String PARAMS_JSON_KEY_SCENE = "scene";
	public final static String PARAMS_JSON_KEY_THUMBIMG = "thumbImg";
	public final static String PARAMS_JSON_KEY_IMAGE = "image";
	public final static String PARAMS_JSON_KEY_WEDPAGEURL = "wedpageUrl";
	public final static String PARAMS_JSON_KEY_TITLE = "title";
	public final static String PARAMS_JSON_KEY_DESCRIPTION = "description";

	/**
	 * 通用
	 */
	private IWXAPI mApi;// 微信OpenAPI访问入口
	private String mAppId;// appId
	public static WeiXinCallback weiXinCallback;// 微信回调接口，设计为公共静态变量主要是因为WXEntryActivity要调用
	// 回调
	public static final String CB_REGISTER_WXAPP_RESULT = "uexWeiXin.cbRegisterApp";// 用户授权回调
	public static final String CB_IS_WXAPP_INSTALLIED = "uexWeiXin.cbIsWXAppInstalled";// 检查微信是否已安装回调

	/**
	 * 微信分享
	 */
	private int mShareType;// 分享类型
	private static final int SHARE_TYPE_TEXT_OLD = 1;
	private static final int SHARE_TYPE_IMAGE_OLD = 2;
	private static final int SHARE_TYPE_TEXT = 3;
	private static final int SHARE_TYPE_IMAGE = 4;
	private static final int SHARE_TYPE_LINK = 5;
	// 回调
	// 旧
	public static final String CB_SEND_TEXT_CONTENT = "uexWeiXin.cbSendTextContent";
	public static final String CB_SEND_IMAGE_CONTENT = "uexWeiXin.cbSendImageContent";
	// 新
	public static final String CB_SHARE_TEXT_CONTENT = "uexWeiXin.cbShareTextContent";
	public static final String CB_SHARE_IMAGE_CONTENT = "uexWeiXin.cbShareImageContent";
	public static final String CB_SHARE_LINK_CONTENT = "uexWeiXin.cbShareLinkContent";

	/**
	 * 微信登录
	 */
	private String mAccessCode;// 授权临时票据code参数，用来获取access_token
	private boolean isLoginNew = false;// 是否使用新登录接口登录
	private int mLoginType;// 用来区分登录操作的类型
	private static final int LOGIN_TYPE_GET_ACCESSTOKEN = 1;
	private static final int LOGIN_TYPE_REFRESH_ACCESSTOKEN = 2;
	private static final int LOGIN_TYPE_CHECK_ACCESSTOKEN = 3;
	private static final int LOGIN_TYPE_UNION_ID = 4;
	// Message.what
	private static final int MSG_LOGIN = 6;
	private static final int MSG_GET_LOGIN_ACCESS_TOKEN = 7;
	private static final int MSG_GET_LOGIN_REFRESH_ACCESS_TOKEN = 8;
	private static final int MSG_GET_LOGIN_CHECK_ACCESS_TOKEN = 9;
	private static final int MSG_GET_LOGIN_UNION_I_D = 10;
	// 回调
	// 旧
	public static final String CB_WEIXIN_LOGIN = "uexWeiXin.cbWeiXinLogin";
	public static final String CB_GET_WEIXIN_LOGIN_ACCESSTOKEN = "uexWeiXin.cbGetWeiXinLoginAccessToken";
	public static final String CB_GET_WEIXIN_LOGIN_REFRESH_ACCESSTOKEN = "uexWeiXin.cbGetWeiXinLoginRefreshAccessToken";
	public static final String CB_GET_WEIXIN_LOGIN_CHECK_ACCESSTOKEN = "uexWeiXin.cbGetWeiXinLoginCheckAccessToken";
	public static final String CB_GET_WEIXIN_LOGIN_UNION_ID = "uexWeiXin.cbGetWeiXinLoginUnionID";
	// 新 详见JsConst类

	/**
	 * 微信支付
	 */
	private long timeStamp;
	private String nonceStr;
	private static String mWindowName = null;
	private static final String BUNDLE_DATA = "data";
	// Message.what
	private static final int MSG_GET_PREPAY_ID = 1;
	private static final int MSG_START_PAY = 2;
	// 回调
	public static final String CB_IS_PAY_SUPPORTED = "uexWeiXin.cbIsSupportPay";
	public static final String CB_GET_ACCESS_TOKEN = "uexWeiXin.cbGetAccessToken";
	public static final String CB_GET_PREPAY_ID = "uexWeiXin.cbGenerateAdvanceOrder";
	public static final String CB_GET_PAY_RESULT = "uexWeiXin.cbGotoPay";
	public static final String CB_GET_ACCESS_TOKEN_LOCAL = "uexWeiXin.cbGetAccessTokenLocal";

	/**
	 * 构造方法
	 * 
	 * @param context
	 * @param parent
	 */
	public EUExWeiXin(Context context, EBrowserView parent) {
		super(context, parent);

		// 初始化WeiXinCallback
		initWeiXinCallback();
	}

	/**
	 * 初始化WeiXinCallback
	 * 
	 * 
	 * @tips 其实回调是在
	 * 
	 *       $应用程序包名$.wxapi.WXEntryActivity和WXPayEntryActivity
	 * 
	 *       中被触发的
	 * 
	 * @tips 这是微信的机制，别的地方收不到微信传来的回调，只能在这两个Activity里
	 * 
	 */
	private void initWeiXinCallback() {

		weiXinCallback = new WeiXinCallback() {

			// 分享回调
			@Override
			public void callbackShareResult(BaseResp resp) {

				MLog.getIns().d("start");

				int errCode = resp.errCode;

				switch (mShareType) {

				// 文字(旧接口)
				case SHARE_TYPE_TEXT_OLD:
					jsCallback(CB_SEND_TEXT_CONTENT, 0, EUExCallback.F_C_TEXT, errCode == 0 ? "0" : "1");
					break;

				// 图片(旧接口)
				case SHARE_TYPE_IMAGE_OLD:
					jsCallback(CB_SEND_IMAGE_CONTENT, 0, EUExCallback.F_C_TEXT, errCode == 0 ? "0" : "1");
					break;

				// 文字
				case SHARE_TYPE_TEXT:
					callbackPlugin2Js(CB_SHARE_TEXT_CONTENT, errCode == 0 ? "0" : "1");
					break;

				// 图片
				case SHARE_TYPE_IMAGE:
					callbackPlugin2Js(CB_SHARE_IMAGE_CONTENT, errCode == 0 ? "0" : "1");
					break;

				// 链接
				case SHARE_TYPE_LINK:
					callbackPlugin2Js(CB_SHARE_LINK_CONTENT, errCode == 0 ? "0" : "1");
					break;

				default:
					break;
				}

			}

			// 登录回调
			@Override
			public void callbackLoginResult(BaseResp resp) {

				MLog.getIns().d("start");

				// 如果是正确返回，获得用户换取access_token的code
				if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
					mAccessCode = ((SendAuth.Resp) resp).code;// 需要转换一下才可以
					MLog.getIns().i("mAccessCode = " + mAccessCode);
				}
				if (isLoginNew) {
					LoginResultVO resultVO = new LoginResultVO();
					resultVO.setErrCode(resp.errCode + "");
					SendAuth.Resp resp2 = (SendAuth.Resp) resp;
					if (resp2 != null) {
						if (resp.errCode == 0) {
							resultVO.setCode(resp2.code);
						}
						resultVO.setState(resp2.state);
						resultVO.setLanguage(resp2.lang);
						resultVO.setCountry(resp2.country);
						String resultStr = DataHelper.gson.toJson(resultVO);
						// shareCallBack(JsConst.CALLBACK_LOGIN, resultStr);
						callbackPlugin2Js(JsConst.CALLBACK_LOGIN, resultStr);
					}
					isLoginNew = false;
				} else {
					callbackOldInterface(CB_WEIXIN_LOGIN, 0, EUExCallback.F_C_TEXT, resp.errCode + "");
				}
			}

			// 支付回调
			@Override
			public void callbackPayResult(BaseResp resp) {
				callbackOldInterface(CB_GET_PAY_RESULT, 0, EUExCallback.F_C_JSON, getJson(resp.errCode + "", resp.errStr));
				callbackPlugin2JsAsync(JsConst.CALLBACK_START_PAY, getJson(resp.errCode + "", resp.errStr));
			}
		};
	}

	public String getJson(String errCode, String errStr) {
		String myString;
		if (errStr == null) {
			errStr = "";
		}
		try {
			myString = new JSONStringer().object().key("errCode").value(errCode).key("errStr").value(errStr).endObject().toString();
			MLog.getIns().d("path = " + myString);
			return myString;
		} catch (JSONException e) {
			Toast.makeText(mContext, "getJson错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 注册应用到微信
	 * 
	 * @param mAppId
	 *            从微信开放平台申请的appId
	 * @return true 注册成功， false 注册失败
	 */
	public boolean registerApp(String[] data) {

		MLog.getIns().d("start");

		if (data == null || data.length < 1) {
			return false;
		}
		mAppId = data[0];
		if (mAppId == null || mAppId.length() == 0) {
			return false;
		}
		mApi = WXAPIFactory.createWXAPI(mContext, mAppId, false);
		boolean regOk = mApi.registerApp(mAppId);
		if (regOk) {
			SharedPreferencesUtil.saveAppIdInSP(mContext, mAppId);
		}
		jsCallback(CB_REGISTER_WXAPP_RESULT, 0, EUExCallback.F_C_INT, regOk ? 0 : 1);// 注册回调0-成功1-失败
		return regOk;
	}

	/**
	 * 判断是否安装微信应用，
	 * 
	 * @return true 已安装， false 未安装
	 */
	public boolean isWXAppInstalled(String[] args) {
		MLog.getIns().d("isWXAppInstalled");
		// 判断是否安装微信，安装返回true。
		boolean isWXInstalled = mApi.isWXAppInstalled();
		jsCallback(CB_IS_WXAPP_INSTALLIED, 0, EUExCallback.F_C_TEXT, isWXInstalled ? 0 : 1);
		return isWXInstalled;
	}

	// TODO
	/* -------------------微信分享---------------------- */

	/**
	 * 分享文本(旧接口，不推荐使用)
	 * 
	 * @param params
	 * @return
	 */
	public boolean sendTextContent(String[] params) {
		MLog.getIns().d("sendTextContent");
		if (params == null || params.length < 2) {
			return false;
		}
		mShareType = SHARE_TYPE_TEXT_OLD;
		try {
			int scene = Integer.parseInt(params[0]);
			String text = params[1];
			return shareText(scene, text);
		} catch (Exception e) {
			Toast.makeText(mContext, "错误： " + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 分享文本(新接口)
	 * 
	 * @param params
	 * @return
	 */
	public boolean shareTextContent(String[] params) {
		MLog.getIns().d("start");
		mShareType = SHARE_TYPE_TEXT;
		try {
			JSONObject jsonObject = new JSONObject(params[0]);
			int scene = jsonObject.getInt(PARAMS_JSON_KEY_SCENE);
			String text = jsonObject.getString(PARAMS_JSON_KEY_TEXT);
			return shareText(scene, text);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 分享文本的处理逻辑
	 * 
	 * @param scene
	 * @param text
	 * @return
	 */
	private boolean shareText(int scene, String text) {
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
		req.transaction = buildTransaction("text");
		MLog.getIns().d("scene = " + scene);
		return mApi.sendReq(req);

	}

	/**
	 * 分享图片(旧接口，不推荐使用)
	 * 
	 * @param params
	 * @return
	 */
	public boolean sendImageContent(String[] params) {
		MLog.getIns().d("start");
		if (params == null || params.length < 3) {
			return false;
		}
		mShareType = SHARE_TYPE_IMAGE_OLD;
		try {
			int scene = Integer.parseInt(params[0]);
			String thumbPath = params[1];
			String imgPath = params[2];
			return shareImage(scene, thumbPath, imgPath);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 分享图片(新接口)
	 * 
	 * @param params
	 * @return
	 */
	public boolean shareImageContent(String[] params) {
		MLog.getIns().d("start");
		mShareType = SHARE_TYPE_IMAGE;
		try {
			JSONObject jsonObject = new JSONObject(params[0]);
			int scene = jsonObject.getInt(PARAMS_JSON_KEY_SCENE);
			String thumbImg = jsonObject.getString(PARAMS_JSON_KEY_THUMBIMG);
			String image = jsonObject.getString(PARAMS_JSON_KEY_IMAGE);
			shareImage(scene, thumbImg, image);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 分享图片的处理逻辑
	 * 
	 * @param scene
	 *            发送场景，0 微信， 1 朋友圈
	 * @param thumImgPath
	 *            缩略图地址
	 * @param realImgPath
	 *            图片地址
	 * @return true 发送成功， false 发送失败
	 */
	public boolean shareImage(int scene, String thumbImgPath, String realImgPath) {
		if (realImgPath == null || realImgPath.length() == 0) {
			return false;
		}

		String imgPath = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(), realImgPath), mBrwView.getCurrentWidget().m_widgetPath, mBrwView.getCurrentWidget().m_wgtType);
		WXImageObject imgObj = BitmapUtil.createImageObject(mContext, imgPath);

		String thumbPath = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(), thumbImgPath), mBrwView.getCurrentWidget().m_widgetPath, mBrwView.getCurrentWidget().m_wgtType);
		// 如果没有缩略图地址，图片地址赋值给缩略图地址，生成缩略图
		if (thumbPath == null || thumbPath.length() == 0) {
			thumbPath = imgPath;
		}
		Bitmap thumbBmp = BitmapUtil.createThumbBitmap(mContext, thumbPath);

		WXMediaMessage msg = new WXMediaMessage();
		msg.thumbData = BitmapUtil.bmpToByteArray(thumbBmp, true);
		msg.mediaObject = imgObj;

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = scene;
		return mApi.sendReq(req);
	}

	/**
	 * 分享链接
	 * 
	 * @param params
	 * @return
	 */
	public boolean shareLinkContent(String[] params) {
		MLog.getIns().d("start");
		mShareType = SHARE_TYPE_LINK;
		try {
			JSONObject jsonObject = new JSONObject(params[0]);
			int scene = jsonObject.getInt(PARAMS_JSON_KEY_SCENE);
			String thumbImg = jsonObject.getString(PARAMS_JSON_KEY_THUMBIMG);
			String wedpageUrl = jsonObject.getString(PARAMS_JSON_KEY_WEDPAGEURL);
			String title = jsonObject.getString(PARAMS_JSON_KEY_TITLE);
			String description = jsonObject.getString(PARAMS_JSON_KEY_DESCRIPTION);
			shareLink(scene, thumbImg, title, description, wedpageUrl);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 分享链接的处理逻辑
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
	public boolean shareLink(int scene, String thumbImgPath, String title, String description, String wedpageUrl) {
		if (wedpageUrl == null || wedpageUrl.length() == 0) {
			return false;
		}

		String thumbPath = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(), thumbImgPath), mBrwView.getCurrentWidget().m_widgetPath, mBrwView.getCurrentWidget().m_wgtType);
		Bitmap thumbBmp = BitmapUtil.createThumbBitmap(mContext, thumbPath);

		WXMediaMessage msg = new WXMediaMessage();
		msg.thumbData = BitmapUtil.bmpToByteArray(thumbBmp, true);
		msg.title = title;
		msg.description = description;
		WXWebpageObject webObj = new WXWebpageObject();
		webObj.webpageUrl = wedpageUrl;
		msg.mediaObject = webObj;

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("link");
		req.message = msg;
		req.scene = scene;
		return mApi.sendReq(req);
	}

	/**
	 * 创建transaction字段
	 * 
	 * @param type
	 * @return
	 */
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	// TODO
	/* -------------------微信登录---------------------- */

	/* 旧接口，不推荐使用,但为了兼容依然保留 */

	/**
	 * 微信授权登录
	 * 
	 * @param params
	 */
	public void weiXinLogin(String[] params) {

		MLog.getIns().d("start");

		if (params.length < 1) {
			MLog.getIns().e("params.length < 1");
			return;
		}

		final SendAuth.Req req = new SendAuth.Req();
		req.scope = params[0];// "snsapi_userinfo"
		if (params.length >= 2) {
			req.state = params[1];
		}
		MLog.getIns().d("req.scope = " + req.scope + " req.state = " + req.state + " appId = " + mAppId);
		boolean result = mApi.sendReq(req);
		MLog.getIns().d("result = " + result);
	}

	/**
	 * 微信登陆获取accessToken
	 * 
	 * @param parms
	 */
	public void getWeiXinLoginAccessToken(String[] parms) {
		MLog.getIns().d("start");
		if (parms.length < 2) {
			return;
		}
		try {
			mLoginType = LOGIN_TYPE_GET_ACCESSTOKEN;
			String secret = parms[0];
			String grant_type = parms[1];

			MLog.getIns().i("grant_type=====>" + grant_type);

			String url = String.format(JsConst.URL_LOGIN_GET_ACCESS_TOKEN, mAppId, secret, mAccessCode, grant_type);
			MLog.getIns().i("url=====>" + url);
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	/**
	 * 微信登陆获取refreshToken
	 * 
	 * @param parms
	 */
	public void getWeiXinLoginRefreshAccessToken(String[] parms) {
		MLog.getIns().d("start");
		if (parms.length < 2) {
			return;
		}
		try {
			mLoginType = LOGIN_TYPE_REFRESH_ACCESSTOKEN;
			String grant_type = parms[0];
			String refresh_token = parms[1];

			String url = String.format(JsConst.URL_LOGIN_REFRESH_ACCESS_TOKEN, mAppId, grant_type, refresh_token);
			MLog.getIns().i("url=====>" + url);
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	/**
	 * 检测微信登陆的token
	 * 
	 * @param parms
	 */
	public void getWeiXinLoginCheckAccessToken(String[] parms) {
		MLog.getIns().d("start");
		if (parms.length < 2) {
			return;
		}
		try {
			mLoginType = LOGIN_TYPE_CHECK_ACCESSTOKEN;
			String access_token = parms[0];
			String openid = parms[1];
			String url = String.format(JsConst.URL_LOGIN_CHECK_ACCESS_TOKEN, access_token, openid);
			MLog.getIns().i("url=====>" + url);
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	/**
	 * 获取个人信息
	 * 
	 * @param parms
	 */
	public void getWeiXinLoginUnionID(String[] parms) {
		MLog.getIns().d("start");
		if (parms.length < 2) {
			return;
		}
		try {
			mLoginType = LOGIN_TYPE_UNION_ID;
			String access_token = parms[0];
			String openid = parms[1];

			String url = String.format(JsConst.URL_LOGIN_UNIONID, access_token, openid);
			MLog.getIns().i("url=====>" + url);
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);

		} catch (Exception e) {
			Toast.makeText(mContext, "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	// TODO
	/* 新接口(推荐使用) */

	/**
	 * 微信授权登录
	 * 
	 * @param params
	 */
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
		if (TextUtils.isEmpty(mAppId)) {
			errorCallback(0, 0, "please register first!");
			return;
		}
		isLoginNew = true;
		try {
			LoginVO dataVO = DataHelper.gson.fromJson(json, LoginVO.class);
			dataVO.setAppid(mAppId);
			SendAuth.Req req = new SendAuth.Req();
			req.scope = dataVO.getScope();// "snsapi_userinfo"
			req.state = dataVO.getState();// "wechat_sdk_demo_test"
			MLog.getIns().i(req.scope + "=======>" + req.state + "======appId====>" + mAppId);
			mApi.sendReq(req);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	/**
	 * 微信登陆获取accessToken
	 * 
	 * @param parms
	 */
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
		MLog.getIns().i("getLoginAccessTokenMsg->" + Arrays.toString(params));
		String json = params[0];
		if (TextUtils.isEmpty(mAppId)) {
			errorCallback(0, 0, "please register first!");
			return;
		}
		try {
			LoginAccessTokenVO dataVO = DataHelper.gson.fromJson(json, LoginAccessTokenVO.class);
			dataVO.setAppid(mAppId);
			isLoginNew = true;
			mLoginType = LOGIN_TYPE_GET_ACCESSTOKEN;
			String url = String.format(JsConst.URL_LOGIN_GET_ACCESS_TOKEN, dataVO.getAppid(), dataVO.getSecret(), dataVO.getCode(), dataVO.getGrant_type());
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	/**
	 * 微信登陆获取refreshToken
	 * 
	 * @param params
	 */
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
		MLog.getIns().i("getLoginRefreshAccessTokenMsg->" + Arrays.toString(params));
		String json = params[0];
		if (TextUtils.isEmpty(mAppId)) {
			errorCallback(0, 0, "please register first!");
			return;
		}
		try {
			LoginRefreshTokenVO dataVO = DataHelper.gson.fromJson(json, LoginRefreshTokenVO.class);
			dataVO.setAppid(mAppId);
			isLoginNew = true;
			mLoginType = LOGIN_TYPE_REFRESH_ACCESSTOKEN;
			String url = String.format(JsConst.URL_LOGIN_REFRESH_ACCESS_TOKEN, dataVO.getAppid(), dataVO.getGrant_type(), dataVO.getRefresh_token());
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	/**
	 * 检测微信登陆的token
	 * 
	 * @param params
	 */
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
		MLog.getIns().i("getLoginCheckAccessTokenMsg->" + Arrays.toString(params));
		String json = params[0];
		try {
			LoginCheckTokenVO dataVO = DataHelper.gson.fromJson(json, LoginCheckTokenVO.class);
			isLoginNew = true;
			mLoginType = LOGIN_TYPE_CHECK_ACCESSTOKEN;
			String url = String.format(JsConst.URL_LOGIN_CHECK_ACCESS_TOKEN, dataVO.getAccess_token(), dataVO.getOpenid());
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	/**
	 * 获取个人信息
	 * 
	 * @param params
	 */
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
		MLog.getIns().i("getLoginUnionIDMsg->" + Arrays.toString(params));
		String json = params[0];
		try {
			LoginCheckTokenVO dataVO = DataHelper.gson.fromJson(json, LoginCheckTokenVO.class);
			isLoginNew = true;
			mLoginType = LOGIN_TYPE_UNION_ID;
			String url = String.format(JsConst.URL_LOGIN_UNIONID, dataVO.getAccess_token(), dataVO.getOpenid());
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	/**
	 * 微信登录Http请求AsyncTask
	 * 
	 * @author Administrator
	 *
	 */
	class NetWorkAsyncTaskToken extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			MLog.getIns().i("url->" + url);
			byte[] buf = NetworkUtil.httpGet(url);
			String callBack = new String(buf);
			return callBack;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			MLog.getIns().i(mLoginType + " 请求回的数据为：" + result);

			if (isLoginNew) {
				switch (mLoginType) {
				case LOGIN_TYPE_GET_ACCESSTOKEN:
					callbackPlugin2Js(JsConst.CALLBACK_GET_LOGIN_ACCESS_TOKEN, result);
					break;
				case LOGIN_TYPE_REFRESH_ACCESSTOKEN:
					callbackPlugin2Js(JsConst.CALLBACK_GET_LOGIN_REFRESH_ACCESS_TOKEN, result);
					break;
				case LOGIN_TYPE_CHECK_ACCESSTOKEN:
					callbackPlugin2Js(JsConst.CALLBACK_GET_LOGIN_CHECK_ACCESS_TOKEN, result);
					break;
				case LOGIN_TYPE_UNION_ID:
					callbackPlugin2Js(JsConst.CALLBACK_GET_LOGIN_UNION_I_D, result);
					break;
				default:
					break;
				}
				isLoginNew = false;
			} else {
				switch (mLoginType) {
				case LOGIN_TYPE_GET_ACCESSTOKEN:
					jsCallback(CB_GET_WEIXIN_LOGIN_ACCESSTOKEN, 0, EUExCallback.F_C_TEXT, result);
					break;
				case LOGIN_TYPE_REFRESH_ACCESSTOKEN:
					jsCallback(CB_GET_WEIXIN_LOGIN_REFRESH_ACCESSTOKEN, 0, EUExCallback.F_C_TEXT, result);
					break;
				case LOGIN_TYPE_CHECK_ACCESSTOKEN:

					String errCode = "";
					try {
						JSONObject jsonObject = new JSONObject(result);
						errCode = jsonObject.getString("errcode");
					} catch (JSONException e) {
						MLog.getIns().e(e);
						e.printStackTrace();
					}
					int returnErrCode = 1;// 给前端返回的错误码，默认为1代表失败
					if (errCode.equals("0")) {
						returnErrCode = 0;
					}
					jsCallback(CB_GET_WEIXIN_LOGIN_CHECK_ACCESSTOKEN, 0, EUExCallback.F_C_TEXT, returnErrCode);
					break;
				case LOGIN_TYPE_UNION_ID:
					jsCallback(CB_GET_WEIXIN_LOGIN_UNION_ID, 0, EUExCallback.F_C_TEXT, result);
					break;
				default:
					break;
				}
			}
		}
	}

	// TODO
	/* -------------------微信支付---------------------- */

	/**
	 * 是否支持支付
	 * 
	 * @param params
	 */
	public void isSupportPay(String[] params) {
		// getWXAppSupportSAPI是否支持微信版本。
		boolean isPaySupported = mApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
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

	private class GetAccessTokenTask extends AsyncTask<Void, Void, GetAccessTokenResult> {

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
				jsCallback(CB_GET_ACCESS_TOKEN, 0, EUExCallback.F_C_TEXT, contentAccess);

			} else {
				jsCallback(CB_GET_ACCESS_TOKEN, 0, EUExCallback.F_C_TEXT, result.localRetCode.name());
			}
		}

		@Override
		protected GetAccessTokenResult doInBackground(Void... params) {
			GetAccessTokenResult result = new GetAccessTokenResult();
			// String.format用与替换路径后面的%s。
			String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, appSecret);
			byte[] buf = NetworkUtil.httpGet(url);
			if (buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}
			contentAccess = new String(buf);
			// *****************************
			// 用于将获取到的Token数据，存储到本地。
			SharedPreferences.Editor token = mContext.getSharedPreferences("token", 0).edit();
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
			jsCallback(CB_GET_ACCESS_TOKEN_LOCAL, 0, EUExCallback.F_C_TEXT, contentAccessLocal);

		} else {
			jsCallback(CB_GET_ACCESS_TOKEN_LOCAL, 0, EUExCallback.F_C_TEXT, getLocalData().localRetCode.name());
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
				e.printStackTrace();
			}
		}
	}

	String jsonPrepayData = "";

	private class GetPrepayIdTask extends AsyncTask<Void, Void, GetPrepayIdResult> {

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
				jsCallback(CB_GET_PREPAY_ID, 0, EUExCallback.F_C_TEXT, jsonPrepayData);
			} else {
				jsCallback(CB_GET_PREPAY_ID, 0, EUExCallback.F_C_TEXT, result.localRetCode.name());
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected GetPrepayIdResult doInBackground(Void... params) {

			String url = String.format("https://api.weixin.qq.com/pay/genprepay?access_token=%s", accessToken);

			MLog.getIns().i("doInBackground, url = " + url);
			MLog.getIns().i("doInBackground, entity = " + entity);

			GetPrepayIdResult result = new GetPrepayIdResult();

			byte[] buf = NetworkUtil.httpPost(url, entity);

			// ****************************************

			if (buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}
			// 获取预付网络Json数据，
			jsonPrepayData = new String(buf);
			String content = new String(buf);
			MLog.getIns().i("doInBackground, content = " + content);
			result.parseFrom(content);
			return result;
		}
	}

	private class GeneratePrepayID extends AsyncTask<Void, Void, GetPrepayIdResult> {

		private ProgressDialog dialog;
		private String accessToken, appKey, packageValue, traceId;

		public GeneratePrepayID(String[] params) {
			this.accessToken = params[0];
			this.appKey = params[1];
			packageValue = params[2];
			this.traceId = getTraceId();
			if (params.length > 3) {
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
				jsCallback(CB_GET_PREPAY_ID, 0, EUExCallback.F_C_TEXT, jsonPrepayData);
			} else {
				jsCallback(CB_GET_PREPAY_ID, 0, EUExCallback.F_C_TEXT, result.localRetCode.name());
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected GetPrepayIdResult doInBackground(Void... params) {

			String url = String.format("https://api.weixin.qq.com/pay/genprepay?access_token=%s", accessToken);

			MLog.getIns().i("doInBackground, url = " + url);
			String entity = genProductArgs(appKey, packageValue, traceId);
			GetPrepayIdResult result = new GetPrepayIdResult();

			byte[] buf = NetworkUtil.httpPost(url, entity);

			// ****************************************

			if (buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}
			// 获取预付网络Json数据，
			jsonPrepayData = new String(buf);
			String content = new String(buf);
			MLog.getIns().i("doInBackground, content = " + content);
			result.parseFrom(content);
			return result;
		}
	}

	private String genProductArgs(String appKey, String packageValue, String traceId) {
		JSONObject json = new JSONObject();

		try {
			json.put("appid", mAppId);
			if (!TextUtils.isEmpty(traceId)) {
				json.put("traceid", traceId);
			}
			nonceStr = genNonceStr();
			json.put("noncestr", nonceStr);
			json.put("package", packageValue);
			timeStamp = genTimeStamp();
			json.put("timestamp", timeStamp);

			List<NameValuePair> signParams = new LinkedList<NameValuePair>();
			signParams.add(new BasicNameValuePair("appid", mAppId));
			signParams.add(new BasicNameValuePair("appkey", appKey));
			signParams.add(new BasicNameValuePair("noncestr", nonceStr));
			signParams.add(new BasicNameValuePair("package", packageValue));
			signParams.add(new BasicNameValuePair("timestamp", String.valueOf(timeStamp)));
			signParams.add(new BasicNameValuePair("traceid", traceId));
			json.put("app_signature", genSign(signParams));

			json.put("sign_method", "sha1");
		} catch (Exception e) {
			MLog.getIns().i("genProductArgs fail, ex = " + e.getMessage());
			e.printStackTrace();
			return null;
		}

		return json.toString();
	}

	public void sendPay(String[] params) {
		PayReq req = new PayReq();
		req.appId = mAppId;
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
		mApi.sendReq(req);
	}

	public void gotoPay(String[] params) {
		PayReq req = new PayReq();
		req.appId = SharedPreferencesUtil.getAppIdFrom(mContext);
		req.partnerId = params[0];
		req.prepayId = params[1];
		req.packageValue = params[2];
		req.nonceStr = params[3];
		req.timeStamp = params[4];
		req.sign = params[5];
		mApi.sendReq(req);
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

		String sha1 = SecurityUtil.sha1(sb.toString());
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
		return SecurityUtil.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
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
				e.printStackTrace();
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
		try {
			String json = params[0];
			PrePayDataVO dataVO = DataHelper.gson.fromJson(json, PrePayDataVO.class);
			WXPayGetPrepayIdTask task = new WXPayGetPrepayIdTask(mContext, dataVO, listener);
			task.getPrepayId();
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
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
		try {
			PayDataVO dataVO = DataHelper.gson.fromJson(json, PayDataVO.class);
			JSONObject jsonObject = new JSONObject(json);
			dataVO.setPackageValue(jsonObject.getString(JsConst.PACKAGE_VALUE));
			WXPayGetPrepayIdTask task = new WXPayGetPrepayIdTask(mContext, dataVO);
			task.pay();
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	public void setCallbackWindowName(String[] params) {
		try {
			if (params == null || params.length < 1)
				return;
			CallbackWindowNameVO dataVO = DataHelper.gson.fromJson(params[0], CallbackWindowNameVO.class);
			if (dataVO != null) {
				mWindowName = dataVO.getWindowName();
			}
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	@Override
	public void onHandleMessage(Message message) {
		if (message == null) {
			return;
		}
		Bundle bundle = message.getData();
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

	private void callbackPlugin2Js(String methodName, String jsonData) {
		String js = SCRIPT_HEADER + "if(" + methodName + "){" + methodName + "('" + jsonData + "');}";
		MLog.getIns().i("js = " + js);
		onCallback(js);
	}

	private void callbackPlugin2JsAsync(String methodName, String jsonData) {
		String js = SCRIPT_HEADER + "if(" + methodName + "){" + methodName + "('" + jsonData + "');}";
		MLog.getIns().i("js = " + js);
		evaluateScript(mWindowName, 0, js);
		// mBrwView.addUriTaskAsyn(js);
	}

	private void callbackOldInterface(String functionName, int opId, int type, String content) {
		String js = SCRIPT_HEADER + "if(" + functionName + "){" + functionName + "(" + opId + "," + type + ",'" + content + "'" + SCRIPT_TAIL;
		evaluateScript(mWindowName, 0, js);
	}

	OnPayResultListener listener = new OnPayResultListener() {
		@Override
		public void onGetPrepayResult(String json) {
			callbackPlugin2Js(JsConst.CALLBACK_GET_PREPAY_ID, json);
		}
	};

	public interface OnPayResultListener {
		public void onGetPrepayResult(String jsonData);
	}

}
