package gmedia.net.id.restauranttakingorder;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.Adapter.AccountAdapter;
import gmedia.net.id.restauranttakingorder.Utils.SavedServerManager;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class AccountActivity extends AppCompatActivity {

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;

    private ServerURL serverURL;
    private SavedServerManager serverManager;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private TextView tvServer;
    private ListView lvAccount;
    private List<CustomItem> masterList;
    private ProgressBar pbLoading;
    private ImageView ivServer;
    private Button btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        /*boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (!tabletSize) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }*/

        serverURL = new ServerURL(AccountActivity.this);
        serverManager = new SavedServerManager(AccountActivity.this);
        setTitle("Log In");
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

        session = new SessionManager(AccountActivity.this);
        initUI();
    }

    private void initUI() {

        tvServer = (TextView) findViewById(R.id.tv_server);
        lvAccount = (ListView) findViewById(R.id.lv_account);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        ivServer = (ImageView) findViewById(R.id.iv_server);
        btnRefresh = (Button) findViewById(R.id.btn_refresh);

        String server = serverManager.getServer();
        if(server.length() > 0){

            tvServer.setText(server);
            getAccountList();
        }else{
            loadChangeServer();
        }

        ivServer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                loadChangeServer();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnRefresh.setVisibility(View.GONE);
                session = new SessionManager(AccountActivity.this);
                String server = serverManager.getServer();
                if(server.length() > 0){

                    tvServer.setText(server);
                    getAccountList();
                }else{
                    loadChangeServer();
                }
            }
        });
    }

    private void getAccountList(){

        pbLoading.setVisibility(View.VISIBLE);
        masterList = new ArrayList<>();
        ApiVolley request = new ApiVolley(AccountActivity.this, new JSONObject(), "GET", serverURL.getAccount(), "", "", 0, "", "", new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        masterList = new ArrayList<>();
                        JSONArray jsonArray = response.getJSONArray("response");

                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("nik"),jo.getString("nama"),jo.getString("bagian"),jo.getString("username")));
                        }

                        setAccountAdapter(masterList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    setAccountAdapter(null);
                    btnRefresh.setVisibility(View.VISIBLE);
                }

                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {
                pbLoading.setVisibility(View.GONE);
                setAccountAdapter(null);
                btnRefresh.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setAccountAdapter(List<CustomItem> listItem) {

        lvAccount.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            AccountAdapter adapter = new AccountAdapter(AccountActivity.this, listItem);
            lvAccount.setAdapter(adapter);

            lvAccount.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);
                    loadPasswordEditor(selectedItem.getItem4());
                }
            });
        }
    }

    //region Change Server
    private void loadChangeServer(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_change_server, null);
        builder.setView(view);
        builder.setTitle("Pengaturan Server");
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

                AlertDialog konfirmasiSimpan = new AlertDialog.Builder(AccountActivity.this)
                        .setTitle("Konfirmasi")
                        .setMessage("Simpan perubahan ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                serverManager.saveLastServer(edtServer.getText().toString());
                                tvServer.setText(edtServer.getText().toString());
                                serverURL = new ServerURL(AccountActivity.this);
                                getAccountList();
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

    private void validateLogin(String username, String password) {

        if(username.length() == 0){

            Toast.makeText(AccountActivity.this, "Username masih kosong", Toast.LENGTH_LONG).show();
            return;
        }

        if(password.length() == 0){

            Toast.makeText(AccountActivity.this, "Pin harap diisi", Toast.LENGTH_LONG).show();
            return;
        }

        doLogin(username, password);
    }

    private void doLogin(final String username, final String password) {

        serverURL = new ServerURL(AccountActivity.this);
        final ProgressDialog progressDialog = new ProgressDialog(AccountActivity.this, R.style.AppTheme_Custom_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("username", username);
            jBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(AccountActivity.this, jBody, "POST", serverURL.login(), "", "", 0, username, password, new ApiVolley.VolleyCallback() {
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
                        Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    }

                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AccountActivity.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(AccountActivity.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    //region Change Server
    private void loadPasswordEditor(final String username){

        final AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_account_password, null);
        builder.setView(view);

        final TextView tvPin = (TextView) view.findViewById(R.id.tv_pin);
        final TextView tv1 = (TextView) view.findViewById(R.id.tv_1);
        final TextView tv2 = (TextView) view.findViewById(R.id.tv_2);
        final TextView tv3 = (TextView) view.findViewById(R.id.tv_3);
        final TextView tv4 = (TextView) view.findViewById(R.id.tv_4);
        final TextView tv5 = (TextView) view.findViewById(R.id.tv_5);
        final TextView tv6 = (TextView) view.findViewById(R.id.tv_6);
        final TextView tv7 = (TextView) view.findViewById(R.id.tv_7);
        final TextView tv8 = (TextView) view.findViewById(R.id.tv_8);
        final TextView tv9 = (TextView) view.findViewById(R.id.tv_9);
        final TextView tv0 = (TextView) view.findViewById(R.id.tv_0);
        final ImageView ivClear = (ImageView) view.findViewById(R.id.iv_clear);
        pinButtonListener(tvPin,tv1);
        pinButtonListener(tvPin,tv2);
        pinButtonListener(tvPin,tv3);
        pinButtonListener(tvPin,tv4);
        pinButtonListener(tvPin,tv5);
        pinButtonListener(tvPin,tv6);
        pinButtonListener(tvPin,tv7);
        pinButtonListener(tvPin,tv8);
        pinButtonListener(tvPin,tv9);
        pinButtonListener(tvPin,tv0);

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(tvPin.getText().length() > 0) tvPin.setText(tvPin.getText().toString().substring(0, tvPin.getText().length()-1));
            }
        });


        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                validateLogin(username, tvPin.getText().toString());
            }
        });


        final Dialog alert = builder.create();

        /*DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        alert.getWindow().setLayout(width,  height);*/
        alert.show();

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private void pinButtonListener(final TextView tvTarget, final TextView tv){

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (tv.getId()){
                    case R.id.tv_1:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"1");
                        break;
                    case R.id.tv_2:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"2");
                        break;
                    case R.id.tv_3:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"3");
                        break;
                    case R.id.tv_4:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"4");
                        break;
                    case R.id.tv_5:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"5");
                        break;
                    case R.id.tv_6:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"6");
                        break;
                    case R.id.tv_7:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"7");
                        break;
                    case R.id.tv_8:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"8");
                        break;
                    case R.id.tv_9:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"9");
                        break;
                    case R.id.tv_0:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"0");
                        break;
                }
            }
        });
    }
    //endregion

    @Override
    public void onBackPressed() {
        // Origin backstage
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(AccountActivity.this, AccountActivity.class);
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
