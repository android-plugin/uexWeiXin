package org.zywx.wbpalmstar.plugin.uexweixin.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.tencent.mm.sdk.modelmsg.WXImageObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.widget.Toast;

/**
 * 图像工具类
 * 
 * @author waka
 * @version createTime:2016年5月10日 上午11:41:51
 */
public class BitmapUtil {

	// 缩略图大小
	private static final int THUMB_SIZE = 100;

	/**
	 * 得到WXImageObject
	 * 
	 * @param context
	 * @param imgPath
	 * @return
	 */
	public static WXImageObject createImageObject(Context context, String imgPath) {

		MLog.getIns().d("start");

		WXImageObject imgObj = new WXImageObject();

		if (imgPath.startsWith("http://") || imgPath.startsWith("https://")) {
			imgObj.imagePath = imgPath;
		} else {
			if (imgPath.startsWith("/")) {
				imgObj.imagePath = imgPath;
			} else {
				try {
					InputStream in = context.getAssets().open(imgPath);
					int lenght = in.available();
					BitmapFactory.Options opts = null;
					int count = 0;
					count = lenght / 100000 + 1;
					opts = new BitmapFactory.Options();
					if (lenght > 100000) {
						opts.inSampleSize = count;
					} else {
						opts.inSampleSize = 1;
					}
					Bitmap bmp = BitmapFactory.decodeStream(context.getAssets().open(imgPath));
					imgObj.imageData = bmpToByteArray(bmp, true);
				} catch (Exception e) {
					Toast.makeText(context, "图片不存在： " + e.getMessage(), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					MLog.getIns().e(e);
				}
			}
		}
		return imgObj;
	}

	/**
	 * 创建缩略图
	 * 
	 * @param context
	 * @param thumbPath
	 * @return
	 */
	public static Bitmap createThumbBitmap(Context context, String thumbPath) {

		MLog.getIns().d("start");

		// 缩略图地址临时变量
		Bitmap bmp = null;
		if (thumbPath.startsWith("http://") || thumbPath.startsWith("https://")) {
			try {
				bmp = BitmapFactory.decodeStream(new URL(thumbPath).openStream());
			} catch (Exception e) {
				Toast.makeText(context, "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				MLog.getIns().e(e);
			}
		} else {
			if (thumbPath.startsWith("/")) {// sd卡路径时
				File f = new File(thumbPath);
				if (!f.exists()) {
					Toast.makeText(context, "File is not exist!", Toast.LENGTH_SHORT).show();
				}
				bmp = BitmapFactory.decodeFile(thumbPath);
			} else {
				try {
					bmp = BitmapFactory.decodeStream(context.getAssets().open(thumbPath));
				} catch (IOException e) {
					Toast.makeText(context, "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					MLog.getIns().e(e);
				}
			}
		}
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);

		if (bmp != null && bmp != thumbBmp) {
			bmp.recycle();
		}
		return thumbBmp;
	}

	/**
	 * Bitmap2byte[]
	 * 
	 * @param bmp
	 * @param needRecycle
	 * @return
	 */
	public static byte[] bmpToByteArray(Bitmap bmp, boolean needRecycle) {
		if (bmp == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, baos);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = baos.toByteArray();
		try {
			baos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
