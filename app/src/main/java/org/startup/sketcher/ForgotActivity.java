package org.startup.sketcher;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import butterknife.InjectView;
import butterknife.OnClick;


public class ForgotActivity extends BaseActivity {

    @InjectView(R.id.etEmail)
    EditText etEmail;
    @InjectView(R.id.tvTitle)
    TextView tvTitle;
    @InjectView(R.id.viewSuccess)
    LinearLayout viewSuccess;
    @InjectView(R.id.tvSuccess)
    TextView tvSuccess;
    @InjectView(R.id.btnReset)
    View btnReset;
    @InjectView(R.id.btnBackToLogin)
    View btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

    }

    @OnClick(R.id.btnReset)
    public void onReset(View view) {
        hideKeyboard();
        String email = getText(etEmail);

        if (Patterns.EMAIL_ADDRESS.matcher(getText(etEmail)).matches() == false) {
            toast("Please enter valid email.");
        } else {
            if (Util.isOnline(getApplicationContext())) {
                (new ForgotPassword(email)).execute();
            } else {
                toast(Constant.network_error);
            }
        }
    }

    @OnClick(R.id.btnBackToLogin)
    public void onBack(View view) {
        finish();
    }

    class ForgotPassword extends AsyncTask<Void, String, String> {

        String email;
        String responseString;
        ProgressDialog progressDialog;

        public ForgotPassword(String email) {
            this.email = email;
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();

            if (progressDialog == null)
                progressDialog = ProgressDialog.show(ForgotActivity.this, null, null, true, false);
        }

        @Override
        protected String doInBackground(Void... params) {

            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">"+
                    "<soapenv:Header/>"+
                    "<soapenv:Body>"+
                    "<urn:forgotPassword soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"+
                    "<email xsi:type=\"xsd:string\">%s</email>"+
                    "</urn:forgotPassword>"+
                    "</soapenv:Body>"+
                    "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, email);

            HttpPost httpPost = new HttpPost(Constant.URL);
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

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            try {
                JSONObject jObj = new JSONObject(result);
                boolean status = jObj.optBoolean("Status");

                if (status) {
                    tvTitle.setVisibility(View.GONE);
                    viewSuccess.setVisibility(View.VISIBLE);
                    tvSuccess.setText("An e-mail has been sent to "+email+" with further instructions.");

                    etEmail.setVisibility(View.GONE);
                    btnReset.setVisibility(View.GONE);
                    btnBackToLogin.setVisibility(View.VISIBLE);

                } else {
                    etEmail.setBackgroundResource(R.drawable.textviewredborder);
                    String message = jObj.optString("Message");
                    toast(message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
