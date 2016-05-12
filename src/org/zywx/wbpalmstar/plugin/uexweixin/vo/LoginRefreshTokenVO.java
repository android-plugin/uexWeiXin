package org.zywx.wbpalmstar.plugin.uexweixin.vo;

import java.io.Serializable;

public class LoginRefreshTokenVO extends LoginBaseVO implements Serializable{
    private static final long serialVersionUID = 7718923783831572402L;
    private String grant_type;
    private String refresh_token;

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}
