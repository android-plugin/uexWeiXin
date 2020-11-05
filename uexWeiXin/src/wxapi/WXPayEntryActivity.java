package net.sourceforge.simcpux.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.plugin.uexweixin.EuexWeChat;
import org.zywx.wbpalmstar.plugin.uexweixin.Utils;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = "WXPayEntryActivity";
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
		String transaction = req.transaction;
		BDebug.i(EuexWeChat.TAG, TAG + "-onReq req.getType():", req.getType() + "----------");
		BDebug.i(EuexWeChat.TAG, TAG + "-onReq openId = " + req.openId);
		BDebug.i(EuexWeChat.TAG,TAG + "-onReq transaction = " + transaction);
	}

	@Override
	public void onResp(BaseResp resp) {
		String transaction = resp.transaction;
		BDebug.i(EuexWeChat.TAG, TAG + "-onResp resp.errCode:", resp.errCode + "----------");
		BDebug.i(EuexWeChat.TAG, TAG + "-onResp resp.getType():", resp.getType() + "----------");
		BDebug.i(EuexWeChat.TAG, TAG + "-onResp openId = " + resp.openId);
		BDebug.i(EuexWeChat.TAG,TAG + "-onResp transaction = " + transaction);
		EuexWeChat.WeChatCallBack callback = EuexWeChat.getAndRemoveWeChatCallbackWithUUIDTransaction(transaction);
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (callback != null) {
				Log.d("resp", ConstantsAPI.COMMAND_PAY_BY_WX + "");
				callback.callBackPayResult(resp);
			}
		} else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {

			if (callback != null) {
				Log.d("resp", resp.errCode + "code");
				callback.callBackShareResult(resp.errCode);
			}
		}
		finish();
	}
}