package com.carles.jogging.login;

import android.content.Intent;
import android.os.Bundle;

import com.carles.jogging.BaseActivity;
import com.carles.jogging.R;
import com.carles.jogging.main.MainActivity;
import com.carles.jogging.util.PrefUtil;

/**
 * Created by carles1 on 11/09/14.
 */
public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PrefUtil.getLoggedUser(this)==null) {
            setContentView(R.layout.activity_login);


        } else {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
