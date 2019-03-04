package com.desbois.mathis.bizzbee;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnexionActivity extends Activity implements View.OnClickListener {
    private static String connectionUrl = "https://bizzbee.maximegautier.fr/login";

    private static final String TAG = "ConnexionActivity";
    private static final int REQUEST_SIGNUP = 0;

    private TextView mNameText;
    private TextView mPasswordText;
    private TextView mPasswordForgetIt;
    private EditText mNameValue;
    private EditText mPasswordValue;
    private Button mConnexionButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        mNameText = findViewById(R.id.activity_connexion_name_text);
        mPasswordText = findViewById(R.id.activity_connexion_password_text);

        mPasswordForgetIt = findViewById(R.id.activity_connexion_password_forgetit);

        mNameValue = findViewById(R.id.activity_connexion_name_value);
        mPasswordValue = findViewById(R.id.activity_connexion_password_value);

        mConnexionButton = findViewById(R.id.activity_connexion_button_connexion);

        mConnexionButton.setOnClickListener(this);

        mPasswordValue.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                Log.i(TAG,"Enter pressed");
                login();
            }
            return false;
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_connexion_button_connexion:
                login();
        }
    }

    public void login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        mConnexionButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(ConnexionActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        OkHttpClient okHttpClient = new OkHttpClient();

        String login = mNameValue.getText().toString();
        String password = mPasswordValue.getText().toString();

        RequestBody requestBody = new FormBody.Builder()
                .add("login", login)
                .add("password", password)
                .build();

        Log.i(TAG, "Login : " + login);
        Log.i(TAG, "Password : " + password);

        Request request = new Request.Builder()
                .url(connectionUrl)
                .post(requestBody)
                .build();

        Log.i(TAG, request.body().contentType().toString());

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.i("Connection", "Failed");
                    Toast.makeText(ConnexionActivity.this, "Failed",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //le retour est effectué dans un thread différent
                final String text = response.body().string();

                runOnUiThread(() -> {
                    if(text.equals("OK")) {
                        onLoginSuccess();
                    } else {
                        onLoginFailed();
                    }
                });

                progressDialog.dismiss();
            }
        };

        Call response = okHttpClient.newCall(request);
        response.enqueue(callback);
        Log.i(TAG, response.request().toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // disable going back to the MainActivity
        //moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    public void onLoginSuccess() {
        mConnexionButton.setEnabled(true);
        Toast.makeText(getBaseContext(), "Login successful", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Connected");
        this.finish();
    }

    public void onLoginFailed() {
        mConnexionButton.setEnabled(true);
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Not connected");
    }

    public boolean validate() {
        boolean valid = true;

        String login = mNameValue.getText().toString();
        String password = mPasswordValue.getText().toString();

        if (login.isEmpty()) {
            mNameValue.setError("enter a valid login");
            valid = false;
        } else {
            mNameValue.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordValue.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordValue.setError(null);
        }

        return valid;
    }
}
