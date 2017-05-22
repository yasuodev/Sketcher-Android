package org.startup.sketcher;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
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


public class ConceptActivity extends BaseActivity {

    @InjectView(R.id.tvTitle)
    TextView tvTitle;
    @InjectView(R.id.rlHome)
    RelativeLayout rlHome;
    @InjectView(R.id.rlCancel)
    RelativeLayout rlCancel;
    @InjectView(R.id.rlEdit)
    RelativeLayout rlEdit;
    @InjectView(R.id.rlSave)
    RelativeLayout rlSave;

    // detail layout
    @InjectView(R.id.viewDetail)
    View viewDetail;
    @InjectView(R.id.tvDescription)
    TextView tvDescription;
    @InjectView(R.id.tvWho)
    TextView tvWho;
    @InjectView(R.id.tvWhy)
    TextView tvWhy;
    @InjectView(R.id.tvWhat)
    TextView tvWhat;

    // edit layout
    @InjectView(R.id.viewEdit)
    View viewEdit;
    @InjectView(R.id.etWorkingTitle)
    EditText etWorkingTitle;
    @InjectView(R.id.etTheIdea)
    EditText etTheIdea;

    // edit concept layout
    @InjectView(R.id.viewConcept)
    View viewConcept;
    @InjectView(R.id.tvEditTitle)
    TextView tvEditTitle;
    @InjectView(R.id.etConcept)
    EditText etConcept;


    String sketchID;
    ProgressDialog progressDialog;

    String strTitle = "", strDescriptoin = "";
    String strWho = "", strWhy = "", strWhat = "", strScrapBook = "";

