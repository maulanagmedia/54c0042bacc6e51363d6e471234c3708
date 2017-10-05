package gmedia.net.id.restauranttakingorder.Order;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.IdRes;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import gmedia.net.id.restauranttakingorder.Order.Adapter.KategoriMenuAdapter;
import gmedia.net.id.restauranttakingorder.Order.Adapter.MenuByKategoriAdapter;
import gmedia.net.id.restauranttakingorder.Order.Adapter.SelectedMenuAdapter;
import gmedia.net.id.restauranttakingorder.PrinterUtils.PrinterTemplate;
import gmedia.net.id.restauranttakingorder.PrinterUtils.ShowMsg;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.FormatItem;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class DetailOrder extends AppCompatActivity{

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
    private String kategoriMenu = "", kdMeja = "", noMeja = "", statusMeja = "";
    private SessionManager session;
    private EditText edtNoBukti, edtUrutan;
    private String noBukti = "";
    private boolean editMode = false;
    private ProgressBar pbLoadOrder;
    private boolean onProcess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Pilih Menu");
        initUI();
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
                    simpanData();
                }else{
                    Toast.makeText(DetailOrder.this, "Harap tunggu hingga proses selesai", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void simpanData() {

        onProcess = true;
        // penjualan_d
        JSONArray penjualanD = new JSONArray();

        final String timestampNow = iv.getCurrentDate(FormatItem.formatTimestamp);
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

        ApiVolley request = new ApiVolley(DetailOrder.this, jBody, "POST", ServerURL.saveOrder, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){
                        printToKitchen(noBukti, timestampNow, noMeja, listSelectedMenu);
                    }
                    onProcess = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    onProcess = false;
                }
            }

            @Override
            public void onError(String result) {
                onProcess = false;
            }
        });
    }

    private void printToKitchen(String nobukti, String timestamp, String nomeja, List<CustomItem> pesanan){

        PrinterTemplate printerTemplate = new PrinterTemplate(DetailOrder.this);
        boolean printed = printerTemplate.printKitchen(nobukti, timestamp, nomeja, pesanan);
    }

    private void getNoBukti() {

        pbLoadOrder.setVisibility(View.VISIBLE);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kdmeja", kdMeja);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailOrder.this, jBody, "POST", ServerURL.getNoBukti, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        noBukti = response.getJSONObject("response").getString("nobukti");
                        String urutan = response.getJSONObject("response").getString("urutan");
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
        ApiVolley request = new ApiVolley(DetailOrder.this, new JSONObject(), "GET", ServerURL.getKategori, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
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

        ApiVolley request = new ApiVolley(DetailOrder.this, new JSONObject(), "GET", ServerURL.getMenu + kategoriMenu, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listMenu.add(new CustomItem(jo.getString("kdbrg"), jo.getString("nmbrg"),jo.getString("harga"),jo.getString("link"),"1",jo.getString("satuan"),jo.getString("diskon"),"",jo.getString("harga_diskon"),""));
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

        final CustomItem[] newItem = {new CustomItem(item.getItem1(), item.getItem2(), item.getItem3(), item.getItem4(), item.getItem5(), item.getItem6(), item.getItem7(), item.getItem8(), item.getItem9())};

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
        final CustomItem[] newItem = {new CustomItem(item.getItem1(), item.getItem2(), item.getItem3(), item.getItem4(), item.getItem5(), item.getItem6(), item.getItem7(), item.getItem8(), item.getItem9())};

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
        CustomItem newItem = new CustomItem(item.getItem1(),item.getItem2(),item.getItem3(),item.getItem4(),edtJumlah.getText().toString(),item.getItem6(), edtDiskon.getText().toString(),item.getItem8(),iv.doubleToStringRound(hargaDiskon));
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
}
