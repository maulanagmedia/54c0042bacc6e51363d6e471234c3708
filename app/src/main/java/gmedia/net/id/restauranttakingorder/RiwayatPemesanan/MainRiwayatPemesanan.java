package gmedia.net.id.restauranttakingorder.RiwayatPemesanan;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
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

import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter.ListTransaksiAdapter;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter.MenuByTransaksiAdapter;
import gmedia.net.id.restauranttakingorder.Utils.FormatItem;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class MainRiwayatPemesanan extends Fragment {

    private View layout;
    private Context context;
    private ItemValidation iv = new ItemValidation();
    private EditText edtNoMeja;
    private EditText edtTanggal;
    private Button btnCari;
    private ListView lvTransaksi;
    private TextView tvNamaPelanggan;
    private TextView tvNoNota;
    private TextView tvWaktu;
    private TextView tvNoMeja;
    private RecyclerView rvListMenu;
    private TextView tvTotal;
    private List<CustomItem> listTransaksi, listMenu;
    private ProgressBar pbLoadTransaksi, pbLoadMenu;
    private boolean firstLoad = true;
    private int startIndex = 0, count = 10;
    private boolean isLoading = false;
    private ServerURL serverURL;
    private SessionManager session;
    private View footerList;
    private String TAG = "Rawayat";
    private ListTransaksiAdapter adapterTransaction;

    public MainRiwayatPemesanan() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            layout = inflater.inflate(R.layout.fragment_main_riwayat_pemesanan, container, false);
        }
        else {
            // Portrait
            boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
            if(!tabletSize){
                layout = inflater.inflate(R.layout.fragment_main_riwayat_pemesanan_phone, container, false);
            }else{
                layout = inflater.inflate(R.layout.fragment_main_riwayat_pemesanan, container, false);
            }
        }

        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        edtNoMeja = (EditText) layout.findViewById(R.id.edt_no_meja);
        edtTanggal = (EditText) layout.findViewById(R.id.edt_tanggal);
        btnCari = (Button) layout.findViewById(R.id.btn_cari);
        lvTransaksi = (ListView) layout.findViewById(R.id.lv_transaksi);
        tvNamaPelanggan = (TextView) layout.findViewById(R.id.tv_nama_pelanggan);
        tvNoNota = (TextView) layout.findViewById(R.id.tv_no_nota);
        tvWaktu = (TextView) layout.findViewById(R.id.tv_waktu);
        tvNoMeja = (TextView) layout.findViewById(R.id.tv_no_meja);
        rvListMenu = (RecyclerView) layout.findViewById(R.id.rv_list_menu);
        tvTotal = (TextView) layout.findViewById(R.id.tv_total);
        pbLoadTransaksi = (ProgressBar) layout.findViewById(R.id.pb_load_transaksi);
        pbLoadMenu = (ProgressBar) layout.findViewById(R.id.pb_load_menu);
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);

        listTransaksi = new ArrayList<>();
        listMenu = new ArrayList<>();

        serverURL = new ServerURL(context);
        session = new SessionManager(context);
        isLoading = false;
        getDataTransaksi();
        setEvent();
    }

    private void getDataTransaksi() {

        pbLoadTransaksi.setVisibility(View.VISIBLE);
        listTransaksi = new ArrayList<>();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nomeja", edtNoMeja.getText().toString());
            jBody.put("tgl", iv.ChangeFormatDateString(edtTanggal.getText().toString(), FormatItem.formatDateDisplay, FormatItem.formatDate));
            jBody.put("start_index", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getRiwayatOrder(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listTransaksi.add(new CustomItem(jo.getString("nobukti"), jo.getString("urutan"), jo.getString("pelanggan"), jo.getString("total"), jo.getString("usertgl"), jo.getString("nomeja"), jo.getString("nama"), jo.getString("jml_item")));
                        }
                    }

                    getListTransaksi(listTransaksi);
                    pbLoadTransaksi.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    getListTransaksi(null);
                    pbLoadTransaksi.setVisibility(View.GONE);
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoadTransaksi.setVisibility(View.GONE);
                getListTransaksi(null);
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getListTransaksi(List<CustomItem> listItem) {

        lvTransaksi.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            adapterTransaction = new ListTransaksiAdapter((Activity) context, listItem);
            lvTransaksi.setAdapter(adapterTransaction);

            lvTransaksi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
                    getDetailTransaksi(item);

                }
            });

            lvTransaksi.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                    if(absListView.getLastVisiblePosition() == i2-1 && lvTransaksi.getCount() > (count-1) && !isLoading ){
                        isLoading = true;
                        lvTransaksi.addFooterView(footerList);
                        startIndex += count;
                        getMoreData();
                        Log.i(TAG, "onScroll: last");
                    }
                }
            });
        }
    }

    private void getMoreData() {

        final List<CustomItem> moreList = new ArrayList<>();
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nomeja", edtNoMeja.getText().toString());
            jBody.put("tgl", iv.ChangeFormatDateString(edtTanggal.getText().toString(), FormatItem.formatDateDisplay, FormatItem.formatDate));
            jBody.put("start_index", String.valueOf(startIndex));
            jBody.put("count", String.valueOf(count));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getRiwayatOrder(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            moreList.add(new CustomItem(jo.getString("nobukti"), jo.getString("urutan"), jo.getString("pelanggan"), jo.getString("total"), jo.getString("usertgl"), jo.getString("nomeja"), jo.getString("nama"), jo.getString("jml_item")));
                        }

                        lvTransaksi.removeFooterView(footerList);
                        if(adapterTransaction != null) adapterTransaction.addMoreData(moreList);
                    }
                    isLoading = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    isLoading = false;
                    lvTransaksi.removeFooterView(footerList);
                }
            }

            @Override
            public void onError(String result) {
                isLoading = false;
                lvTransaksi.removeFooterView(footerList);
            }
        });
    }

    private void getDetailTransaksi(CustomItem selectedItem) {

        pbLoadMenu.setVisibility(View.VISIBLE);

        tvNamaPelanggan.setText(selectedItem.getItem3());
        tvNoNota.setText(selectedItem.getItem1());
        tvWaktu.setText(iv.ChangeFormatDateString(selectedItem.getItem5(), FormatItem.formatTimestamp, FormatItem.formatTime));
        tvNoMeja.setText(selectedItem.getItem6());
        tvTotal.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(selectedItem.getItem4())));

        listMenu = new ArrayList<>();
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("nobukti", selectedItem.getItem1());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getDetailRiwayatOrder(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listMenu.add(new CustomItem(jo.getString("id"), jo.getString("nmbrg"), jo.getString("harga"), jo.getString("catatan"), jo.getString("jml"), jo.getString("total")));
                        }
                    }

                    pbLoadMenu.setVisibility(View.GONE);
                    setMenuTable(listMenu);
                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoadMenu.setVisibility(View.GONE);
                    setMenuTable(null);
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                pbLoadMenu.setVisibility(View.GONE);
                setMenuTable(null);
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setMenuTable(List<CustomItem> listItem) {

        rvListMenu.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            final MenuByTransaksiAdapter menuAdapter = new MenuByTransaksiAdapter(context, listItem);

            final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 1);
            rvListMenu.setLayoutManager(mLayoutManager);
//        rvListMenu.addItemDecoration(new NavMenu.GridSpacingItemDecoration(2, dpToPx(10), true));
            rvListMenu.setItemAnimator(new DefaultItemAnimator());
            rvListMenu.setAdapter(menuAdapter);

        }
    }

    private void setEvent() {

        iv.datePickerEvent(context, edtTanggal, "RIGHT", FormatItem.formatDateDisplay, iv.getCurrentDate(FormatItem.formatDateDisplay));

        btnCari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String tanggalCari = edtTanggal.getText().toString();
                if(!iv.isValidFormat(FormatItem.formatDateDisplay, tanggalCari)) {
                    tanggalCari = "";
                    edtTanggal.setText(tanggalCari);
                }

                startIndex = 0;
                getDataTransaksi();
                iv.hideSoftKey(context);
            }
        });
    }

}
