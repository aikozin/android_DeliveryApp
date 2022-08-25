package com.bazikyan.deliveryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
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

public class Registration extends AppCompatActivity {

    EditText editText1, editText2, editText3, editText4, editText5, editText6, editText7,
            editText8, editText9, editText10, editText11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Objects.requireNonNull(getSupportActionBar()).hide();

        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        editText3 = findViewById(R.id.editText3);
        editText4 = findViewById(R.id.editText4);
        editText5 = findViewById(R.id.editText5);
        editText6 = findViewById(R.id.editText6);
        editText7 = findViewById(R.id.editText7);
        editText8 = findViewById(R.id.editText8);
        editText9 = findViewById(R.id.editText9);
        editText10 = findViewById(R.id.editText10);
        editText11 = findViewById(R.id.editText11);
        Button buttonRegistration = findViewById(R.id.buttonRegistration);

        EditText[] edList = {editText1, editText2, editText4, editText5, editText6, editText7, editText9, editText10, editText11};
        TextWatcher textWatcher = new TextWatcher(edList, buttonRegistration);
        for (EditText editText : edList) editText.addTextChangedListener(textWatcher);

        buttonRegistration.setOnClickListener(v -> {
            if (!editText10.getText().toString().equals(editText11.getText().toString())) {
                Toast.makeText(Registration.this, "Введенные пароли не совпадают", Toast.LENGTH_LONG).show();
            } else {
                new ServerThread().execute();
            }
        });
    }

    public class ServerThread extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String url = "http://80.254.124.90/00bazikyan/registration/?data=" +
                    editText1.getText().toString() + "," +
                    editText2.getText().toString() + "," +
                    editText3.getText().toString() + "," +
                    editText4.getText().toString() + "," +
                    editText5.getText().toString() + "," +
                    editText6.getText().toString() + "," +
                    editText7.getText().toString() + "," +
                    editText8.getText().toString() + "," +
                    editText9.getText().toString() + "," +
                    editText10.getText().toString();
            API api = new API(url);

            return api.getJsonString();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);

            try {
                if (json.getString("status").equals("ok")) {
                    Toast.makeText(Registration.this, "Регистрация успешно завершена", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent();
                    intent.putExtra("login", editText9.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(Registration.this, "Что-то пошло не так...", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}