package com.bazikyan.deliveryapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bazikyan.deliveryapp.server.ProductGet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingCart extends AppCompatActivity {

    List<Integer> codeProducts;
    List<ProductGet> products;
    List<Integer> countProducts;

    TextView textViewEmptyShoppingCart;
    ProgressBar progressShoppingCart;
    ListView productsListView;

    SharedPreferences settings;

    int price;
    EditText comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        Objects.requireNonNull(getSupportActionBar()).hide();

        settings = getSharedPreferences("app", MODE_PRIVATE);

        textViewEmptyShoppingCart = findViewById(R.id.textViewEmptyShoppingCart);
        progressShoppingCart = findViewById(R.id.progressShoppingCart);
        productsListView = findViewById(R.id.productsListView);

        codeProducts = new ArrayList<>();
        products = new ArrayList<>();
        countProducts = new ArrayList<>();

        View footerView = LayoutInflater.from(this).inflate(R.layout.list_product_footer_in_shopping_cart, null);
        (footerView.findViewById(R.id.addProduct)).setOnClickListener(v -> {
            Intent intent = new Intent(ShoppingCart.this, SelectCategoryAndProducts.class);
            startActivityForResult(intent, 0);
        });
        (footerView.findViewById(R.id.complete)).setOnClickListener(v -> {
            if (codeProducts.size() != 0)
                new ServerThreadComplete().execute();
            else
                Toast.makeText(ShoppingCart.this, "Список продуктов пуст. Добавьте хотя бы один продукт.", Toast.LENGTH_LONG).show();
        });
        comment = footerView.findViewById(R.id.editTextTextMultiLine);
        productsListView.addFooterView(footerView);
        productsListView.setAdapter(new MyAdapter(ShoppingCart.this));
    }

    @Override
    public void onBackPressed() {
        if (codeProducts.size() > 0) {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        super.onBackPressed();
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCart.this);
            builder.setMessage("При выходе сформированный заказ будет потерян. Вы уверены?").setPositiveButton("Да", dialogClickListener)
                    .setNegativeButton("Нет", dialogClickListener).show();
        } else {
            super.onBackPressed();
        }
    }

    private void updatePrice() {
        price = 0;
        for (int i = 0; i < codeProducts.size(); i++) {
            price += products.get(i).getFixPrice() * countProducts.get(i);
        }
        ((TextView) findViewById(R.id.textView7)).setText("" + price + " р.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0)
            if (resultCode == RESULT_OK) {
                int codeProduct = data.getIntExtra("codeProduct", 0);
                if (codeProduct != 0) {
                    codeProducts.add(codeProduct);
                    new ServerThreadUpdateProductListView().execute();
                }
            }
    }

    private class ServerThreadUpdateProductListView extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressShoppingCart.setVisibility(View.VISIBLE);
            productsListView.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            products = new ArrayList<>();
            for (int i = 0; i < codeProducts.size(); i++) {
                String url = "http://80.254.124.90/00bazikyan/product/get/?data=product," + codeProducts.get(i);
                API api = new API(url);
                JSONObject json = api.getJsonString();
                try {
                    json = json.getJSONArray("data").getJSONObject(0);
                    products.add(new ProductGet(json));
                    products.get(i).setFixPrice(json.getInt("fixPrice"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            countProducts.add(1);

            productsListView.setAdapter(new MyAdapter(ShoppingCart.this));

            progressShoppingCart.setVisibility(View.GONE);
            productsListView.setVisibility(View.VISIBLE);

            updatePrice();
        }
    }

    private class ServerThreadComplete extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String products = "", quantity = "";
            for (int i = 0; i < codeProducts.size(); i++) {
                products += "" + codeProducts.get(i) + (i < codeProducts.size() - 1 ? "," : "");
                quantity += "" + countProducts.get(i) + (i < countProducts.size() - 1 ? "," : "");
            }
            String commentText = "";
            try {
                commentText = URLEncoder.encode(comment.getText().toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = "http://80.254.124.90/00bazikyan/contract/set/?data=" +
                    settings.getInt("clientCode", 0) + "," +
                    price + "," +
                    commentText + "," +
                    "&products=" + products +
                    "&quantity=" + quantity;
            API api = new API(url);
            return api.getJsonString();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            try {
                if (jsonObject.getString("status").equals("ok")) {
                    finish();
                } else {
                    Toast.makeText(ShoppingCart.this, "Что-то пошло не так...", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class MyAdapter extends BaseAdapter {
        LayoutInflater layoutInflater;

        public MyAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return codeProducts.size();
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
                convertView = layoutInflater.inflate(R.layout.list_product_in_shopping_card, null);

            ((TextView) convertView.findViewById(R.id.productName)).setText(products.get(position).getName());
            ((TextView) convertView.findViewById(R.id.productPrice)).setText("Примерная цена: " + products.get(position).getFixPrice());

            EditText editTextNumber = (EditText) convertView.findViewById(R.id.editTextNumber);
            editTextNumber.setText("" + countProducts.get(position) + " шт.");
            (convertView.findViewById(R.id.buttonMinus)).setOnClickListener(v -> {
                int count = countProducts.get(position);
                count -= count > 1 ? 1 : 0;
                countProducts.set(position, count);
                editTextNumber.setText("" + countProducts.get(position) + " шт.");

                updatePrice();
            });

            (convertView.findViewById(R.id.buttonPlus)).setOnClickListener(v -> {
                int count = countProducts.get(position);
                count++;
                countProducts.set(position, count);
                editTextNumber.setText("" + countProducts.get(position) + " шт.");

                updatePrice();
            });

            return convertView;
        }
    }
}