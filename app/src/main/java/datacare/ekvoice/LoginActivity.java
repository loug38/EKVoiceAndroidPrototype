package datacare.ekvoice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by kchinnap on 3/1/2016.
 * A simple login activity,Because we don't have access to any way of actual user authentication we
 * simply have the login button do a login without checking credentials.
 */
public class LoginActivity extends Activity{

    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);
        setTheme(R.style.AppTheme_Login);
        setContentView(R.layout.login_layout);
    }

    public void login_event(View v) {
        Intent intent = new Intent(this, CaseList.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }
}
