package gmedia.net.id.restauranttakingorder.RiwayatPemesanan;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import gmedia.net.id.restauranttakingorder.R;

public class MainRiwayatPemesanan extends Fragment {

    private View layout;
    private Context context;

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


    }
}
