package com.bazikyan.deliveryapp.server;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

public class CategoryGet {

    private int code;
    private String name;
    private int fixPrice;
    private String urlImage;
    private Bitmap image;

    public CategoryGet(JSONObject jsonInput) throws JSONException {
        this.code = jsonInput.getInt("code");
        this.name = jsonInput.getString("name");
        this.fixPrice = jsonInput.getInt("fixPrice");
        this.urlImage = jsonInput.getString("urlImage");
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getFixPrice() {
        return fixPrice;
    }

    public String getUrlImage() {
        return urlImage;
    }
}
