package org.thetale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Andrey.Titov on 10/18/13
 */
public class LoginActivity extends Activity {
    public static final String USER_ID_KEY = "user_id";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
    }

    public void loginButtonPressed(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("user_id", 1);
        startActivity(intent);
    }
}