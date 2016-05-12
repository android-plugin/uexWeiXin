package org.zywx.wbpalmstar.plugin.uexweixin.vo;

import java.io.Serializable;

public class LoginResultVO implements Serializable{
    private static final long serialVersionUID = 6461157892438453583L;
    private String errCode;
    private String code;
    private String state;
    private String language;
    private String country;

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
