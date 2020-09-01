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
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.ChooseCardFromWXCardPackage;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
import org.zywx.wbpalmstar.plugin.uexweixin.VO.OpenChooseInvoiceVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.OpenMiniProgramVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.PayDataVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.PrePayDataVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.ShareMusicVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.ShareVideoVO;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.IFeedback;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.JsConst;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.WXPayGetPrepayIdTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@SuppressLint("SdCardPath")
public class EuexWeChat extends EUExBase {

	public static final String TAG = "EuexWeChat";

    private final static String PARAMS_JSON_KEY_TEXT = "text";
    private final static String PARAMS_JSON_KEY_SCENE = "scene";
    private final static String PARAMS_JSON_KEY_THUMBIMG = "thumbImg";
    private final static String PARAMS_JSON_KEY_IMAGE = "image";
    private final static String PARAMS_JSON_KEY_WEDPAGEURL = "wedpageUrl";
    private final static String PARAMS_JSON_KEY_TITLE = "title";
    private final static String PARAMS_JSON_KEY_DESCRIPTION = "description";

    private static final String CB_REGISTER_WXAPP_RESULT = "uexWeiXin.cbRegisterApp";//注册回调
    private static final String CB_IS_WXAPP_INSTALLIED = "uexWeiXin.cbIsWXAppInstalled";//
    private static final String CB_SEND_TEXT_CONTENT = "uexWeiXin.cbSendTextContent";//
    private static final String CB_SEND_IMAGE_CONTENT = "uexWeiXin.cbSendImageContent";
    private static final String CB_IS_PAY_SUPPORTED = "uexWeiXin.cbIsSupportPay";
    private static final String CB_GET_ACCESS_TOKEN = "uexWeiXin.cbGetAccessToken";
    private static final String CB_GET_PREPAY_ID = "uexWeiXin.cbGenerateAdvanceOrder";
    private static final String CB_GET_PAY_RESULT = "uexWeiXin.cbGotoPay";
    private static final String CB_GET_ACCESS_TOKEN_LOCAL = "uexWeiXin.cbGetAccessTokenLocal";
    private static final String CB_SHARE_TEXT_CONTENT = "uexWeiXin.cbShareTextContent";
    private static final String CB_SHARE_IMAGE_CONTENT = "uexWeiXin.cbShareImageContent";
    private static final String CB_SHARE_LINK_CONTENT = "uexWeiXin.cbShareLinkContent";

	// 微信登陆
    private static final String CB_LOGIN_WEIXIN = "uexWeiXin.cbWeiXinLogin";
    private static final String CB_GETWEIXINLOGINACCESSTOKEN = "uexWeiXin.cbGetWeiXinLoginAccessToken";
    private static final String CB_GETWEIXINLOGINREFRESHACCESSTOKEN = "uexWeiXin.cbGetWeiXinLoginRefreshAccessToken";
    private static final String CB_GETWEIXINLOGINCHECKACCESSTOKEN = "uexWeiXin.cbGetWeiXinLoginCheckAccessToken";
    private static final String CB_GETWEIXINLOGINUNIONID = "uexWeiXin.cbGetWeiXinLoginUnionID";

	private static final int THUMB_SIZE = 100;

	private static final int TEXT_CODE = 1;
	private static final int IMAGE_CODE = 2;
	private static final int SHARE_TEXT_CONTENT_CODE = 3;
	private static final int SHARE_IMAGE_CONTENT_CODE = 4;
	private static final int SHARE_LINK_CONTENT_CODE = 5;
    private static final int SHARE_VIDEO_CONTENT_CODE = 6;
    private static final int SHARE_MUSIC_CONTENT_CODE = 7;
	private static IWXAPI api;
	public static WeChatCallBack weChatCallBack;
	private static String appId;
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

    //分享text的回调函数
    public String shareTextFunId;
    //分享图片的回调函数
    private String shareImageFunId;
    private String shareLinkFunId;
    private String shareVideoFunId;
    private String shareMusicFunId;
    private String loginFunId;
    private String getLoginAccessTokenFunId;
    private String getLoginRefreshAccessTokenFunId;
    private String getLoginCheckAccessTokenFunId;
    private String getLoginUnionIDFuncId;
    private String getPrepayIdFuncId;
    private String startPayFuncId;
    private String getAccessTokenFunId;
    private String getAccessTokenLocalFunId;
    private String openChooseInvoiceFuncId;


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
			Toast.makeText(mContext, "getJson错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return null;
	}

