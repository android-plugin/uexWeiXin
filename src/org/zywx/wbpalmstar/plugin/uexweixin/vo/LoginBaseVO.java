package org.zywx.wbpalmstar.plugin.uexweixin.vo;

import java.io.Serializable;

public class LoginBaseVO implements Serializable{
    private static final long serialVersionUID = 653020164121259992L;
    private String appid;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }
}
