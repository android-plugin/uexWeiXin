package net.sourceforge.simcpux.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.zywx.wbpalmstar.plugin.uexweixin.EuexWeChat;
import org.zywx.wbpalmstar.plugin.uexweixin.Utils;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(getApplicationContext(),
				Utils.getAppId(getApplicationContext()));
		api.handleIntent(getIntent(), this);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
		System.out.println(api.handleIntent(intent, this) + "boolean");
	}

	@Override
	public void onReq(BaseReq req) {

	}

	@Override
	public void onResp(BaseResp resp) {

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (EuexWeChat.weChatCallBack != null) {
				Log.d("resp", ConstantsAPI.COMMAND_PAY_BY_WX + "");
				EuexWeChat.weChatCallBack.callBackPayResult(resp);
			}
		} else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {

			if (EuexWeChat.weChatCallBack != null) {
				Log.d("resp", resp.errCode + "code");
				EuexWeChat.weChatCallBack.callBackShareResult(resp.errCode);
			}
		}
		finish();
	}
}