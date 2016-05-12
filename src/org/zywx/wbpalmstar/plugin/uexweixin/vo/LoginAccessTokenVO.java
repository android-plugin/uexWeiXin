package org.zywx.wbpalmstar.plugin.uexweixin.vo;

import java.io.Serializable;

public class LoginAccessTokenVO extends LoginBaseVO implements Serializable{
    private static final long serialVersionUID = -2046946960015082540L;
    private String secret;
    private String code;
    private String grant_type;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }
}
