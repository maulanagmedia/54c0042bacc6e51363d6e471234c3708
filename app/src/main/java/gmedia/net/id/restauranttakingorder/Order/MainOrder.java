package gmedia.net.id.restauranttakingorder.Order;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.Order.Adapter.KategoriMenuAdapter;
import gmedia.net.id.restauranttakingorder.Order.Adapter.MenuByKategoriAdapter;
import gmedia.net.id.restauranttakingorder.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MainOrder#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainOrder extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View layout;
    private Context context;
    private ListView lvKategori;
    private EditText edtSearchMenu;
    private RecyclerView rvListMenu;
    private EditText edtNamaPelanggan, edtNoMeja;
    private ListView lvOrder;
    private TextView tvTotal;
    private Button btnCetak, btnSimpan;
    private List<CustomItem> listKategori;
    private List<CustomItem> listMenu;
    private List<CustomItem> selectedMenu;
    private ProgressBar pbLoadMenu;
    private boolean firstLoad = false;
    private ItemValidation iv = new ItemValidation();

    public MainOrder() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainOrder.
     */
    // TODO: Rename and change types and number of parameters
    public static MainOrder newInstance(String param1, String param2) {
        MainOrder fragment = new MainOrder();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        pbLoadMenu = (ProgressBar) layout.findViewById(R.id.pb_load_menu);
        edtNamaPelanggan = (EditText) layout.findViewById(R.id.edt_nama_pelanggan);
        edtNoMeja = (EditText) layout.findViewById(R.id.edt_no_meja);
        lvOrder = (ListView) layout.findViewById(R.id.lv_order);
        tvTotal = (TextView) layout.findViewById(R.id.tv_total);
        btnCetak = (Button) layout.findViewById(R.id.btn_cetak);
        btnSimpan = (Button) layout.findViewById(R.id.btn_simpan);

        getKategoriData();

    }

    private void getKategoriData() {

        listKategori = new ArrayList<>();

        listKategori.add(new CustomItem("1", "Favorit"));
        listKategori.add(new CustomItem("2", "Semua"));

        setKategoriTable();
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
                    adapter.selectedPosition = i;
                    adapter.notifyDataSetChanged();

                    getMenuByKategori(selected);
                }
            });
        }
    }

    private void getMenuByKategori(CustomItem selected) {

        listMenu = new ArrayList<>();

        listMenu.add(new CustomItem("1", "Rawon Enak", "20000", ""));
        listMenu.add(new CustomItem("2", "Rawon Goreng", "25000", ""));
        listMenu.add(new CustomItem("3", "Rawon Balado", "25000", ""));
        listMenu.add(new CustomItem("4", "Rawon Telur Puyuh", "25000", ""));

        setManuSearchView();
        setMenuTable(listMenu);
    }

    private void setMenuTable(List<CustomItem> listItem) {

        rvListMenu.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            final MenuByKategoriAdapter menuAdapter = new MenuByKategoriAdapter(context, listItem);

            final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 2);
            rvListMenu.setLayoutManager(mLayoutManager);
//        rvListMenu.addItemDecoration(new NavMenu.GridSpacingItemDecoration(2, dpToPx(10), true));
            rvListMenu.setItemAnimator(new DefaultItemAnimator());
            rvListMenu.setAdapter(menuAdapter);
        }
    }

    private void setManuSearchView() {

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

}
