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
import android.widget.CalendarView;
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

import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.UpsellingAndSoldout.Adapter.UpsellingMenuAdapter;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class ListUpsellingMenu extends AppCompatActivity {

    private EditText edtUpselling;
    private ListView lvUpselling;
    private ProgressBar pbUpselling;
    private ItemValidation iv = new ItemValidation();
    private SessionManager session;
    private List<CustomItem> listMenu;
    private Context context;
    private ServerURL serverURL;
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_upselling_menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        context = this;

        setTitle("Upselling Menu");

        initUI();
    }

    private void initUI() {

        edtUpselling = (EditText) findViewById(R.id.edt_upselling);
        lvUpselling = (ListView) findViewById(R.id.lv_upselling);
        pbUpselling = (ProgressBar) findViewById(R.id.pb_upselling);

        session = new SessionManager(context);
        serverURL = new ServerURL(context);
        firstLoad = true;

        edtUpselling.setText("");
        getUpsellingMenu();
    }

    private void getUpsellingMenu(){

        pbUpselling.setVisibility(View.VISIBLE);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", session.getNik());
            jBody.put("keyword", edtUpselling.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getMenuUpselling(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
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
                            listMenu.add(new CustomItem(jo.getString("kdbrg"), jo.getString("nmbrg"),jo.getString("harga"),jo.getString("link"),jo.getString("sisa")));
                            // id, nama, harga, gambar
                        }

                        pbUpselling.setVisibility(View.GONE);
                        setUpsellingMenuSearch();
                        setUpsellingMenuTable(listMenu);
                    }else{
                        pbUpselling.setVisibility(View.GONE);
                        setUpsellingMenuSearch();
                        setUpsellingMenuTable(null);
                    }
                } catch (JSONException e) {
                    pbUpselling.setVisibility(View.GONE);
                    e.printStackTrace();
                    setUpsellingMenuSearch();
                    setUpsellingMenuTable(null);
                }
                pbUpselling.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {
                pbUpselling.setVisibility(View.GONE);
                setUpsellingMenuSearch();
                setUpsellingMenuTable(null);
            }
        });
    }

    private void setUpsellingMenuSearch(){

        if(firstLoad){

            firstLoad = false;
            edtUpselling.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    getUpsellingMenu();
                }
            });

            edtUpselling.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                    if(i == EditorInfo.IME_ACTION_SEARCH){

                        getUpsellingMenu();
                        iv.hideSoftKey(context);
                        return true;
                    }
                    return false;
                }
            });
        }

    }

    private void setUpsellingMenuTable(List<CustomItem> listItem){

        lvUpselling.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            UpsellingMenuAdapter adapterSoldOut = new UpsellingMenuAdapter((Activity) context, listItem);
            lvUpselling.setAdapter(adapterSoldOut);
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
