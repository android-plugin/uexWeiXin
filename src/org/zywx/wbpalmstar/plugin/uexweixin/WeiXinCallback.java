package org.zywx.wbpalmstar.plugin.uexweixin;

import com.tencent.mm.sdk.modelbase.BaseResp;

/**
 * 微信回调
 * 
 * @author waka
 * @version createTime:2016年5月10日 下午4:20:14
 */
public interface WeiXinCallback {

	// 分享回调
	void callbackShareResult(BaseResp resp);

	// 登录回调
	void callbackLoginResult(BaseResp resp);

	// 支付回调
	void callbackPayResult(BaseResp resp);
}
