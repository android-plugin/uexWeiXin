package org.zywx.wbpalmstar.plugin.uexweixin;

import com.tencent.mm.opensdk.modelbase.BaseResp;

/**
 * File Description: 回调操作
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 2020/11/13.
 */
public interface WeChatCallBack {

    void callBackPayResult(BaseResp msg);

    void callBackShareResult(int message);

    void backLoginResult(BaseResp msg);

    void callbackMiniProgram(BaseResp msg);

    void callbackChooseCard(String[] inputParams, BaseResp msg);

}
