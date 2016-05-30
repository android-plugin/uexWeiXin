package org.zywx.wbpalmstar.plugin.uexweixin.vo;

import java.io.Serializable;

public class CallbackWindowNameVO implements Serializable{
    private static final long serialVersionUID = -5879823128452414511L;
    private String windowName;

    public String getWindowName() {
        return windowName;
    }

    public void setWindowName(String windowName) {
        this.windowName = windowName;
    }
}
