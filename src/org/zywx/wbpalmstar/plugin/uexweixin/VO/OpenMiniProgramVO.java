package org.zywx.wbpalmstar.plugin.uexweixin.VO;

/**
 * File Description: 打开微信小程序的传参
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 2020/8/5.
 */
public class OpenMiniProgramVO {

    public static final String MINI_PROGRAME_TYPE_TEST = "1";
    public static final String MINI_PROGRAME_TYPE_PREVIEW = "2";
    public static final String MINI_PROGRAME_TYPE_RELEASE = "0";

    /**
     * userName : gh_d43f693ca31f@app
     * path :
     * miniProgramType : 0
     */

    private String userName;
    private String path;
    private String miniProgramType;

    /**
     * 判断打开的小程序类型
     *
     * @param miniProgramType
     * @return
     */
    public boolean checkMiniProgramType(String miniProgramType){
        return this.miniProgramType != null && this.miniProgramType.equals(miniProgramType);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMiniProgramType() {
        return miniProgramType;
    }

    public void setMiniProgramType(String miniProgramType) {
        this.miniProgramType = miniProgramType;
    }
}
