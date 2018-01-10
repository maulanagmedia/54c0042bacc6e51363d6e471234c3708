package gmedia.net.id.restauranttakingorder.Order;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.Order.Adapter.ProcessOrderAdapter;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class ReleaseOrder extends AppCompatActivity {

    private ItemValidation iv = new ItemValidation();
    private TabLayout tabLayout;
    private RelativeLayout tabContainer;
    private RelativeLayout tab1;
    private RelativeLayout tab2;
    private String noBukti = "";
    private ListView lvOnProcess, lvRelease;
    private ProgressBar pbOnProcess;
    private ProgressBar pbRelease;
    private List<CustomItem> listMenuProcess;
    private ServerURL serverURL;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_order);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        initUI();
    }

    private void initUI() {

        tabLayout = (TabLayout) findViewById(R.id.tab_top);
        lvOnProcess = (ListView) findViewById(R.id.lv_on_process);
        pbOnProcess = (ProgressBar) findViewById(R.id.pb_on_process);
        lvRelease = (ListView) findViewById(R.id.lv_release);
        pbRelease = (ProgressBar) findViewById(R.id.pb_release);
        tabLayout.addTab(tabLayout.newTab().setText("Belum Keluar").setIcon(R.mipmap.ic_o_order));
        tabLayout.addTab(tabLayout.newTab().setText("Sudah Keluar").setIcon(R.mipmap.ic_o_release));
        serverURL = new ServerURL(ReleaseOrder.this);
        session = new SessionManager(ReleaseOrder.this);

        tabContainer = (RelativeLayout) findViewById(R.id.tab_container);
        tab1 = (RelativeLayout) findViewById(R.id.tab_1);
        tab2 = (RelativeLayout) findViewById(R.id.tab_2);
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            String nama = bundle.getString("nama");
            noBukti = bundle.getString("nobukti");

            if(nama != null && nama.length() > 0){

                getSupportActionBar().setSubtitle("a/n "+nama);
            }

            if(noBukti != null &&  noBukti.length()>0){

                tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {

                        setTabPosition(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });

                TabLayout.Tab tab = tabLayout.getTabAt(0);
                tab.select();
                setTabPosition(0);
            }
        }

    }

    private void setTabPosition(int position){

        if(tab1 != null && tab2 != null){

            switch (position){
                case 0:
                    setTitle("Pesanan Dalam Proses");
                    tab1.setVisibility(View.VISIBLE);
                    tab2.setVisibility(View.GONE);

                    if(noBukti != null  && noBukti.length() >0){
                        getMenuByProcess("0");
                    }

                    break;
                case 1:
                    setTitle("Pesanan Sudah Keluar");
                    tab1.setVisibility(View.GONE);
                    tab2.setVisibility(View.VISIBLE);

                    if(noBukti != null  && noBukti.length() >0){
                        getMenuByProcess("1");
                    }
                    break;
            }
        }
    }

    private void getMenuByProcess(final String flag) {

        pbOnProcess.setVisibility(View.VISIBLE);
        pbRelease.setVisibility(View.VISIBLE);
        listMenuProcess = new ArrayList<>();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nobukti", noBukti);
            jBody.put("flag", flag);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(ReleaseOrder.this, jBody, "POST", serverURL.getOrderProcess(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listMenuProcess = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listMenuProcess.add(new CustomItem(jo.getString("id"), jo.getString("nmbrg"),jo.getString("jml"),jo.getString("jenis_order"), jo.getString("catatan"), jo.getString("lama"), jo.getString("flag")));
                        }

                        pbOnProcess.setVisibility(View.GONE);
                        pbRelease.setVisibility(View.GONE);

                        setMenuTableProcess(listMenuProcess, flag);
                    }else{
                        pbOnProcess.setVisibility(View.GONE);
                        pbRelease.setVisibility(View.GONE);
                        setMenuTableProcess(null, flag);
                    }
                } catch (JSONException e) {
                    pbOnProcess.setVisibility(View.GONE);
                    pbRelease.setVisibility(View.GONE);
                    e.printStackTrace();
                    setMenuTableProcess(null, flag);
                }

            }

            @Override
            public void onError(String result) {
                pbOnProcess.setVisibility(View.GONE);
                pbRelease.setVisibility(View.GONE);
                setMenuTableProcess(null,flag);
            }
        });

    }

    private void setMenuTableProcess(List<CustomItem> listItem, String flag) {

        if(flag.equals("0")){

            lvOnProcess.setAdapter(null);

            if(listItem != null && listItem.size() > 0){

                final ProcessOrderAdapter adapter = new ProcessOrderAdapter(ReleaseOrder.this, listItem, flag);
                lvOnProcess.setAdapter(adapter);
            }
        }else{

            lvRelease.setAdapter(null);

            if(listItem != null && listItem.size() > 0){

                final ProcessOrderAdapter adapter = new ProcessOrderAdapter(ReleaseOrder.this, listItem, flag);
                lvRelease.setAdapter(adapter);
            }
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
