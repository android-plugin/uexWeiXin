package net.sourceforge.simcpux.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.zywx.wbpalmstar.plugin.uexweixin.EUExWeiXin;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.MLog;
import org.zywx.wbpalmstar.plugin.uexweixin.utils.SharedPreferencesUtil;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(getApplicationContext(), SharedPreferencesUtil.getAppIdFrom(getApplicationContext()));
		api.handleIntent(getIntent(), this);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
		System.out.println(api.handleIntent(intent, this) + "boolean");
	}

	/**
	 * 微信发送请求到第三方应用时，会回调到该方法
	 */
	@Override
	public void onReq(BaseReq req) {

		MLog.getIns().d("start");

	}

	/**
	 * 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	 */
	@Override
	public void onResp(BaseResp resp) {

		MLog.getIns().d("start");

		int type = resp.getType();
		MLog.getIns().i("type = " + type);

		if (type == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (EUExWeiXin.weiXinCallback != null) {
				EUExWeiXin.weiXinCallback.callbackPayResult(resp);
				MLog.getIns().i("ConstantsAPI.COMMAND_PAY_BY_WX " + ConstantsAPI.COMMAND_PAY_BY_WX);
			}
		}

		else if (type == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
			if (EUExWeiXin.weiXinCallback != null) {
				EUExWeiXin.weiXinCallback.callbackShareResult(resp);
				MLog.getIns().i("ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX " + ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX);
			}
		}

		finish();
	}
}