package org.startup.sketcher.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.startup.sketcher.LoginActivity;
import org.startup.sketcher.R;
import org.startup.sketcher.util.Constant;
import org.startup.sketcher.util.Util;

import butterknife.ButterKnife;

public class HomeFragment extends Fragment {

    private String sort_option = "Most Recent";


    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();
        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Util.isOnline(getActivity())) {
            String userid = Util.ReadSharePreference(getActivity(), Constant.SHARED_KEY.Key_UserID);
            (new GetData(userid, sort_option)).execute();
        } else {
            toast(Constant.network_error);
        }

    }

    protected void toast(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }


    class GetData extends AsyncTask<Void, String, String>{
        ProgressDialog progressDialog;
        String responseString;
        String userid;
        String sortby;

        private GetData(String userid, String sortby){
            this.userid = userid;
            this.sortby = sortby;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            if (getActivity() != null){
                if (progressDialog == null)
                    progressDialog = ProgressDialog.show(getActivity(), null, "Loading...", true, false);
            }
        }

        @Override
        protected String doInBackground(Void... params){
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">\n"+
            "<soapenv:Header/>\n"+
            "<soapenv:Body>\n"+
            "<urn:SketchListing soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"+
            "<owner_id xsi:type=\"xsd:string\">%s</owner_id>\n"+
            "<sort_by xsi:type=\"xsd:string\">%s</sort_by>"+
            "</urn:SketchListing>\n"+
            "</soapenv:Body>\n"+
            "</soapenv:Envelope>";

            String soapmessage = String.format(envelope, userid, sortby);

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

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }


        }
    }
}
