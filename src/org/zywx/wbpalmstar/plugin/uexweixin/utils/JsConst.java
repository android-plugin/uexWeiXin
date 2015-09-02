package org.zywx.wbpalmstar.plugin.uexweixin.utils;



public class JsConst {
    public static final String APPID = "appid";
    public static final String MCH_ID = "mch_id";
    public static final String DEVICE_INFO = "device_info";
    public static final String BODY = "body";
    public static final String DETAIL = "detail";
    public static final String ATTACH = "attach";
    public static final String FEE_TYPE = "fee_type";
    public static final String TOTAL_FEE = "total_fee";
    public static final String SPBILL_CREATE_IP = "spbill_create_ip";
    public static final String TIME_START = "time_start";
    public static final String TIME_EXPIRE = "time_expire";
    public static final String GOODS_TAG = "goods_tag";
    public static final String NOTIFY_URL = "notify_url";

    public static final String OUT_TRADE_NO = "out_trade_no";
    public static final String NONCE_STR = "nonce_str";
    public static final String TRADE_TYPE = "trade_type";

    public static final String PACKAGE_VALUE = "package";
    public static final String PARTNERID = "partnerid";
    public static final String PREPAYID = "prepayid";
    public static final String TIMESTAMP = "timestamp";
    public static final String NONCESTR = "noncestr";

    public static final String PREPARE_ID_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    public static final String CALLBACK_GET_PREPAY_ID = "uexWeiXin.cbGetPrepayId";
    public static final String CALLBACK_START_PAY = "uexWeiXin.cbStartPay";

    public static final String PRODUCT_ID = "product_id";
    public static final String OPENID = "openid";

    public static final String CALLBACK_LOGIN = "uexWeiXin.cbLogin";
    public static final String CALLBACK_GET_LOGIN_ACCESS_TOKEN = "uexWeiXin.cbGetLoginAccessToken";
    public static final String CALLBACK_GET_LOGIN_REFRESH_ACCESS_TOKEN = "uexWeiXin.cbGetLoginRefreshAccessToken";
    public static final String CALLBACK_GET_LOGIN_CHECK_ACCESS_TOKEN = "uexWeiXin.cbGetLoginCheckAccessToken";
    public static final String CALLBACK_GET_LOGIN_UNION_I_D = "uexWeiXin.cbGetLoginUnionID";

    public static final String URL_LOGIN_GET_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=%s";
    public static final String URL_LOGIN_REFRESH_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=%s&grant_type=%s&refresh_token=%s";
    public static final String URL_LOGIN_CHECK_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/auth?access_token=%s&openid=%s";
    public static final String URL_LOGIN_UNIONID = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
}
