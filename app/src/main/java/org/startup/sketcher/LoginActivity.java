package org.startup.sketcher;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.startup.sketcher.util.Constant;
import org.startup.sketcher.util.Util;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etUsername;
    private EditText etPassword;
    private ImageView imgCheck;

    private boolean isRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText)findViewById(R.id.etUsername);
        etPassword = (EditText)findViewById(R.id.etPassword);
        imgCheck = (ImageView)findViewById(R.id.imgCheck);

        findViewById(R.id.rlLogin).setOnClickListener(this);
        findViewById(R.id.rlForgot).setOnClickListener(this);
        findViewById(R.id.rlCreate).setOnClickListener(this);
        findViewById(R.id.rlRemember).setOnClickListener(this);

        String is_remember_user = read(Constant.SHARED_KEY.Key_IsRememberUser);
        if (is_remember_user.equalsIgnoreCase("true")) {
            isRemember = true;
        } else {
            isRemember = false;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlLogin:
                login();
                break;
            case R.id.rlForgot:
                forgotPassword();
                break;
            case R.id.rlCreate:
                register();
                break;
            case R.id.rlRemember:
                rememberMe();
                break;
        }
    }


    private void login() {
        hideKeyboard();
        if (isValidate()) {
            if (Util.isOnline(getApplicationContext()))
                new Login(getText(etUsername), getText(etPassword)).execute();
            else
                Toast.makeText(this, Constant.network_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void forgotPassword() {
        startActivity(ForgotActivity.class);
    }

    private void register() {
        startActivity(RegisterActivity.class);
    }

    private void rememberMe() {

        isRemember = !isRemember;

        if (isRemember) {
            imgCheck.setImageResource(R.drawable.checked);
            write(Constant.SHARED_KEY.Key_IsRememberUser, "true");
        } else {
            imgCheck.setImageResource(R.drawable.unchecked);
            write(Constant.SHARED_KEY.Key_IsRememberUser, "false");
        }
    }


    protected void hideKeyboard() {
        // Check if no view has focus:
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private boolean isValidate() {
        if (isEmpty(getText(etUsername))) {
            toast("please enter user name");
            return false;
        }
        if (isEmpty(getText(etPassword))) {
            toast("please enter password");
            return false;
        }
        return true;
    }

    protected String getText(EditText eText) {
        return eText == null ? "" : eText.getText().toString().trim();
    }

    protected void toast(CharSequence text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void startActivity(Class klass) {
        startActivity(new Intent(this, klass));
    }


    private class Login extends AsyncTask<Void, String, String> {

        String username, password;
        ProgressDialog progressDialog;
        String url;
        String soapAction = "urn:quote5#UserLogin";
        String responseString = "";

        public Login(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            responseString = "";
            url = Constant.URL;

            if (progressDialog == null)
                progressDialog = ProgressDialog.show(LoginActivity.this, null, "Loading...", true, false);
        }

        @Override
        protected String doInBackground(Void... parameters) {

            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">\n"+
                    "<soapenv:Header/>\n"+
                    "<soapenv:Body>\n"+
                    "<urn:UserLogin soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"+
                    "<user_email xsi:type=\"xsd:string\">%s</user_email>\n"+
                    "<password xsi:type=\"xsd:string\">%s</password>\n"+
                    "</urn:UserLogin>\n"+
                    "</soapenv:Body>\n"+
                    "</soapenv:Envelope>";

            //String soapmessage = String.format(envelope, username, password);
            String soapmessage = String.format(envelope, "yasuodev", "k78gtqb");

            HttpPost httpPost = new HttpPost(url);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("request: ", responseString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return responseString;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            try {
                JSONObject jObj = new JSONObject(result);
                boolean status = jObj.optBoolean("Status");
                String message = jObj.optString("Message");
                String userid = jObj.optString("UserID");
                String username = jObj.optString("UserName");

                Log.d("message====:", message);

                if (status) {
                    write(Constant.SHARED_KEY.Key_UserID, userid);

                    if (isRemember) {
                        write(Constant.SHARED_KEY.Key_UserName, username);
                        write(Constant.SHARED_KEY.Key_Password, password);
                    } else {
                        write(Constant.SHARED_KEY.Key_UserName, "");
                        write(Constant.SHARED_KEY.Key_Password, "");
                    }

                    startActivity(HomeActivity.class);
                    finish();
                } else {

                    if (message.equalsIgnoreCase("Invalid email-address or password."))
                        message = "Invalid username and password.";

                    toast(message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected void write(String key, String val) {
        Util.WriteSharePreference(this, key, val);
    }

    protected String read(String key) {
        return Util.ReadSharePreference(this, key);
    }
}
