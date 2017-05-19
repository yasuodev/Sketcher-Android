package org.startup.sketcher;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.startup.sketcher.fragment.AboutFragment;
import org.startup.sketcher.fragment.HomeFragment;
import org.startup.sketcher.util.Constant;
import org.startup.sketcher.util.Util;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class HomeActivity extends FragmentActivity {

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.list_slidermenu)
    ListView mDrawerList;
    @InjectView(R.id.tvAboutTitle)
    TextView tvAboutTitle;
    @InjectView(R.id.rlMainTitle)
    View rlMainTitle;
    @InjectView(R.id.tvMainTitle)
    TextView tvMainTitle;
    @InjectView(R.id.rlAddSketch)
    RelativeLayout rlAddSketch;
    @InjectView(R.id.viewPopup)
    View viewPopup;

    FragmentTransaction fragmentTransaction;
    Fragment fragment;

    ArrayList<String> listMenu = new ArrayList<String>();
    public static int selectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        setMenuList();
        displayView(1);
    }


    @OnClick(R.id.rlMenu)
    public void onMenu(View view) {
        hideKeyboard();
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            mDrawerLayout.openDrawer(mDrawerList);
        }
    }

    @OnClick(R.id.rlAddSketch)
    public void addSketch(View view) {
        startActivity(AddSketchActivity.class);
    }

    @OnClick(R.id.rlMainTitle)
    public void showPopup(View view) {
        if (viewPopup.getVisibility() == View.GONE) {
            viewPopup.setVisibility(View.VISIBLE);
        } else {
            viewPopup.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.rlMostRecent)
    public void onMostRecent(View view){
        viewPopup.setVisibility(View.GONE);

        tvMainTitle.setText("Most Recent");
        fragment = HomeFragment.newInstance("Most Recent");
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment).commit();
        }
    }

    @OnClick(R.id.rlAZ)
    public void onAZ(View view) {
        viewPopup.setVisibility(View.GONE);
        tvMainTitle.setText("A-Z");
        fragment = HomeFragment.newInstance("A-Z");
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment).commit();
        }
    }

    @OnClick(R.id.rlZA)
    public void onZA(View view){
        viewPopup.setVisibility(View.GONE);
        tvMainTitle.setText("Z-A");
        fragment = HomeFragment.newInstance("Z-A");
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment).commit();
        }
    }

    @OnClick(R.id.rlStarred)
    public void onStarred(View view){
        viewPopup.setVisibility(View.GONE);

        tvMainTitle.setText("Starred");
        fragment = HomeFragment.newInstance("Starred");
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment).commit();
        }
    }

    private void setMenuList() {
        listMenu.clear();

        listMenu.add("Logout");
        listMenu.add("Home");
        listMenu.add("About");
    }

    private void displayView(int position) {

        if (selectedPosition == position){
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(mDrawerList);
            mDrawerList.setAdapter(new MenuListAdapter(getApplicationContext(), listMenu, position));
            return;
        }

        selectedPosition = position;
        switch (position) {
            case 1:
                rlMainTitle.setVisibility(View.VISIBLE);
                tvMainTitle.setText("Most Recent");
                tvAboutTitle.setVisibility(View.GONE);
                rlAddSketch.setVisibility(View.VISIBLE);

                fragment = HomeFragment.newInstance("Most Recent");
                break;
            case 2:
                rlMainTitle.setVisibility(View.GONE);
                tvAboutTitle.setVisibility(View.VISIBLE);
                rlAddSketch.setVisibility(View.GONE);
                fragment = AboutFragment.newInstance();
                break;
            default:
                fragment = HomeFragment.newInstance("Most Recent");
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();

            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment).commit();

            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
            frameLayout.setVisibility(View.VISIBLE);

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(mDrawerList);
            mDrawerList.setAdapter(new MenuListAdapter(getApplicationContext(), listMenu, position));
        }
    }


    static class ViewHolder {
        @InjectView(R.id.rlMain)
        RelativeLayout rlMain;
        @InjectView(R.id.menu_title)
        TextView menuTitle;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    public class MenuListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater = null;
        ArrayList<String> locallist;
        int selectedPosition;

        public MenuListAdapter(Context context, ArrayList<String> locallist, int selectedPosition) {
            this.mContext = context;
            this.locallist = locallist;
            this.selectedPosition = selectedPosition;
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return locallist.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            ViewHolder holder;

            if (position == 0) {
                view = inflater.inflate(R.layout.menulist_row_logout, null);
            } else {
                view = inflater.inflate(R.layout.menulist_row, null);
            }

            holder = new ViewHolder(view);
            view.setTag(holder);

            if (position == selectedPosition)
                holder.rlMain.setBackgroundColor(getResources().getColor(R.color.gray_selected));
            else
                holder.rlMain.setBackgroundColor(getResources().getColor(R.color.transparent));

            holder.menuTitle.setText(locallist.get(position));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    selectedPosition = position;

                    switch (position) {
                        case 0:
                            logout();
                            break;
                        default:
                            displayView(position);
                            break;
                    }
                }
            });

            return view;
        }

    }


    private void logout() {
        mDrawerList.setItemChecked(0, true);
        mDrawerList.setSelection(0);
        mDrawerLayout.closeDrawer(mDrawerList);
        mDrawerList.setAdapter(new MenuListAdapter(getApplicationContext(), listMenu, 0));

        write(Constant.SHARED_KEY.Key_IsRememberUser, "false");
        write(Constant.SHARED_KEY.Key_UserName, "");
        write(Constant.SHARED_KEY.Key_Password, "");
        write(Constant.SHARED_KEY.Key_UserID, "");

        startActivity(LoginActivity.class);
    }

    protected void write(String key, String val) {
        Util.WriteSharePreference(this, key, val);
    }

    protected void startActivity(Class klass) {
        startActivity(new Intent(this, klass));
    }

    protected void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            super.onBackPressed();
            finish();
        }
    }

}
