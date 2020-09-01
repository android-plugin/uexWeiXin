package org.zywx.wbpalmstar.plugin.uexweixin.VO;

import java.util.List;

/**
 * File Description: 打开发票页面的传参
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 2020/8/5.
 */
public class OpenChooseInvoiceVO {

    private List<CardAryBean> cardAry;

    public List<CardAryBean> getCardAry() {
        return cardAry;
    }

    public void setCardAry(List<CardAryBean> cardAry) {
        this.cardAry = cardAry;
    }

    public static class CardAryBean {
        /**
         * cardId : wx69b6673576ec5a65
         * encryptCode : pDe7ajrY4G5z_SIDSauDkLSuF9NI
         * appID : O/mPnGTpBu22a1szmK2ogzhFPBh9eYzv2p70L8yzyymSPw4zpNYIVN0JMyArQ9smSepbKd2CQdkv3NvGuaGLaJYjrlrdSVrGhDOnedMr01zKjzDJkO4MOSALnNeDuIpb
         */

        private String cardId;
        private String encryptCode;
        private String appID;

        public String getCardId() {
            return cardId;
        }

        public void setCardId(String cardId) {
            this.cardId = cardId;
        }

        public String getEncryptCode() {
            return encryptCode;
        }

        public void setEncryptCode(String encryptCode) {
            this.encryptCode = encryptCode;
        }

        public String getAppID() {
            return appID;
        }

        public void setAppID(String appID) {
            this.appID = appID;
        }
    }
}
