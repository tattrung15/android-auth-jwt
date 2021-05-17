package com.hithaui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.hithaui.configs.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView btnSignIn;
    LinearLayout linearLayout3, linearLayout4;
    ProgressBar progressBar;
    TextInputEditText txtUsername, txtPassword;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tools.setSystemBarLight(this);
        Tools.setSystemBarColor(this, R.color.white);

        RequestQueue queue = Volley.newRequestQueue(this);

        sharedPreferences = getSharedPreferences("USER_AUTH", MODE_PRIVATE);

        btnSignIn = findViewById(R.id.btnSignIn);
        linearLayout3 = findViewById(R.id.linearLayout3);
        linearLayout4 = findViewById(R.id.linearLayout4);
        progressBar = findViewById(R.id.progressBar);
        txtUsername = findViewById(R.id.txvUsername);
        txtPassword = findViewById(R.id.txvPassword);

        linearLayout3.bringToFront();
        linearLayout4.bringToFront();
        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);

        validateLogin();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (txtUsername.getText().toString().trim().compareTo("") == 0 || txtPassword.getText().toString().trim().compareTo("") == 0) {
                    Toast.makeText(getBaseContext(), "Nhập tài khoản không hợp lệ", Toast.LENGTH_LONG).show();
                    return;
                }

                Map<String, String> paramsLogin = new HashMap<>();
                paramsLogin.put("username", txtUsername.getText().toString());
                paramsLogin.put("password", txtPassword.getText().toString());

                JSONObject objectLogin = new JSONObject(paramsLogin);

                JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, Constants.BASE_API + "/auth/login",
                        objectLogin, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String jwt = response.getString("jwt");
                            String username = response.getString("username");

                            rememberLogin(jwt, username);

                            progressBar.setVisibility(View.INVISIBLE);
                            btnSignIn.setVisibility(View.VISIBLE);

                            Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                            startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        btnSignIn.setVisibility(View.VISIBLE);
                    }
                });

                progressBar.setVisibility(View.VISIBLE);
                btnSignIn.setVisibility(View.INVISIBLE);

                queue.add(loginRequest);
            }
        });
    }

    private void rememberLogin(String jwt, String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jwt", jwt);
        editor.putString("username", username);
        editor.apply();
    }

    private void validateLogin() {
        String jwt = sharedPreferences.getString("jwt", null);
        String username = sharedPreferences.getString("username", null);
        if (jwt != null) {

            txtUsername.setText(username);

            RequestQueue queue = Volley.newRequestQueue(this);

            progressBar.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.INVISIBLE);

            Map<String, String> paramsLogin = new HashMap<>();
            paramsLogin.put("jwt", jwt);

            JSONObject objectLogin = new JSONObject(paramsLogin);

            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, Constants.BASE_API + "/auth/validate",
                    objectLogin, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String jwt = response.getString("jwt");
                        String username = response.getString("username");

                        rememberLogin(jwt, username);

                        Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getBaseContext(), "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    btnSignIn.setVisibility(View.VISIBLE);
                }
            });

            queue.add(loginRequest);
        }
    }
}