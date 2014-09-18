package com.carles.jogging.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.carles.jogging.BaseActivity;
import com.carles.jogging.R;
import com.carles.jogging.main.MainActivity;
import com.carles.jogging.model.JoggingSQLiteHelper;
import com.carles.jogging.model.UserModel;
import com.carles.jogging.util.PrefUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by carles1 on 11/09/14.
 */
public class LoginActivity extends BaseActivity {

    private static final int MIN_WINDOW_VISIBLE_SPACE_TO_SHOW_LOGO = 400;
    
    private Context ctx;

    private ViewGroup lytRoot;
    private TextView txtAppName;
    private ImageView imgLogo;
    private ImageView imgUsername;
    private EditText txtUsername;
    private ImageView imgPassword;
    private EditText txtPassword;
    private TextView txtError;
    private Button btnLogin;
    private Button btnNewUser;
    private Button btnLoginWithUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PrefUtil.getLoggedUser(this) == null) {
            setContentView(R.layout.activity_login);
            ctx = this;

            lytRoot = (ViewGroup) findViewById(R.id.lyt_root);
            imgLogo = (ImageView) findViewById(R.id.img_logo);
            txtAppName = (TextView) findViewById(R.id.txt_app_name);
            imgUsername = (ImageView) findViewById(R.id.img_username);
            txtUsername = (EditText) findViewById(R.id.txt_username);
            imgPassword = (ImageView) findViewById(R.id.img_password);
            txtPassword = (EditText) findViewById(R.id.txt_password);
            txtError = (TextView) findViewById(R.id.txt_error);
            btnLogin = (Button) findViewById(R.id.btn_login);
            btnNewUser = (Button) findViewById(R.id.btn_new_user);
            btnLoginWithUsername = (Button) findViewById(R.id.btn_login_with_username);

            txtUsername.addTextChangedListener(new LoginTextWatcher());
            txtPassword.addTextChangedListener(new LoginTextWatcher());

            // perform changes in the view if keyboard is shown
            lytRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // int heightDiff = lytRoot.getRootView().getHeight() - lytRoot.getHeight();
                    //  if (heightDiff > 100) {
                    //      // if more than 100 pixels (its probably a keyboard) and
                    //      imgLogo.setVisibility(View.GONE);
                    //  } else {
                    //      imgLogo.setVisibility(View.VISIBLE);
                    //  }

                    Rect r = new Rect();
                    View rootview = LoginActivity.this.getWindow().getDecorView();
                    rootview.getWindowVisibleDisplayFrame(r);
                    if (r.height() < MIN_WINDOW_VISIBLE_SPACE_TO_SHOW_LOGO) {
                        // not much space available, hide the logo
                        imgLogo.setVisibility(View.GONE);

                    } else {
                        imgLogo.setVisibility(View.VISIBLE);
                    }
                }
            });

            // set and EditorActionListener to perform and action when key pressed, ie
            // EditorInfo.IME_NULL, EditorInfo.IME_ACTION_SEND, or action in imeOptions attr
            // txtPassword.setOnEditorActionListener(...)

        } else {
            startMainActivity();
        }
    }

    public void actionLogin(final View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);

        UserModel user = JoggingSQLiteHelper.getInstance(ctx).queryUser(txtUsername.getText().toString(), txtPassword.getText().toString());

        if (user != null) {
            PrefUtil.setLoggedUser(ctx, user);
            startMainActivity();

        } else {
            txtError.setText(R.string.login_error);
            txtError.setVisibility(View.VISIBLE);
            imgUsername.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            imgPassword.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public void actionNewUser(final View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);

        long id;
        UserModel user = new UserModel();
        user.setName(txtUsername.getText().toString());
        // user.setPassword(txtPassword.getText().toString());
        user.setPassword("");
        user.setEmail("");
        id = JoggingSQLiteHelper.getInstance(ctx).insertUser(user);

        if (id != -1) {
            PrefUtil.setLoggedUser(ctx, user);
            startMainActivity();

        } else {
            txtError.setText(R.string.login_error_new_user);
            txtError.setVisibility(View.VISIBLE);
            imgUsername.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            imgPassword.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public void actionLoginWithUsername(final View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);

        // UserModel user = JoggingSQLiteHelper.getInstance(ctx).queryUser(txtUsername.getText().toString(), txtPassword.getText().toString());
        UserModel user = JoggingSQLiteHelper.getInstance(ctx).queryUser(txtUsername.getText().toString(), "");
        if (user == null) {
            // new user
            user = new UserModel();
            user.setName(txtUsername.getText().toString());
            user.setPassword("");
            user.setEmail("");

            if (JoggingSQLiteHelper.getInstance(ctx).insertUser(user) == -1) {
                // this should never happen
                txtError.setText(R.string.login_error_unknown);
                txtError.setVisibility(View.VISIBLE);
                imgUsername.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                return;
            }

        } else {
            PrefUtil.setLoggedUser(ctx, user);
            startMainActivity();
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_activity_to_left_in, R.anim.slide_activity_to_left_out);
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    class LoginTextWatcher implements TextWatcher {

        public LoginTextWatcher() { }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

        @Override
        public void onTextChanged(CharSequence s, int i, int i2, int i3) {
            boolean usernameIsEmpty = StringUtils.isBlank(txtUsername.getText().toString());
            boolean passwordIsEmpty = StringUtils.isBlank(txtPassword.getText().toString());

            // change image color depending on if there's text or not
            if (usernameIsEmpty) {
                imgUsername.setColorFilter(0, PorterDuff.Mode.SRC_ATOP);
            } else {
                imgUsername.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
            }
            if (passwordIsEmpty) {
                imgPassword.setColorFilter(0, PorterDuff.Mode.SRC_ATOP);
            } else {
                imgPassword.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
            }

            // disable buttons if a text is empty
            if (usernameIsEmpty /*|| passwordIsEmpty*/) {
                btnLogin.setEnabled(false);
                btnNewUser.setEnabled(false);
                btnLoginWithUsername.setEnabled(false);
            } else {
                btnLogin.setEnabled(true);
                btnNewUser.setEnabled(true);
                btnLoginWithUsername.setEnabled(true);
            }

            // hide validation error text
            txtError.setVisibility(View.GONE);
        }

        @Override
        public void afterTextChanged(Editable editable) { }
    }

}
