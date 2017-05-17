package org.startup.sketcher;

import android.os.Bundle;
import android.os.Handler;

import org.startup.sketcher.util.Constant;

/**
 * Created by Spring on 5/16/2017.
 */

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        goNextScreen();
    }

    private void goNextScreen() {
        if (read(Constant.SHARED_KEY.Key_IsRememberUser).equals("true")) {
            startActivity(HomeActivity.class);
            finish();
        } else {
            setContentView(R.layout.activity_splash);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(LoginActivity.class);
                    overridePendingTransition(R.anim.hold_bottom, R.anim.fade_out);
                    finish();
                }
            }, 2000);
        }
    }
}
