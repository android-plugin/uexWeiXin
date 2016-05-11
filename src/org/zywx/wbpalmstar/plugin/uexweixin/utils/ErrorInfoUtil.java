package org.zywx.wbpalmstar.plugin.uexweixin.utils;

import com.tencent.mm.sdk.modelbase.BaseResp;

/**
 * 错误信息工具类
 * 
 * 微信全局错误码地址：http://mp.weixin.qq.com/wiki/17/fa4e1434e57290788bde25603fa2fcbd.
 * html
 * 
 * @author waka
 * @version createTime:2016年5月10日 下午5:49:37
 */
public class ErrorInfoUtil {

	/**
	 * BaseResp.errCode OpenAPI响应基类错误码转换成字符串
	 * 
	 * @param errCode
	 * @return
	 */
	public static String respErrorCode2String(int errCode) {

		String errorString = "";

		switch (errCode) {

		// 认证被否决
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			errorString = "认证被否决";
			break;

		// 一般错误
		case BaseResp.ErrCode.ERR_COMM:
			errorString = "一般错误";
			break;

		// 正确返回
		case BaseResp.ErrCode.ERR_OK:
			errorString = "正确返回";
			break;

		// 发送失败
		case BaseResp.ErrCode.ERR_SENT_FAILED:
			errorString = "发送失败";
			break;

		// 不支持错误
		case BaseResp.ErrCode.ERR_UNSUPPORT:
			errorString = "不支持错误";
			break;

		// 用户取消
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			errorString = "用户取消";
			break;

		default:
			errorString = "未知错误";
			break;
		}
		return errorString;
	}

}
