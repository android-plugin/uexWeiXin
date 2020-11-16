package org.zywx.wbpalmstar.plugin.uexweixin.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.plugin.uexweixin.Utils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class WXChooseInvoiceDetailTask extends AsyncTask<Void, Void, String>{

    public interface OnResultListener{
        void onResult(String jsonData);
    }

    private static final String TAG = "WXChooseInvoiceDetailTask";

    private IWXAPI msgApi;
    private OnResultListener mListener;

    private String handleUrl;
    private JSONArray cardArrParam;

    public WXChooseInvoiceDetailTask(Context mContext, String appId, String handleUrl, JSONArray cardArr, OnResultListener listener) {
        this.mListener = listener;
        if (msgApi == null){
            msgApi = WXAPIFactory.createWXAPI(mContext, null);
        }
        msgApi.registerApp(appId);
        this.handleUrl = handleUrl;
        this.cardArrParam = cardArr;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(Void... params) {
        String resultContent = null;
        try {
            String url = this.handleUrl;
            String cardArrStr = URLEncoder.encode(this.cardArrParam.toString(), "UTF-8");
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("model", cardArrStr);
            BDebug.i(TAG, "requestParams model: " + cardArrStr);
            byte[] resultBuffer = Utils.httpPostForm(url, paramsMap);
            resultContent = new String(resultBuffer);
            BDebug.i(TAG, resultContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultContent;
    }

    @Override
    protected void onPostExecute(String result) {
        if (mListener != null){
            mListener.onResult(result);
        }
    }

    @Override
    protected void onCancelled() {
        if (mListener != null){
            mListener.onResult(null);
        }
    }

}


