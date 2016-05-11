package org.zywx.wbpalmstar.plugin.uexweixin.vo;

import java.io.Serializable;

public class LoginCheckTokenVO extends LoginBaseVO implements Serializable{
    private static final long serialVersionUID = -6036767751983396028L;
    private String access_token;
    private String openid;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
