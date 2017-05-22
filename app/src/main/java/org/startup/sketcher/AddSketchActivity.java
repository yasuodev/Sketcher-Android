package org.startup.sketcher;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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

/**
 * Created by Spring on 5/18/2017.
 */

public class AddSketchActivity extends BaseActivity {

    @InjectView(R.id.etTitle)
    EditText etTitle;
    @InjectView(R.id.etIdea)
    EditText etIdea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sketch);

    }

    @OnClick(R.id.rlCancel)
    public void onCancel(View view){
        hideKeyboard();
        finish();
    }

    @OnClick(R.id.rlSubmit)
    public void onSubmit(View view) {
        hideKeyboard();

        int i = 1;
        if (getText(etTitle).length() <= 0){
            toast("Title cannot empty");
            i = 0;
        }
        if (getText(etIdea).length() <= 0) {
            toast("Idea cannot empty");
            i = 0;
        }

        if (i == 1){
            (new AddSketch(getText(etTitle), getText(etIdea))).execute();
        }
    }


    class AddSketch extends AsyncTask<Void, String, String> {

        String userid,sketch_title, sketch_idea;
        String responseString;
        ProgressDialog progressDialog;

        public AddSketch(String sketch_title, String sketch_idea){
            this.sketch_title = sketch_title;
            this.sketch_idea = sketch_idea;
            this.userid = Util.ReadSharePreference(getApplicationContext(), Constant.SHARED_KEY.Key_UserID);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            if (progressDialog == null)
                progressDialog = ProgressDialog.show(AddSketchActivity.this, null, "Loading...", true, false);

        }

        @Override
        protected String doInBackground(Void... params) {
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">\n"+
            "<soapenv:Header/>\n"+
            "<soapenv:Body>\n"+
            "<urn:AddNewSketch soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"+
            "<owner_id xsi:type=\"xsd:string\">%s</owner_id>\n"+
            "<sketch_title xsi:type=\"xsd:string\">%s</sketch_title>\n"+
            "<original_idea xsi:type=\"xsd:string\">%s</original_idea>\n"+
            "<isPublic xsi:type=\"xsd:string\">%s</isPublic>\n"+
            "</urn:AddNewSketch>\n"+
            "</soapenv:Body>\n"+
            "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, userid, sketch_title, sketch_idea, "1");

            HttpPost httpPost = new HttpPost(Constant.URL);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("AddSketch: ", responseString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            try{
                JSONObject jObj = new JSONObject(result);
                boolean status = jObj.optBoolean("Status");
                String message = jObj.optString("Message");

                if (status){
                    String sketchID = jObj.optString("sketchID");
                    toast(message);

                    finish();
                } else {
                    String error = jObj.getString("Error");

                    toast(error);
                }

            } catch (Exception e){
                toast("no result");
            }
        }
    }


}