	private void init() {
		if(weChatCallBack != null) {
		    return;
		}
		weChatCallBack = new WeChatCallBack() {
			@Override
			public void callBackPayResult(BaseResp msg) {
                callbackOldInterface(CB_GET_PAY_RESULT, 0, EUExCallback.F_C_JSON,
                        getJson(msg.errCode + "", msg.errStr));
                callBackPluginJsAsync(JsConst.CALLBACK_START_PAY,
                        getJson(msg.errCode + "", msg.errStr));
                if(null != startPayFuncId) {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("errCode", msg.errCode);
                        obj.put("errStr", msg.errStr);
                        callbackToJs(Integer.parseInt(startPayFuncId), false, obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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
                    if(null != shareImageFunId) {
                        callbackToJs(Integer.parseInt(shareImageFunId), false, message == 0 ? EUExCallback.F_C_SUCCESS: EUExCallback.F_C_FAILED);
                    }
				} else if (code == SHARE_TEXT_CONTENT_CODE) {
					shareCallBack(CB_SHARE_TEXT_CONTENT, message == 0 ? "0"
							: "1");
                    if (null != shareTextFunId) {
                        callbackToJs(Integer.parseInt(shareTextFunId), false, message == 0 ? EUExCallback.F_C_SUCCESS: EUExCallback.F_C_FAILED);
                    }
                } else if (code == SHARE_IMAGE_CONTENT_CODE) {
					shareCallBack(CB_SHARE_IMAGE_CONTENT, message == 0 ? "0"
							: "1");
                    if(null != shareImageFunId) {
                        callbackToJs(Integer.parseInt(shareImageFunId), false, message == 0 ? EUExCallback.F_C_SUCCESS: EUExCallback.F_C_FAILED);
                    }
				} else if (code == SHARE_LINK_CONTENT_CODE) {
					shareCallBack(CB_SHARE_LINK_CONTENT, message == 0 ? "0"
							: "1");
                    if (null != shareLinkFunId) {
                        callbackToJs(Integer.parseInt(shareLinkFunId), false, message == 0 ? EUExCallback.F_C_SUCCESS: EUExCallback.F_C_FAILED);
                    }
				} else if (code == SHARE_VIDEO_CONTENT_CODE) {
                    if (null != shareVideoFunId) {
                        callbackToJs(Integer.parseInt(shareVideoFunId), false, message == 0 ? EUExCallback.F_C_SUCCESS: EUExCallback.F_C_FAILED);
                    }
                } else if (code == SHARE_MUSIC_CONTENT_CODE) {
                    if (null != shareMusicFunId) {
                        callbackToJs(Integer.parseInt(shareMusicFunId), false, message == 0 ? EUExCallback.F_C_SUCCESS: EUExCallback.F_C_FAILED);
                    }
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
                    resultVO.setErrCode(msg.errCode);
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
                        if (null != loginFunId) {
                            callbackToJs(Integer.parseInt(loginFunId), false, DataHelper.gson.toJsonTree(resultVO));
//                            try {
//                                callbackToJs(Integer.parseInt(loginFunId), false, new JSONObject(resultStr));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                    isLoginNew = false;
                }else{
                    callbackOldInterface(CB_LOGIN_WEIXIN, 0, EUExCallback.F_C_TEXT,
                            msg.errCode + "");
                }
			}

			@Override
			public void callbackMiniProgram(BaseResp msg) {
				WXLaunchMiniProgram.Resp launchMiniProResp = (WXLaunchMiniProgram.Resp) msg;
				String extraData = launchMiniProResp.extMsg; //对应小程序组件 <button open-type="launchApp"> 中的 app-parameter 属性
				// TODO 未处理小程序调用原生App时的传参，后期有需要再定义回调开发。
				Log.i(TAG, "callbackMiniProgram: " + extraData);
			}

			@Override
			public void callbackChooseCard(BaseResp msg) {
				ChooseCardFromWXCardPackage.Resp chooseCardFromWXCardPackage = (ChooseCardFromWXCardPackage.Resp) msg;
				if(null != openChooseInvoiceFuncId) {
					try {
						JSONObject obj = new JSONObject();
						obj.put("errCode", msg.errCode);
						obj.put("errStr", msg.errStr);
						try {
							JSONArray cardArrayJson = new JSONArray(chooseCardFromWXCardPackage.cardItemList);
							obj.put("cardAry", cardArrayJson);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						callbackToJs(Integer.parseInt(openChooseInvoiceFuncId), false, obj);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	/**
	 * 注册应用到微信
	 *
	 * @return true 注册成功， false 注册失败
	 */
	public int registerApp(String[] data) {
		if (data == null || data.length < 1) {
			return EUExCallback.F_C_FAILED;
		}
		appId = data[0];
		if (data.length >= 2){
			// iOS使用的参数， Android暂时无用
			String universalLink = data[1];
		}
		if (appId == null || appId.length() == 0) {
			return EUExCallback.F_C_FAILED;
		}
		api = WXAPIFactory.createWXAPI(mContext, appId, true);
		boolean regOk = api.registerApp(appId);
		if (regOk) {
			Utils.setAppId(mContext, appId);
		}
		jsCallback(CB_REGISTER_WXAPP_RESULT, 0, EUExCallback.F_C_INT, regOk ? 0
				: 1);// 注册回调 0-成功 1-失败
		return regOk ? 0 : 1;
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
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	// 微信登陆获取refreshToken
	public void getWeiXinLoginRefreshAccessToken(String[] parms) {
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
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
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
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);
		} catch (Exception e) {
			Toast.makeText(mContext, "错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	// 获取个人信息
	public void getWeiXinLoginUnionID(String[] parms) {
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
			NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
			token.execute(url);

		} catch (Exception e) {
			Toast.makeText(mContext, "错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();		}

	}

	class NetWorkAsyncTaskToken extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			byte[] buf = Utils.httpGet(url);
			String callBack = new String(buf);
			return callBack;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
            if (isLoginNew){
                switch (countCode){
                    case Constants.token:
                        if(getLoginAccessTokenFunId != null) {
                            try {
                                JSONObject jsonObject = null;
                                if (!TextUtils.isEmpty(result)) {
                                    jsonObject =  new JSONObject(result);
                                }
                                callbackToJs(Integer.parseInt(getLoginAccessTokenFunId), false, jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //callbackToJs(Integer.parseInt(getLoginAccessTokenFunId), false, result);
                        } else {
                            callBackPluginJs(JsConst.CALLBACK_GET_LOGIN_ACCESS_TOKEN, result);
                        }
                        break;
                    case Constants.refresh:
                        if(getLoginRefreshAccessTokenFunId != null) {
                            try {
                                JSONObject jsonObject = null;
                                if (!TextUtils.isEmpty(result)) {
                                    jsonObject =  new JSONObject(result);
                                }
                                callbackToJs(Integer.parseInt(getLoginRefreshAccessTokenFunId), false, jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //callbackToJs(Integer.parseInt(getLoginRefreshAccessTokenFunId), false, result);
                        } else {
                            callBackPluginJs(JsConst.CALLBACK_GET_LOGIN_REFRESH_ACCESS_TOKEN, result);
                        }
                        break;
                    case Constants.check:
                        if(getLoginCheckAccessTokenFunId != null) {
                            try {
                                JSONObject jsonObject = null;
                                if (!TextUtils.isEmpty(result)) {
                                    jsonObject =  new JSONObject(result);
                                }
                                callbackToJs(Integer.parseInt(getLoginCheckAccessTokenFunId), false, jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //callbackToJs(Integer.parseInt(getLoginCheckAccessTokenFunId), false, result);
                        } else {
                            callBackPluginJs(JsConst.CALLBACK_GET_LOGIN_CHECK_ACCESS_TOKEN, result);
                        }
                        break;
                    case Constants.union:
                        callBackPluginJs(JsConst.CALLBACK_GET_LOGIN_UNION_I_D, result);
                        if(getLoginUnionIDFuncId != null) {
                            try {
                                JSONObject jsonObject = null;
                                if (!TextUtils.isEmpty(result)) {
                                    jsonObject =  new JSONObject(result);
                                }
                                callbackToJs(Integer.parseInt(getLoginUnionIDFuncId), false, jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
                isLoginNew = false;
            }else{
                if (countCode == 1) {
                    jsCallback(CB_GETWEIXINLOGINACCESSTOKEN, 0,
                            EUExCallback.F_C_JSON, result);
                } else if (countCode == 2) {
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
		if (params == null || params.length < 2) {
			return false;
		}
		code = TEXT_CODE;
		try {
			int scene = Integer.parseInt(params[0]);
			String text = params[1];
			return sendTextContent(scene, text);
		} catch (Exception e) {
			Toast.makeText(mContext, "错误： " + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		}
		return false;
	}

	public boolean sendImageContent(String[] params) {
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
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断是否安装微信应用，
	 *
	 * @return true 已安装， false 未安装
	 */
	public boolean isWXAppInstalled(String[] args) {
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

	public boolean isSupportPay(String[] params) {
		// getWXAppSupportSAPI是否支持微信版本。
		boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        int result = isPaySupported ? 0 : 1;
        jsCallback(CB_IS_PAY_SUPPORTED, 0, EUExCallback.F_C_INT, result);
        return isPaySupported;
    }

	public void getAccessToken(String[] params) {
        if (params != null && params.length < 2) {
            errorCallback(0, 0, "error params!");
            return;
        }
        if (params.length == 3) {
            getAccessTokenFunId = params[3];
        }
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
        if (null != params && params.length == 1) {
            getAccessTokenLocalFunId = params[0];
        }
        setCallBackData();
	}

	String contentAccessLocal = "";

	public void setCallBackData() {
		if (getLocalData().localRetCode == LocalRetCode.ERR_OK) {
			jsCallback(CB_GET_ACCESS_TOKEN_LOCAL, 0, EUExCallback.F_C_TEXT,
					contentAccessLocal);
            if (null != getAccessTokenLocalFunId) {
                callbackToJs(Integer.parseInt(getAccessTokenFunId), false, contentAccessLocal);
            }
		} else {

			jsCallback(CB_GET_ACCESS_TOKEN_LOCAL, 0, EUExCallback.F_C_TEXT,
					getLocalData().localRetCode.name());
            if (null != getAccessTokenLocalFunId) {
                callbackToJs(Integer.parseInt(getAccessTokenFunId), false, getLocalData().localRetCode.name());
            }
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
                if (null != getAccessTokenFunId) {
                    callbackToJs(Integer.parseInt(getAccessTokenFunId), false, contentAccess);
                }
			} else {
				jsCallback(CB_GET_PREPAY_ID, 0, EUExCallback.F_C_TEXT,
						result.localRetCode.name());
                if (null != getAccessTokenFunId) {
                    callbackToJs(Integer.parseInt(getAccessTokenFunId), false, result.localRetCode.name());
                }
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
            e.printStackTrace();
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

	public interface WeChatCallBack {
		void callBackPayResult(BaseResp msg);

		void callBackShareResult(int message);

		void backLoginResult(BaseResp msg);

		void callbackMiniProgram(BaseResp msg);

		void callbackChooseCard(BaseResp msg);

	}

	public boolean shareTextContent(String[] params) {
		code = SHARE_TEXT_CONTENT_CODE;
        if (params.length == 2) {
            shareTextFunId = params[1];
        }
		try {
			JSONObject jsonObject = new JSONObject(params[0]);
			int scene = jsonObject.getInt(PARAMS_JSON_KEY_SCENE);
			String text = jsonObject.getString(PARAMS_JSON_KEY_TEXT);
			return sendTextContent(scene, text);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		}
		return false;
	}

	public boolean shareImageContent(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        if (params.length == 2) {
            shareImageFunId = params[1];
        }
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
			e.printStackTrace();
		}
		return false;
	}

	/**
	 *
	 * @param scene
	 *            发送场景，0 微信， 1 朋友圈
	 * @param realImgPath
	 *            图片地址
	 * @return true 发送成功， false 发送失败
	 */
	public void shareImage(final int scene, String thumbImgPath,
			String realImgPath) {
		if (realImgPath == null || realImgPath.length() == 0) {
			return;
		}

        String imgPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), realImgPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        final WXImageObject imgObj = createImageObject(imgPath);

		String thumbPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), thumbImgPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		// 如果没有缩略图地址，图片地址赋值给缩略图地址，生成缩略图
		if (thumbPath == null || thumbPath.length() == 0) {
			thumbPath = imgPath;
		}

        new DecodeImageAsyncTask(mContext, new IFeedback<Bitmap>() {
            @Override
            public void onFeedback(Bitmap bitmap) {
            	if (bitmap != null){
					try {
						WXMediaMessage msg = new WXMediaMessage();
						msg.thumbData = Utils.bmpToByteArray(bitmap, true);
						msg.mediaObject = imgObj;

						SendMessageToWX.Req req = new SendMessageToWX.Req();
						req.transaction = buildTransaction("img");
						req.message = msg;
						req.scene = scene;
						api.sendReq(req);
					} catch (Exception e) {
						e.printStackTrace();
						// 图片解析失败，将错误返回
						weChatCallBack.callBackShareResult(1);
					}
				}else{
            		// 图片解析失败，将错误返回
					Log.e(TAG, "DecodeImageAsyncTask==onFeedback==>解析失败，shareImage无法分享");
					weChatCallBack.callBackShareResult(1);
				}
            }
        }).execute(thumbPath);
//
//		Bitmap thumbBmp = createThumbBitmap(thumbPath);
//
//		WXMediaMessage msg = new WXMediaMessage();
//		msg.thumbData = Utils.bmpToByteArray(thumbBmp, true);
//		msg.mediaObject = imgObj;
//
//		SendMessageToWX.Req req = new SendMessageToWX.Req();
//		req.transaction = buildTransaction("img");
//		req.message = msg;
//		req.scene = scene;
//		return api.sendReq(req);
	}

	public boolean shareLinkContent(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        if (params.length == 2) {
            shareLinkFunId = params[1];
        }
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
			e.printStackTrace();
		}
		return false;
	}

    public boolean shareVideoContent(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        if (params.length == 2) {
            shareVideoFunId = params[1];
        }
        code = SHARE_VIDEO_CONTENT_CODE;

        final ShareVideoVO dataVO = DataHelper.gson.fromJson(params[0], ShareVideoVO.class);

        if((TextUtils.isEmpty(dataVO.getVideoUrl()) && TextUtils.isEmpty(dataVO.getVideoLowBandUrl()))
                || TextUtils.isEmpty(dataVO.getTitle())
                || TextUtils.isEmpty(dataVO.getThumbImg())
        ){
            Toast.makeText(mContext, "参数错误!", Toast.LENGTH_SHORT).show();
        }else{
            if(TextUtils.isEmpty(dataVO.getThumbImg())){
                shareVideo(dataVO,null);
            }else{
                String thumbPath = BUtility.makeRealPath(
                        BUtility.makeUrl(mBrwView.getCurrentUrl(), dataVO.getThumbImg()),
                        mBrwView.getCurrentWidget().m_widgetPath,
                        mBrwView.getCurrentWidget().m_wgtType);
                new DecodeImageAsyncTask(mContext, new IFeedback<Bitmap>() {
                    @Override
                    public void onFeedback(Bitmap bitmap) {
                    	if (bitmap != null){
							shareVideo(dataVO,bitmap);
						}else{
							// 图片解析失败，将错误返回
							Log.e(TAG, "DecodeImageAsyncTask==onFeedback==>解析失败，shareVideo无法分享");
						}
                    }
                }).execute(thumbPath);
            }
        }

        return false;
    }

    public boolean shareMusicContent(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        if (params.length == 2) {
            shareMusicFunId = params[1];
        }
        code = SHARE_MUSIC_CONTENT_CODE;

        final ShareMusicVO dataVO = DataHelper.gson.fromJson(params[0], ShareMusicVO.class);

        if(!dataVO.isMusicValid()){
            Toast.makeText(mContext, "参数错误!", Toast.LENGTH_SHORT).show();
        }else{
            if(TextUtils.isEmpty(dataVO.getThumbImg())){
                shareMusic(dataVO,null);
            }else{
                String thumbPath = BUtility.makeRealPath(
                        BUtility.makeUrl(mBrwView.getCurrentUrl(), dataVO.getThumbImg()),
                        mBrwView.getCurrentWidget().m_widgetPath,
                        mBrwView.getCurrentWidget().m_wgtType);
                new DecodeImageAsyncTask(mContext, new IFeedback<Bitmap>() {
                    @Override
                    public void onFeedback(Bitmap bitmap) {
                    	if (bitmap != null){
							shareMusic(dataVO,bitmap);
						}else{
							// 图片解析失败，将错误返回
							Log.e(TAG, "DecodeImageAsyncTask==onFeedback==>解析失败，shareMusic无法分享");
						}
                    }
                }).execute(thumbPath);
            }
        }

        return false;
    }

    private void shareMusic(ShareMusicVO vo, Bitmap bitmap){
        final String url = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), vo.getMusicUrl()),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        final String lowBandUrl = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), vo.getMusicLowBandUrl()),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        WXMusicObject music = new WXMusicObject();
        if(!TextUtils.isEmpty(url)){
            music.musicUrl = url;
        }
        if(!TextUtils.isEmpty(lowBandUrl)){
            music.musicLowBandUrl = lowBandUrl;
        }
        WXMediaMessage msg = new WXMediaMessage(music);
        if(!TextUtils.isEmpty(vo.getTitle())){
            msg.title = vo.getTitle();
        }
        if(!TextUtils.isEmpty(vo.getDescription())){
            msg.description = vo.getDescription();
        }
        if(bitmap != null){
            msg.thumbData = Utils.bmpToByteArray(bitmap, true);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("music");
        req.message = msg;
        req.scene = vo.getScene();
        api.sendReq(req);

    }

    private void shareVideo(ShareVideoVO vo, Bitmap bitmap) {

        final String videoUrl = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), vo.getVideoUrl()),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        final String videoLowBandUrl = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), vo.getVideoLowBandUrl()),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        WXVideoObject video = new WXVideoObject();
        if(!TextUtils.isEmpty(videoUrl)){
            video.videoUrl = videoUrl;
        }
        if(!TextUtils.isEmpty(videoLowBandUrl)){
            video.videoLowBandUrl = videoLowBandUrl;
        }
        WXMediaMessage msg = new WXMediaMessage(video);
        if(!TextUtils.isEmpty(vo.getTitle())){
            msg.title = vo.getTitle();
        }
        if(!TextUtils.isEmpty(vo.getDescription())){
            msg.description = vo.getDescription();
        }
        if(bitmap != null){
            msg.thumbData = Utils.bmpToByteArray(bitmap, true);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("video");
        req.message = msg;
        req.scene = vo.getScene();
        api.sendReq(req);

    }

    /**
	 *
	 * @param scene
	 *            发送场景，0 微信， 1 朋友圈
	 * @param title
	 *            网页标题
	 * @param description
	 *            网页描述
	 * @param wedpageUrl
	 *            网页url
	 * @return true 发送成功， false 发送失败
	 */
	public void shareLink(final int scene, final String thumbImgPath,
			final String title, final String description, final String wedpageUrl) {
		if (wedpageUrl == null || wedpageUrl.length() == 0 || api == null) {
			weChatCallBack.callBackShareResult(1);
			return;
		}

        String thumbPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), thumbImgPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);

        new DecodeImageAsyncTask(mContext, new IFeedback<Bitmap>() {
            @Override
            public void onFeedback(Bitmap bitmap) {
            	if (bitmap != null){
					WXMediaMessage msg = new WXMediaMessage();
					msg.thumbData = Utils.bmpToByteArray(bitmap, true);
					msg.title = title;
					msg.description = description;
					WXWebpageObject webObj = new WXWebpageObject();
					webObj.webpageUrl = wedpageUrl;
					msg.mediaObject = webObj;

					SendMessageToWX.Req req = new SendMessageToWX.Req();
					req.transaction = buildTransaction("link");
					req.message = msg;
					req.scene = scene;
					api.sendReq(req);
				}else{
					// 图片解析失败，将错误返回
					Log.e(TAG, "DecodeImageAsyncTask==onFeedback==>解析失败，shareLink无法分享");
				}
            }
        }).execute(thumbPath);
	}

	private WXImageObject createImageObject(String imgPath) {
		WXImageObject imgObj = new WXImageObject();
		if (imgPath.startsWith("http://")||imgPath.startsWith("https://")) {
			imgObj.imagePath = imgPath;
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
					e.printStackTrace();
				}
			}
		}
		return imgObj;
	}

	private Bitmap createThumbBitmap(String thumbPath) {
		// 缩略图地址临时变量
		Bitmap bmp = null;
		if (thumbPath.startsWith("http://")||thumbPath.startsWith("https://")) {
			try {
				bmp = BitmapFactory.decodeStream(new URL(thumbPath)
						.openStream());
			} catch (Exception e) {
				Toast.makeText(mContext, "错误：" + e.getMessage(),
						Toast.LENGTH_SHORT).show();
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
					Toast.makeText(mContext, "错误：" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
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
        try {
			String json = params[0];
            if (params.length == 2) {
                getPrepayIdFuncId = params[1];
            }
            PrePayDataVO dataVO = DataHelper.gson.fromJson(json, PrePayDataVO.class);
			WXPayGetPrepayIdTask task = new WXPayGetPrepayIdTask(mContext, dataVO, listener);
			task.getPrepayId();
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
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
        if (params.length == 2) {
            startPayFuncId = params[1];
        }
        try {
        	PayDataVO dataVO = DataHelper.gson.fromJson(json, PayDataVO.class);
            JSONObject jsonObject = new JSONObject(json);
            dataVO.setPackageValue(jsonObject.getString(JsConst.PACKAGE_VALUE));
            WXPayGetPrepayIdTask task = new WXPayGetPrepayIdTask(mContext, dataVO);
        	task.pay();
        } catch (Exception e) {
        	Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
    }

	/**
	 * 打开小程序
	 *
	 * @param params
	 */
    public void openMiniProgram(String[] params){
		if (params == null || params.length < 1) {
			errorCallback(0, 0, "error params!");
			return;
		}
		String json = params[0];
		try {
			OpenMiniProgramVO openMiniProgramVO = DataHelper.gson.fromJson(json, OpenMiniProgramVO.class);

			WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
			req.userName = openMiniProgramVO.getUserName(); // 填小程序原始id
			req.path = openMiniProgramVO.getPath();                  ////拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
			// 可选打开 开发版，体验版和正式版
			if (openMiniProgramVO.checkMiniProgramType(OpenMiniProgramVO.MINI_PROGRAME_TYPE_RELEASE)){
				req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;
			} else if (openMiniProgramVO.checkMiniProgramType(OpenMiniProgramVO.MINI_PROGRAME_TYPE_PREVIEW)){
				req.miniprogramType = WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_PREVIEW;
			} else if (openMiniProgramVO.checkMiniProgramType(OpenMiniProgramVO.MINI_PROGRAME_TYPE_TEST)){
				req.miniprogramType = WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_TEST;
			}
			api.sendReq(req);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openChooseInvoice(String[] params){
		if (params == null || params.length < 1) {
			errorCallback(0, 0, "error params!");
			return;
		}
		if (params.length >= 2){
			openChooseInvoiceFuncId = params[1];
		}
		String json = params[0];
		try {
			List<String> paramList = new ArrayList<>();
			ChooseCardFromWXCardPackage.Req req = new ChooseCardFromWXCardPackage.Req();
			String timestampStr = String.valueOf(System.currentTimeMillis());
			nonceStr = genNonceStr();
			req.appId = appId;
			paramList.add(req.appId);
			req.cardType = "INVOICE";//发票类型填写 INVOICE
			paramList.add(req.cardType);
			req.nonceStr = nonceStr;
			paramList.add(req.nonceStr);
			req.timeStamp = timestampStr;
			paramList.add(req.timeStamp);
			req.signType = "SHA1"; // 微信文档写的是目前仅支持SHA1
			String cardSign = Utils.sha1(paramList);
			req.cardSign = cardSign;
			api.sendReq(req);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void login(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        if (params.length == 2) {
            loginFunId = params[1];
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
		try {
			LoginVO dataVO = DataHelper.gson.fromJson(json, LoginVO.class);
			dataVO.setAppid(appId);
			SendAuth.Req req = new SendAuth.Req();
			req.scope = dataVO.getScope();// "snsapi_userinfo"
			req.state = dataVO.getState();// "wechat_sdk_demo_test"
			Log.d(TAG, req.scope + "=======>" + req.state + "======appId====>" + appId);
			api.sendReq(req);
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
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
        String json = params[0];
        if (params.length == 2) {
            getLoginAccessTokenFunId = params[1];
        }
        if (TextUtils.isEmpty(appId)){
            errorCallback(0, 0, "please register first!");
            return;
        }
        try {
        	LoginAccessTokenVO dataVO = DataHelper.gson.fromJson(json, LoginAccessTokenVO.class);
        	dataVO.setAppid(appId);
        	isLoginNew = true;
            countCode = Constants.token;
            String url = String
                    .format(JsConst.URL_LOGIN_GET_ACCESS_TOKEN,
                            dataVO.getAppid(), dataVO.getSecret(),
                            dataVO.getCode(), dataVO.getGrant_type());
            NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
            token.execute(url);
        } catch (Exception e) {
        	Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
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
        String json = params[0];
        if (TextUtils.isEmpty(appId)){
            errorCallback(0, 0, "please register first!");
            return;
        }
        if (params.length == 2) {
            getLoginRefreshAccessTokenFunId = params[1];
        }
        try {
        	LoginRefreshTokenVO dataVO = DataHelper.gson.fromJson(json, LoginRefreshTokenVO.class);
        	dataVO.setAppid(appId);
        	isLoginNew = true;
            countCode = Constants.refresh;
            String url = String
                    .format(JsConst.URL_LOGIN_REFRESH_ACCESS_TOKEN, dataVO.getAppid(),
                            dataVO.getGrant_type(), dataVO.getRefresh_token());
            NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
            token.execute(url);
        } catch (Exception e) {
        	Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
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
        String json = params[0];
        if(params.length == 2) {
            getLoginCheckAccessTokenFunId = params[1];
        }
        try {
        	LoginCheckTokenVO dataVO = DataHelper.gson.fromJson(json, LoginCheckTokenVO.class);
        	isLoginNew = true;
            countCode = Constants.check;
            String url = String
                    .format(JsConst.URL_LOGIN_CHECK_ACCESS_TOKEN,
                            dataVO.getAccess_token(), dataVO.getOpenid());
            NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
            token.execute(url);
        } catch (Exception e) {
        	Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
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
        String json = params[0];
        if (params.length == 2) {
            getLoginUnionIDFuncId = params[1];
        }
        try {
        	LoginCheckTokenVO dataVO = DataHelper.gson.fromJson(json, LoginCheckTokenVO.class);
        	isLoginNew = true;
            countCode = Constants.union;
            String url = String
                    .format(JsConst.URL_LOGIN_UNIONID,
                            dataVO.getAccess_token(), dataVO.getOpenid());
            NetWorkAsyncTaskToken token = new NetWorkAsyncTaskToken();
            token.execute(url);
        } catch (Exception e) {
        	Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
        }
    }

    public void setCallbackWindowName(String[] params){
        try {
			if (params == null || params.length < 1)
				return;
			CallbackWindowNameVO dataVO = DataHelper.gson.fromJson(params[0],
			        CallbackWindowNameVO.class);
			if (dataVO != null){
			    mWindowName = dataVO.getWindowName();
			}
		} catch (Exception e) {
			Toast.makeText(mContext, "参数错误：" + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
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
        public void onGetPrepayResult(JSONObject json) {
            if (null != getPrepayIdFuncId) {
                callbackToJs(Integer.parseInt(getPrepayIdFuncId), false, json);
            } else {
                callBackPluginJs(JsConst.CALLBACK_GET_PREPAY_ID, json.toString());
            }
        }
    };

    public interface OnPayResultListener{
        public void onGetPrepayResult(JSONObject jsonData);
    }


}
