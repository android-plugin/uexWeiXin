package org.zywx.wbpalmstar.plugin.uexweixin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.zywx.wbpalmstar.plugin.uexweixin.utils.IFeedback;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by fred on 16/11/29.
 */
public class DecodeImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "DecodeImageAsyncTask";
    
    private IFeedback<Bitmap> feedback;

    private Context mContext;

    public DecodeImageAsyncTask(Context context, IFeedback<Bitmap> feedback) {
        this.mContext = context;
        this.feedback = feedback;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String url = params[0];
        Bitmap bmp = null;
        if (url.startsWith("http://")||url.startsWith("https://")) {
            try {
                bmp = BitmapFactory.decodeStream(new URL(url)
                        .openStream());
            } catch (final Exception e) {

                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "错误：" + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

                e.printStackTrace();
                return null;
            }
        } else {
            if (url.startsWith("/")) {// sd卡路径时
                File f = new File(url);
                if (!f.exists()) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "File is not exist!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    return null;

                }
                bmp = BitmapFactory.decodeFile(url);
            } else {
                try {

                    bmp = BitmapFactory.decodeStream(mContext.getAssets().open(url));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (bmp != null){
            //thumb的尺寸是100，和以前的逻辑保持一致
            final int THUMB_SIZE = 100;
            Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE,
                    THUMB_SIZE, true);
            return thumbBmp;
        }else{
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        feedback.onFeedback(bitmap);

    }
}
