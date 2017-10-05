package gmedia.net.id.restauranttakingorder.Printer;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gmedia.net.id.restauranttakingorder.Printer.Adapter.ListPrinterAdapter;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.SavedPrinterManager;

public class DiscoverPrinter extends AppCompatActivity {

    private ListView lvPrinter;
    private List<CustomItem> listPrinter;
    private final String TAG = "DISCOVER";
    private ItemValidation iv = new ItemValidation();
    private boolean isActive = false;
    private SavedPrinterManager printerManager;

    //Cuma Contoh, perlu perbaikan
    private ArrayList<HashMap<String, String>> mPrinterList = null;
    private SimpleAdapter mPrinterListAdapter = null;

    private ListPrinterAdapter printerAdapter;
    private LinearLayout llRefreshContainer;
    private Button btnRefresh;
    private int jenis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dis_cover_printer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setTitle("Pemindai Printer");

        initUI();
    }

    private void initUI() {

        lvPrinter = (ListView) findViewById(R.id.lv_printer);
        llRefreshContainer = (LinearLayout) findViewById(R.id.ll_refresh_conteiner);
        btnRefresh = (Button) findViewById(R.id.btn_refresh);

        printerManager = new SavedPrinterManager(DiscoverPrinter.this);
        setData();

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isActive){
                    stopDiscovery();
                }else {
                    startDiscovery();
                }
            }
        });
    }

    private void setData() {

        /*mPrinterList = new ArrayList<HashMap<String, String>>();
        mPrinterListAdapter = new SimpleAdapter(this, mPrinterList, R.layout.adapter_list_printer,
                new String[] { "PrinterName", "Target" },
                new int[] { R.id.PrinterName, R.id.Target });

        lvPrinter.setAdapter(mPrinterListAdapter);*/

        listPrinter = new ArrayList<>();
        printerAdapter = new ListPrinterAdapter(DiscoverPrinter.this, listPrinter);
        lvPrinter.setAdapter(printerAdapter);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            jenis = bundle.getInt("jenis");
            String namaPrinter =  "";
            switch (jenis){
                case 1:
                    namaPrinter = getResources().getString(R.string.printer_1);
                    break;
                case 2:
                    namaPrinter = getResources().getString(R.string.printer_2);
                    break;
                case 3:
                    namaPrinter = getResources().getString(R.string.printer_3);
                    break;
            }

            final String finalNamaPrinter = namaPrinter;
            lvPrinter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    final CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
                    AlertDialog builder = new AlertDialog.Builder(DiscoverPrinter.this)
                            .setTitle("Konfirmasi")
                            .setMessage("Atur " + item.getItem2() + " sebagai "+ finalNamaPrinter + " ?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    printerManager.saveLastPrinter(jenis, item.getItem2(), item.getItem3(), item.getItem1());
                                    onBackPressed();
                                }
                            })
                            .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                }
            });
            startDiscovery();
        }
    }

    private  void startDiscovery() {

        isActive = true;
        FilterOption filterOption = null;

        llRefreshContainer.setVisibility(View.VISIBLE);
        listPrinter  = new ArrayList<>();
        printerAdapter.clear();
        printerAdapter.notifyDataSetChanged();

        filterOption = new FilterOption();
        filterOption.setPortType(Discovery.PORTTYPE_ALL);
        filterOption.setBroadcast("255.255.255.255");
        filterOption.setDeviceModel(Discovery.MODEL_ALL);
        filterOption.setEpsonFilter(Discovery.FILTER_NAME);
        filterOption.setDeviceType(Discovery.TYPE_ALL);

        try {
            Discovery.start(DiscoverPrinter.this, filterOption, mDiscoveryListener);


        }
        catch (Exception e) {
            //ShowMsg.showException(e, "start", DiscoverPrinter.this);
            /*stopDiscovery();
            startDiscovery();*/
            Log.e(TAG, "startDiscovery: " + e.toString());
        }
    }

    @Override
    protected void onPause() {
        stopDiscovery();
        super.onPause();
    }

    @Override
    protected void onResume() {
        startDiscovery();
        super.onResume();
    }

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {

                    String printerType = (deviceInfo.getDeviceName().toString().split("\\("))[0];
                    CustomItem item = new CustomItem(deviceInfo.getIpAddress(), deviceInfo.getDeviceName(), printerType);
                    Log.d(TAG, "PrinterName: "+ printerType);
                    printerAdapter.addMoreData(item);
                    llRefreshContainer.setVisibility(View.GONE);
                }
            });
        }
    };

    private void stopDiscovery() {

        isActive = false;
        try {
            Discovery.stop();
        }
        catch (Epos2Exception e) {
            if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                //ShowMsg.showException(e, "stop", DiscoverPrinter.this);
                Log.d(TAG, "stopDiscovery: "+ e.toString());
            }
        }
    }

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
        stopDiscovery();
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
