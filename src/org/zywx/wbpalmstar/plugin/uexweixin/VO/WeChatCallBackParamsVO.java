package org.zywx.wbpalmstar.plugin.uexweixin.VO;

import org.zywx.wbpalmstar.plugin.uexweixin.WeChatCallBack;

/**
 * File Description: 用于记录每次插件请求所携带的参数
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 2020/11/13.
 */
public class WeChatCallBackParamsVO {

    private String[] params;
    private WeChatCallBack callback;

    public WeChatCallBackParamsVO(String[] params, WeChatCallBack callback) {
        this.params = params;
        this.callback = callback;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public WeChatCallBack getCallback() {
        return callback;
    }

    public void setCallback(WeChatCallBack callback) {
        this.callback = callback;
    }
}
