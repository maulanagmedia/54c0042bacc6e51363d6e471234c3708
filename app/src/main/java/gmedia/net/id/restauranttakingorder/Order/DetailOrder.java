package gmedia.net.id.restauranttakingorder.Order;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
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
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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
import gmedia.net.id.restauranttakingorder.PrinterUtils.PrinterChecker;
import gmedia.net.id.restauranttakingorder.PrinterUtils.ShowMsg;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.FormatItem;
import gmedia.net.id.restauranttakingorder.Utils.SavedPrinterManager;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class DetailOrder extends AppCompatActivity implements ReceiveListener{

    private static final String TAG = "DetailOrder";
    private ListView lvKategori;
    private EditText edtSearchMenu;
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
    private String kategoriMenu = "", kdMeja = "", statusMeja = "";
    private SessionManager session;
    private EditText edtNoBukti, edtUrutan;
    private static String noBukti = "";
    private static String noMeja = "";
    private boolean editMode = false;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);
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
            statusMeja = bundle.getString("statusmeja");
            noBukti = bundle.getString("nobukti");

            edtNoMeja.setText(noMeja);
            setSelectedOrderEvent();
            getKategoriData();
            if(noBukti != null && noBukti.length() > 0){

                editMode = true;
            }else{

                editMode = false;
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

                if(edtUrutan.getText().toString().length() == 0){

                    Toast.makeText(DetailOrder.this, "Urutan tidak termuat, periksa koneksi anda", Toast.LENGTH_LONG).show();
                    return;
                }

                if(listSelectedMenu == null || listSelectedMenu.size() == 0){

                    Toast.makeText(DetailOrder.this, "Harap pilih minimal satu menu", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!onProcess){
                    AlertDialog dialog = new AlertDialog.Builder(DetailOrder.this)
                            .setTitle("Konfirmasi")
                            .setMessage("Proses pesanan "+ noBukti+ " ?")
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
                            .show();
                }else{
                    Toast.makeText(DetailOrder.this, "Harap tunggu hingga proses selesai", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void simpanData() {

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
            penjualan.put("jumlah_plg", noMeja);
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

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("penjualan", penjualan);
            jBody.put("penjualan_d", penjualanD);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrder.this, jBody, "POST", serverURL.saveOrder(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        progressDialog.dismiss();
                        String message1 = response.getJSONObject("response").getString("message");
                        for(int i = 0; i < toastTimer; i++){
                            Toast.makeText(DetailOrder.this, message1 + ".\n Tunggu hingga proses mencetak selesai, aplikasi akan menuju ke daftar transaksi", Toast.LENGTH_LONG).show();
                        }

                        if(printStatus){

                            printStatus = false;
                            printDataAll();
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
                    Toast.makeText(DetailOrder.this, "Gagal menyimpan data, harap ulangi", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                onProcess = false;
                progressDialog.dismiss();
                Toast.makeText(DetailOrder.this, "Gagal menyimpan data, harap ulangi", Toast.LENGTH_LONG).show();
            }
        });
    }

    //region =================================== Setting printer
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

            String message = status + " pada saat mencetak "+ state;
            for (int i = 0; i < toastTimer; i++)
            {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }

        if(printState == 1){

            //loadPrintingDialog(context, "Printing kitchen label...");
            printToKitchen(noBukti, timestampNow, noMeja, listSelectedMenu);
        }else if(printState == 2){

            //loadPrintingDialog(context, "Printing bar label...");
            printToBar(noBukti, timestampNow, noMeja, listSelectedMenu);
        }else if(printState == 3){

            //finish printing
            printStatus = true;
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("riwayat", true);
            context.startActivity(intent);
            ((Activity)context).finish();
        }
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


        dialogLoading = builder
                .setPositiveButton("Skip", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        changePrintState(mContext, 1, "Printer belum di atur");
                        dialogLoading.dismiss();
                    }
                })
                .setCancelable(false)
                .create();

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
                                        .setMessage("Printout CASHIER tidak akan tercetak")
                                        .setPositiveButton("Lanjutkan", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                changePrintState(mContext, 1, "Gagal mencetak");
                                            }
                                        })
                                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
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
            }else{
                changePrintState(mContext, Epos2CallbackCode.CODE_SUCCESS, "Berhasil mencetak");
            }

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
                                        .setMessage("Printout KITCHEN tidak akan tercetak")
                                        .setPositiveButton("Lanjutkan", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                changePrintState(mContext, 1, "Gagal mencetak");
                                            }
                                        })
                                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                printToKitchen(nobukti, timestamp, nomeja, pesanan);
                                            }
                                        })
                                        .show();
                            }
                        }).show();
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
            }else{
                changePrintState(mContext, Epos2CallbackCode.CODE_SUCCESS, "Berhasil mencetak");
            }

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
                                        .setPositiveButton("Lanjutkan", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                changePrintState(mContext, 1, "Gagal mencetak");
                                            }
                                        })
                                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                printToBar(nobukti, timestamp, nomeja, pesanan);
                                            }
                                        })
                                        .show();
                            }
                        }).show();


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
                loadEditOrderDialog(i);
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
                        setManuSearchView();
                        setMenuTable(null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    setManuSearchView();
                    setMenuTable(null);
                }
            }

            @Override
            public void onError(String result) {
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_edit_order, null);
        builder.setView(view);

        final CustomItem[] newItem = {new CustomItem(item.getItem1(), item.getItem2(), item.getItem3(), item.getItem4(), item.getItem5(), item.getItem6(), item.getItem7(), item.getItem8(), item.getItem9(),item.getItem10(), item.getItem11())};

        final TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        final TextView tvHarga = (TextView) view.findViewById(R.id.tv_harga);
        final EditText edtHarga = (EditText) view.findViewById(R.id.edt_harga);
        final EditText edtJumlah = (EditText) view.findViewById(R.id.edt_jumlah);
        final EditText edtSatuan = (EditText) view.findViewById(R.id.edt_satuan);
        final EditText edtDiskon = (EditText) view.findViewById(R.id.edt_diskon);
        final EditText edtHargaDiskon = (EditText) view.findViewById(R.id.edt_harga_diskon);
        final EditText edtCatatan = (EditText) view.findViewById(R.id.edt_catatan);
        final EditText edtTagMeja = (EditText) view.findViewById(R.id.edt_tag_meja);
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
        edtTagMeja.setText("A");

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

                AlertDialog konfirmasiSimpan = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Simpan " + item.getItem2() + " ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                newItem[0].setItem8(edtCatatan.getText().toString());
                                newItem[0].setItem10(edtTagMeja.getText().toString());
                                addMoreSelectedMenu(newItem[0]);
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

        alert.show();

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private static void addMoreSelectedMenu(CustomItem selectedMenu){

        if(listSelectedMenu != null){

            if(listSelectedMenu.size() > 0){

                boolean isExist = false;
                int existIndex = 0;
                double lastJumlah = 0;
                for(CustomItem item: listSelectedMenu){

                    if(item.getItem1().equals(selectedMenu.getItem1())){
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

    //region dialogEdit

    private void loadEditOrderDialog(final int position){

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrder.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_edit_order, null);
        builder.setView(view);

        final CustomItem item = listSelectedMenu.get(position);
        final CustomItem[] newItem = {new CustomItem(item.getItem1(), item.getItem2(), item.getItem3(), item.getItem4(), item.getItem5(), item.getItem6(), item.getItem7(), item.getItem8(), item.getItem9(), item.getItem10(), item.getItem11())};

        final TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        final TextView tvHarga = (TextView) view.findViewById(R.id.tv_harga);
        final EditText edtHarga = (EditText) view.findViewById(R.id.edt_harga);
        final EditText edtJumlah = (EditText) view.findViewById(R.id.edt_jumlah);
        final EditText edtSatuan = (EditText) view.findViewById(R.id.edt_satuan);
        final EditText edtDiskon = (EditText) view.findViewById(R.id.edt_diskon);
        final EditText edtHargaDiskon = (EditText) view.findViewById(R.id.edt_harga_diskon);
        final EditText edtCatatan = (EditText) view.findViewById(R.id.edt_catatan);
        final EditText edtTagMeja = (EditText) view.findViewById(R.id.edt_tag_meja);
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
        edtTagMeja.setText(item.getItem10());
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

                AlertDialog konfirmasiDelete = new AlertDialog.Builder(DetailOrder.this)
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

                String tagMeja = edtTagMeja.getText().toString();
                newItem[0].setItem10(tagMeja);
                AlertDialog konfirmasiSimpan = new AlertDialog.Builder(DetailOrder.this)
                        .setTitle("Konfirmasi")
                        .setMessage("Simpan perubahan " + item.getItem2() + " ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                newItem[0].setItem8(edtCatatan.getText().toString());
                                listSelectedMenu.set(position, newItem[0]);
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

        alert.show();

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private static CustomItem hitungHargaDiskon(CustomItem item, EditText edtJumlah, EditText edtDiskon, EditText edtHargaDiskon, TextView tvTotal){

        double jumlah = iv.parseNullDouble(edtJumlah.getText().toString());
        double harga = iv.parseNullDouble(item.getItem3());
        double diskon = iv.parseNullDouble(edtDiskon.getText().toString());
        double hargaDiskon = (diskon != 0) ? (harga - (harga * diskon / 100)): harga;
        edtHargaDiskon.setText(iv.ChangeToRupiahFormat(hargaDiskon));
        tvTotal.setText(iv.ChangeToRupiahFormat(jumlah*hargaDiskon));
        CustomItem newItem = new CustomItem(item.getItem1(),item.getItem2(),item.getItem3(),item.getItem4(),edtJumlah.getText().toString(),item.getItem6(), edtDiskon.getText().toString(),item.getItem8(),iv.doubleToStringRound(hargaDiskon), item.getItem10(), item.getItem11());
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
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }


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
            mPrinter.addText(textData.toString());
            textData.append(noBukti+"\n");
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatDateDisplay)+"\n");
            textData.append(iv.ChangeFormatDateString(timestamp, FormatItem.formatTimestamp, FormatItem.formatTime)+"\n");
            textData.append(noMeja+ "-" + urutan +"\n");
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

                String itemToPrint = item.getItem5() +" "+ item.getItem2();
                textData.append( itemToPrint+"\n");

                if(item.getItem8().length()>0){
                    String[] s = item.getItem8().split("\\r?\\n");
                    for(String note: s){
                        textData.append( "   " + note +"\n");
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
            textData.append(noMeja+"-"+urutan+"\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextSize(2, 2);
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
            mPrinter.addTextSize(2, 2);

            // 1. id, 2. nama, 3. harga, 4. gambar,  5. banyak, 6. satuan, 7. diskon, 8. catatan, 9. hargaDiskon, 10. tag meja

            int x = 1;
            for(CustomItem item : pesanan){

                String itemToPrint = item.getItem5() +" "+ item.getItem2();
                textData.append( itemToPrint+"\n");

                if(item.getItem8().length()>0){
                    String[] s = item.getItem8().split("\\r?\\n");
                    int j = 0;
                    for(String note: s){

                        if(s.length == 1){
                            textData.append( "  (" + note +")\n");
                        }else{
                            if(j == 0){
                                textData.append( "  (" + note +"\n");
                            }else if(j == s.length - 1){
                                textData.append( "   " + note +")\n");
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
            textData.append(noMeja+"-"+urutan+"\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            mPrinter.addTextSize(2, 2);
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
            mPrinter.addTextSize(2, 2);

            // 1. id, 2. nama, 3. harga, 4. gambar,  5. banyak, 6. satuan, 7. diskon, 8. catatan, 9. hargaDiskon, 10. tag meja

            int x = 1;
            for(CustomItem item : pesanan){

                String itemToPrint = item.getItem5() +" "+ item.getItem2();
                textData.append( itemToPrint+"\n");

                if(item.getItem8().length()>0){
                    String[] s = item.getItem8().split("\\r?\\n");
                    int j = 0;
                    for(String note: s){

                        if(s.length == 1){
                            textData.append( "  (" + note +")\n");
                        }else{
                            if(j == 0){
                                textData.append( "  (" + note +"\n");
                            }else if(j == s.length - 1){
                                textData.append( "   " + note +")\n");
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
