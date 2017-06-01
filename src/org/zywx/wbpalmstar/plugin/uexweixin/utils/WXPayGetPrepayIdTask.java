package org.zywx.wbpalmstar.plugin.uexweixin.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.zywx.wbpalmstar.plugin.uexweixin.EuexWeChat.OnPayResultListener;
import org.zywx.wbpalmstar.plugin.uexweixin.Utils;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.PayDataVO;
import org.zywx.wbpalmstar.plugin.uexweixin.VO.PrePayDataVO;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WXPayGetPrepayIdTask {
    private Context mContext;
    private PrePayDataVO prepayData;
    private static final String TAG = "WXPayGetPrepayIdTask";

    private PayReq req;
    private IWXAPI msgApi;
    private OnPayResultListener mListener;

    private PayDataVO payData;

    public WXPayGetPrepayIdTask(Context mContext, PrePayDataVO prepayData, OnPayResultListener listener) {
        this.mContext = mContext;
        this.prepayData = prepayData;
        this.mListener = listener;
        init(prepayData.getAppid());
    }

    public WXPayGetPrepayIdTask(Context mContext, PayDataVO payData) {
        this.mContext = mContext;
        this.payData = payData;
        init(payData.getAppid());
    }

    private void init(String appId) {
        if (msgApi == null){
            msgApi = WXAPIFactory.createWXAPI(mContext, null);
        }
        msgApi.registerApp(appId);
    }

    public void getPrepayId() {
        //生成预支付订单
        GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
        getPrepayId.execute();
    }

    public void pay(){
        if (req == null){
            req = new PayReq();
        }
        req.appId = payData.getAppid();
        req.partnerId = payData.getPartnerid();
        req.prepayId = payData.getPrepayid();
        req.packageValue = payData.getPackageValue();
        //req.nonceStr = genNonceStr();
        req.nonceStr = payData.getNoncestr();
        req.timeStamp = payData.getTimestamp();


//        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
//        signParams.add(new BasicNameValuePair(JsConst.APPID, req.appId));
//        signParams.add(new BasicNameValuePair(JsConst.NONCESTR, req.nonceStr));
//        signParams.add(new BasicNameValuePair(JsConst.PACKAGE_VALUE, req.packageValue));
//        signParams.add(new BasicNameValuePair(JsConst.PARTNERID, req.partnerId));
//        signParams.add(new BasicNameValuePair(JsConst.PREPAYID, req.prepayId));
//        signParams.add(new BasicNameValuePair(JsConst.TIMESTAMP, req.timeStamp));

        //req.sign = genAppSign(signParams);
        req.sign = payData.getSign();

        msgApi.sendReq(req);
    }

    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            JSONObject jsonObject = new JSONObject();
            for (String key : result.keySet()) {
                Log.i("GetPrepayIdTask", key + "," + result.get(key));
                try {
                    jsonObject.put(key, result.get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (mListener != null){
                mListener.onGetPrepayResult(jsonObject);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {

            String url = String.format(JsConst.PREPARE_ID_URL);
            String entity = genProductArgs();

            Log.e("orion", entity);

            byte[] buf = Utils.httpPost(url, entity);

            String content = new String(buf);
            Log.e("orion", content);
            Map<String, String> xml = decodeXml(content);

            return xml;
        }
    }

    public Map<String,String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName=parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if("xml".equals(nodeName)==false){
                            //实例化student对象
                            xml.put(nodeName,parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            Log.e("orion",e.toString());
            e.printStackTrace();
        }
        return null;

    }

    private String genProductArgs() {
        StringBuffer xml = new StringBuffer();

        try {
            xml.append("</xml>");
            List<NameValuePair> packageParams = new LinkedList<NameValuePair>();

            packageParams.add(new BasicNameValuePair(JsConst.APPID, prepayData.getAppid()));
            packageParams.add(new BasicNameValuePair(JsConst.BODY, prepayData.getBody()));
            packageParams.add(new BasicNameValuePair(JsConst.MCH_ID, prepayData.getMch_id()));
            packageParams.add(new BasicNameValuePair(JsConst.NONCE_STR, prepayData.getNonce_str()));
            packageParams.add(new BasicNameValuePair(JsConst.NOTIFY_URL, prepayData.getNotify_url()));
            packageParams.add(new BasicNameValuePair(JsConst.OUT_TRADE_NO, prepayData.getOut_trade_no()));
            packageParams.add(new BasicNameValuePair(JsConst.SPBILL_CREATE_IP, prepayData.getSpbill_create_ip()));
            packageParams.add(new BasicNameValuePair(JsConst.TOTAL_FEE, prepayData.getTotal_fee()));
            packageParams.add(new BasicNameValuePair(JsConst.TRADE_TYPE, prepayData.getTrade_type()));

            if (!TextUtils.isEmpty(prepayData.getAttach())){
                packageParams.add(new BasicNameValuePair(JsConst.ATTACH, prepayData.getAttach()));
            }
            if (!TextUtils.isEmpty(prepayData.getDetail())){
                packageParams.add(new BasicNameValuePair(JsConst.DETAIL, prepayData.getDetail()));
            }
            if (!TextUtils.isEmpty(prepayData.getDevice_info())){
                packageParams.add(new BasicNameValuePair(JsConst.DEVICE_INFO, prepayData.getDevice_info()));
            }
            if (!TextUtils.isEmpty(prepayData.getFee_type())){
                packageParams.add(new BasicNameValuePair(JsConst.FEE_TYPE, prepayData.getFee_type()));
            }
            if (!TextUtils.isEmpty(prepayData.getTime_expire())){
                packageParams.add(new BasicNameValuePair(JsConst.TIME_EXPIRE, prepayData.getTime_expire()));
            }
            if (!TextUtils.isEmpty(prepayData.getGoods_tag())){
                packageParams.add(new BasicNameValuePair(JsConst.GOODS_TAG, prepayData.getGoods_tag()));
            }
            if (!TextUtils.isEmpty(prepayData.getTime_start())){
                packageParams.add(new BasicNameValuePair(JsConst.TIME_START, prepayData.getTime_start()));
            }
            if (!TextUtils.isEmpty(prepayData.getProduct_id())){
                packageParams.add(new BasicNameValuePair(JsConst.PRODUCT_ID, prepayData.getProduct_id()));
            }
            if (!TextUtils.isEmpty(prepayData.getOpenid())){
                packageParams.add(new BasicNameValuePair(JsConst.OPENID,prepayData.getOpenid()));
            }

            String sign = prepayData.getSign();
            packageParams.add(new BasicNameValuePair("sign", sign));
            String getXml = toXml(packageParams);

            return new String(getXml.toString().getBytes(), "ISO-8859-1");

        } catch (Exception e) {
            Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    private String toXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<"+params.get(i).getName()+">");


            sb.append(params.get(i).getValue());
            sb.append("</"+params.get(i).getName()+">");
        }
        sb.append("</xml>");

        Log.e("orion",sb.toString());
        return sb.toString();
    }
}


