package com.bazikyan.deliveryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Account extends AppCompatActivity {

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        settings = getSharedPreferences("app", MODE_PRIVATE);

        new ServerThread().execute();

        (findViewById(R.id.logOut)).setOnClickListener(v -> {
            settings.edit().remove("clientCode").apply();
            Intent intent = new Intent(Account.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private class ServerThread extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String url = "http://80.254.124.90/00bazikyan/client/get/?data=" +
                    settings.getInt("clientCode", 0);
            API api = new API(url);

            return api.getJsonString();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            try {
                ((TextView) findViewById(R.id.personName)).setText(jsonObject.getString("name"));
                ((TextView) findViewById(R.id.personAddress)).setText(jsonObject.getString("address"));
                ((TextView) findViewById(R.id.personPhone)).setText(jsonObject.getString("phone"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}