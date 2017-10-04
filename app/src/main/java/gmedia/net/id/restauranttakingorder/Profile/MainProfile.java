package gmedia.net.id.restauranttakingorder.Profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import gmedia.net.id.restauranttakingorder.R;

public class MainProfile extends Fragment {

    private Context context;
    private View layout;
    private TextView tvNamaPelanggan, tvNIK, tvAlamat, tvKota, tvTelepon, tvBagian;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_main_profile, container, false);
        context = getContext();
        initUI();
        return layout;
    }

    private void initUI() {

        tvNamaPelanggan = (TextView) layout.findViewById(R.id.tv_nama_pelanggan);
        tvNIK = (TextView) layout.findViewById(R.id.tv_nik);
        tvAlamat = (TextView) layout.findViewById(R.id.tv_alamat);
        tvKota = (TextView) layout.findViewById(R.id.tv_kota);
        tvTelepon = (TextView) layout.findViewById(R.id.tv_telepon);
        tvBagian = (TextView) layout.findViewById(R.id.tv_bagian);
    }

}
