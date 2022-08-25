package com.bazikyan.deliveryapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import android.widget.ListView;
import android.widget.TextView;

import com.bazikyan.deliveryapp.server.ContractGet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class Contracts extends AppCompatActivity {

    SharedPreferences settings;
    List<ContractGet> contracts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contracts);

        Objects.requireNonNull(getSupportActionBar()).hide();

        settings = getSharedPreferences("app", MODE_PRIVATE);

        restart();

        (findViewById(R.id.imageButton)).setOnClickListener(v -> restart());
        (findViewById(R.id.addContract)).setOnClickListener(v -> {
            Intent intent = new Intent(Contracts.this, ShoppingCart.class);
            startActivity(intent);
        });

        (findViewById(R.id.buttonPerson)).setOnClickListener(v -> {
            Intent intent = new Intent(Contracts.this, Account.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        restart();
    }

    public void restart() {
        (findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        (findViewById(R.id.content)).setVisibility(View.GONE);
        contracts = new ArrayList<>();
        new ServerThread().execute();
    }

    public class ServerThread extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String url = "http://80.254.124.90/00bazikyan/contract/get/?data=" +
                    settings.getInt("clientCode", 0);
            API api = new API(url);
            JSONObject json = api.getJsonString();
            try {
                JSONArray contractsJSON = json.getJSONArray("data");
                for (int i = 0; i < contractsJSON.length(); i++) {
                    json = contractsJSON.getJSONObject(i);
                    contracts.add(new ContractGet(json));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            (findViewById(R.id.progressBar)).setVisibility(View.GONE);
            (findViewById(R.id.content)).setVisibility(View.VISIBLE);

            ListView listContracts = findViewById(R.id.listContracts);
            listContracts.setAdapter(new MyAdapter(Contracts.this));
        }
    }

    public class ServerThreadDeleteContract extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            String url = "http://80.254.124.90/00bazikyan/contract/delete/?data=" +
                    integers[0];
            API api = new API(url);
            api.getJsonString();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            restart();
        }
    }

    public class MyAdapter extends BaseAdapter {
        LayoutInflater layoutInflater;

        public MyAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return contracts.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = layoutInflater.inflate(R.layout.list_contracts, null);

            TextView tvStatus = convertView.findViewById(R.id.tvStatus);
            String status = contracts.get(position).getStatus();
            tvStatus.setText(status);
            if (status.equals("Принят на обработку"))
                tvStatus.setBackgroundResource(R.drawable.status_background_1);
            if (status.equals("На обработке"))
                tvStatus.setBackgroundResource(R.drawable.status_background_2);
            if (status.equals("Доставлено"))
                tvStatus.setBackgroundResource(R.drawable.status_background_3);

            ((TextView) convertView.findViewById(R.id.tvProducts)).setText(contracts.get(position).getProducts());
            ((TextView) convertView.findViewById(R.id.tvSupplier)).setText(contracts.get(position).getSupplier());
            ((TextView) convertView.findViewById(R.id.tvPrice)).setText("" + contracts.get(position).getPrice() + "р.");
            ((TextView) convertView.findViewById(R.id.tvRemark)).setText("Примечание: " + (contracts.get(position).getRemark().equals("") ? "пусто" : contracts.get(position).getRemark()));

            long time = contracts.get(position).getTime() * (long) 1000;
            Date date = new Date(time);
            SimpleDateFormat format = new SimpleDateFormat("dd MMM - HH:mm");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            ((TextView) convertView.findViewById(R.id.tvTime)).setText(format.format(date));

            convertView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            new ServerThreadDeleteContract().execute(contracts.get(position).getCode());
                            break;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Contracts.this);
                builder.setMessage("Вы действительно хотите удалить заказ?").setPositiveButton("Да", dialogClickListener)
                        .setNegativeButton("Нет", dialogClickListener).show();
            });

            return convertView;
        }
    }
}