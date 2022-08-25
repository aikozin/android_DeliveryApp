package com.bazikyan.deliveryapp.server;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bazikyan.deliveryapp.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ContractGet {

    private JSONObject json;
    private int code;
    private int time;
    private String products = "";
    private String status;
    private String supplier;
    private int price;
    private String remark;

    public ContractGet(JSONObject jsonInput) throws JSONException {
        this.json = jsonInput;
        this.code = json.getInt("code");
        this.time = json.getInt("time");
        this.status = json.getString("status");
        this.price = json.getInt("price");
        this.remark = json.getString("remark");

        JSONArray jsonProducts = new JSONArray(json.getString("jsonProducts"));
        for (int i = 0; i < jsonProducts.length(); i++) {
            String url = "http://80.254.124.90/00bazikyan/product/get/?data=product," +
                    jsonProducts.get(i);
            API api = new API(url);
            JSONObject jsonTemp = api.getJsonString();
            String name = jsonTemp.getJSONArray("data").getJSONObject(0).getString("name");

            products += name;
            if (i < jsonProducts.length() - 1) {
                products += ", ";
            }
        }

        int supplierCode = json.isNull("supplierCode") ? -1 : json.getInt("supplierCode");
        if (supplierCode == -1)
            supplier = "Доставщик еще не назначен";
        else {
            String url = "http://80.254.124.90/00bazikyan/supplier/get/?data=" + supplierCode;
            API api = new API(url);
            JSONObject jsonTemp = api.getJsonString();
            supplier = jsonTemp.getString("data");
        }
    }

    public int getCode() {
        return code;
    }

    public int getTime() {
        return time;
    }

    public String getProducts() {
        return products;
    }

    public String getStatus() {
        return status;
    }

    public String getSupplier() {
        return supplier;
    }

    public int getPrice() {
        return price;
    }

    public String getRemark() {
        return remark;
    }
}
