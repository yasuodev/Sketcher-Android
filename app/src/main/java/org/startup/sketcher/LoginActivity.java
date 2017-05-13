package org.startup.sketcher;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etUsername;
    private EditText etPassword;
    private ImageView imgCheck;

    private boolean isRemember = false;

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

        }
    }

    private void forgotPassword() {
        startActivity(ForgotActivity.class);
    }

    private void register() {
        startActivity(RegisterActivity.class);
    }

    private void rememberMe() {
        if (isRemember) {
            imgCheck.setImageResource(R.drawable.unchecked);
        } else {
            imgCheck.setImageResource(R.drawable.checked);
        }

        isRemember = !isRemember;
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
}
