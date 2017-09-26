package gmedia.net.id.restauranttakingorder;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maulana.custommodul.SessionManager;

public class LoginScreen extends AppCompatActivity {


    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;

    private EditText edtUsername;
    private EditText edtPassword;
    private Button btnLogIn;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        //Check close statement
        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            if(bundle.getBoolean("exit", false)){
                exitState = true;
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        }

        session = new SessionManager(LoginScreen.this);
        initUI();
    }

    private void initUI() {
        
        edtUsername = (EditText) findViewById(R.id.edt_username);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        btnLogIn = (Button) findViewById(R.id.btn_login);

        checkLogin();

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateLogin();
            }
        });
    }

    private void checkLogin() {

        if(session.isSaved()){
            edtUsername.setText(session.getUserInfo(SessionManager.TAG_USERNAME));
            edtPassword.setText(session.getUserInfo(SessionManager.TAG_PASSWORD));
            validateLogin();
        }
    }

    private void validateLogin() {

        if(edtUsername.getText().length() == 0){

            edtUsername.setError("Username tidak boleh kosong");
            return;
        }else{
            edtUsername.setError(null);
        }

        if(edtPassword.getText().length() == 0){

            edtPassword.setError("Password tidak boleh kosong");
            return;
        }else{
            edtPassword.setError(null);
        }

        session.createLoginSession("123","123",edtUsername.getText().toString(), edtUsername.getText().toString(), edtPassword.getText().toString(), "1","");
        Toast.makeText(LoginScreen.this, getResources().getString(R.string.login_success) + session.getUserInfo(SessionManager.TAG_NAMA), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginScreen.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Origin backstage
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(LoginScreen.this, LoginScreen.class);
            intent.putExtra("exit", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //System.exit(0);
        }

        if(!exitState && !doubleBackToExitPressedOnce){
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.app_exit), Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, timerClose);
    }
}
