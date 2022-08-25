package com.bazikyan.deliveryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bazikyan.deliveryapp.server.CategoryGet;
import com.bazikyan.deliveryapp.server.ProductGet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectCategoryAndProducts extends AppCompatActivity {

    private static final int TYPE_CATEGORY = 1, TYPE_PRODUCT = 2;
    int currentType;
    List<CategoryGet> categories;
    List<ProductGet> products;
    ListView listMain;
    ProgressBar progressSelectCatAndProd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category_and_products);

        Objects.requireNonNull(getSupportActionBar()).hide();

        progressSelectCatAndProd = (ProgressBar) findViewById(R.id.progressSelectCatAndProd);

        categories = new ArrayList<>();
        new ServerThreadGetCategories().execute();

        currentType = TYPE_CATEGORY;

        listMain = findViewById(R.id.listMain);
        listMain.setOnItemClickListener((parent, view, position, id) -> {
            switch (currentType) {
                case TYPE_CATEGORY:
                    String nameCategory = categories.get(position).getName();
                    ((TextView) (findViewById(R.id.textView3))).setText("Категории - " + nameCategory);
                    int codeCategory = categories.get(position).getCode();

                    products = new ArrayList<>();
                    new ServerThreadGetProducts().execute(codeCategory);

                    currentType = TYPE_PRODUCT;
                    break;
                case TYPE_PRODUCT:
                    Intent intent = new Intent();
                    intent.putExtra("codeProduct", products.get(position).getCode());
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
            }
        });
    }

    public class ServerThreadGetCategories extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressSelectCatAndProd.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String url = "http://80.254.124.90/00bazikyan/product/get/?data=category";
            API api = new API(url);
            JSONArray data = null;
            try {
                data = api.getJsonString().getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    categories.add(new CategoryGet(data.getJSONObject(i)));

                    InputStream in = new java.net.URL(categories.get(i).getUrlImage()).openStream();
                    categories.get(i).setImage(BitmapFactory.decodeStream(in));
                }
            } catch (JSONException | MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            listMain.setAdapter(new MyAdapter(SelectCategoryAndProducts.this, TYPE_CATEGORY));
            progressSelectCatAndProd.setVisibility(View.GONE);
        }
    }

    public class ServerThreadGetProducts extends AsyncTask<Integer, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressSelectCatAndProd.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            int codeCategory = ints[0];

            String url = "http://80.254.124.90/00bazikyan/product/get/?data=product&category=" + codeCategory;
            API api = new API(url);
            JSONArray data = null;
            try {
                data = api.getJsonString().getJSONArray("data");
                JSONObject json = data.getJSONObject(0);

                int fixPrice = json.getInt("fixPrice");
                JSONArray product = json.getJSONArray("product");
                for (int i = 0; i < product.length(); i++) {
                    products.add(new ProductGet(product.getJSONObject(i)));

                    if (!products.get(i).getUrlImage().equals("")) {
                        InputStream in = new java.net.URL(products.get(i).getUrlImage()).openStream();
                        products.get(i).setImage(BitmapFactory.decodeStream(in));
                    }

                    products.get(i).setFixPrice(fixPrice);
                }
            } catch (JSONException | MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            listMain.setAdapter(new MyAdapter(SelectCategoryAndProducts.this, TYPE_PRODUCT));
            progressSelectCatAndProd.setVisibility(View.GONE);
        }
    }

    public class MyAdapter extends BaseAdapter {
        LayoutInflater layoutInflater;
        int type; //1 - categories, 2 - products

        public MyAdapter(Context context, int typeInput) {
            layoutInflater = LayoutInflater.from(context);
            type = typeInput;
        }

        @Override
        public int getCount() {
            if (type == TYPE_CATEGORY)
                    return categories.size();
            if (type == TYPE_PRODUCT)
                    return products.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = layoutInflater.inflate(R.layout.list_category_and_product, null);
            switch (type) {
                case TYPE_CATEGORY:
                    ((TextView) convertView.findViewById(R.id.catprodName)).setText(categories.get(position).getName());
                    ((TextView) convertView.findViewById(R.id.catprodPrice)).setText("Примерная цена: " + categories.get(position).getFixPrice());
                    ((ImageView) convertView.findViewById(R.id.catprodImage)).setImageBitmap(categories.get(position).getImage());
                    break;
                case TYPE_PRODUCT:
                    ((TextView) convertView.findViewById(R.id.catprodName)).setText(products.get(position).getName());
                    ((TextView) convertView.findViewById(R.id.catprodPrice)).setText("Примерная цена: " + products.get(position).getFixPrice());
                    //((ImageView) convertView.findViewById(R.id.categoryImage)).setImageBitmap(categories.get(position).getImage());
                    break;
            }

            return convertView;
        }
    }
}