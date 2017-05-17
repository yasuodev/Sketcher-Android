package org.startup.sketcher.fragment;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.startup.sketcher.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutFragment extends Fragment {

    public static AboutFragment newInstance() {
        AboutFragment aboutFragment = new AboutFragment();
        return aboutFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @OnClick(R.id.rlInfoEmail)
    public void sendEmail(View view) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"info@startupsketcher.org"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact from Startup Sketcher Android app");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Nice to See you!");
            emailIntent.setType("message/rfc822");
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            toast("Sorry, no email client found :(");
        }
    }

    protected void toast(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
}
