package gmedia.net.id.restauranttakingorder.Order;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import gmedia.net.id.restauranttakingorder.Order.Adapter.ListMejaAdapter;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class MainOpenOrder extends Fragment {

    private Context context;
    private View layout;
    private RecyclerView rvMeja;
    private ProgressBar pbLoad;
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();
    private List<CustomItem> listMeja;
    private ServerURL serverURL;
    private RelativeLayout rlRefresh;
    private Button btnRefresh;
    private TextView tvTitle;

    public MainOpenOrder() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_main_open_order, container, false);
        context = getContext();
        serverURL = new ServerURL(context);
        initUI();
        return layout;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

        }
    }

    private void initUI() {

        tvTitle = (TextView) layout.findViewById(R.id.tv_title1);
        rvMeja = (RecyclerView) layout.findViewById(R.id.rv_list_meja);
        pbLoad = (ProgressBar) layout.findViewById(R.id.pb_load_meja);
        rlRefresh = (RelativeLayout) layout.findViewById(R.id.rl_refresh_container);
        btnRefresh = (Button) layout.findViewById(R.id.btn_refresh);

        session = new SessionManager(context);
        tvTitle.setText(session.getUserInfo(SessionManager.TAG_NAMA));
        listMeja = new ArrayList<>();
        //getData();

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rlRefresh.setVisibility(View.GONE);
                getData();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        getData();
    }

    private void getData() {

        pbLoad.setVisibility(View.VISIBLE);
        listMeja = new ArrayList<>();

        ApiVolley request = new ApiVolley(context, new JSONObject(), "GET", serverURL.getMeja(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");

                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listMeja.add(new CustomItem(jo.getString("kdmeja"), jo.getString("nmmeja"),jo.getString("Status"),jo.getString("flag"),jo.getString("nobukti"),jo.getString("total")));
                        }

                        setMejaTable(listMeja);
                        pbLoad.setVisibility(View.GONE);
                    }else{
                        setMejaTable(null);
                        pbLoad.setVisibility(View.GONE);
                        rlRefresh.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    setMejaTable(null);
                    pbLoad.setVisibility(View.GONE);
                    rlRefresh.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String result) {
                setMejaTable(null);
                pbLoad.setVisibility(View.GONE);
                rlRefresh.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setMejaTable(List<CustomItem> listItem) {

        rvMeja.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            final ListMejaAdapter menuAdapter = new ListMejaAdapter(context, listItem);

            boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
            if(!tabletSize){
                final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 2);
                rvMeja.setLayoutManager(mLayoutManager);
            }else{
                final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 3);
                rvMeja.setLayoutManager(mLayoutManager);

            }
//        rvListMenu.addItemDecoration(new NavMenu.GridSpacingItemDecoration(2, dpToPx(10), true));
            rvMeja.setItemAnimator(new DefaultItemAnimator());
            rvMeja.setAdapter(menuAdapter);

        }
    }
}
