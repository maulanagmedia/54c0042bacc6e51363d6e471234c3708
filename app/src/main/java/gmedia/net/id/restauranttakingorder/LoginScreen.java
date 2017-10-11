package gmedia.net.id.restauranttakingorder;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import gmedia.net.id.restauranttakingorder.Utils.SavedServerManager;
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
    private SavedServerManager serverManager;
    private ServerURL serverURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        serverURL = new ServerURL(LoginScreen.this);
        serverManager = new SavedServerManager(LoginScreen.this);
        setTitle("Sign In");

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

        String server = serverManager.getServer();
        if(server.length() > 0){

            setBtnClickOption(server);
        }else{
            loadChangeServer();
        }

    }

    private void setBtnClickOption(String server){
        checkLogin();
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateLogin();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            loadChangeServer();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        serverURL = new ServerURL(LoginScreen.this);
        final ProgressDialog progressDialog = new ProgressDialog(LoginScreen.this, R.style.AppTheme_Custom_Dialog);
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
        ApiVolley request = new ApiVolley(LoginScreen.this, jBody, "POST", serverURL.login(), "", "", 0, username, password, new ApiVolley.VolleyCallback() {
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

    //region Change Server
    private void loadChangeServer(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginScreen.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_change_server, null);
        builder.setView(view);
        builder.setCancelable(false);

        final EditText edtServer = (EditText) view.findViewById(R.id.edt_server);
        final Button btnBatal = (Button) view.findViewById(R.id.btn_batal);
        final Button btnSimpan = (Button) view.findViewById(R.id.btn_simpan);

        String ip = serverManager.getServer();
        if(ip.length() > 0) edtServer.setText(ip);

        final AlertDialog alert = builder.create();
        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.dismiss();
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Validasi
                if(edtServer.getText().toString().length() <= 0){
                    edtServer.setError("Harap diisi");
                    edtServer.requestFocus();
                    return;
                }

                AlertDialog konfirmasiSimpan = new AlertDialog.Builder(LoginScreen.this)
                        .setTitle("Konfirmasi")
                        .setMessage("Simpan perubahan ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                serverManager.saveLastServer(edtServer.getText().toString());
                                setBtnClickOption(edtServer.getText().toString());
                                alert.dismiss();
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();

            }
        });

        alert.show();

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }
    //endregion
}
