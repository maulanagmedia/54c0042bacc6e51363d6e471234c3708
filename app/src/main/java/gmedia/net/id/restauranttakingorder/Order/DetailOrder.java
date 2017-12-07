package gmedia.net.id.restauranttakingorder.Order;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.MainActivity;
import gmedia.net.id.restauranttakingorder.Order.Adapter.KategoriMenuAdapter;
import gmedia.net.id.restauranttakingorder.Order.Adapter.MenuByKategoriAdapter;
import gmedia.net.id.restauranttakingorder.Order.Adapter.SelectedMenuAdapter;
import gmedia.net.id.restauranttakingorder.Order.Adapter.SummaryAdapter;
import gmedia.net.id.restauranttakingorder.PrinterUtils.ShowMsg;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.FormatItem;
import gmedia.net.id.restauranttakingorder.Utils.SavedPrinterManager;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class DetailOrder extends AppCompatActivity implements ReceiveListener{

    private static final String TAG = "DetailOrder";
    private ListView lvKategori;
    private static EditText edtSearchMenu;
    private RecyclerView rvListMenu;
    private EditText edtNoMeja;
    private ListView lvOrder;
    private static TextView tvTotal;
    private Button btnCetak, btnSimpan;
    private List<CustomItem> listKategori;
    private static List<CustomItem> listMenu;
    public static List<CustomItem> listSelectedMenu;
    private ProgressBar pbLoadMenu, pbLoadKategori;
    private boolean firstLoad = true;
    private static ItemValidation iv = new ItemValidation();
    private static SelectedMenuAdapter selectedMenuAdapter;
    public static final int GET_PELANGGAN = 12;
    private FloatingActionButton fabScanBarcode;
    private String kategoriMenu = "", kdMeja = "";
    private SessionManager session;
    private EditText edtNoBukti, edtUrutan;
    private static String noBukti = "";
    private static String noMeja = "";
    private boolean editPenjualanMode = false;
    private ProgressBar pbLoadOrder;
    private boolean onProcess = false;
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
    private ServerURL serverURL;
    private static int maxIterFix = 30;
    private static int maxIter = 6;
    private static int delayTime = 1000;
    private static AlertDialog dialogLoading;
    private static Context mContext;
    private Printer mPrinter;

    private boolean printStatus = true;
    private static boolean phoneMode = true;
    private static RelativeLayout tabContainer, tab1, tab2, tab3;
    private static TabLayout tabLayout;
    private String printNo = "1";
    private AlertDialog printDialog;
    private String upselling = "";
    private EditText edtJmlPelanggan;
    private String jumlahPlg = "";
    private TextInputLayout tilJmlPelanggan;
    private boolean firstLoadJumlah = true;

    private static boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);

        doubleBackToExitPressedOnce = false;
        //phoneMode = false;
        phoneMode = true;
        boolean tabletMode = true;
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape

        }
        else {
            // Portrait
            //boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
            tabletMode = getResources().getBoolean(R.bool.isTablet);
            boolean tabletSize = false;
            if(!tabletSize){
                if(!tabletMode) setContentView(R.layout.activity_detail_order_phone);

                tabLayout = (TabLayout) findViewById(R.id.tab_top);
                /*tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.add_live));
                tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.calendar_live));
                tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.group_live));*/
                tabLayout.addTab(tabLayout.newTab().setText("Kategori"));
                tabLayout.addTab(tabLayout.newTab().setText("Menu"));
                tabLayout.addTab(tabLayout.newTab().setText("Detail Pesanan"));

                tabContainer = (RelativeLayout) findViewById(R.id.tab_container);
                tab1 = (RelativeLayout) findViewById(R.id.tab_1);
                tab2 = (RelativeLayout) findViewById(R.id.tab_2);
                tab3 = (RelativeLayout) findViewById(R.id.tab_3);
                phoneMode = true;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        mContext = this;
        serverURL = new ServerURL(DetailOrder.this);
        setTitle("Pilih Menu");
        initUI();

        try {
            com.epson.epos2.Log.setLogSettings(mContext, com.epson.epos2.Log.PERIOD_TEMPORARY, com.epson.epos2.Log.OUTPUT_STORAGE, null, 0, 1, com.epson.epos2.Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "setLogSettings", mContext);
        }
    }

    private static void setTabPosition(int position){

        if(phoneMode){

            if(tab1 != null && tab2 != null && tab3 != null){

                switch (position){
                    case 0:
                        tab1.setVisibility(View.VISIBLE);
                        tab2.setVisibility(View.GONE);
                        tab3.setVisibility(View.GONE);
                        break;
                    case 1:
                        tab1.setVisibility(View.GONE);
                        tab2.setVisibility(View.VISIBLE);
                        tab3.setVisibility(View.GONE);
                        break;
                    case 2:
                        tab1.setVisibility(View.GONE);
                        tab2.setVisibility(View.GONE);
                        tab3.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    }

    private void initUI() {

        lvKategori = (ListView) findViewById(R.id.lv_kategori);
        edtSearchMenu = (EditText) findViewById(R.id.edt_search_menu);
        rvListMenu = (RecyclerView) findViewById(R.id.rv_list_menu);
        pbLoadKategori = (ProgressBar) findViewById(R.id.pb_load_kategori);
        pbLoadMenu = (ProgressBar) findViewById(R.id.pb_load_menu);
        edtNoMeja = (EditText) findViewById(R.id.edt_no_meja);
        lvOrder = (ListView) findViewById(R.id.lv_order);
        tvTotal = (TextView) findViewById(R.id.tv_total);
        pbLoadOrder =  (ProgressBar) findViewById(R.id.pb_load_order);
        edtNoBukti = (EditText) findViewById(R.id.edt_no_bukti);
        edtUrutan= (EditText) findViewById(R.id.edt_urutan);
        tilJmlPelanggan = (TextInputLayout) findViewById(R.id.til_jml_pelanggan);
        edtJmlPelanggan= (EditText) findViewById(R.id.edt_jml_pelanggan);
        btnCetak = (Button) findViewById(R.id.btn_cetak);
        btnSimpan = (Button) findViewById(R.id.btn_simpan);
        fabScanBarcode = (FloatingActionButton) findViewById(R.id.fab_scan);

        progressDialog = new ProgressDialog(DetailOrder.this, R.style.AppTheme_Custom_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing...");
        printerManager = new SavedPrinterManager(DetailOrder.this);
        //printerTemplate = new PrinterTemplate(DetailOrder.this);
        session = new SessionManager(DetailOrder.this);
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            kdMeja = bundle.getString("kdmeja");
            noMeja = bundle.getString("nomeja");
            noBukti = bundle.getString("nobukti");

            edtNoMeja.setText(noMeja);
            setSelectedOrderEvent();
            getKategoriData();
            if(noBukti != null && noBukti.length() > 0){

                urutan = bundle.getString("urutan");
                jumlahPlg = bundle.getString("jmlplg");
                printNo = String.valueOf(iv.parseNullInteger(bundle.getString("printno")) + 1);
                editPenjualanMode = true;
                edtNoBukti.setText(noBukti);
                edtUrutan.setText(urutan);
                edtJmlPelanggan.setText(jumlahPlg);
            }else{

                editPenjualanMode = false;
                getNoBukti();
            }
        }

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Validation
                if(edtNoBukti.getText().toString().length() == 0){

                    Toast.makeText(DetailOrder.this, "No Bukti tidak termuat, periksa koneksi anda", Toast.LENGTH_LONG).show();
                    return;
                }

                if(noMeja.length() == 0){

                    Toast.makeText(DetailOrder.this, "No Meja tidak termuat, periksa koneksi anda", Toast.LENGTH_LONG).show();
                    return;
                }

                if(iv.parseNullInteger(edtJmlPelanggan.getText().toString()) == 0){
                    edtJmlPelanggan.setError("Jumlah Pelanggan harus lebih dari 0");
                    edtJmlPelanggan.requestFocus();
                    return;
                }else{
                    edtJmlPelanggan.setError(null);
                    jumlahPlg = edtJmlPelanggan.getText().toString();
                }

                /*if(edtUrutan.getText().toString().length() == 0){

                    Toast.makeText(DetailOrder.this, "Urutan tidak termuat, periksa koneksi anda", Toast.LENGTH_LONG).show();
                    return;
                }*/

                if(listSelectedMenu == null || listSelectedMenu.size() == 0){

                    Toast.makeText(DetailOrder.this, "Harap pilih minimal satu menu", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!onProcess){

                    loadSaveDialog(listSelectedMenu);
                }else{
                    Toast.makeText(DetailOrder.this, "Harap tunggu hingga proses selesai", Toast.LENGTH_LONG).show();
                }

            }
        });

        edtJmlPelanggan.requestFocus();
        edtJmlPelanggan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadJumlahPelanggan(edtJmlPelanggan.getText().toString());
            }
        });

        tilJmlPelanggan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadJumlahPelanggan(edtJmlPelanggan.getText().toString());
            }
        });

        if(firstLoadJumlah){
            firstLoadJumlah = false;
            edtJmlPelanggan.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(iv.parseNullInteger(edtJmlPelanggan.getText().toString()) == 0){
                        edtJmlPelanggan.setError("Jumlah Pelanggan harus lebih dari 0");
                    }else{
                        edtJmlPelanggan.setError(null);
                    }
                }
            });
        }
    }

    private void loadSaveDialog(List<CustomItem> listItem){

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrder.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_summary, null);
        builder.setTitle("Konfirmasi");
        builder.setView(view);

        final TextView tvSummary = (TextView) view.findViewById(R.id.tv_summary);
        final ListView lvSummary = (ListView) view.findViewById(R.id.lv_summary);

        tvSummary.setText("Proses pesanan ini (" + noBukti +") ?");

        lvSummary.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            SummaryAdapter adapter = new SummaryAdapter(DetailOrder.this, listItem);
            lvSummary.setAdapter(adapter);
        }

        dialogLoading = builder
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        simpanData();
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

    private void simpanData() {

        btnSimpan.setEnabled(false);
        progressDialog.show();
        onProcess = true;
        // penjualan_d
        JSONArray penjualanD = new JSONArray();

        timestampNow = iv.getCurrentDate(FormatItem.formatTimestamp);
        final String dateNow = iv.getCurrentDate(FormatItem.formatDate);

        double total = 0;
        for(CustomItem item: listSelectedMenu){

            // 1. id, 2. nama, 3. harga, 4. gambar,  5. banyak, 6. satuan, 7. diskon, 8. catatan, 9. hargaDiskon, 10. tag meja
            JSONObject jo = new JSONObject();
            try {
                jo.put("nobukti", noBukti);
                jo.put("tag_meja", item.getItem10());
                jo.put("kdbrg", item.getItem1());
                jo.put("jml", item.getItem5());
                jo.put("sat", item.getItem6());
                jo.put("harga", item.getItem3());
                jo.put("diskon", item.getItem7());
                total += (iv.parseNullDouble(item.getItem5()) * iv.parseNullDouble(item.getItem9()));
                jo.put("total", iv.doubleToStringRound(iv.parseNullDouble(item.getItem5()) * iv.parseNullDouble(item.getItem9())));
                jo.put("tglinput", timestampNow);
                jo.put("nik", session.getUserInfo(SessionManager.TAG_NIK));
                jo.put("catatan", item.getItem8());
                jo.put("jenis_order", item.getItem12());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            penjualanD.put(jo);
        }

        // Penjjualan
        JSONObject penjualan = new JSONObject();
        try {
            penjualan.put("nobukti", noBukti);
            penjualan.put("tgl", dateNow);
            penjualan.put("kdcus", "");
            penjualan.put("nmplg", "");
            penjualan.put("kdmeja", kdMeja);
            penjualan.put("nomeja", noMeja);
            penjualan.put("jumlah_plg", edtJmlPelanggan.getText().toString());
            penjualan.put("card", "");
            penjualan.put("nik", session.getUserInfo(SessionManager.TAG_NIK));
            penjualan.put("total", iv.doubleToStringRound(total));
            penjualan.put("userid", session.getUserInfo(SessionManager.TAG_USERNAME));
            penjualan.put("usertgl", timestampNow);
            penjualan.put("status", "1");
            penjualan.put("urutan", edtUrutan.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String method = "POST";
        if(editPenjualanMode){
            method = "PUT";
        }
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("penjualan", penjualan);
            jBody.put("penjualan_d", penjualanD);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrder.this, jBody, method, serverURL.saveOrder(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        progressDialog.dismiss();
                        String message1 = response.getJSONObject("response").getString("message");
                        upselling = response.getJSONObject("response").getString("upselling");

                        for(int i = 0; i < toastTimer; i++){
                            Toast.makeText(DetailOrder.this, message1 + ".\n Tunggu hingga proses mencetak selesai, aplikasi akan menuju ke daftar transaksi", Toast.LENGTH_LONG).show();
                        }

                        if(printStatus){

                            printStatus = false;
                            //printDataAll();
                            loadPrintingDialog();
                        }

                    }else{
                        Toast.makeText(DetailOrder.this, message, Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                    onProcess = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    onProcess = false;
                    btnSimpan.setEnabled(true);
                    Toast.makeText(DetailOrder.this, "Gagal menyimpan data, harap ulangi", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                onProcess = false;
                progressDialog.dismiss();
                Toast.makeText(DetailOrder.this, "Gagal menyimpan data, harap ulangi", Toast.LENGTH_LONG).show();
                btnSimpan.setEnabled(true);
            }
        });
    }

    //region =================================== Setting printer

    //region Selected Order Menu
    private void loadPrintingDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrder.this, R.style.AppTheme_Custom_Dialog);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_printer_loading, null);
        builder.setView(view);

        final TextView tvTitle = (TextView) view.findViewById(R.id.tv_loading);

        //Load Data
        tvTitle.setText("Mencetak...");


        printDialog = builder
                .setCancelable(false)
                .create();

        printDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                printDataAll();
            }
        });

        printDialog.show();
    }

    private void printDataAll() {

        printState = 1;
        printCashierState = false;
        printKitchenState = false;
        printBarState = false;

        //Print ke Cashier
        //printState = 3;
        //changePrintState(DetailOrder.this, 1, "Gagal mencetak");
        maxIter = maxIterFix;
        //loadPrintingDialog(DetailOrder.this, "Printing cashier label...");
        printToCashier(noBukti, timestampNow, noMeja, listSelectedMenu);
        //printToKitchen(DetailOrder.this, noBukti, timestampNow, noMeja, listSelectedMenu);

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

        if(dialogLoading != null){
            try {
                dialogLoading.dismiss();
            }catch (Exception e){
                e.printStackTrace();
            }
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    if(printState == 1){

                        printCashierState = false;
                        //loadPrintingDialog(context, "Printing kitchen label...");
                        printToKitchen(noBukti, timestampNow, noMeja, listSelectedMenu);
                    }else if(printState == 2){

                        printKitchenState = false;
                        //loadPrintingDialog(context, "Printing bar label...");
                        printToBar(noBukti, timestampNow, noMeja, listSelectedMenu);
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
                printToKitchen(noBukti, timestampNow, noMeja, listSelectedMenu);
            }else if(printState == 2){

                //loadPrintingDialog(context, "Printing bar label...");
                printToBar(noBukti, timestampNow, noMeja, listSelectedMenu);
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

        ApiVolley request = new ApiVolley(DetailOrder.this, jBody, "POST", serverURL.updatePrinterStatus(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                if(printDialog != null){
                    try {
                        printDialog.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(DetailOrder.this, MainActivity.class);
                //intent.putExtra("riwayat", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String result) {

                if(printDialog != null){
                    try {
                        printDialog.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(DetailOrder.this, MainActivity.class);
                //intent.putExtra("riwayat", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    //region Selected Order Menu
    private void loadPrintingDialog(final Context context, String message){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_printer_loading, null);
        builder.setView(view);

        final TextView tvTitle = (TextView) view.findViewById(R.id.tv_loading);

        //Load Data
        tvTitle.setText(message);


        printDialog = builder
                .setPositiveButton("Skip", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        changePrintState(mContext, 1, "Printer belum di atur");
                        dialogLoading.dismiss();
                    }
                })
                .setCancelable(false)
                .create();

        printDialog.show();

    }

    // Bagian printing
    private void printToCashier(final String nobukti, final String timestamp, final String nomeja, final List<CustomItem> pesanan){

        printState = 1;

        if(printerManager.getData(SavedPrinterManager.TAG_IP1) == null){
            changePrintState(mContext, 1, "Printer belum di atur");
        }else{

            printCashierState = printCashier(upselling, nobukti, timestamp, nomeja, pesanan);

            if(!printCashierState){
                //changePrintState(context, 1, "Gagal mencetak");
                /*if(maxIter > 0){

                    maxIter -= 1;

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    printToCashier(context, nobukti, timestamp, nomeja, pesanan);
                                }
                            }, delayTime);
                        }
                    });
                }else{
                    changePrintState(context, 1, "Gagal mencetak");
                }*/

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

            /*PrinterChecker checker = new PrinterChecker(context);
            boolean statusCheck = checker.checkPinter(printerManager.getData(SavedPrinterManager.TAG_IP1));
            checker.clearPrinter(printerManager.getData(SavedPrinterManager.TAG_IP1));
            if(statusCheck){
                printState = 1;

                printCashierState = printerTemplate.printCashier(urutan, nobukti, timestamp, nomeja, pesanan);

                if(!printCashierState){
                    changePrintState(context, 1, "Gagal mencetak");
                }
            }else if(maxIter > 0){

                maxIter -= 1;

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                printToCashier(context, nobukti, timestamp, nomeja, pesanan);
                            }
                        }, delayTime);
                    }
                });
            }else{
                changePrintState(context, 1, "Gagal mencetak");
            }*/
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
                /*if(maxIter > 0){

                    maxIter -= 1;

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    printToKitchen(context, nobukti, timestamp, nomeja, pesanan);
                                }
                            }, delayTime);
                        }
                    });
                }else{
                    changePrintState(context, 1, "Gagal mencetak");
                }*/

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

            /*PrinterChecker checker = new PrinterChecker(context);
            boolean statusCheck = checker.checkPinter(printerManager.getData(SavedPrinterManager.TAG_IP2));
            checker.clearPrinter(printerManager.getData(SavedPrinterManager.TAG_IP2));
            if(statusCheck){

                boolean isKitchen = false;
                List<CustomItem> listMakanan = new ArrayList<>();
                for(CustomItem item: pesanan){
                    if(item.getItem11().equals(katMakanan)){
                        isKitchen = true;
                        listMakanan.add(item);
                    }
                }

                if(isKitchen){
                    printKitchenState = printerTemplate.printKitchen(urutan, nobukti, timestamp, nomeja, listMakanan);
                }else{
                    changePrintState(context, Epos2CallbackCode.CODE_SUCCESS, "Berhasil mencetak");
                }

                if(!printKitchenState){
                    changePrintState(context, 1, "Gagal mencetak");
                }
            }else if(maxIter > 0){

                maxIter -= 1;

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                printToKitchen(context, nobukti, timestamp, nomeja, pesanan);
                            }
                        }, delayTime);
                    }
                });
            }else{
                changePrintState(context, 1, "Gagal mencetak");
            }*/
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
                /*if(maxIter > 0){

                    maxIter -= 1;

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    printToBar(context, nobukti, timestamp, nomeja, pesanan);
                                }
                            }, delayTime);
                        }
                    });
                }else{
                    changePrintState(context, 1, "Gagal mencetak");
                }*/
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

            /*PrinterChecker checker = new PrinterChecker(context);
            boolean statusCheck = checker.checkPinter(printerManager.getData(SavedPrinterManager.TAG_IP3));
            checker.clearPrinter(printerManager.getData(SavedPrinterManager.TAG_IP3));
            if(statusCheck){
                boolean isBar = false;
                List<CustomItem> listMinuman = new ArrayList<>();
                for(CustomItem item: pesanan){
                    if(item.getItem11().equals(katMinuman)){
                        isBar = true;
                        listMinuman.add(item);
                    }
                }

                if(isBar){
                    printBarState = printerTemplate.printBar(urutan, nobukti, timestamp, nomeja, listMinuman);
                }else{
                    changePrintState(context, Epos2CallbackCode.CODE_SUCCESS, "Berhasil mencetak");
                }

                if(!printBarState){
                    changePrintState(context, 1, "Gagal mencetak");
                }
            }else if(maxIter > 0){

                maxIter -= 1;

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                printToBar(context, nobukti, timestamp, nomeja, pesanan);
                            }
                        }, delayTime);
                    }
                });
            }else{
                changePrintState(context, 1, "Gagal mencetak");
            }*/

        }
    }
    //endregion

    private void getNoBukti() {

        pbLoadOrder.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kdmeja", kdMeja);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrder.this, jBody, "POST", serverURL.getNoBukti(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        noBukti = response.getJSONObject("response").getString("nobukti");
                        urutan = response.getJSONObject("response").getString("urutan");
                        edtNoBukti.setText(noBukti);
                        edtUrutan.setText(urutan);
                    }

                    pbLoadOrder.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoadOrder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {
                pbLoadOrder.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GET_PELANGGAN) {

            super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == Activity.RESULT_OK){
                String nama = data.getStringExtra("nama");
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }else{
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {

                    Log.d(TAG, "onActivityResult: Scan failed ");
                } else {

                    Log.d(TAG, "barcode: "+result.getContents());
                    getMenuByBarcode(result.getContents());
                }
            }else{
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void setSelectedOrderEvent() {

        listSelectedMenu = new ArrayList<>();
        selectedMenuAdapter = new SelectedMenuAdapter(DetailOrder.this, listSelectedMenu);
        lvOrder.setAdapter(selectedMenuAdapter);

        lvOrder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                loadEditOrderDialog(DetailOrder.this, i, false);
            }
        });

        fabScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openScanBarcode();
            }
        });
    }

    private void openScanBarcode() {

        IntentIntegrator integrator = new IntentIntegrator(DetailOrder.this);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.initiateScan();
    }

    //region setting kategori
    private void getKategoriData() {

        pbLoadKategori.setVisibility(View.VISIBLE);
        listKategori = new ArrayList<>();
        ApiVolley request = new ApiVolley(DetailOrder.this, new JSONObject(), "GET", serverURL.getKategori(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        listKategori.add(new CustomItem("", "Semua",""));
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listKategori.add(new CustomItem(jo.getString("Kdmenu"), jo.getString("nmmenu"),jo.getString("type")));
                        }

                        pbLoadKategori.setVisibility(View.GONE);
                        setKategoriTable();
                    }else{
                        pbLoadKategori.setVisibility(View.GONE);
                        setKategoriTable();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    pbLoadKategori.setVisibility(View.GONE);
                    setKategoriTable();
                }
            }

            @Override
            public void onError(String result) {
                pbLoadKategori.setVisibility(View.GONE);
                setKategoriTable();
            }
        });
    }

    private void setKategoriTable() {

        lvKategori.setAdapter(null);

        if(listKategori != null && listKategori.size() > 0){

            final KategoriMenuAdapter adapter = new KategoriMenuAdapter(DetailOrder.this, listKategori);
            adapter.selectedPosition = 0;
            lvKategori.setAdapter(adapter);

            lvKategori.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                    CustomItem selected = (CustomItem) adapterView.getItemAtPosition(i);
                    kategoriMenu = selected.getItem1();
                    typeKategori = selected.getItem3();
                    adapter.selectedPosition = i;
                    adapter.notifyDataSetChanged();
                    //setTabPosition(1);
                    if(phoneMode){
                        TabLayout.Tab tab = tabLayout.getTabAt(1);
                        tab.select();
                    }

                    getMenuByKategori(selected);
                }
            });

            CustomItem item = listKategori.get(0);
            adapter.selectedPosition = 0;
            getMenuByKategori(item);
        }
    }
    //endregion

    //region Menu
    private void getMenuByKategori(CustomItem selected) {

        pbLoadMenu.setVisibility(View.VISIBLE);
        listMenu = new ArrayList<>();

        ApiVolley request = new ApiVolley(DetailOrder.this, new JSONObject(), "GET", serverURL.getMenu() + kategoriMenu, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
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
                            listMenu.add(new CustomItem(jo.getString("kdbrg"), jo.getString("nmbrg"),jo.getString("harga"),jo.getString("link"),"1",jo.getString("satuan"),jo.getString("diskon"),jo.getString("catatan"),jo.getString("harga_diskon"),"",jo.getString("type"), "DN"));
                            // id, nama, harga, gambar, banyak, satuan, diskon, catatan, hargaDiskon, tag meja
                        }

                        pbLoadMenu.setVisibility(View.GONE);
                        setManuSearchView();
                        setMenuTable(listMenu);
                    }else{
                        pbLoadMenu.setVisibility(View.GONE);
                        setManuSearchView();
                        setMenuTable(null);
                    }
                } catch (JSONException e) {
                    pbLoadMenu.setVisibility(View.GONE);
                    e.printStackTrace();
                    setManuSearchView();
                    setMenuTable(null);
                }
                pbLoadMenu.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {
                pbLoadMenu.setVisibility(View.GONE);
                setManuSearchView();
                setMenuTable(null);
            }
        });

    }

    private void loadJumlahPelanggan(final String jml){

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrder.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_number, null);
        builder.setView(view);

        final TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
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
        tvTitle.setText("JUMLAH PELANGGAN");
        tvPin.setText(jml);
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

        builder.setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(tvPin.getText().length() > 0) {

                    tvPin.setText(tvPin.getText().toString().substring(0, tvPin.getText().length()-1));

                    if(tvPin.getText().length() == 0){
                        tvPin.setText("0");
                    }
                }
            }
        });

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                edtJmlPelanggan.setText(tvPin.getText().toString());
            }
        });


        final AlertDialog alert = builder.create();
        alert.show();

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private void getMenuByBarcode(String barcode) {

        pbLoadMenu.setVisibility(View.VISIBLE);
        listMenu = new ArrayList<>();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("barcode", barcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrder.this, jBody, "POST", serverURL.getMenuBarcode(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listMenu.add(new CustomItem(jo.getString("kdbrg"), jo.getString("nmbrg"),jo.getString("harga"),jo.getString("link"),"1",jo.getString("satuan"),jo.getString("diskon"),jo.getString("catatan"),jo.getString("harga_diskon"),"",jo.getString("type")));
                            // id, nama, harga, gambar, banyak, satuan, diskon, catatan, hargaDiskon, tag meja
                        }

                        pbLoadMenu.setVisibility(View.GONE);
                        setManuSearchView();
                        setMenuTable(listMenu);
                    }else{
                        pbLoadMenu.setVisibility(View.GONE);
                        setManuSearchView();
                        setMenuTable(null);
                    }
                } catch (JSONException e) {
                    pbLoadMenu.setVisibility(View.GONE);
                    e.printStackTrace();
                    setManuSearchView();
                    setMenuTable(null);
                }
                pbLoadMenu.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {
                pbLoadMenu.setVisibility(View.GONE);
                setManuSearchView();
                setMenuTable(null);
            }
        });

    }

    private void setMenuTable(List<CustomItem> listItem) {

        rvListMenu.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            final MenuByKategoriAdapter menuAdapter = new MenuByKategoriAdapter(DetailOrder.this, listItem);

            final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(DetailOrder.this, 1);
            rvListMenu.setLayoutManager(mLayoutManager);
//        rvListMenu.addItemDecoration(new NavMenu.GridSpacingItemDecoration(2, dpToPx(10), true));
            rvListMenu.setItemAnimator(new DefaultItemAnimator());
            rvListMenu.setAdapter(menuAdapter);

        }
    }

    private void setManuSearchView() {

        if(firstLoad){
            firstLoad = false;
            edtSearchMenu.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(edtSearchMenu.getText().length() == 0){

                        setMenuTable(listMenu);
                    }
                }
            });
        }

        edtSearchMenu.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<CustomItem> items = new ArrayList<CustomItem>();
                    String keyword = edtSearchMenu.getText().toString().toUpperCase();
                    for(CustomItem item: listMenu){

                        if(item.getItem2().toUpperCase().contains(keyword)) items.add(item);
                    }

                    setMenuTable(items);
                    iv.hideSoftKey(DetailOrder.this);
                    return true;
                }
                return false;
            }
        });
    }
    //endregion

    //region Selected Order Menu
    public static void loadAddOrderDialog(final Context context, final CustomItem item){

        int checkPosition = 0;
        boolean editMode = false;
        for(CustomItem listMenu: listSelectedMenu){

            if(listMenu.getItem1().equals(item.getItem1()) && listMenu.getItem12().equals(item.getItem12())) {
                editMode = true;
                break;
            }
            checkPosition++;
        }

        if(editMode){

            loadEditOrderDialog(context, checkPosition, true);
        }else{
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_edit_order, null);
            builder.setView(view);

            final CustomItem[] newItem = {new CustomItem(item.getItem1(), item.getItem2(), item.getItem3(), item.getItem4(), item.getItem5(), item.getItem6(), item.getItem7(), item.getItem8(), item.getItem9(),item.getItem10(), item.getItem11())};

            final TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            final TextView tvHarga = (TextView) view.findViewById(R.id.tv_harga);
            final EditText edtHarga = (EditText) view.findViewById(R.id.edt_harga);
            final RadioGroup rgJenisOrder = (RadioGroup) view.findViewById(R.id.rg_jenis_order);
            final RadioButton rbDN = (RadioButton) view.findViewById(R.id.rb_dn);
            final RadioButton rbTA = (RadioButton) view.findViewById(R.id.rb_ta);
            final ImageView ivJmlPlus = (ImageView) view.findViewById(R.id.iv_jml_plus);
            final ImageView ivJmlMin = (ImageView) view.findViewById(R.id.iv_jml_min);
            final EditText edtJumlah = (EditText) view.findViewById(R.id.edt_jumlah);
            final EditText edtSatuan = (EditText) view.findViewById(R.id.edt_satuan);
            final EditText edtDiskon = (EditText) view.findViewById(R.id.edt_diskon);
            final EditText edtHargaDiskon = (EditText) view.findViewById(R.id.edt_harga_diskon);
            final EditText edtCatatan = (EditText) view.findViewById(R.id.edt_catatan);
            final CheckBox cbA = (CheckBox) view.findViewById(R.id.cb_a);
            final CheckBox cbB = (CheckBox) view.findViewById(R.id.cb_b);
            final CheckBox cbC = (CheckBox) view.findViewById(R.id.cb_c);
            final CheckBox cbD = (CheckBox) view.findViewById(R.id.cb_d);
            final CheckBox cbE = (CheckBox) view.findViewById(R.id.cb_e);
            final CheckBox cbF = (CheckBox) view.findViewById(R.id.cb_f);
            final CheckBox cbG = (CheckBox) view.findViewById(R.id.cb_g);
            final CheckBox cbH = (CheckBox) view.findViewById(R.id.cb_h);
            final CheckBox cbI = (CheckBox) view.findViewById(R.id.cb_i);
            final CheckBox cbJ = (CheckBox) view.findViewById(R.id.cb_j);
            final Button btnBatal = (Button) view.findViewById(R.id.btn_batal);
            final Button btnHapus = (Button) view.findViewById(R.id.btn_hapus);
            btnHapus.setVisibility(View.GONE);
            final Button btnSimpan = (Button) view.findViewById(R.id.btn_simpan);

            //Load Data
            tvTitle.setText(item.getItem2());
            tvHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem9())*iv.parseNullDouble(item.getItem5())));
            edtHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem3())));
            edtJumlah.setText(item.getItem5());
            edtSatuan.setText(item.getItem6());
            edtDiskon.setText(item.getItem7());
            edtCatatan.setText(item.getItem8());
            edtHargaDiskon.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem9())));
            if(item.getItem12().equals("DN")) {
                rbDN.setChecked(true);
            }else{
                rbTA.setChecked(true);
            }

            edtJumlah.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(iv.parseNullDouble(edtJumlah.getText().toString()) > 0){
                        edtJumlah.setError(null);
                        newItem[0] = hitungHargaDiskon(item, edtJumlah, edtDiskon, edtHargaDiskon, tvHarga);
                    }else{
                        edtJumlah.setError("Jumlah harus lebih dari 0");
                    }
                }
            });

            edtJumlah.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    loadJumlahEditor(context, edtJumlah, edtJumlah.getText().toString());
                }
            });

            rgJenisOrder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {

                    String selectedJenis = "DN";
                    if(i == rbTA.getId()) selectedJenis = "TA";
                    int position = 0;
                    boolean isExist = false;
                    for(CustomItem itemAtPosition: listSelectedMenu){

                        if(item.getItem1().equals(itemAtPosition.getItem1()) && itemAtPosition.getItem12().equals(selectedJenis)){

                            isExist = true;
                            break;
                        }
                        position++;
                    }

                    if(isExist){

                        CustomItem itemAtPosition = listSelectedMenu.get(position);

                        tvTitle.setText(itemAtPosition.getItem2());
                        tvHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemAtPosition.getItem9())*iv.parseNullDouble(itemAtPosition.getItem5())));
                        edtHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemAtPosition.getItem3())));
                        edtJumlah.setText(itemAtPosition.getItem5());
                        edtSatuan.setText(itemAtPosition.getItem6());
                        edtDiskon.setText(itemAtPosition.getItem7());
                        edtCatatan.setText(itemAtPosition.getItem8());
                        edtHargaDiskon.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemAtPosition.getItem9())));
                        /*if(itemAtPosition.getItem12().equals("DN")) {
                            rbDN.setChecked(true);
                        }else{
                            rbTA.setChecked(true);
                        }*/

                    }else{

                        tvTitle.setText(item.getItem2());
                        tvHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem9())*iv.parseNullDouble(item.getItem5())));
                        edtHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem3())));
                        edtJumlah.setText("1");
                        edtSatuan.setText(item.getItem6());
                        edtDiskon.setText(item.getItem7());
                        edtCatatan.setText(item.getItem8());
                        edtHargaDiskon.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem9())));
                        /*if(item.getItem12().equals("DN")) {
                            rbDN.setChecked(true);
                        }else{
                            rbTA.setChecked(true);
                        }*/
                    }
                }
            });

            setCheckedTagMeja(cbA, cbB, cbC, cbD, cbE, cbF, cbG, cbH, cbI, cbJ, edtJumlah);

            ivJmlMin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String jml = edtJumlah.getText().toString();
                    if(iv.parseNullInteger(jml) > 1){
                        int jmlBaru = iv.parseNullInteger(jml) - 1;
                        edtJumlah.setText(String.valueOf(jmlBaru));
                    }
                }
            });

            ivJmlPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int jmlBaru = iv.parseNullInteger(edtJumlah.getText().toString()) + 1;
                    edtJumlah.setText(String.valueOf(jmlBaru));
                }
            });

            edtDiskon.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(iv.parseNullDouble(edtDiskon.getText().toString()) >= 0){
                        if(iv.parseNullDouble(edtDiskon.getText().toString()) <= 100){
                            edtDiskon.setError(null);
                            newItem[0] = hitungHargaDiskon(item, edtJumlah, edtDiskon, edtHargaDiskon, tvHarga);
                        }else{
                            edtDiskon.setError("Jumlah harus kurang dari 100");
                        }
                    }
                }
            });

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
                    if(iv.parseNullDouble(edtJumlah.getText().toString()) <= 0){
                        edtJumlah.setError("Jumlah harus lebih dari 0");
                        edtJumlah.requestFocus();
                        return;
                    }

                    if(iv.parseNullDouble(edtDiskon.getText().toString()) > 0){
                        if(iv.parseNullDouble(edtDiskon.getText().toString()) > 100){
                            edtDiskon.setError("Jumlah harus kurang dari 100");
                            edtDiskon.requestFocus();
                            return;
                        }
                    }

                    newItem[0].setItem8(edtCatatan.getText().toString());
                    List<String> listTagMeja = new ArrayList<String>();

                    if(cbA.isChecked())listTagMeja.add("A");
                    if(cbB.isChecked())listTagMeja.add("B");
                    if(cbC.isChecked())listTagMeja.add("C");
                    if(cbD.isChecked())listTagMeja.add("D");
                    if(cbE.isChecked())listTagMeja.add("E");
                    if(cbF.isChecked())listTagMeja.add("F");
                    if(cbG.isChecked())listTagMeja.add("G");
                    if(cbH.isChecked())listTagMeja.add("H");
                    if(cbI.isChecked())listTagMeja.add("I");
                    if(cbJ.isChecked())listTagMeja.add("J");

                    String tagMeja = "";
                    int x = 0;
                    for(String tag : listTagMeja){
                        if(x == 0){
                            tagMeja = tag;
                        }else{
                            tagMeja = tagMeja + ", " + tag;
                        }
                        x++;
                    }

                    newItem[0].setItem10(tagMeja);
                    String jenisOrder = "DN";
                    if(rbTA.isChecked()) jenisOrder = "TA";
                    newItem[0].setItem12(jenisOrder);
                    addMoreSelectedMenu(newItem[0]);
                    alert.dismiss();
                    updateHargaTotal();
                    //setTabPosition(2);
                    if(phoneMode){
                        TabLayout.Tab tab = tabLayout.getTabAt(1);
                        tab.select();
                    }

                    edtSearchMenu.setText("");

                }
            });

            alert.show();

            alert.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );
        }
    }

    private static void addMoreSelectedMenu(CustomItem selectedMenu){

        if(listSelectedMenu != null){

            if(listSelectedMenu.size() > 0){

                boolean isExist = false;
                int existIndex = 0;
                double lastJumlah = 0;
                for(CustomItem item: listSelectedMenu){

                    if(item.getItem1().equals(selectedMenu.getItem1()) && item.getItem12().equals(selectedMenu.getItem12())){
                        isExist = true;
                        lastJumlah = iv.parseNullDouble(item.getItem5());
                        break;
                    }
                    existIndex++;
                }

                if(isExist){

                    //double jmlBaru = lastJumlah+1;
                    //selectedMenu.setItem5(iv.doubleToStringRound(jmlBaru));
                    listSelectedMenu.set(existIndex, selectedMenu);
                }else{

                    //selectedMenu.setItem5("1");
                    listSelectedMenu.add(selectedMenu);
                }
                selectedMenuAdapter.notifyDataSetChanged();
            }else{
                //selectedMenu.setItem5("1");
                listSelectedMenu.add(selectedMenu);
                selectedMenuAdapter.notifyDataSetChanged();
            }

            updateHargaTotal();
        }
    }
    //endregion

    private static void updateHargaTotal(){

        double total = 0;
        if(listSelectedMenu != null && listSelectedMenu.size() > 0){

            for(CustomItem item: listSelectedMenu){

                total += (iv.parseNullDouble(item.getItem9())* iv.parseNullDouble(item.getItem5()));
            }
        }
        tvTotal.setText(iv.ChangeToRupiahFormat(total));
    }

    //region Change Server
    private static void loadJumlahEditor(final Context context, final EditText edtJumlah, final String jml){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_number, null);
        builder.setView(view);

        final TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
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
        tvTitle.setText("JUMLAH");
        tvPin.setText(jml);
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

        builder.setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(tvPin.getText().length() > 0) {

                    tvPin.setText(tvPin.getText().toString().substring(0, tvPin.getText().length()-1));

                    if(tvPin.getText().length() == 0){
                        tvPin.setText("0");
                    }
                }
            }
        });

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                edtJumlah.setText(tvPin.getText().toString());
            }
        });


        final AlertDialog alert = builder.create();
        alert.show();

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private static void pinButtonListener(final TextView tvTarget, final TextView tv){

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (tv.getId()){
                    case R.id.tv_1:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"1");
                        break;
                    case R.id.tv_2:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"2");
                        break;
                    case R.id.tv_3:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"3");
                        break;
                    case R.id.tv_4:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"4");
                        break;
                    case R.id.tv_5:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"5");
                        break;
                    case R.id.tv_6:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"6");
                        break;
                    case R.id.tv_7:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"7");
                        break;
                    case R.id.tv_8:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"8");
                        break;
                    case R.id.tv_9:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"9");
                        break;
                    case R.id.tv_0:
                        if(tvTarget.getText().toString().equals("0")) tvTarget.setText("");
                        tvTarget.setText(tvTarget.getText()+"0");
                        break;
                }
            }
        });
    }
    //endregion

    //region dialogEdit

    private static void loadEditOrderDialog(final Context context, final int position, final boolean fromInserted){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_edit_order, null);
        builder.setView(view);

        final CustomItem item = listSelectedMenu.get(position);
        final CustomItem[] newItem = {new CustomItem(item.getItem1(), item.getItem2(), item.getItem3(), item.getItem4(), item.getItem5(), item.getItem6(), item.getItem7(), item.getItem8(), item.getItem9(), item.getItem10(), item.getItem11())};

        final TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        final TextView tvHarga = (TextView) view.findViewById(R.id.tv_harga);
        final EditText edtHarga = (EditText) view.findViewById(R.id.edt_harga);
        final RadioGroup rgJenisOrder = (RadioGroup) view.findViewById(R.id.rg_jenis_order);
        final RadioButton rbDN = (RadioButton) view.findViewById(R.id.rb_dn);
        final RadioButton rbTA = (RadioButton) view.findViewById(R.id.rb_ta);
        final ImageView ivJmlPlus = (ImageView) view.findViewById(R.id.iv_jml_plus);
        final ImageView ivJmlMin = (ImageView) view.findViewById(R.id.iv_jml_min);
        final EditText edtJumlah = (EditText) view.findViewById(R.id.edt_jumlah);
        final EditText edtSatuan = (EditText) view.findViewById(R.id.edt_satuan);
        final EditText edtDiskon = (EditText) view.findViewById(R.id.edt_diskon);
        final EditText edtHargaDiskon = (EditText) view.findViewById(R.id.edt_harga_diskon);
        final EditText edtCatatan = (EditText) view.findViewById(R.id.edt_catatan);
        final CheckBox cbA = (CheckBox) view.findViewById(R.id.cb_a);
        final CheckBox cbB = (CheckBox) view.findViewById(R.id.cb_b);
        final CheckBox cbC = (CheckBox) view.findViewById(R.id.cb_c);
        final CheckBox cbD = (CheckBox) view.findViewById(R.id.cb_d);
        final CheckBox cbE = (CheckBox) view.findViewById(R.id.cb_e);
        final CheckBox cbF = (CheckBox) view.findViewById(R.id.cb_f);
        final CheckBox cbG = (CheckBox) view.findViewById(R.id.cb_g);
        final CheckBox cbH = (CheckBox) view.findViewById(R.id.cb_h);
        final CheckBox cbI = (CheckBox) view.findViewById(R.id.cb_i);
        final CheckBox cbJ = (CheckBox) view.findViewById(R.id.cb_j);
        final Button btnBatal = (Button) view.findViewById(R.id.btn_batal);
        final Button btnHapus = (Button) view.findViewById(R.id.btn_hapus);
        final Button btnSimpan = (Button) view.findViewById(R.id.btn_simpan);

        //Load Data
        tvTitle.setText(item.getItem2());
        tvHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem9())*iv.parseNullDouble(item.getItem5())));
        edtHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem3())));
        edtJumlah.setText(item.getItem5());
        edtSatuan.setText(item.getItem6());
        edtDiskon.setText(item.getItem7());
        edtCatatan.setText(item.getItem8());
        if(item.getItem12().equals("DN")) {
            rbDN.setChecked(true);
        }else{
            rbTA.setChecked(true);
        }

        String[] listTag = item.getItem10().split(",");
        for(int i = 0; i < listTag.length; i++){

            switch (listTag[i].trim()){
                case "A":
                    cbA.setChecked(true);
                    break;
                case "B":
                    cbB.setChecked(true);
                    break;
                case "C":
                    cbC.setChecked(true);
                    break;
                case "D":
                    cbD.setChecked(true);
                    break;
                case "E":
                    cbE.setChecked(true);
                    break;
                case "F":
                    cbF.setChecked(true);
                    break;
                case "G":
                    cbG.setChecked(true);
                    break;
                case "H":
                    cbH.setChecked(true);
                    break;
                case "I":
                    cbI.setChecked(true);
                    break;
                case "J":
                    cbJ.setChecked(true);
                    break;
            }
        }

        edtHargaDiskon.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem9())));

        edtJumlah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(iv.parseNullDouble(edtJumlah.getText().toString()) > 0){
                    edtJumlah.setError(null);
                    newItem[0] = hitungHargaDiskon(item, edtJumlah, edtDiskon, edtHargaDiskon, tvHarga);
                }else{
                    edtJumlah.setError("Jumlah harus lebih dari 0");
                }
            }
        });

        edtJumlah.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_UP == motionEvent.getAction())
                {
                    loadJumlahEditor(context, edtJumlah, edtJumlah.getText().toString());
                    return true;
                }
                return true;
            }
        });

        rgJenisOrder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                String selectedJenis = "DN";
                if(i == rbTA.getId()) selectedJenis = "TA";
                int position = 0;
                boolean isExist = false;
                for(CustomItem itemAtPosition: listSelectedMenu){

                    if(item.getItem1().equals(itemAtPosition.getItem1()) && itemAtPosition.getItem12().equals(selectedJenis)){

                        isExist = true;
                        break;
                    }
                    position++;
                }

                if(isExist){

                    CustomItem itemAtPosition = listSelectedMenu.get(position);

                    tvTitle.setText(itemAtPosition.getItem2());
                    tvHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemAtPosition.getItem9())*iv.parseNullDouble(itemAtPosition.getItem5())));
                    edtHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemAtPosition.getItem3())));
                    edtJumlah.setText(itemAtPosition.getItem5());
                    edtSatuan.setText(itemAtPosition.getItem6());
                    edtDiskon.setText(itemAtPosition.getItem7());
                    edtCatatan.setText(itemAtPosition.getItem8());
                    edtHargaDiskon.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemAtPosition.getItem9())));
                    /*if(itemAtPosition.getItem12().equals("DN")) {
                        rbDN.setChecked(true);
                    }else{
                        rbTA.setChecked(true);
                    }*/

                }else{

                    tvTitle.setText(item.getItem2());
                    tvHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem9())*iv.parseNullDouble(item.getItem5())));
                    edtHarga.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem3())));
                    edtJumlah.setText("1");
                    edtSatuan.setText(item.getItem6());
                    edtDiskon.setText(item.getItem7());
                    edtCatatan.setText(item.getItem8());
                    edtHargaDiskon.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(item.getItem9())));
                    /*if(item.getItem12().equals("DN")) {
                        rbDN.setChecked(true);
                    }else{
                        rbTA.setChecked(true);
                    }*/
                }
            }
        });

        setCheckedTagMeja(cbA, cbB, cbC, cbD, cbE, cbF, cbG, cbH, cbI, cbJ, edtJumlah);

        if(fromInserted) edtJumlah.setText(String.valueOf(iv.parseNullLong(item.getItem5()) + 1));

        ivJmlMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String jml = edtJumlah.getText().toString();
                if(iv.parseNullInteger(jml) > 1){
                    int jmlBaru = iv.parseNullInteger(jml) - 1;
                    edtJumlah.setText(String.valueOf(jmlBaru));
                }
            }
        });

        ivJmlPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int jmlBaru = iv.parseNullInteger(edtJumlah.getText().toString()) + 1;
                edtJumlah.setText(String.valueOf(jmlBaru));
            }
        });

        edtDiskon.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(iv.parseNullDouble(edtDiskon.getText().toString()) >= 0){
                    if(iv.parseNullDouble(edtDiskon.getText().toString()) <= 100){
                        edtDiskon.setError(null);
                        newItem[0] = hitungHargaDiskon(item, edtJumlah, edtDiskon, edtHargaDiskon, tvHarga);
                    }else{
                        edtDiskon.setError("Jumlah harus kurang dari 100");
                    }
                }
            }
        });

        final AlertDialog alert = builder.create();
        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.dismiss();
            }
        });

        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog konfirmasiDelete = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin menghapus " + item.getItem2() + " dari daftar ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                listSelectedMenu.remove(position);
                                selectedMenuAdapter.notifyDataSetChanged();
                                alert.dismiss();
                                updateHargaTotal();
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

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Validasi
                if(iv.parseNullDouble(edtJumlah.getText().toString()) <= 0){
                    edtJumlah.setError("Jumlah harus lebih dari 0");
                    edtJumlah.requestFocus();
                    return;
                }

                if(iv.parseNullDouble(edtDiskon.getText().toString()) > 0){
                    if(iv.parseNullDouble(edtDiskon.getText().toString()) > 100){
                        edtDiskon.setError("Jumlah harus kurang dari 100");
                        edtDiskon.requestFocus();
                        return;
                    }
                }

                // Simpan data
                newItem[0].setItem8(edtCatatan.getText().toString());
                List<String> listTagMeja = new ArrayList<String>();

                if(cbA.isChecked())listTagMeja.add("A");
                if(cbB.isChecked())listTagMeja.add("B");
                if(cbC.isChecked())listTagMeja.add("C");
                if(cbD.isChecked())listTagMeja.add("D");
                if(cbE.isChecked())listTagMeja.add("E");
                if(cbF.isChecked())listTagMeja.add("F");
                if(cbG.isChecked())listTagMeja.add("G");
                if(cbH.isChecked())listTagMeja.add("H");
                if(cbI.isChecked())listTagMeja.add("I");
                if(cbJ.isChecked())listTagMeja.add("J");

                String tagMeja = "";
                int x = 0;
                for(String tag : listTagMeja){
                    if(x == 0){
                        tagMeja = tag;
                    }else{
                        tagMeja = tagMeja + ", " + tag;
                    }
                    x++;
                }

                newItem[0].setItem10(tagMeja);
                String jenisOrder = "DN";
                if(rbTA.isChecked()) jenisOrder = "TA";
                newItem[0].setItem12(jenisOrder);

                int position = 0;
                boolean isExist = false;
                for(CustomItem itemAtPosition: listSelectedMenu){

                    if(newItem[0].getItem1().equals(itemAtPosition.getItem1()) && newItem[0].getItem12().equals(itemAtPosition.getItem12())){

                        isExist = true;
                        break;
                    }
                    position++;
                }

                if(isExist){
                    listSelectedMenu.set(position, newItem[0]);
                }else{
                    listSelectedMenu.add(newItem[0]);
                }

                selectedMenuAdapter.notifyDataSetChanged();
                alert.dismiss();
                updateHargaTotal();
                //setTabPosition(2);
                if(phoneMode){

                    if(fromInserted){
                        TabLayout.Tab tab = tabLayout.getTabAt(1);
                        tab.select();
                    }else{
                        TabLayout.Tab tab = tabLayout.getTabAt(2);
                        tab.select();
                    }

                }

                edtSearchMenu.setText("");

            }
        });

        alert.show();

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private static void setCheckedTagMeja(final CheckBox cA, final CheckBox cB, final CheckBox cC, final CheckBox cD, final CheckBox cE, final CheckBox cF, final CheckBox cG, final CheckBox cH, final CheckBox cI, final CheckBox cJ, final EditText target){

        cA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cB.isChecked()) anotherCheck = true;
                if(cC.isChecked()) anotherCheck = true;
                if(cD.isChecked()) anotherCheck = true;
                if(cE.isChecked()) anotherCheck = true;
                if(cF.isChecked()) anotherCheck = true;
                if(cG.isChecked()) anotherCheck = true;
                if(cH.isChecked()) anotherCheck = true;
                if(cI.isChecked()) anotherCheck = true;
                if(cJ.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });

        cB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cA.isChecked()) anotherCheck = true;
                if(cC.isChecked()) anotherCheck = true;
                if(cD.isChecked()) anotherCheck = true;
                if(cE.isChecked()) anotherCheck = true;
                if(cF.isChecked()) anotherCheck = true;
                if(cG.isChecked()) anotherCheck = true;
                if(cH.isChecked()) anotherCheck = true;
                if(cI.isChecked()) anotherCheck = true;
                if(cJ.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });

        cC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cA.isChecked()) anotherCheck = true;
                if(cB.isChecked()) anotherCheck = true;
                if(cD.isChecked()) anotherCheck = true;
                if(cE.isChecked()) anotherCheck = true;
                if(cF.isChecked()) anotherCheck = true;
                if(cG.isChecked()) anotherCheck = true;
                if(cH.isChecked()) anotherCheck = true;
                if(cI.isChecked()) anotherCheck = true;
                if(cJ.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });

        cD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cA.isChecked()) anotherCheck = true;
                if(cB.isChecked()) anotherCheck = true;
                if(cC.isChecked()) anotherCheck = true;
                if(cE.isChecked()) anotherCheck = true;
                if(cF.isChecked()) anotherCheck = true;
                if(cG.isChecked()) anotherCheck = true;
                if(cH.isChecked()) anotherCheck = true;
                if(cI.isChecked()) anotherCheck = true;
                if(cJ.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });

        cE.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cA.isChecked()) anotherCheck = true;
                if(cB.isChecked()) anotherCheck = true;
                if(cC.isChecked()) anotherCheck = true;
                if(cD.isChecked()) anotherCheck = true;
                if(cF.isChecked()) anotherCheck = true;
                if(cG.isChecked()) anotherCheck = true;
                if(cH.isChecked()) anotherCheck = true;
                if(cI.isChecked()) anotherCheck = true;
                if(cJ.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });

        cF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cA.isChecked()) anotherCheck = true;
                if(cB.isChecked()) anotherCheck = true;
                if(cC.isChecked()) anotherCheck = true;
                if(cD.isChecked()) anotherCheck = true;
                if(cE.isChecked()) anotherCheck = true;
                if(cG.isChecked()) anotherCheck = true;
                if(cH.isChecked()) anotherCheck = true;
                if(cI.isChecked()) anotherCheck = true;
                if(cJ.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });

        cG.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cA.isChecked()) anotherCheck = true;
                if(cB.isChecked()) anotherCheck = true;
                if(cC.isChecked()) anotherCheck = true;
                if(cD.isChecked()) anotherCheck = true;
                if(cE.isChecked()) anotherCheck = true;
                if(cF.isChecked()) anotherCheck = true;
                if(cH.isChecked()) anotherCheck = true;
                if(cI.isChecked()) anotherCheck = true;
                if(cJ.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });

        cH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cA.isChecked()) anotherCheck = true;
                if(cB.isChecked()) anotherCheck = true;
                if(cC.isChecked()) anotherCheck = true;
                if(cD.isChecked()) anotherCheck = true;
                if(cE.isChecked()) anotherCheck = true;
                if(cF.isChecked()) anotherCheck = true;
                if(cG.isChecked()) anotherCheck = true;
                if(cI.isChecked()) anotherCheck = true;
                if(cJ.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });

        cI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cA.isChecked()) anotherCheck = true;
                if(cB.isChecked()) anotherCheck = true;
                if(cC.isChecked()) anotherCheck = true;
                if(cD.isChecked()) anotherCheck = true;
                if(cE.isChecked()) anotherCheck = true;
                if(cF.isChecked()) anotherCheck = true;
                if(cG.isChecked()) anotherCheck = true;
                if(cH.isChecked()) anotherCheck = true;
                if(cJ.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });

        cJ.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                boolean anotherCheck = false;
                if(cA.isChecked()) anotherCheck = true;
                if(cB.isChecked()) anotherCheck = true;
                if(cC.isChecked()) anotherCheck = true;
                if(cD.isChecked()) anotherCheck = true;
                if(cE.isChecked()) anotherCheck = true;
                if(cF.isChecked()) anotherCheck = true;
                if(cG.isChecked()) anotherCheck = true;
                if(cH.isChecked()) anotherCheck = true;
                if(cI.isChecked()) anotherCheck = true;

                if(anotherCheck){

                    if(b){
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) + 1));
                    }else{
                        target.setText(String.valueOf(iv.parseNullInteger(target.getText().toString()) - 1));
                    }
                }
            }
        });
    }

    private static CustomItem hitungHargaDiskon(CustomItem item, EditText edtJumlah, EditText edtDiskon, EditText edtHargaDiskon, TextView tvTotal){

        double jumlah = iv.parseNullDouble(edtJumlah.getText().toString());
        double harga = iv.parseNullDouble(item.getItem3());
        double diskon = iv.parseNullDouble(edtDiskon.getText().toString());
        double hargaDiskon = (diskon != 0) ? (harga - (harga * diskon / 100)): harga;
        edtHargaDiskon.setText(iv.ChangeToRupiahFormat(hargaDiskon));
        tvTotal.setText(iv.ChangeToRupiahFormat(jumlah*hargaDiskon));
        CustomItem newItem = new CustomItem(item.getItem1(),item.getItem2(),item.getItem3(),item.getItem4(),edtJumlah.getText().toString(),item.getItem6(), edtDiskon.getText().toString(),item.getItem8(),iv.doubleToStringRound(hargaDiskon), item.getItem10(), item.getItem11(), item.getItem12());
        return newItem;
    }
    //endregion

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

        if (doubleBackToExitPressedOnce) {

            super.onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }

        if(!doubleBackToExitPressedOnce){

            AlertDialog builder = new AlertDialog.Builder(DetailOrder.this)
                    .setIcon(R.mipmap.ic_warning)
                    .setTitle("Peringatan")
                    .setMessage("Pesanan belum terproses (tekan Proses pada Detail Pesanan untuk memproses).\nApakah anda yakin ingin kembali tanpa memproses pesanan?")
                    .setCancelable(false)
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            doubleBackToExitPressedOnce = true;
                            onBackPressed();
                        }
                    })
                    .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            doubleBackToExitPressedOnce=false;
                        }
                    }).show();
        }
    }

    //region Prinnter

    //region cashier
    public boolean printCashier(String upsell, String noBukti, String timestamp, String noMeja, List<CustomItem> pesanan){

        return runPrintCashierSequence(upsell, timestamp, noBukti, noMeja, pesanan);
    }

    private boolean runPrintCashierSequence(String upsell, String timestamp, String noBukti, String noMeja, List<CustomItem> pesanan) {
        if (!initializeObject()) {
            return false;
        }

        if (!createCashierData(upsell, noBukti, timestamp, noMeja, pesanan)) {
            finalizeObject();
            return false;
        }

        if (!printData(SavedPrinterManager.TAG_IP1)) {
            finalizeObject();
            return false;
        }

        return true;
    }

    private boolean createCashierData(String upsell, String noBukti, String timestamp, String noMeja, List<CustomItem> pesanan) {

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
            textData.append(noBukti+"\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatDateDisplay)+"-");
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatTime)+"\n");
            if(upselling.equals("1")){
                textData.append(noMeja +"/"+ jumlahPlg+" plg" + "/" +  session.getName() +"\n");
            }else{
                textData.append(noMeja +"/"+ jumlahPlg+" plg" + "/" + "RE " + upselling + "/" + session.getName() +"\n");
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

                String itemToPrint = item.getItem5() + " ("+ item.getItem12()+ ")" +" X "+ item.getItem2();
                if(item.getItem12().toUpperCase().equals("DN")){
                    itemToPrint = item.getItem5() + " X "+ item.getItem2();
                }
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
            textData.append(noBukti+"\n");
            if(upselling.equals("1")){
                textData.append(noMeja +"/"+ jumlahPlg+" plg"+ "/" +  session.getName() +"\n");
            }else{
                textData.append(noMeja +"/"+ jumlahPlg+" plg" + "/" + "RE " + upselling + "/" + session.getName() +"\n");
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

                String itemToPrint = item.getItem5() + " ("+ item.getItem12()+ ")" +" X "+ item.getItem2();
                if(item.getItem12().toUpperCase().equals("DN")){
                    itemToPrint = item.getItem5() + " X "+ item.getItem2();
                }
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
            textData.append(noBukti+"\n");
            if(upselling.equals("1")){
                textData.append(noMeja +"/"+ jumlahPlg+" plg" + "/" +  session.getName() +"\n");
            }else{
                textData.append(noMeja +"/"+ jumlahPlg+" plg" + "/" + "RE " + upselling + "/" + session.getName() +"\n");
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

                String itemToPrint = item.getItem5() + " ("+ item.getItem12()+ ")" +" X "+ item.getItem2();
                if(item.getItem12().toUpperCase().equals("DN")){
                    itemToPrint = item.getItem5() + " X "+ item.getItem2();
                }
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
            runOnUiThread(new Runnable() {
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
            runOnUiThread(new Runnable() {
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

        runOnUiThread(new Runnable() {
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
