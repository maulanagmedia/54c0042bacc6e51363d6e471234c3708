package gmedia.net.id.restauranttakingorder.RiwayatPemesanan;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.Order.Adapter.SummaryAdapter;
import gmedia.net.id.restauranttakingorder.PrinterUtils.ShowMsg;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter.ListChangeMejaAdapter;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter.ListTransaksiAdapter;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter.ListUpsellingAdapter;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter.MenuByTransaksiAdapter;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter.PreCetakAdapter;
import gmedia.net.id.restauranttakingorder.Utils.FormatItem;
import gmedia.net.id.restauranttakingorder.Utils.SavedPrinterManager;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;
import gmedia.net.id.restauranttakingorder.Utils.Status;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MainRiwayatPemesanan extends Fragment implements ReceiveListener {

    private View layout;
    private static Context context;
    private static ItemValidation iv = new ItemValidation();
    private static EditText edtNoMeja;
    private static EditText edtTanggal;
    private Button btnCari;
    private static ListView lvTransaksi;
    private static TextView tvNamaPelanggan;
    private static TextView tvNoNota;
    private static TextView tvWaktu;
    private static TextView tvNoMeja;
    private static RecyclerView rvListMenu;
    private static TextView tvTotal;
    private static List<CustomItem> listTransaksi;
    private static List<CustomItem> listMenu;
    private static ProgressBar pbLoadTransaksi;
    private static ProgressBar pbLoadMenu;
    private boolean firstLoad = true;
    private static int startIndex = 0;
    private static int count = 10;
    private static boolean isLoading = false;
    private static ServerURL serverURL;
    private static SessionManager session;
    private static View footerList;
    private static String TAG = "Rawayat";
    private static ListTransaksiAdapter adapterTransaction;
    private static TextView tvCashierStatus;
    private static TextView tvKitchenStatus;
    private static TextView tvBarStatus;

    //Print
    private static int printState = 0;
    private static boolean printCashierState = false;
    private static boolean printKitchenState = false;
    private static boolean printBarState = false;
    private static String timestampNow = "";
    //private static PrinterTemplate printerTemplate;
    private static ProgressDialog progressDialog;
    private static SavedPrinterManager printerManager;
    private String typeKategori = "";
    public static String katMakanan = "MAKANAN";
    public static String katMinuman = "MINUMAN";
    private static int toastTimer = 2;
    private static String urutan = "";
    private static int maxIterFix = 30;
    private static int maxIter = 6;
    private static int delayTime = 1000;
    private static AlertDialog dialogLoading, upSellingDialog;
    private static Context mContext;
    private Printer mPrinter;
    private static String noBukti = "";
    private static String noMeja = "";
    private static String printNo = "1";
    private boolean printStatus = true;
    public static List<CustomItem> listSelectedMenu, listSelectedMenuPerUpSelling;
    private Button btnPrint;
    private Button btnChangeMeja;
    private List<CustomItem> listMeja;
    public static AlertDialog mejaDialog;
    private String upselling = "";
    private static CustomItem lastSelectedOrder;
    AlertDialog.Builder loadMejaDialog;
    private static TextView tvStatus;

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
        tvStatus = (TextView) layout.findViewById(R.id.tv_status);
        tvNamaPelanggan = (TextView) layout.findViewById(R.id.tv_nama_pelanggan);
        tvNoNota = (TextView) layout.findViewById(R.id.tv_no_nota);
        tvWaktu = (TextView) layout.findViewById(R.id.tv_waktu);
        tvNoMeja = (TextView) layout.findViewById(R.id.tv_no_meja);
        tvCashierStatus = (TextView) layout.findViewById(R.id.tv_cashier_status);
        tvKitchenStatus = (TextView) layout.findViewById(R.id.tv_kitchen_status);
        tvBarStatus = (TextView) layout.findViewById(R.id.tv_bar_status);
        rvListMenu = (RecyclerView) layout.findViewById(R.id.rv_list_menu);
        tvTotal = (TextView) layout.findViewById(R.id.tv_total);
        pbLoadTransaksi = (ProgressBar) layout.findViewById(R.id.pb_load_transaksi);
        pbLoadMenu = (ProgressBar) layout.findViewById(R.id.pb_load_menu);
        LayoutInflater li = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        footerList = li.inflate(R.layout.footer_list, null);
        btnPrint = (Button) layout.findViewById(R.id.btn_print);
        btnChangeMeja = (Button) layout.findViewById(R.id.btn_change_meja);

        listTransaksi = new ArrayList<>();
        listMenu = new ArrayList<>();

        mContext = context;
        startIndex = 0;
        noBukti = "";
        noMeja = "";
        printNo = "1";
        listTransaksi = new ArrayList<>();
        listSelectedMenu = new ArrayList<>();
        serverURL = new ServerURL(context);
        session = new SessionManager(context);
        isLoading = false;
        getDataTransaksi();
        setEvent();
        printerManager = new SavedPrinterManager(context);

        try {
            com.epson.epos2.Log.setLogSettings(mContext, com.epson.epos2.Log.PERIOD_TEMPORARY, com.epson.epos2.Log.OUTPUT_STORAGE, null, 0, 1, com.epson.epos2.Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "setLogSettings", mContext);
        }
    }

    private static void getDataTransaksi() {

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
                    listTransaksi = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listTransaksi.add(new CustomItem(jo.getString("nobukti"), jo.getString("urutan"), jo.getString("pelanggan"), jo.getString("total"), jo.getString("usertgl"), jo.getString("nomeja"), jo.getString("nama"), jo.getString("jml_item"), jo.getString("cashier_status"), jo.getString("kitchen_status"), jo.getString("bar_status"), jo.getString("print_no"), jo.getString("status")));
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

    private static void getListTransaksi(List<CustomItem> listItem) {

        lvTransaksi.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            adapterTransaction = new ListTransaksiAdapter((Activity) context, listItem);
            lvTransaksi.setAdapter(adapterTransaction);

            lvTransaksi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
                    lastSelectedOrder = item;
                    getDetailTransaksi(lastSelectedOrder);
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

    private static void getMoreData() {

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
                            moreList.add(new CustomItem(jo.getString("nobukti"), jo.getString("urutan"), jo.getString("pelanggan"), jo.getString("total"), jo.getString("usertgl"), jo.getString("nomeja"), jo.getString("nama"), jo.getString("jml_item"), jo.getString("cashier_status"), jo.getString("kitchen_status"), jo.getString("bar_status"), jo.getString("print_no"), jo.getString("status")));
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

    private static void getDetailTransaksi(CustomItem selectedItem) {

        pbLoadMenu.setVisibility(View.VISIBLE);

        tvStatus.setText(Status.getPenjualanStatus(selectedItem.getItem13()));
        tvNamaPelanggan.setText(selectedItem.getItem3());
        tvNoNota.setText(selectedItem.getItem1());
        tvWaktu.setText(iv.ChangeFormatDateString(selectedItem.getItem5(), FormatItem.formatTimestamp, FormatItem.formatTime));
        tvNoMeja.setText(selectedItem.getItem6());
        tvCashierStatus.setText((selectedItem.getItem9().equals("1")) ? "Tercetak" : "Tidak Tercetak");
        tvKitchenStatus.setText((selectedItem.getItem10().equals("1")) ? "Tercetak" : "Tidak Tercetak");
        tvBarStatus.setText((selectedItem.getItem11().equals("1")) ? "Tercetak" : "Tidak Tercetak");
        tvTotal.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(selectedItem.getItem4())));
        noBukti = selectedItem.getItem1();
        noMeja = selectedItem.getItem6();
        printNo = String.valueOf(iv.parseNullInteger(selectedItem.getItem12()) + 1);
        urutan = selectedItem.getItem2();

        listMenu = new ArrayList<>();
        listSelectedMenu = new ArrayList<>();
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
                    String upselling = "";
                    listMenu = new ArrayList<>();
                    listSelectedMenu = new ArrayList<>();
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            if(!upselling.equals(jo.getString("upselling"))){
                                listMenu.add(new CustomItem("H", jo.getString("username") ,jo.getString("upselling")));
                                upselling = jo.getString("upselling");
                            }

                            listMenu.add(new CustomItem(jo.getString("id"), jo.getString("nmbrg"), jo.getString("harga"), jo.getString("catatan"), jo.getString("jml"), jo.getString("total"),jo.getString("print_menu"),jo.getString("print_rekap")));
                            listSelectedMenu.add(new CustomItem(jo.getString("kdbrg"), jo.getString("nmbrg"),jo.getString("harga"),jo.getString("link"),jo.getString("jml"),jo.getString("satuan"),jo.getString("diskon"),jo.getString("catatan"),jo.getString("harga_diskon"),jo.getString("tag_meja"),jo.getString("type"),jo.getString("upselling"),jo.getString("print_menu"),jo.getString("print_rekap"), "1"));
                            // 1. id, 2. nama, 3. harga, 4. gambar,  5. banyak, 6. satuan, 7. diskon, 8. catatan, 9. hargaDiskon, 10. tag meja, 11. type, 12. upselling, 13. print menu, 14. print rekap, 15. flag cetak
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

    private static void setMenuTable(List<CustomItem> listItem) {

        rvListMenu.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            // 1. id, 2. nama, 3. harga, 4. gambar,  5. banyak, 6. satuan, 7. diskon, 8. catatan,
            // 9. hargaDiskon, 10. tag meja, 11. type, 12. upselling, 13. print menu, 14. print rekap
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

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Validasi
                if(noBukti.length() == 0){
                    Toast.makeText(context, "Silahkan pilih pesanan", Toast.LENGTH_LONG).show();
                    return;
                }

                if(listSelectedMenu == null || listSelectedMenu.size() == 0){
                    Toast.makeText(context, "Silahkan pilih pesanan", Toast.LENGTH_LONG).show();
                    return;
                }
                
                loadUpsellingDialog();

                //loadPrintingDialog();
            }
        });

        btnChangeMeja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Validasi
                if(noBukti.length() == 0){
                    Toast.makeText(context, "Silahkan pilih pesanan", Toast.LENGTH_LONG).show();
                    return;
                }

                getDataMeja();
            }
        });
    }

    private void loadUpsellingDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_summary, null);
        builder.setTitle("Pilih order yang ingin dicetak");
        builder.setView(view);

        final TextView tvSummary = (TextView) view.findViewById(R.id.tv_summary);
        final ListView lvSummary = (ListView) view.findViewById(R.id.lv_summary);

        tvSummary.setText("Tekan pada list untuk memilih item yang akan dicetak");
        List<CustomItem> listUpselling = new ArrayList<>();

        for(CustomItem item: listMenu){
            if(item.getItem1().equals("H")) listUpselling.add(item);
        }

        lvSummary.setAdapter(null);

        if(listUpselling != null && listUpselling.size() > 0){

            final ListUpsellingAdapter adapter = new ListUpsellingAdapter((Activity) context, listUpselling);
            lvSummary.setAdapter(adapter);
            lvSummary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
                    upselling = item.getItem3();
                    loadPreCetakDialog();
                }
            });
        }

        upSellingDialog = builder
                .setNeutralButton("Tutup", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();

        upSellingDialog.show();
    }

    private void loadPreCetakDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_summary, null);
        builder.setTitle("Konfirmasi");
        builder.setView(view);

        final TextView tvSummary = (TextView) view.findViewById(R.id.tv_summary);
        final ListView lvSummary = (ListView) view.findViewById(R.id.lv_summary);

        tvSummary.setText("Cetak order ini ?");

        List<CustomItem> bufferList = new ArrayList<>();
        bufferList.add(new CustomItem());

        for(CustomItem item: listSelectedMenu){
            item.setItem15("1");
            if(item.getItem12().equals(upselling)) bufferList.add(item);
        }

        lvSummary.setAdapter(null);

        if(bufferList != null && bufferList.size() > 0){

            PreCetakAdapter adapter = new PreCetakAdapter((Activity) context, bufferList);
            lvSummary.setAdapter(adapter);
        }

        dialogLoading = builder
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        listSelectedMenuPerUpSelling = new ArrayList<>();
                        listSelectedMenuPerUpSelling = ((PreCetakAdapter) lvSummary.getAdapter()).getItems();
                        loadPrintingDialog();
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();

        dialogLoading.show();

    }

    public static void changeMeja(String noMejaString){

        if(mejaDialog != null){

            try {
                mejaDialog.dismiss();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        String tanggalCari = edtTanggal.getText().toString();
        if(!iv.isValidFormat(FormatItem.formatDateDisplay, tanggalCari)) {
            tanggalCari = "";
            edtTanggal.setText(tanggalCari);
        }

        startIndex = 0;
        getDataTransaksi();
        iv.hideSoftKey(context);
        noMeja = noMejaString;
        tvNoMeja.setText(noMeja);
    }

    private void getDataMeja() {

        pbLoadMenu.setVisibility(View.VISIBLE);
        listMeja = new ArrayList<>();

        ApiVolley request = new ApiVolley(context, new JSONObject(), "GET", serverURL.getMeja(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listMeja = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");

                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listMeja.add(new CustomItem(jo.getString("kdmeja"), jo.getString("nmmeja"),jo.getString("Status"),jo.getString("flag"),jo.getString("nobukti"),jo.getString("total"),jo.getString("jenis"),jo.getString("urutan")));
                        }

                        loadMejaDilalog();
                        pbLoadMenu.setVisibility(View.GONE);
                    }else{
                        pbLoadMenu.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoadMenu.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {
                pbLoadMenu.setVisibility(View.GONE);
            }
        });
    }

    private void loadMejaDilalog(){

        loadMejaDialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_change_meja, null);
        loadMejaDialog.setView(view);
        loadMejaDialog.setTitle("Pilih Meja");
        loadMejaDialog.setIcon(R.mipmap.ic_launcher);

        final RecyclerView rvMeja1 = (RecyclerView) view.findViewById(R.id.rv_list_meja_1);
        final RecyclerView rvMeja2 = (RecyclerView) view.findViewById(R.id.rv_list_meja_2);

        rvMeja1.setAdapter(null);
        rvMeja2.setAdapter(null);

        if(listMeja!= null && listMeja.size() > 0){

            List<CustomItem> listItem1 = new ArrayList<>();
            List<CustomItem> listItem2 = new ArrayList<>();

            for(CustomItem item: listMeja){

                switch (iv.parseNullInteger(item.getItem7())){
                    case 1:
                        listItem1.add(item);
                        break;
                    case 2:
                        listItem2.add(item);
                        break;
                }
            }

            final ListChangeMejaAdapter mejaAdapter1 = new ListChangeMejaAdapter(context, listItem1, noBukti);
            final ListChangeMejaAdapter mejaAdapter2 = new ListChangeMejaAdapter(context, listItem2, noBukti);

            boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
            if(!tabletSize){
                final RecyclerView.LayoutManager mLayoutManager1 = new GridLayoutManager(context, 2);
                final RecyclerView.LayoutManager mLayoutManager2 = new GridLayoutManager(context, 2);
                rvMeja1.setLayoutManager(mLayoutManager1);
                rvMeja2.setLayoutManager(mLayoutManager2);
            }else{
                final RecyclerView.LayoutManager mLayoutManager1 = new GridLayoutManager(context, 3);
                final RecyclerView.LayoutManager mLayoutManager2 = new GridLayoutManager(context, 3);
                rvMeja1.setLayoutManager(mLayoutManager1);
                rvMeja2.setLayoutManager(mLayoutManager2);
            }

            rvMeja1.setItemAnimator(new DefaultItemAnimator());
            rvMeja1.setAdapter(mejaAdapter1);

            rvMeja2.setItemAnimator(new DefaultItemAnimator());
            rvMeja2.setAdapter(mejaAdapter2);
        }

        loadMejaDialog.setNeutralButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        mejaDialog = loadMejaDialog
                .setCancelable(false)
                .create();

        mejaDialog.show();

    }

    //region =================================== Setting printer =======================================
    private void printDataAll() {

        printStatus = false;
        printState = 1;
        printCashierState = false;
        printKitchenState = false;
        printBarState = false;

        maxIter = maxIterFix;
        timestampNow = iv.getCurrentDate(FormatItem.formatTimestamp);
        printToCashier(noBukti, timestampNow, noMeja, listSelectedMenuPerUpSelling);

    }

    public void changePrintState(final Context context, int code, String status){

        String state = "";
        switch (printState){
            case 1:
                state = context.getString(R.string.printer_1);
                break;
            case 2:
                state = context.getString(R.string.printer_2);
                break;
            case 3:
                state = context.getString(R.string.printer_3);
                break;
        }

        maxIter = maxIterFix;

        if(code != Epos2CallbackCode.CODE_SUCCESS){

            if (Looper.myLooper() == null)
            {
                Looper.prepare();
            }
            final String message = status + " pada saat mencetak "+ state;
            /*for (int i = 0; i < toastTimer; i++)
            {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }*/
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                    if(printState == 1){

                        //loadPrintingDialog(context, "Printing kitchen label...");
                        printCashierState = false;
                        printToKitchen(noBukti, timestampNow, noMeja, listSelectedMenuPerUpSelling);
                    }else if(printState == 2){

                        //loadPrintingDialog(context, "Printing bar label...");
                        printKitchenState = false;
                        printToBar(noBukti, timestampNow, noMeja, listSelectedMenuPerUpSelling);
                    }else if(printState == 3){

                        //finish printing
                        printBarState = false;
                        printStatus = true;
                        updatePrinter();
                    }
                }
            });

        }else{
            if(printState == 1){

                //loadPrintingDialog(context, "Printing kitchen label...");
                printToKitchen(noBukti, timestampNow, noMeja, listSelectedMenuPerUpSelling);
            }else if(printState == 2){

                //loadPrintingDialog(context, "Printing bar label...");
                printToBar(noBukti, timestampNow, noMeja, listSelectedMenuPerUpSelling);
            }else if(printState == 3){

                //finish printing
                printStatus = true;
                updatePrinter();
            }
        }
    }

    private void updatePrinter() {

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nobukti", noBukti);
            jBody.put("upselling", upselling);
            jBody.put("cashier_status", (printCashierState) ? "1": "0");
            jBody.put("kitchen_status", (printKitchenState) ? "1": "0");
            jBody.put("bar_status", (printBarState) ? "1": "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.updatePrinterStatus(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String message = response.getJSONObject("response").getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                        tvCashierStatus.setText((printCashierState) ? "Tercetak" : "Tidak Tercetak");
                        tvKitchenStatus.setText((printKitchenState) ? "Tercetak" : "Tidak Tercetak");
                        tvBarStatus.setText((printBarState) ? "Tercetak" : "Tidak Tercetak");

                        String tanggalCari = edtTanggal.getText().toString();
                        if(!iv.isValidFormat(FormatItem.formatDateDisplay, tanggalCari)) {
                            tanggalCari = "";
                            edtTanggal.setText(tanggalCari);
                        }

                        startIndex = 0;
                        if(upSellingDialog != null){
                            try {
                                upSellingDialog.dismiss();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        getDataTransaksi();
                        getDetailTransaksi(lastSelectedOrder);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(dialogLoading != null){
                    try {
                        dialogLoading.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String result) {

                if(dialogLoading != null){
                    try {
                        dialogLoading.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //region Selected Order Menu
    private void loadPrintingDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Custom_Dialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_printer_loading, null);
        builder.setView(view);

        final TextView tvTitle = (TextView) view.findViewById(R.id.tv_loading);

        //Load Data
        tvTitle.setText("Mencetak...");


        dialogLoading = builder
                .setCancelable(false)
                .create();

        dialogLoading.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                printDataAll();
            }
        });

        dialogLoading.show();


    }

    // Bagian printing
    private void printToCashier(final String nobukti, final String timestamp, final String nomeja, final List<CustomItem> pesanan){

        printState = 1;

        if(printerManager.getData(SavedPrinterManager.TAG_IP1) == null){
            changePrintState(mContext, 1, "Printer belum di atur");
        }else{

            printCashierState = printCashier(urutan, nobukti, timestamp, nomeja, pesanan);

            if(!printCashierState){

                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle("Peringatan")
                        .setIcon(R.mipmap.ic_warning)
                        .setMessage("Tidak dapat mencetak printout untuk CASHIER.")
                        .setCancelable(false)
                        .setPositiveButton("Ulangi Mencetak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                printToCashier(nobukti, timestamp, nomeja, pesanan);
                            }
                        }).setNegativeButton("Lewati", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                AlertDialog dialog1 = new AlertDialog.Builder(mContext)
                                        .setTitle("Konfirmasi")
                                        .setCancelable(false)
                                        .setMessage("Printout CASHIER tidak akan tercetak")
                                        .setPositiveButton("Lanjutkan Tanpa Mencetak", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                changePrintState(mContext, 1, "Gagal mencetak");
                                            }
                                        })
                                        .setNegativeButton("Coba Cetak Kembali", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                printToCashier(nobukti, timestamp, nomeja, pesanan);
                                            }
                                        })
                                        .show();
                            }
                        }).show();
            }
        }
    }

    private void printToKitchen( final String nobukti, final String timestamp, final String nomeja, final List<CustomItem> pesanan){

        printState = 2;

        if(printerManager.getData(SavedPrinterManager.TAG_IP2) == null){
            changePrintState(mContext, 1, "Printer belum di atur");
        }else{

            boolean isKitchen = false;
            List<CustomItem> listMakanan = new ArrayList<>();
            for(CustomItem item: pesanan){
                if(item.getItem11().equals(katMakanan)){
                    isKitchen = true;
                    listMakanan.add(item);
                }
            }

            if(isKitchen){
                printKitchenState = printKitchen(urutan, nobukti, timestamp, nomeja, listMakanan);

                if(!printKitchenState){

                    //changePrintState(context, 1, "Gagal mencetak");
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setTitle("Peringatan")
                            .setCancelable(false)
                            .setIcon(R.mipmap.ic_warning)
                            .setMessage("Tidak dapat mencetak printout untuk KITCHEN")
                            .setPositiveButton("Ulangi Mencetak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    printToKitchen(nobukti, timestamp, nomeja, pesanan);
                                }
                            }).setNegativeButton("Lewati", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    AlertDialog dialog1 = new AlertDialog.Builder(mContext)
                                            .setTitle("Konfirmasi")
                                            .setCancelable(false)
                                            .setMessage("Printout KITCHEN tidak akan tercetak")
                                            .setPositiveButton("Lanjutkan Tanpa Mencetak", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    changePrintState(mContext, 1, "Gagal mencetak");
                                                }
                                            })
                                            .setNegativeButton("Coba Ulangi Mencetak", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    printToKitchen(nobukti, timestamp, nomeja, pesanan);
                                                }
                                            })
                                            .show();
                                }
                            }).show();
                }
            }else{
                printKitchenState = true;
                changePrintState(mContext, Epos2CallbackCode.CODE_SUCCESS, "Berhasil mencetak");
            }
        }
    }

    private void printToBar(final String nobukti, final String timestamp, final String nomeja, final List<CustomItem> pesanan){

        printState = 3;
        //printBarState = true;

        if(printerManager.getData(SavedPrinterManager.TAG_IP3) == null){
            changePrintState(mContext, 1, "Printer belum di atur");
        }else{

            boolean isBar = false;
            List<CustomItem> listMinuman = new ArrayList<>();
            for(CustomItem item: pesanan){
                if(item.getItem11().equals(katMinuman)){
                    isBar = true;
                    listMinuman.add(item);
                }
            }

            if(isBar){
                printBarState = printBar(urutan, nobukti, timestamp, nomeja, listMinuman);

                if(!printBarState){
                    //changePrintState(context, 1, "Gagal mencetak");
                    final AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setTitle("Peringatan")
                            .setIcon(R.mipmap.ic_warning)
                            .setCancelable(false)
                            .setMessage("Tidak dapat mencetak printout untuk BAR")
                            .setPositiveButton("Ulangi Mencetak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    printToBar(nobukti, timestamp, nomeja, pesanan);
                                }
                            }).setNegativeButton("Lewati", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    AlertDialog dialog1 = new AlertDialog.Builder(mContext)
                                            .setTitle("Konfirmasi")
                                            .setMessage("Printout BAR tidak akan tercetak")
                                            .setCancelable(false)
                                            .setPositiveButton("Lanjutkan Tanpa Mencetak", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    changePrintState(mContext, 1, "Gagal mencetak");
                                                }
                                            })
                                            .setNegativeButton("Coba Ulangi Mencetak", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    printToBar(nobukti, timestamp, nomeja, pesanan);
                                                }
                                            })
                                            .show();
                                }
                            }).show();


                }

            }else{
                printBarState = true;
                changePrintState(mContext, Epos2CallbackCode.CODE_SUCCESS, "Berhasil mencetak");
            }
        }
    }
    //endregion

    //region Prinnter

    //region cashier
    public boolean printCashier(String urutan, String noBukti, String timestamp, String noMeja, List<CustomItem> pesanan){

        return runPrintCashierSequence(urutan, timestamp, noBukti, noMeja, pesanan);
    }

    private boolean runPrintCashierSequence(String urutan, String timestamp, String noBukti, String noMeja, List<CustomItem> pesanan) {
        if (!initializeObject()) {
            return false;
        }

        if (!createCashierData(urutan, noBukti, timestamp, noMeja, pesanan)) {
            finalizeObject();
            return false;
        }

        if (!printData(SavedPrinterManager.TAG_IP1)) {
            finalizeObject();
            return false;
        }

        return true;
    }

    private boolean createCashierData(String urutan, String noBukti, String timestamp, String noMeja, List<CustomItem> pesanan) {

        // baris maks 30 char
        int maxRow= 30;
        int maxRow2= 16;
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource( getResources(), R.mipmap.ic_launcher);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            textData.append("SUMMARY ORDER\n");
            method = "addText";
            textData.append(noBukti+" (RE)\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatDateDisplay)+"-");
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatTime)+"\n");
            if(upselling.equals("1")){
                textData.append(noMeja+"/"+  session.getName() +"\n");
            }else{
                textData.append(noMeja+"/"+ "RE " + upselling + "/" + session.getName() +"\n");
            }
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            textData.append("------------------------------\n"); // 30 Line

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);

            // 1. id, 2. nama, 3. harga, 4. gambar,  5. banyak, 6. satuan, 7. diskon, 8. catatan, 9. hargaDiskon, 10. tag meja
            for(CustomItem item : pesanan){

                String itemToPrint = item.getItem5() +" X "+ item.getItem2();
                if(item.getItem10().length()>0){
                    itemToPrint = itemToPrint + " (" + item.getItem10() + ")";
                }

                textData.append( itemToPrint+"\n");

                /*if(item.getItem10().length()>0){
                    textData.append( "   " + item.getItem10() +"\n");
                }*/

                if(item.getItem8().length()>0){
                    String[] s = item.getItem8().split("\\r?\\n");
                    for(String note: s){
                        textData.append( "   \"" + note +"\"\n");
                    }
                }
            }

            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            /*method = "addBarcode";
            mPrinter.addBarcode("01209457",
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);*/

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, method, context);
            android.util.Log.d(TAG, method.toString()+ " " + e.toString());
            return false;
        }

        textData = null;

        return true;
    }

    //endregion

    //region kitchen
    public boolean printKitchen(String urutan, String noBukti, String timestamp, String noMeja, List<CustomItem> pesanan){

        return runPrintKitchenSequence(urutan, timestamp, noBukti, noMeja, pesanan);
    }

    private boolean runPrintKitchenSequence(String urutan, String timestamp, String noBukti, String noMeja, List<CustomItem> pesanan) {
        if (!initializeObject()) {
            return false;
        }

        if (!createKitchenData(urutan, noBukti, timestamp, noMeja, pesanan)) {
            finalizeObject();
            return false;
        }

        if (!printData(SavedPrinterManager.TAG_IP2)) {
            finalizeObject();
            return false;
        }

        return true;
    }

    private boolean initializeObject() {

        try {
            mPrinter = new Printer(Printer.TM_U220,
                    Printer.MODEL_ANK,
                    mContext);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "Printer", context);
            android.util.Log.d(TAG, "initializeObject: " + e.toString());
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }

    private boolean createKitchenData(String urutan, String noBukti, String timestamp, String noMeja, List<CustomItem> pesanan) {

        // baris maks 30 char
        int maxRow= 30;
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {
            /*method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            method = "addImage";
            mPrinter.addImage(logoData, 0, 0,
                    logoData.getWidth(),
                    logoData.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);

            method = "addTextAlign";*/
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            /*method = "addFeedLine";
            mPrinter.addFeedLine(1);*/
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            textData.append(noBukti+" (RE)\n");
            if(upselling.equals("1")){
                textData.append(noMeja+"/"+  session.getName() +"\n");
            }else{
                textData.append(noMeja+"/"+ "RE " + upselling + "/" + session.getName() +"\n");
            }
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextSize(2, 1);
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatDateDisplay)+"\n");
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatTime)+"\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            textData.append("------------------------------\n"); // 40 Line
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            method = "addTextSize";
            mPrinter.addTextSize(2, 1);

            // 1. id, 2. nama, 3. harga, 4. gambar,  5. banyak, 6. satuan, 7. diskon, 8. catatan, 9. hargaDiskon, 10. tag meja

            int x = 1;
            for(CustomItem item : pesanan){

                String itemToPrint = item.getItem5() +" X "+ item.getItem2();
                if(item.getItem10().length()>0){
                    itemToPrint = itemToPrint + " (" + item.getItem10() + ")";
                }

                textData.append( itemToPrint+"\n");

                if(item.getItem8().length()>0){
                    String[] s = item.getItem8().split("\\r?\\n");
                    int j = 0;
                    for(String note: s){

                        if(s.length == 1){
                            textData.append( "  \"" + note +"\"\n");
                        }else{
                            if(j == 0){
                                textData.append( "  \"" + note +"\n");
                            }else if(j == s.length - 1){
                                textData.append( "   " + note +"\"\n");
                            }else{
                                textData.append( "   " + note +"\n");
                            }
                        }
                        j++;
                    }
                }

                x++;
            }

            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            /*method = "addFeedLine";
            mPrinter.addFeedLine(2);*/

            /*method = "addBarcode";
            mPrinter.addBarcode("01209457",
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);*/

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, method, context);
            android.util.Log.d(TAG, method.toString()+" " + e.toString());
            return false;
        }

        textData = null;

        return true;
    }

    //endregion

    //region Bar
    public boolean printBar(String urutan, String noBukti, String timestamp, String noMeja, List<CustomItem> pesanan){

        return runPrintBarSequence(urutan, timestamp, noBukti, noMeja, pesanan);
    }

    private boolean runPrintBarSequence(String urutan, String timestamp, String noBukti, String noMeja, List<CustomItem> pesanan) {
        if (!initializeObject()) {
            return false;
        }

        if (!createBarData(urutan, noBukti, timestamp, noMeja, pesanan)) {
            finalizeObject();
            return false;
        }

        if (!printData(SavedPrinterManager.TAG_IP3)) {
            finalizeObject();
            return false;
        }

        return true;
    }

    private boolean createBarData(String urutan, String noBukti, String timestamp, String noMeja, List<CustomItem> pesanan) {

        // baris maks 30 char
        int maxRow= 30;
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {
            /*method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            method = "addImage";
            mPrinter.addImage(logoData, 0, 0,
                    logoData.getWidth(),
                    logoData.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);

            method = "addTextAlign";*/
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            /*method = "addFeedLine";
            mPrinter.addFeedLine(1);*/
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            textData.append(noBukti+" (RE)\n");
            if(upselling.equals("1")){
                textData.append(noMeja + "/" +  session.getName() +"\n");
            }else{
                textData.append(noMeja + "/" + "RE " + upselling + "/" + session.getName() +"\n");
            }
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextSize(2, 1);
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatDateDisplay)+"\n");
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatTime)+"\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            textData.append("------------------------------\n"); // 40 Line
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            method = "addTextSize";
            mPrinter.addTextSize(2, 1);

            // 1. id, 2. nama, 3. harga, 4. gambar,  5. banyak, 6. satuan, 7. diskon, 8. catatan, 9. hargaDiskon, 10. tag meja

            int x = 1;
            for(CustomItem item : pesanan){

                String itemToPrint = item.getItem5() +" X "+ item.getItem2();
                if(item.getItem10().length()>0){
                    itemToPrint = itemToPrint + " (" + item.getItem10() + ")";
                }
                textData.append( itemToPrint+"\n");

                if(item.getItem8().length()>0){
                    String[] s = item.getItem8().split("\\r?\\n");
                    int j = 0;
                    for(String note: s){

                        if(s.length == 1){
                            textData.append( "  \"" + note +"\"\n");
                        }else{
                            if(j == 0){
                                textData.append( "  \"" + note +"\n");
                            }else if(j == s.length - 1){
                                textData.append( "   " + note +"\"\n");
                            }else{
                                textData.append( "   " + note +"\n");
                            }
                        }
                        j++;
                    }
                }

                x++;
            }

            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            /*method = "addFeedLine";
            mPrinter.addFeedLine(2);*/

            /*method = "addBarcode";
            mPrinter.addBarcode("01209457",
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);*/

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, method, context);
            android.util.Log.d(TAG, method.toString()+" " + e.toString());
            return false;
        }

        textData = null;

        return true;
    }

    //endregion

    private void finalizeObject() {

        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private boolean printData(String key) {

        String ip = printerManager.getData(key);

        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter(ip)) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            //ShowMsg.showMsg(makeErrorMessage(status), context);
            android.util.Log.d(TAG, "error : " + makeErrorMessage(status));
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "sendData", context);
            android.util.Log.d(TAG, "sendData : " + e.toString());
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }

    private boolean isPrintable(PrinterStatusInfo status) {

        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        else {
            //print available
        }

        return true;
    }

    private void disconnectPrinter() {

        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "endTransaction", mContext);
                }
            });
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "disconnect", mContext);
                }
            });
        }

        finalizeObject();
    }

    private boolean connectPrinter(String ip) {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            mPrinter.connect(ip, Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "connect", context);
            android.util.Log.d(TAG, "connect : " + e.toString());
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "beginTransaction", context);
            android.util.Log.d(TAG, "beginTransaction : " + e.toString());
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {

        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getResources().getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getResources().getString(R.string.handlingmsg_warn_battery_near_end);
        }

        android.util.Log.d(TAG, "dispPrinterWarnings: " + warningsMsg);
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {

                /*if(code != Epos2CallbackCode.CODE_SUCCESS){
                    ShowMsg.showResult(code, makeErrorMessage(status), context);
                }*/

                dispPrinterWarnings(status);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                        changePrintState(mContext, code, makeErrorMessage(status));
                    }
                }).start();
            }
        });
    }

    public String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += getString(R.string.handlingmsg_err_autocutter);
            msg += getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    //endregion
}
