package gmedia.net.id.restauranttakingorder.RiwayatPemesanan;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter.ListTransaksiAdapter;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter.MenuByTransaksiAdapter;
import gmedia.net.id.restauranttakingorder.Utils.FormatItem;

public class MainRiwayatPemesanan extends Fragment {

    private View layout;
    private Context context;
    private ItemValidation iv = new ItemValidation();
    private EditText edtNamaPelanggan;
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
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_main_riwayat_pemesanan, container, false);
        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        edtNamaPelanggan = (EditText) layout.findViewById(R.id.edt_nama_pelanggan);
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

        listTransaksi = new ArrayList<>();
        listMenu = new ArrayList<>();

        getDataTransaksi();
        setEvent();
    }

    private void getDataTransaksi() {

        pbLoadTransaksi.setVisibility(View.VISIBLE);
        listTransaksi = new ArrayList<>();

        listTransaksi.add(new CustomItem("NOTA0001", "1", "Maulana", "215000", "2017-09-10 08:30:34", "3"));
        listTransaksi.add(new CustomItem("NOTA0002", "", "Umum", "500000", "2017-09-10 14:21:34", "5"));
        listTransaksi.add(new CustomItem("NOTA0003", "2", "Husni", "160000", "2017-09-11 10:43:34", "1"));
        listTransaksi.add(new CustomItem("NOTA0004", "1", "Maulana", "350000", "2017-09-12 11:43:34", "2"));

        pbLoadTransaksi.setVisibility(View.GONE);
        getListTransaksi(listTransaksi);
    }

    private void getListTransaksi(List<CustomItem> listItem) {

        lvTransaksi.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            ListTransaksiAdapter adapter = new ListTransaksiAdapter((Activity) context, listItem);
            lvTransaksi.setAdapter(adapter);

            lvTransaksi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
                    getDetailTransaksi(item);

                }
            });
        }
    }

    private void getDetailTransaksi(CustomItem selectedItem) {

        pbLoadMenu.setVisibility(View.VISIBLE);

        tvNamaPelanggan.setText(selectedItem.getItem3());
        tvNoNota.setText(selectedItem.getItem1());
        tvWaktu.setText(iv.ChangeFormatDateString(selectedItem.getItem5(), FormatItem.formatTimestamp, FormatItem.formatTime));
        tvNoMeja.setText(selectedItem.getItem6());
        tvTotal.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(selectedItem.getItem4())));

        listMenu = new ArrayList<>();

        listMenu.add(new CustomItem("1", "Rawon Enak", "20000", "", "2", "40000")); // id, nama, harga, gambar, banyak, satuan, diskon, catatan, hargaDiskon
        listMenu.add(new CustomItem("2", "Rawon Goreng", "25000", "", "3", "75000"));
        listMenu.add(new CustomItem("3", "Rawon Balado", "25000", "", "2", "50000"));
        listMenu.add(new CustomItem("4", "Rawon Telur Puyuh", "25000", "", "2", "50000"));

        pbLoadMenu.setVisibility(View.GONE);
        setMenuTable(listMenu);
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

                String namaPelanggan = edtNamaPelanggan.getText().toString();
                String tanggalCari = edtTanggal.getText().toString();
                if(!iv.isValidFormat(FormatItem.formatDateDisplay, tanggalCari)) {
                    tanggalCari = "";
                    edtTanggal.setText(tanggalCari);
                }

                List<CustomItem> items = new ArrayList<CustomItem>();

                for (CustomItem item:listTransaksi){
                    String tgl = iv.ChangeFormatDateString(item.getItem5(), FormatItem.formatTimestamp, FormatItem.formatDateDisplay);

                    if(namaPelanggan.length() > 0 && tanggalCari.length() > 0){

                        if((item.getItem3().toUpperCase().contains(namaPelanggan.toUpperCase()) && !namaPelanggan.equals("")) && tgl.equals(tanggalCari)){
                            items.add(item);
                        }
                    }else{

                        if((item.getItem3().toUpperCase().contains(namaPelanggan.toUpperCase()) && !namaPelanggan.equals("")) || tgl.equals(tanggalCari)){
                            items.add(item);
                        }else if(namaPelanggan.equals("") && tanggalCari.equals("")){
                            items.add(item);
                        }
                    }

                }

                getListTransaksi(items);
            }
        });
    }
}
