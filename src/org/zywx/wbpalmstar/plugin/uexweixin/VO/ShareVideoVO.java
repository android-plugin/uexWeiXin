package org.zywx.wbpalmstar.plugin.uexweixin.VO;

import android.text.TextUtils;

import java.io.Serializable;


public class ShareVideoVO implements Serializable {
    private static final long serialVersionUID = 6843584365661219717L;
    private String thumbImg;
    private String videoUrl;
    private String videoLowBandUrl;

    private int scene = 0;
    private String title;
    private String description;

    public String getVideoLowBandUrl() {
        return videoLowBandUrl;
    }

    public void setVideoLowBandUrl(String videoLowBandUrl) {
        this.videoLowBandUrl = videoLowBandUrl;
    }

    public String getThumbImg() {
        return thumbImg;
    }

    public void setThumbImg(String thumbImg) {
        this.thumbImg = thumbImg;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getScene() {
        return scene;
    }

    public void setScene(int scene) {
        this.scene = scene;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVideoValid(){
        return ((!TextUtils.isEmpty(videoUrl) || !TextUtils.isEmpty(videoLowBandUrl))
                && !TextUtils.isEmpty(title)
                && !TextUtils.isEmpty(thumbImg));
    }
}
