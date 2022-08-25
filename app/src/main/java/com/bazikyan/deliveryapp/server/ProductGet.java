package com.bazikyan.deliveryapp.server;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bazikyan.deliveryapp.API;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductGet {

    private int code;
    private int fixPrice;
    private String name;
    private String urlImage;
    private Bitmap image;

    public ProductGet(JSONObject jsonInput) throws JSONException {
        this.code = jsonInput.getInt("code");
        this.name = jsonInput.getString("name");
        this.urlImage = jsonInput.getString("urlImage");
    }

    public int getCode() {
        return code;
    }

    public int getFixPrice() {
        return fixPrice;
    }

    public void setFixPrice(int fixPrice) {
        this.fixPrice = fixPrice;
    }

    public String getName() {
        return name;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
