package org.zywx.wbpalmstar.plugin.uexweixin.VO;

import android.text.TextUtils;

import java.io.Serializable;


public class ShareMusicVO implements Serializable {
    private static final long serialVersionUID = -8501736338320586001L;
    private String musicUrl;
    private String musicLowBandUrl;
    private int scene = 0;
    private String title;
    private String description;
    private String thumbImg;

    public String getThumbImg() {
        return thumbImg;
    }

    public void setThumbImg(String thumbImg) {
        this.thumbImg = thumbImg;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getMusicLowBandUrl() {
        return musicLowBandUrl;
    }

    public void setMusicLowBandUrl(String musicLowBandUrl) {
        this.musicLowBandUrl = musicLowBandUrl;
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

    public boolean isMusicValid(){
        return (!TextUtils.isEmpty(musicUrl) || !TextUtils.isEmpty(musicLowBandUrl))
                && !TextUtils.isEmpty(title);
    }
}
