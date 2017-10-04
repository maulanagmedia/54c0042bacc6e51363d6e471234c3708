package gmedia.net.id.restauranttakingorder.Printer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.maulana.custommodul.CustomItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gmedia.net.id.restauranttakingorder.PrinterUtils.ShowMsg;
import gmedia.net.id.restauranttakingorder.R;

public class DisCoverPrinter extends AppCompatActivity {

    private ListView lvPrinter;
    private List<CustomItem> listPrinter;

    //Cuma Contoh, perlu perbaikan
    private ArrayList<HashMap<String, String>> mPrinterList = null;
    private SimpleAdapter mPrinterListAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dis_cover_printer);

        initUI();
    }

    private void initUI() {

        lvPrinter =(ListView) findViewById(R.id.lv_printer);
        listPrinter = new ArrayList<>();

        setData();
    }

    private void setData() {

        mPrinterList = new ArrayList<HashMap<String, String>>();
        mPrinterListAdapter = new SimpleAdapter(this, mPrinterList, R.layout.adapter_list_printer,
                new String[] { "PrinterName", "Target" },
                new int[] { R.id.PrinterName, R.id.Target });

        lvPrinter.setAdapter(mPrinterListAdapter);

        startDiscovery();
    }

    private  void startDiscovery() {
        FilterOption filterOption = null;

        mPrinterList.clear();
        mPrinterListAdapter.notifyDataSetChanged();

        filterOption = new FilterOption();
        filterOption.setPortType(Discovery.PORTTYPE_ALL);
        filterOption.setBroadcast("255.255.255.255");
        filterOption.setDeviceModel(Discovery.MODEL_ALL);
        filterOption.setEpsonFilter(Discovery.FILTER_NAME);
        filterOption.setDeviceType(Discovery.TYPE_ALL);

        try {
            Discovery.start(DisCoverPrinter.this, filterOption, mDiscoveryListener);


        }
        catch (Exception e) {
            //ShowMsg.showException(e, "start", DisCoverPrinter.this);
            stopDiscovery();
            startDiscovery();
        }
    }

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("PrinterName", deviceInfo.getDeviceName());
                    item.put("Target", deviceInfo.getTarget());
                    mPrinterList.add(item);
                    mPrinterListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private void stopDiscovery() {
        try {
            Discovery.stop();
        }
        catch (Epos2Exception e) {
            if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                ShowMsg.showException(e, "stop", DisCoverPrinter.this);
            }
        }
    }
}
