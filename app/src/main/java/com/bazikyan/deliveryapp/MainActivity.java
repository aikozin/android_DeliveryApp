package com.bazikyan.deliveryapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    EditText editTextLogin, editTextPassword;
    Button buttonLogin;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences("app", MODE_PRIVATE);

        if (settings.contains("clientCode")) {
            openContracts();
        }

        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

        editTextLogin = findViewById(R.id.editText1);
        editTextPassword = findViewById(R.id.editText2);
        buttonLogin = findViewById(R.id.buttonRegistration);

        EditText[] edList = {editTextLogin, editTextPassword};
        TextWatcher textWatcher = new TextWatcher(edList, buttonLogin);
        for (EditText editText : edList) editText.addTextChangedListener(textWatcher);

        buttonLogin.setOnClickListener(v -> new ServerThread().execute());

        findViewById(R.id.textViewReg).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Registration.class);
            startActivityForResult(intent, 0);
        });
    }

    public void openContracts() {
        Intent intent = new Intent(MainActivity.this, Contracts.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0)
            if (resultCode == RESULT_OK) {
                editTextLogin.setText(data.getStringExtra("login"));
            }
    }

    public class ServerThread extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String url = "http://80.254.124.90/00bazikyan/login/?data=" +
                    editTextLogin.getText().toString() + "," +
                    editTextPassword.getText().toString();
            API api = new API(url);

            return api.getJsonString();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);

            try {
                if (json.getString("status").equals("ok")) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("login", editTextLogin.getText().toString());
                    editor.putInt("clientCode", json.getInt("clientCode"));
                    editor.apply();

                    openContracts();
                } else {
                    Toast.makeText(MainActivity.this, "Логин или пароль введены неверно", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}