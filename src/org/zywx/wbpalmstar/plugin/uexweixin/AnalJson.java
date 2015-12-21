package org.zywx.wbpalmstar.plugin.uexweixin;

import org.json.JSONObject;

import android.util.Log;
public class AnalJson {
	public static CheckModel getJson(String data){
		CheckModel model=new CheckModel();
		JSONObject jsob;
		try {
			jsob = new JSONObject(data);
			model.errcode=jsob.getString("errcode");
			Log.i("AnalJson", "model.errcode"+model.errcode);
			model.errmsg=jsob.getString("errmsg");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return model;
	} 
}
