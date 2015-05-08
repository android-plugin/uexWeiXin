package org.zywx.wbpalmstar.plugin.uexweixin;

public interface Constants {
	public static final String accessToken = "https://api.weixin.qq.com/sns/oauth2/access_token?";
	public static final String refreshToken = "https://api.weixin.qq.com/sns/oauth2/refresh_token?";
	public static final String checkToken = "https://api.weixin.qq.com/sns/auth?";
	public static final String unionID = "https://api.weixin.qq.com/sns/userinfo?";

	int token = 1;
	int refresh = 2;
	int check = 3;
	int union = 4;
}
