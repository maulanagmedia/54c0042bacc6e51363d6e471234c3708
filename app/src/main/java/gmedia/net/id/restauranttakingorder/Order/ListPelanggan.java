package gmedia.net.id.restauranttakingorder.Order;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.Order.Adapter.ListPelangganAdapter;
import gmedia.net.id.restauranttakingorder.R;

public class ListPelanggan extends AppCompatActivity {

    private ItemValidation iv = new ItemValidation();
    private EditText edtSearch;
    private ListView lvPelanggan;
    private ProgressBar pbLoading;
    private List<CustomItem> listPelanggan;
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pelanggan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setTitle("Daftar Pelanggan");

        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pelanggan_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void initUI() {

        edtSearch = (EditText) findViewById(R.id.edt_search);
        lvPelanggan = (ListView) findViewById(R.id.lv_list);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        getData();
    }

    private void getData() {

        pbLoading.setVisibility(View.VISIBLE);
        listPelanggan = new ArrayList<>();

        listPelanggan.add(new CustomItem("1", "Maulana", "Jangli", "Semarang", "081"));
        listPelanggan.add(new CustomItem("2", "Test 2", "Jangli", "Semarang", "081"));
        listPelanggan.add(new CustomItem("1", "Contoh 3", "Jangli", "Semarang", "081"));

        pbLoading.setVisibility(View.GONE);
        getSearch();
        getTable(listPelanggan);
    }

    private void getTable(List<CustomItem> listItem) {

        lvPelanggan.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            ListPelangganAdapter adapter = new ListPelangganAdapter(ListPelanggan.this, listItem);
            lvPelanggan.setAdapter(adapter);

            lvPelanggan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    final CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);

                    AlertDialog builder = new AlertDialog.Builder(ListPelanggan.this)
                            .setTitle("Konfirmasi")
                            .setMessage("Anda memilih pelanggan "+ item.getItem2()+" ?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("kdcus",item.getItem1());
                                    returnIntent.putExtra("nama",item.getItem2());
                                    setResult(Activity.RESULT_OK, returnIntent);
                                    finish();
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
        }
    }

    private void getSearch() {

        if(firstLoad){
            firstLoad = false;

            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(edtSearch.getText().length() == 0){
                        getTable(listPelanggan);
                    }
                }
            });
        }

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<CustomItem> items = new ArrayList<CustomItem>();
                    String keyword = edtSearch.getText().toString().toUpperCase();
                    for(CustomItem item: listPelanggan){

                        if(item.getItem2().toUpperCase().contains(keyword)) items.add(item);
                    }

                    getTable(items);
                    iv.hideSoftKey(ListPelanggan.this);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.nav_add:
                showTambahPelanggan();

        }

        return super.onOptionsItemSelected(item);
    }

    private void showTambahPelanggan(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(ListPelanggan.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_tambah_pelanggan, null);
        builder.setView(view);
        builder.setTitle("Tambah Pelanggan");

        final EditText edtNamaPelanggan = (EditText) view.findViewById(R.id.edt_nama_pelanggan);
        final EditText edtAlamat = (EditText) view.findViewById(R.id.edt_alamat);
        final EditText edtKota= (EditText) view.findViewById(R.id.edt_kota);
        final EditText edtTelepon= (EditText) view.findViewById(R.id.edt_telepon);
        final Button btnBatal = (Button) view.findViewById(R.id.btn_batal);
        final Button btnSimpan = (Button) view.findViewById(R.id.btn_simpan);

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
                if(edtNamaPelanggan.getText().length() <= 0){
                    edtNamaPelanggan.setError("Jumlah harus lebih dari 0");
                    edtNamaPelanggan.requestFocus();
                    return;
                }else{
                    edtNamaPelanggan.setError(null);
                }

                AlertDialog konfirmasiSimpan = new AlertDialog.Builder(ListPelanggan.this)
                        .setTitle("Konfirmasi")
                        .setMessage("Simpan pelanggan " + edtNamaPelanggan.getText().toString() + " ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                                alert.dismiss();
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

    public static void showEditPelanggan(final Context context, CustomItem item){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_tambah_pelanggan, null);
        builder.setView(view);
        builder.setTitle("Pengaturan Pelanggan");

        final EditText edtNamaPelanggan = (EditText) view.findViewById(R.id.edt_nama_pelanggan);
        final EditText edtAlamat = (EditText) view.findViewById(R.id.edt_alamat);
        final EditText edtKota= (EditText) view.findViewById(R.id.edt_kota);
        final EditText edtTelepon= (EditText) view.findViewById(R.id.edt_telepon);
        final Button btnBatal = (Button) view.findViewById(R.id.btn_batal);
        final Button btnSimpan = (Button) view.findViewById(R.id.btn_simpan);

        edtNamaPelanggan.setText(item.getItem2());
        edtAlamat.setText(item.getItem3());
        edtKota.setText(item.getItem4());
        edtTelepon.setText(item.getItem5());

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
                if(edtNamaPelanggan.getText().length() <= 0){
                    edtNamaPelanggan.setError("Jumlah harus lebih dari 0");
                    edtNamaPelanggan.requestFocus();
                    return;
                }else{
                    edtNamaPelanggan.setError(null);
                }

                AlertDialog konfirmasiSimpan = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Simpan pelanggan " + edtNamaPelanggan.getText().toString() + " ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                                alert.dismiss();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
