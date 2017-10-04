package gmedia.net.id.restauranttakingorder.Order;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MainOrder extends Fragment {

    private static final String TAG = "Order";
    private View layout;
    private Context context;
    private ListView lvKategori;
    private EditText edtSearchMenu;
    private RecyclerView rvListMenu;
    private EditText edtNamaPelanggan, edtNoMeja;
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
    private ImageButton ibPelanggan;
    public static final int GET_PELANGGAN = 12;
    private FloatingActionButton fabScanBarcode;
    private String kategoriMenu = "";
    private SessionManager session;

    public MainOrder() {
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
        layout = inflater.inflate(R.layout.fragment_main_order, container, false);
        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        lvKategori = (ListView) layout.findViewById(R.id.lv_kategori);
        edtSearchMenu = (EditText) layout.findViewById(R.id.edt_search_menu);
        rvListMenu = (RecyclerView) layout.findViewById(R.id.rv_list_menu);
        pbLoadKategori = (ProgressBar) layout.findViewById(R.id.pb_load_kategori);
        pbLoadMenu = (ProgressBar) layout.findViewById(R.id.pb_load_menu);
        edtNamaPelanggan = (EditText) layout.findViewById(R.id.edt_nama_pelanggan);
        edtNoMeja = (EditText) layout.findViewById(R.id.edt_no_meja);
        ibPelanggan = (ImageButton) layout.findViewById(R.id.ib_pelanggan);
        lvOrder = (ListView) layout.findViewById(R.id.lv_order);
        tvTotal = (TextView) layout.findViewById(R.id.tv_total);
        btnCetak = (Button) layout.findViewById(R.id.btn_cetak);
        btnSimpan = (Button) layout.findViewById(R.id.btn_simpan);
        fabScanBarcode = (FloatingActionButton) layout.findViewById(R.id.fab_scan);

        session = new SessionManager(context);
        setSelectedOrderEvent();
        getKategoriData();

        ibPelanggan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ListPelanggan.class);
                startActivityForResult(intent, GET_PELANGGAN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GET_PELANGGAN) {

            super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == Activity.RESULT_OK){
                String nama = data.getStringExtra("nama");
                edtNamaPelanggan.setText(nama);
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public static void getBarcodeData(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {

                Log.d(TAG, "onActivityResult: Scan failed ");
            } else {

                Log.d(TAG, "barcode: "+result.getContents());
            }
        }
    }

    private void setSelectedOrderEvent() {

        listSelectedMenu = new ArrayList<>();
        selectedMenuAdapter = new SelectedMenuAdapter((Activity) context, listSelectedMenu);
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

        IntentIntegrator integrator = new IntentIntegrator((Activity) context);
        //integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.initiateScan();
    }

    //region setting kategori
    private void getKategoriData() {

        pbLoadKategori.setVisibility(View.VISIBLE);
        listKategori = new ArrayList<>();
        ApiVolley request = new ApiVolley(context, new JSONObject(), "GET", ServerURL.getKategori, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
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

                        setKategoriTable();
                    }else{
                        setKategoriTable();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    setKategoriTable();
                }
            }

            @Override
            public void onError(String result) {
                setKategoriTable();
            }
        });
    }

    private void setKategoriTable() {

        lvKategori.setAdapter(null);

        if(listKategori != null && listKategori.size() > 0){

            final KategoriMenuAdapter adapter = new KategoriMenuAdapter((Activity) context, listKategori);
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
            getMenuByKategori(item);
        }
    }
    //endregion

    //region Menu
    private void getMenuByKategori(CustomItem selected) {

        listMenu = new ArrayList<>();

        listMenu.add(new CustomItem("1", "Rawon Enak", "20000", "", "1", "Porsi", "0", "", "20000")); // id, nama, harga, gambar, banyak, satuan, diskon, catatan, hargaDiskon
        listMenu.add(new CustomItem("2", "Rawon Goreng", "25000", "", "1", "Porsi", "0", "", "25000"));
        listMenu.add(new CustomItem("3", "Rawon Balado", "25000", "", "1", "Porsi", "0", "", "25000"));
        listMenu.add(new CustomItem("4", "Rawon Telur Puyuh", "25000", "", "1", "Porsi", "0", "", "25000"));

        setManuSearchView();
        setMenuTable(listMenu);
    }

    private void setMenuTable(List<CustomItem> listItem) {

        rvListMenu.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            final MenuByKategoriAdapter menuAdapter = new MenuByKategoriAdapter(context, listItem);

            final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 1);
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
                    iv.hideSoftKey(context);
                    return true;
                }
                return false;
            }
        });
    }
    //endregion

    //region Selected Order Menu
    public static void addMoreSelectedMenu(CustomItem selectedMenu){

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

                    double jmlBaru = lastJumlah+1;
                    selectedMenu.setItem5(iv.doubleToStringRound(jmlBaru));
                    listSelectedMenu.set(existIndex, selectedMenu);
                }else{

                    selectedMenu.setItem5("1");
                    listSelectedMenu.add(selectedMenu);
                }
                selectedMenuAdapter.notifyDataSetChanged();
            }else{
                selectedMenu.setItem5("1");
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
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

                AlertDialog konfirmasiSimpan = new AlertDialog.Builder(context)
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
    }

    private CustomItem hitungHargaDiskon(CustomItem item, EditText edtJumlah, EditText edtDiskon, EditText edtHargaDiskon, TextView tvTotal){

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

}
