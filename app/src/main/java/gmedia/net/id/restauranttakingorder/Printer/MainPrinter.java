package gmedia.net.id.restauranttakingorder.Printer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.SavedPrinterManager;

public class MainPrinter extends Fragment {

    private Context context;
    private View layout;
    private SavedPrinterManager printerManager;
    private TextView tvJenis1, tvJenis2, tvIP1, tvIP2;
    private LinearLayout llRefresh1, llRefresh2;

    public MainPrinter() {
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
        layout = inflater.inflate(R.layout.fragment_main_printer, container, false);
        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        printerManager = new SavedPrinterManager(context);
        tvJenis1 = (TextView) layout.findViewById(R.id.tv_jenis_1);
        tvJenis2 = (TextView) layout.findViewById(R.id.tv_jenis_2);
        tvIP1 = (TextView) layout.findViewById(R.id.tv_ip_1);
        tvIP2 = (TextView) layout.findViewById(R.id.tv_ip_2);
        llRefresh1= (LinearLayout) layout.findViewById(R.id.ll_refresh_1);
        llRefresh2= (LinearLayout) layout.findViewById(R.id.ll_refresh_2);

        llRefresh1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, DisCoverPrinter.class);
                context.startActivity(intent);
            }
        });
    }
}
