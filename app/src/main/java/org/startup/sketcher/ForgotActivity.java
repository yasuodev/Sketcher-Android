package org.startup.sketcher;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.Voice;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.startup.sketcher.util.Constant;
import org.startup.sketcher.util.Util;

import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;


public class ForgotActivity extends BaseActivity {

    @InjectView(R.id.etEmail)
    EditText etEmail;

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
                new ForgotPassword(email).execute();
            } else {
                toast(Constant.network_error);
            }
        }
    }

    class ForgotPassword extends AsyncTask<Void, String, String> {

        String email;
        ProgressDialog progressDialog;
        String response;

        public ForgotPassword(String email) {
            this.email = email;
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ForgotActivity.this, R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject jData = new JSONObject();
                jData.put("email", email);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            try {
                JSONObject jObj = new JSONObject(result);
                int status = jObj.optInt("success");

                toast(jObj.optString("msg"));
                if (status == 1) {
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
