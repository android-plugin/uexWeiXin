package net.sourceforge.simcpux.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.ChooseCardFromWXCardPackage;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.plugin.uexweixin.EuexWeChat;
import org.zywx.wbpalmstar.plugin.uexweixin.Utils;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = "WXEntryActivity";
	private IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			api = WXAPIFactory.createWXAPI(this, Utils.getAppId(this));
			api.registerApp(Utils.getAppId(this));
			api.handleIntent(getIntent(), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		try {
			setIntent(intent);
			api.handleIntent(getIntent(), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReq(BaseReq req) {
		String transaction = req.transaction;
		BDebug.i(EuexWeChat.TAG, TAG + "-onReq req.getType():", req.getType() + "----------");
		BDebug.i(EuexWeChat.TAG, TAG + "-onReq openId = " + req.openId);
		BDebug.i(EuexWeChat.TAG,TAG + "-onReq transaction = " + transaction);
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

	@Override
	public void onResp(final BaseResp resp) {
		int statusCode = 0;
		String transaction = resp.transaction;
		BDebug.i(EuexWeChat.TAG, TAG + "-onResp resp.errCode:", resp.errCode + "----------");
		BDebug.i(EuexWeChat.TAG, TAG + "-onResp resp.getType():", resp.getType() + "----------");
		BDebug.i(EuexWeChat.TAG, TAG + "-onResp openId = " + resp.openId);
		BDebug.i(EuexWeChat.TAG,TAG + "-onResp transaction = " + transaction);
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			statusCode = -4;
			break;
		case BaseResp.ErrCode.ERR_COMM:
			statusCode = -1;
			break;
		case BaseResp.ErrCode.ERR_OK:
			statusCode = 0;
			break;
		case BaseResp.ErrCode.ERR_SENT_FAILED:
			statusCode = -3;
			break;

		case BaseResp.ErrCode.ERR_UNSUPPORT:
			statusCode = -5;
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			statusCode = -2;
		default:
			break;
		}
		EuexWeChat.WeChatCallBack callback = EuexWeChat.getAndRemoveWeChatCallbackWithUUIDTransaction(transaction);
        if (resp instanceof SendMessageToWX.Resp){
            if (callback != null) {
				callback.callBackShareResult(statusCode);
            }
        }

        if (resp instanceof SendAuth.Resp){
            if (callback != null) {
				callback.backLoginResult(resp);
            }
        }

		if (resp.getType() == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM
				&& resp instanceof WXLaunchMiniProgram.Resp) {
			if (callback != null){
				callback.callbackMiniProgram(resp);
			}
		}

		if (resp.getType() == ConstantsAPI.COMMAND_CHOOSE_CARD_FROM_EX_CARD_PACKAGE && resp instanceof ChooseCardFromWXCardPackage.Resp) {
			if (callback != null){
				callback.callbackChooseCard(resp);
			}
		}

		finish();
	}
}
