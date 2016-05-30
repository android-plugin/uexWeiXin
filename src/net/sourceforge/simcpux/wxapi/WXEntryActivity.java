package net.sourceforge.simcpux.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.zywx.wbpalmstar.plugin.uexweixin.EUExWeiXin;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.ErrorInfoUtil;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.MLog;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.SharedPreferencesUtil;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	private IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(this, SharedPreferencesUtil.getAppIdFrom(this));
		api.registerApp(SharedPreferencesUtil.getAppIdFrom(this));
		api.handleIntent(getIntent(), this);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(getIntent(), this);
	}

	/**
	 * 微信发送请求到第三方应用时，会回调到该方法
	 */
	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			break;
		case ConstantsAPI.COMMAND_SENDAUTH:
			break;
		case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX:
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			break;
		case ConstantsAPI.COMMAND_UNKNOWN:
			break;
		default:
			break;
		}

	}

	/**
	 * 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	 */
	@Override
	public void onResp(final BaseResp resp) {

		MLog.getIns().d("start");

		int type = resp.getType();
		int errCode = resp.errCode;
		String errStr = resp.errStr;// 微信回调自带的错误信息，不过经常是null
		String myErrorStr = ErrorInfoUtil.respErrorCode2String(errCode);// 通过错误码自己转换的错误信息
		MLog.getIns().i("type = " + type);
		MLog.getIns().i("errCode = " + errCode);
		MLog.getIns().i("errStr = " + errStr);
		MLog.getIns().i("myErrorStr = " + myErrorStr);

		if (resp instanceof SendMessageToWX.Resp) {
			if (EUExWeiXin.weiXinCallback != null) {
				EUExWeiXin.weiXinCallback.callbackShareResult(resp);
				MLog.getIns().i("callbackShareResult");
			}
		}

		if (resp instanceof SendAuth.Resp) {
			if (EUExWeiXin.weiXinCallback != null) {
				EUExWeiXin.weiXinCallback.callbackLoginResult(resp);
				MLog.getIns().i("callbackShareResult");
			}
		}

		finish();
	}
}
