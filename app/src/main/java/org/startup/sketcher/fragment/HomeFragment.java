package org.startup.sketcher.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.startup.sketcher.ConceptActivity;
import org.startup.sketcher.R;
import org.startup.sketcher.util.Constant;
import org.startup.sketcher.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HomeFragment extends Fragment {

    @InjectView(R.id.lvHome)
    com.baoyz.swipemenulistview.SwipeMenuListView lvHome;
    @InjectView(R.id.etSearch)
    EditText etSearch;

    public static String sort_option = "Most Recent";
    private String sortBy = "";

    PostListAdapter postListAdapter;

    ArrayList<HashMap<String, String>> listData = new ArrayList<HashMap<String, String>>();
    ArrayList<Integer> listIndex = new ArrayList<Integer>();


    public static HomeFragment newInstance(String sortBy) {
        sort_option = sortBy;
        HomeFragment homeFragment = new HomeFragment();
        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                // set item background
                deleteItem.setBackground(R.color.red);
                // set item width
                final float scale = getActivity().getResources().getDisplayMetrics().density;
                int pixel = (int) (90 * scale + 0.5f);
                deleteItem.setWidth(pixel);
                // set a icon
                deleteItem.setTitleColor(Color.WHITE);
                deleteItem.setTitleSize(20);
                deleteItem.setTitle("Delete");

                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        lvHome.setMenuCreator(creator);
        lvHome.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        lvHome.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                (new DeleteSketch(position)).execute();
                return false;
            }
        });

        lvHome.setOnScrollListener(new AbsListView.OnScrollListener() {

            int mLastFirstVissibleItem;
            boolean mIsScrollingUp;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (view.getId() == lvHome.getId()) {
                    final int currentFirstVisibleItem = lvHome.getFirstVisiblePosition();

                    if (currentFirstVisibleItem > mLastFirstVissibleItem) {
                        mIsScrollingUp = false;
                    } else if (currentFirstVisibleItem < mLastFirstVissibleItem) {
                        mIsScrollingUp = true;
                    }

                    mLastFirstVissibleItem = currentFirstVisibleItem;
                }

                if (mIsScrollingUp && listIsAtTop()){

                    mIsScrollingUp = false;
                    if (Util.isOnline(getActivity())) {
                        String userid = Util.ReadSharePreference(getActivity(), Constant.SHARED_KEY.Key_UserID);
                        if (sort_option.equals("Most Recent")){
                            sortBy = "";
                        } else if (sort_option.equals("A-Z")){
                            sortBy = "DESC";
                        } else if (sort_option.equals("Z-A")){
                            sortBy = "ASC";
                        } else if (sort_option.equals("Starred")){
                            sortBy = "STARRED";
                        }

                        (new GetData(userid, sortBy)).execute();
                    } else {
                        toast(Constant.network_error);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        init();
        return view;
    }

    private boolean listIsAtTop() {
        if (lvHome.getChildCount() == 0) return true;
        return lvHome.getChildAt(0).getTop() == 0;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Util.isOnline(getActivity())) {
            String userid = Util.ReadSharePreference(getActivity(), Constant.SHARED_KEY.Key_UserID);
            if (sort_option.equals("Most Recent")){
                sortBy = "";
            } else if (sort_option.equals("A-Z")){
                sortBy = "DESC";
            } else if (sort_option.equals("Z-A")){
                sortBy = "ASC";
            } else if (sort_option.equals("Starred")){
                sortBy = "STARRED";
            }

            (new GetData(userid, sortBy)).execute();
        } else {
            toast(Constant.network_error);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void init(){
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN){
                    hideKeyboard();

                    String searchString = etSearch.getText().toString().trim().toLowerCase();

                    listIndex = new ArrayList<Integer>();

                    if (searchString.length() > 0){
                        for (int i = 0; i < listData.size(); i++){
                            HashMap<String, String> sketchData = listData.get(i);
                            String sketchTitle = sketchData.get("sketchTitle").toLowerCase();
                            if (sketchTitle.contains(searchString)){
                                listIndex.add(i);
                            }
                        }
                    } else {
                        for (int i = 0; i < listData.size(); i++){
                            listIndex.add(i);
                        }
                    }
                    postListAdapter = new PostListAdapter(getActivity(), listIndex);
                    lvHome.setAdapter(postListAdapter);

                }
                return false;
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchString = s.toString().toLowerCase();
                listIndex = new ArrayList<Integer>();

                if (searchString.length() > 0){
                    for (int i = 0; i < listData.size(); i++){
                        HashMap<String, String> sketchData = listData.get(i);
                        String sketchTitle = sketchData.get("sketchTitle").toLowerCase();
                        if (sketchTitle.contains(searchString)){
                            listIndex.add(i);
                        }
                    }
                } else {
                    for (int i = 0; i < listData.size(); i++){
                        listIndex.add(i);
                    }
                }
                postListAdapter = new PostListAdapter(getActivity(), listIndex);
                lvHome.setAdapter(postListAdapter);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    protected void toast(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    protected void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public class PostListAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater = null;
        ArrayList<Integer> locallist;

        public PostListAdapter(Context context, ArrayList<Integer> locallist) {
            this.context = context;
            this.locallist = locallist;

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        private void add(Integer index) {
            locallist.add(index);
            notifyDataSetChanged();
        }

        @Override
        public int getCount(){
            return locallist.size();
        }

        @Override
        public Object getItem(int i){
            return null;
        }

        @Override
        public long getItemId(int i){
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.list_row, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            final Integer selectedIndex = locallist.get(position);

            Log.d("selectedIndex", String.valueOf(selectedIndex));
            final HashMap<String, String> sketchData = listData.get(selectedIndex);
            holder.tvDate.setText(sketchData.get("sketchDate"));
            holder.tvIdea.setText(sketchData.get("sketchTitle"));
            holder.tvDescription.setText(sketchData.get("sketchIdea"));
            if (sketchData.get("starred").equals("1"))
                holder.btnFlag.setBackgroundResource(R.drawable.star_selected);
            else
                holder.btnFlag.setBackgroundResource(R.drawable.star_unselected);

            holder.btnFlag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Util.isOnline(getActivity())){
                        (new Starred(selectedIndex)).execute();
                    }
                }
            });

            holder.viewMainRow.setOnTouchListener(new OnSwipeTouchListener(getActivity()){


                public void onSelect() {
                    Intent intent = new Intent(context, ConceptActivity.class);
                    intent.putExtra("sketchID", sketchData.get("sketchID"));
                    context.startActivity(intent);
                }

                public void onSwipeLeft() {
                    lvHome.smoothOpenMenu(position);
                }

                public void onSwipeRight() {
                    lvHome.smoothCloseMenu();
                }

            });

            return view;
        }
    }

    static class ViewHolder {

        @InjectView(R.id.viewMainRow)
        View viewMainRow;
        @InjectView(R.id.tvDate)
        TextView tvDate;
        @InjectView(R.id.tvIdea)
        TextView tvIdea;
        @InjectView(R.id.tvDescription)
        TextView tvDescription;
        @InjectView(R.id.btnFlag)
        ImageButton btnFlag;
        @InjectView(R.id.btnDetail)
        ImageButton btnDetail;


        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

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
                Log.d("home request: ", responseString);
            } catch (Exception e) {
                Log.d("TEST: ", e.toString());
                e.printStackTrace();
            }

            return responseString;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            listData = new ArrayList<HashMap<String, String>>();
            listIndex = new ArrayList<Integer>();

            try{
                JSONObject jObj = new JSONObject(result);
                boolean status = jObj.optBoolean("Status");

                if (status) {
                    JSONArray jsonArray = jObj.getJSONArray("Sketch");

                    for (int i = jsonArray.length()-1; i >= 0; i--) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<String, String>();

                        hashMap.put("sketchID", jsonObject.optString("sketchID"));
                        hashMap.put("sketchTitle", jsonObject.optString("sketchTitle"));
                        hashMap.put("sketchDate", jsonObject.optString("sketchDate"));
                        hashMap.put("sketchIdea", jsonObject.optString("sketchIdea"));
                        hashMap.put("starred", jsonObject.optString("starred"));

                        listData.add(hashMap);
                        listIndex.add(listData.indexOf(hashMap));
                    }

                } else {
                    String message = jObj.optString("Message");
                    toast(message);
                }

            } catch (Exception e) {

                toast("no result");
                e.printStackTrace();
            }

            if (getActivity() != null){
                postListAdapter = new PostListAdapter(getActivity(), listIndex);
                lvHome.setAdapter(postListAdapter);
            }

        }
    }

    class Starred extends AsyncTask<Void, String, String>{

        ProgressDialog progressDialog;
        String responseString;
        int selectedIndex = 0;

        public Starred(Integer selectedIndex){
            this.selectedIndex = selectedIndex;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            if (getActivity() != null){
                if (progressDialog == null)
                    progressDialog = ProgressDialog.show(getActivity(), null, null, true, false);
            }
        }

        @Override
        protected String doInBackground(Void... params){
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">"+
                "<soapenv:Header/>"+
                "<soapenv:Body>"+
                "<urn:starSketch soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"+
                "<sketch_id xsi:type=\"xsd:string\">%s</sketch_id>"+
                "</urn:starSketch>"+
                "</soapenv:Body>"+
                "</soapenv:Envelope>";

            String sketchID = listData.get(selectedIndex).get("sketchID");
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
                Log.d("starredRequest: ", responseString);
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
                    HashMap<String, String> sketchData = (HashMap<String, String>) listData.get(selectedIndex);
                    if (sketchData.get("starred").equals("1")){
                        sketchData.put("starred", "0");
                    } else {
                        sketchData.put("starred", "1");
                    }
                    listData.set(selectedIndex, sketchData);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (getActivity() != null){

                postListAdapter = new PostListAdapter(getActivity(), listIndex);
                lvHome.setAdapter(postListAdapter);
            }

        }

    }

    class DeleteSketch extends AsyncTask<Void, String, String>{
        ProgressDialog progressDialog;
        String responseString;
        int selectedIndex = 0;

        public DeleteSketch(Integer selectedIndex){
            this.selectedIndex = selectedIndex;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            if (getActivity() != null){
                if (progressDialog == null)
                    progressDialog = ProgressDialog.show(getActivity(), null, null, true, false);
            }
        }

        @Override
        protected String doInBackground(Void... params){
            String envelope = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:quote5\">"+
                        "<soapenv:Header/>"+
                        "<soapenv:Body>"+
                        "<urn:deleteSketch soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"+
                        "<sketch_id xsi:type=\"xsd:string\">%s</sketch_id>"+
                        "</urn:deleteSketch>"+
                        "</soapenv:Body>"+
                        "</soapenv:Envelope>";

            String sketchID = listData.get(selectedIndex).get("sketchID");
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
                Log.d("DeleteRequest: ", responseString);
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
                    listData.remove(this.selectedIndex);
                    etSearch.setText("");

                    listIndex = new ArrayList<Integer>();
                    for (int i = 0; i < listData.size(); i++){
                        listIndex.add(i);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (getActivity() != null){

                postListAdapter = new PostListAdapter(getActivity(), listIndex);
                lvHome.setAdapter(postListAdapter);
            }

        }
    }

    class OnSwipeTouchListener implements OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        private final class GestureListener extends SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 50;

            @Override
            public boolean onDown(MotionEvent e){
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e){
                onSelect();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                        result = true;
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = false;
                    }


                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public boolean onTouch(View v, MotionEvent event) {
            //stuff to do on list item click
            return gestureDetector.onTouchEvent(event);
        }

        public void onSelect(){
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    }
}
