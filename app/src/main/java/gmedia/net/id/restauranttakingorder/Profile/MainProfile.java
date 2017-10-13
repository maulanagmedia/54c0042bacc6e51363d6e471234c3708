package gmedia.net.id.restauranttakingorder.Profile;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class MainProfile extends Fragment {

    private Context context;
    private View layout;
    private TextView tvNama, tvNIK, tvAlamat, tvKota, tvTelepon, tvBagian;
    private SessionManager session;
    private ItemValidation iv = new ItemValidation();
    private ServerURL serverURL;

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

        tvNama = (TextView) layout.findViewById(R.id.tv_nama);
        tvNIK = (TextView) layout.findViewById(R.id.tv_nik);
        tvAlamat = (TextView) layout.findViewById(R.id.tv_alamat);
        tvKota = (TextView) layout.findViewById(R.id.tv_kota);
        tvTelepon = (TextView) layout.findViewById(R.id.tv_telepon);
        tvBagian = (TextView) layout.findViewById(R.id.tv_bagian);

        session = new SessionManager(context);
        serverURL = new ServerURL(context);

        getProfile();
    }

    private void getProfile() {

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", session.getUserInfo(SessionManager.TAG_NIK));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getProfile(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONObject jo= response.getJSONObject("response");

                        tvNama.setText(jo.getString("nama"));
                        tvNIK.setText(jo.getString("nik"));
                        tvAlamat.setText(jo.getString("alamat"));
                        tvKota.setText(jo.getString("kota"));
                        tvTelepon.setText(jo.getString("notelp"));
                        tvBagian.setText(jo.getString("bagian"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(context, "Terjadi kesalahan saat memuat data", Toast.LENGTH_LONG).show();
            }
        });
    }

}
