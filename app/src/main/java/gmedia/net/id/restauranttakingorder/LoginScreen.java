package gmedia.net.id.restauranttakingorder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class LoginScreen extends AppCompatActivity {


    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;

    private EditText edtUsername;
    private EditText edtPassword;
    private Button btnLogIn;
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

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

        doLogin(edtUsername.getText().toString(),edtPassword.getText().toString());
    }

    private void doLogin(final String username, final String password) {

        final ProgressDialog progressDialog = new ProgressDialog(LoginScreen.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("username", username);
            jBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(LoginScreen.this, jBody, "POST", ServerURL.login, "", "", 0, username, password, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        progressDialog.dismiss();
                        JSONObject jo = response.getJSONObject("response");
                        session.createLoginSession(jo.getString("nik"),jo.getString("nik"),jo.getString("nama"), username, password, "1","");
                        Toast.makeText(LoginScreen.this, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    }
                    Toast.makeText(LoginScreen.this, message, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginScreen.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(LoginScreen.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
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
