package gmedia.net.id.restauranttakingorder.UpsellingAndSoldout;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.Order.Adapter.SoldOutMenuAdapter;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class ListSoldOutMenu extends AppCompatActivity {

    private EditText edtSoldOut;
    private ListView lvSoldOut;
    private ProgressBar pbSoldOut;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private List<CustomItem> listMenu;
    private Context context;
    private ServerURL serverURL;
    private boolean firstSoldOutMenuLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sold_out_menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        context = this;

        setTitle("Sold Out Menu");

        initUI();
    }

    private void initUI() {

        edtSoldOut = (EditText) findViewById(R.id.edt_sold_out);
        lvSoldOut = (ListView) findViewById(R.id.lv_sold_out);
        pbSoldOut = (ProgressBar) findViewById(R.id.pb_sold_out);

        session = new SessionManager(context);
        serverURL = new ServerURL(context);
        firstSoldOutMenuLoad = true;

        edtSoldOut.setText("");
        getSoldOutMenu();
    }

    private void getSoldOutMenu(){

        pbSoldOut.setVisibility(View.VISIBLE);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", session.getNik());
            jBody.put("keyword", edtSoldOut.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getMenuSoldOut(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listMenu = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listMenu.add(new CustomItem(jo.getString("kdbrg"), jo.getString("nmbrg"),jo.getString("harga"),jo.getString("link")));
                            // id, nama, harga, gambar
                        }

                        pbSoldOut.setVisibility(View.GONE);
                        setSoldOutMenuSearch();
                        setSoldOutMenuTable(listMenu);
                    }else{
                        pbSoldOut.setVisibility(View.GONE);
                        setSoldOutMenuSearch();
                        setSoldOutMenuTable(null);
                    }
                } catch (JSONException e) {
                    pbSoldOut.setVisibility(View.GONE);
                    e.printStackTrace();
                    setSoldOutMenuSearch();
                    setSoldOutMenuTable(null);
                }
                pbSoldOut.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {
                pbSoldOut.setVisibility(View.GONE);
                setSoldOutMenuSearch();
                setSoldOutMenuTable(null);
            }
        });
    }

    private void setSoldOutMenuSearch(){

        if(firstSoldOutMenuLoad){

            firstSoldOutMenuLoad = false;
            edtSoldOut.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    getSoldOutMenu();
                }
            });

            edtSoldOut.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                    if(i == EditorInfo.IME_ACTION_SEARCH){

                        getSoldOutMenu();
                        iv.hideSoftKey(context);
                        return true;
                    }
                    return false;
                }
            });
        }

    }

    private void setSoldOutMenuTable(List<CustomItem> listItem){

        lvSoldOut.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            SoldOutMenuAdapter adapterSoldOut = new SoldOutMenuAdapter((Activity) context, listItem);
            lvSoldOut.setAdapter(adapterSoldOut);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