    boolean isWho = false, isWhy = false, isWhat = false, isScrapBook = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concept);

        Intent intent = getIntent();
        sketchID = (String) intent.getSerializableExtra("sketchID");
        Log.d("concept:", sketchID);

        viewDetail.setVisibility(View.VISIBLE);
        viewEdit.setVisibility(View.GONE);
        viewConcept.setVisibility(View.GONE);
        rlHome.setVisibility(View.VISIBLE);
        rlEdit.setVisibility(View.VISIBLE);
        rlCancel.setVisibility(View.GONE);
        rlSave.setVisibility(View.GONE);

        fetchSketchData();
        showSketchData();
    }

    private void showSketchData(){
        hideKeyboard();

        viewDetail.setVisibility(View.VISIBLE);
        viewEdit.setVisibility(View.GONE);
        viewConcept.setVisibility(View.GONE);
        rlHome.setVisibility(View.VISIBLE);
        rlEdit.setVisibility(View.VISIBLE);
        rlCancel.setVisibility(View.GONE);
        rlSave.setVisibility(View.GONE);

        tvTitle.setText(strTitle);
        tvDescription.setText(strDescriptoin);
        tvWho.setText(strWho);
        tvWhy.setText(strWhy);
        tvWhat.setText(strWhat);
    }

    @OnClick(R.id.rlHome)
    public void onHome(View view){
        finish();
    }

    @OnClick(R.id.rlCancel)
    public void onCancel(View view){
        hideKeyboard();

        viewDetail.setVisibility(View.VISIBLE);
        viewEdit.setVisibility(View.GONE);
        viewConcept.setVisibility(View.GONE);
        rlHome.setVisibility(View.VISIBLE);
        rlEdit.setVisibility(View.VISIBLE);
        rlCancel.setVisibility(View.GONE);
        rlSave.setVisibility(View.GONE);

        isWho = false;
        isWhy = false;
        isWhat = false;
        isScrapBook = false;

        fetchSketchData();
    }

    @OnClick(R.id.rlEdit)
    public void onEdit(View view){

        viewDetail.setVisibility(View.GONE);
        viewEdit.setVisibility(View.VISIBLE);
        viewConcept.setVisibility(View.GONE);
        rlEdit.setVisibility(View.GONE);
        rlSave.setVisibility(View.VISIBLE);
        rlHome.setVisibility(View.GONE);
        rlCancel.setVisibility(View.VISIBLE);

        etWorkingTitle.setText(strTitle);
        etTheIdea.setText(strDescriptoin);
    }

    @OnClick(R.id.rlSave)
    public void onSave(View view){

        if (isWho){
            String who = getText(etConcept);
            if (Util.isOnline(getApplicationContext())){
                (new EditWhoWhyWhat(sketchID, who, strWhy, strWhat)).execute();
            } else {
                toast(Constant.network_error);
            }
        } else if (isWhy){
            String why = getText(etConcept);
            if (Util.isOnline(getApplicationContext())){
                (new EditWhoWhyWhat(sketchID, strWho, why, strWhat)).execute();
            } else {
                toast(Constant.network_error);
            }
        } else if (isWhat){
            String what = getText(etConcept);
            if (Util.isOnline(getApplicationContext())){
                (new EditWhoWhyWhat(sketchID, strWho, strWhy, what)).execute();
            } else {
                toast(Constant.network_error);
            }
        } else if (isScrapBook){
            if (Util.isOnline(getApplicationContext())){
                (new EditScrapbook(sketchID, getText(etConcept))).execute();
            } else {
                toast(Constant.network_error);
            }
        } else {
            if (Util.isOnline(getApplicationContext())){
                (new EditSketchIdea(sketchID, getText(etWorkingTitle), getText(etTheIdea))).execute();
            } else {
                toast(Constant.network_error);
            }
        }

        isWho = false;
        isWhy = false;
        isWhat = false;
        isScrapBook = false;
    }

    @OnClick(R.id.rlWho)
    public void onWho(View view){
        viewDetail.setVisibility(View.GONE);
        viewEdit.setVisibility(View.GONE);
        viewConcept.setVisibility(View.VISIBLE);
        rlEdit.setVisibility(View.GONE);
        rlSave.setVisibility(View.VISIBLE);
        rlHome.setVisibility(View.GONE);
        rlCancel.setVisibility(View.VISIBLE);

        isWho = true;
        isWhy = false;
        isWhat = false;
        isScrapBook = false;

        String first = "Edit your ";
        String second = "WHO";
        String third = " target: Describe the customer or consumer SEGMENT you would be targeting with your product: Who would be using your product? Retailers, Consumers, B2B...";
        String str = first + second + third;
        Spannable spannable = new SpannableString(str);

        spannable.setSpan(new ForegroundColorSpan(Color.rgb(0, 136, 43)), first.length(), (first + second).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvEditTitle.setText(spannable, TextView.BufferType.SPANNABLE);

        etConcept.setText(strWho);
    }

    @OnClick(R.id.rlWhy)
    public void onWhy(View view){
        viewDetail.setVisibility(View.GONE);
        viewEdit.setVisibility(View.GONE);
        viewConcept.setVisibility(View.VISIBLE);
        rlEdit.setVisibility(View.GONE);
        rlSave.setVisibility(View.VISIBLE);
        rlHome.setVisibility(View.GONE);
        rlCancel.setVisibility(View.VISIBLE);

        isWho = false;
        isWhy = true;
        isWhat = false;
        isScrapBook = false;

        String first = "Edit your ";
        String second = "WHY";
        String third = " target: Describe the NEED of the customer/consumer in ther segment you would be targeting. I.e. towards which need will your product/service be playing against?";
        String str = first + second + third;
        Spannable spannable = new SpannableString(str);

        spannable.setSpan(new ForegroundColorSpan(Color.rgb(0, 136, 43)), first.length(), (first + second).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvEditTitle.setText(spannable, TextView.BufferType.SPANNABLE);

        etConcept.setText(strWhy);
    }

    @OnClick(R.id.rlWhat)
    public void onWhat(View view){
        viewDetail.setVisibility(View.GONE);
        viewEdit.setVisibility(View.GONE);
        viewConcept.setVisibility(View.VISIBLE);
        rlEdit.setVisibility(View.GONE);
        rlSave.setVisibility(View.VISIBLE);
        rlHome.setVisibility(View.GONE);
        rlCancel.setVisibility(View.VISIBLE);

        isWho = false;
        isWhy = false;
        isWhat = true;
        isScrapBook = false;

        String first = "Edit your ";
        String second = "WHY";
        String third = " target: What is the SOLUTION or BENEFIT you will be offering with your product/service?";
        String str = first + second + third;
        Spannable spannable = new SpannableString(str);

        spannable.setSpan(new ForegroundColorSpan(Color.rgb(0, 136, 43)), first.length(), (first + second).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvEditTitle.setText(spannable, TextView.BufferType.SPANNABLE);

        etConcept.setText(strWhat);
    }

    @OnClick(R.id.rlScrapBook)
    public void onScrapBook(View view){
        viewDetail.setVisibility(View.GONE);
        viewEdit.setVisibility(View.GONE);
        viewConcept.setVisibility(View.VISIBLE);
        rlEdit.setVisibility(View.GONE);
        rlSave.setVisibility(View.VISIBLE);
        rlHome.setVisibility(View.GONE);
        rlCancel.setVisibility(View.VISIBLE);

        isWho = false;
        isWhy = false;
        isWhat = false;
        isScrapBook = true;

        tvEditTitle.setText("Put your thoughts down on a piece of digital paper! I.e. things you should do, follow-ups of your Sketch and much more!...");

        (new SketchScrapbook(sketchID)).execute();
    }

    private void fetchSketchData(){
        isWho = false;
        isWhy = false;
        isWhat = false;
        isScrapBook = false;

        if (progressDialog == null)
            progressDialog = ProgressDialog.show(ConceptActivity.this, null, null, true, false);

        if (Util.isOnline(getApplicationContext())){
            (new SketchDetail(sketchID)).execute();
        } else {
            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }
            toast("Please check your internet connectivity!");
        }
    }

    class SketchDetail extends AsyncTask<Void, String, String> {

        String sketchID;
        String responseString;

        public SketchDetail(String sketchID){
            this.sketchID = sketchID;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params){
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">\n"+
                "<soapenv:Header/>\n"+
                "<soapenv:Body>\n"+
                "<urn:SketchDetail soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"+
                "<sketch_id xsi:type=\"xsd:string\">%s</sketch_id>\n"+
                "</urn:SketchDetail>\n"+
                "</soapenv:Body>\n"+
                "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, sketchID);

            HttpPost httpPost = new HttpPost(Constant.URL);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("SketchDetail:", responseString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return responseString;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try{
                JSONObject jObj = new JSONObject(result);
                boolean status = jObj.optBoolean("Status");
                if (status){
                    strTitle = jObj.optString("Title");
                    strDescriptoin = jObj.optString("OrigionalIdea");

                    tvTitle.setText(strTitle);
                    tvDescription.setText(strDescriptoin);

                    (new SketchConcept(sketchID)).execute();
                } else {
                    if (progressDialog != null) {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                    }
                    String message = jObj.optString("Message");
                    toast(message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class SketchConcept extends AsyncTask<Void, String, String> {
        String sketchID;
        String responseString;

        public SketchConcept(String sketchID){
            this.sketchID = sketchID;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params){
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">\n"+
                    "<soapenv:Header/>\n"+
                    "<soapenv:Body>\n"+
                    "<urn:SketchConcept soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"+
                    "<sketch_id xsi:type=\"xsd:string\">%s</sketch_id>\n"+
                    "</urn:SketchConcept>\n"+
                    "</soapenv:Body>\n"+
                    "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, sketchID);

            HttpPost httpPost = new HttpPost(Constant.URL);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("SketchConcept:", responseString);
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
                if (status){
                    strTitle = jObj.optString("Title");
                    //String conceptID = jObj.optString("conceptID");
                    strWho = jObj.optString("Who");
                    strWhy = jObj.optString("Why");
                    strWhat = jObj.optString("What");

                    tvWho.setText(strWho);
                    tvWhy.setText(strWhy);
                    tvWhat.setText(strWhat);

                } else {
                    String message = jObj.optString("Message");
                    AlertDialog alertDialog = new AlertDialog.Builder(ConceptActivity.this).create();
                    alertDialog.setTitle(message);
                    alertDialog.setMessage("Click on WHO, WHY, and WHAT to start working on the concept.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            showSketchData();
        }
    }

    class EditSketchIdea extends AsyncTask<Void, String, String> {
        String sketchID;
        String responseString;
        String userID;
        String sketch_title;
        String sketch_idea;

        public EditSketchIdea(String sketchID, String sketch_title, String sketch_idea){
            this.sketchID = sketchID;
            this.sketch_title = sketch_title;
            this.sketch_idea = sketch_idea;
            this.userID = read(Constant.SHARED_KEY.Key_UserID);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            if (progressDialog == null)
                progressDialog = ProgressDialog.show(ConceptActivity.this, null, null, true, false);
            else
                progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params){
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">"+
                    "<soapenv:Header/>"+
                    "<soapenv:Body>"+
                    "<urn:editSketch soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"+
                    "<sketch_id xsi:type=\"xsd:string\">%s</sketch_id>"+
                    "<owner_id xsi:type=\"xsd:string\">%s</owner_id>"+
                    "<sketch_title xsi:type=\"xsd:string\">%s</sketch_title>"+
                    "<original_idea xsi:type=\"xsd:string\">%s</original_idea>"+
                    "<isPublic xsi:type=\"xsd:string\">%s</isPublic>"+
                    "</urn:editSketch>"+
                    "</soapenv:Body>"+
                    "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, sketchID, userID, sketch_title, sketch_idea, "1");

            HttpPost httpPost = new HttpPost(Constant.URL);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("EditSketchIdea:", responseString);
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
                    strTitle = sketch_title;
                    strDescriptoin = sketch_idea;
                    showSketchData();
                } else {
                    toast(message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class EditWhoWhyWhat extends AsyncTask<Void, String, String> {
        String sketchID;
        String who;
        String why;
        String what;

        String responseString;

        public EditWhoWhyWhat(String sketchID, String who, String why, String what){
            this.sketchID = sketchID;
            this.who = urlEncode(who);
            this.why = urlEncode(why);
            this.what = urlEncode(what);
        }

        private String urlEncode(String str){
            String[] a = {"fds"};
            String[] escapeChars = {";", "/" , "?" , ":" ,
                    "@" , "&", "=", "+" ,
                    "$" , ",", "[" , "]",
                    "#", "!", "'", "(",
                    ")", "*", " ", "{", "}", "<", ">", "\""};


            String[] replaceChars = {"%3B", "%2F", "%3F",
                    "%3A", "%40" , "%26" ,
                    "%3D", "%2B" , "%24" ,
                    "%2C", "%5B" , "%5D",
                    "%23", "%21", "%27",
                    "%28", "%29", "%2A", "+", "%7B", "%7D", "%3C", "%3E", "%22"};

            int len = escapeChars.length;

            String temp = str;

            int i;
            for(i = 0; i < len; i++)
            {
		        temp.replace(escapeChars[i], replaceChars[i]);
            }

            String result = temp;
            return result;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            if (progressDialog == null)
                progressDialog = ProgressDialog.show(ConceptActivity.this, null, null, true, false);
            else
                progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params){
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">\n"+
            "<soapenv:Header/>\n"+
            "<soapenv:Body>\n"+
            "<urn:EditConcept soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"+
            "<sketch_id xsi:type=\"xsd:string\">%s</sketch_id>\n"+
            "<text_target xsi:type=\"xsd:string\">%s</text_target>\n"+
            "<text_benefit xsi:type=\"xsd:string\">%s</text_benefit>\n"+
            "<text_pain xsi:type=\"xsd:string\">%s</text_pain>\n"+
            "</urn:EditConcept>\n"+
            "</soapenv:Body>\n"+
            "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, sketchID, who, why, what);

            HttpPost httpPost = new HttpPost(Constant.URL);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("EditSketchIdea:", responseString);
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
                    strWho = this.who;
                    strWhy = this.why;
                    strWhat = this.what;

                    showSketchData();
                } else {
                    toast(message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class SketchScrapbook extends AsyncTask<Void, String, String> {
        String sketchID;

        String responseString;

        public SketchScrapbook(String sketchID){
            this.sketchID = sketchID;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            if (progressDialog == null)
                progressDialog = ProgressDialog.show(ConceptActivity.this, null, null, true, false);
            else
                progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params){
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">\n"+
                    "<soapenv:Header/>\n"+
                    "<soapenv:Body>\n"+
                    "<urn:SketchScrapbook soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"+
                    "<sketch_id xsi:type=\"xsd:string\">%s</sketch_id>\n"+
                    "</urn:SketchScrapbook>\n"+
                    "</soapenv:Body>\n"+
                    "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, sketchID);

            HttpPost httpPost = new HttpPost(Constant.URL);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("SketchScrapbook:", responseString);
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
                    //String scrapbookID = jObj.optString("scrapbookID");
                    String scrapBook = jObj.optString("Scrapbook");
                    etConcept.setText(scrapBook);

                } else {
                    toast(message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class EditScrapbook extends AsyncTask<Void, String, String> {
        String sketchID;
        String scrap;
        String responseString;

        public EditScrapbook(String sketchID, String scrap){
            this.sketchID = sketchID;
            this.scrap = urlEncode(scrap);
        }

        private String urlEncode(String str){
            String[] a = {"fds"};
            String[] escapeChars = {";", "/" , "?" , ":" ,
                    "@" , "&", "=", "+" ,
                    "$" , ",", "[" , "]",
                    "#", "!", "'", "(",
                    ")", "*", " ", "{", "}", "<", ">", "\""};


            String[] replaceChars = {"%3B", "%2F", "%3F",
                    "%3A", "%40" , "%26" ,
                    "%3D", "%2B" , "%24" ,
                    "%2C", "%5B" , "%5D",
                    "%23", "%21", "%27",
                    "%28", "%29", "%2A", "+", "%7B", "%7D", "%3C", "%3E", "%22"};

            int len = escapeChars.length;

            String temp = str;

            int i;
            for(i = 0; i < len; i++)
            {
                temp.replace(escapeChars[i], replaceChars[i]);
            }

            String result = temp;
            return result;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            if (progressDialog == null)
                progressDialog = ProgressDialog.show(ConceptActivity.this, null, null, true, false);
            else
                progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params){
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">\n"+
                        "<soapenv:Header/>\n"+
                        "<soapenv:Body>\n"+
                        "<urn:EditScrapbook soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"+
                        "<sketch_id xsi:type=\"xsd:string\">%s</sketch_id>\n"+
                        "<text_scrapbook xsi:type=\"xsd:string\">%s</text_scrapbook>\n"+
                        "</urn:EditScrapbook>\n"+
                        "</soapenv:Body>\n"+
                        "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, sketchID, scrap);

            HttpPost httpPost = new HttpPost(Constant.URL);
            StringEntity entity;

            try {
                entity = new StringEntity(soapmessage, HTTP.UTF_8);
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                responseString = EntityUtils.toString(response.getEntity());
                Log.d("EditScrapbook:", responseString);
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
                    strScrapBook = scrap;
                    showSketchData();
                } else {
                    toast(message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
