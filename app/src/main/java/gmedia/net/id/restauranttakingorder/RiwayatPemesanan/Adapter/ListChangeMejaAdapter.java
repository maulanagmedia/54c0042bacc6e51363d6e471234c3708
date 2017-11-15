package gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.MainRiwayatPemesanan;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

public class ListChangeMejaAdapter extends RecyclerView.Adapter<ListChangeMejaAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;
    private ServerURL serverURL;
    private SessionManager session;
    private List<CustomItem> listPenjualan;
    private boolean nonMeja = false;
    private String noBukti = "";

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout rlContainer;
        public TextView tvItem1, tvItem2, tvItem3;

        public MyViewHolder(View view) {

            super(view);

            rlContainer = (RelativeLayout) view.findViewById(R.id.rl_container);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
            tvItem2 = (TextView) view.findViewById(R.id.tv_item2);
            tvItem3 = (TextView) view.findViewById(R.id.tv_item3);
        }
    }

    public ListChangeMejaAdapter(Context context, List masterlist, String noBukti){
        this.context = context;
        this.masterList = masterlist;
        serverURL = new ServerURL(context);
        session = new SessionManager(context);
        this.noBukti = noBukti;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_meja, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final CustomItem cli = masterList.get(position);

        if(position == selectedPosition){


        }else{


        }

        holder.tvItem1.setText(cli.getItem2());
        if(cli.getItem3().equals("1")){
            holder.rlContainer.setBackgroundColor(context.getResources().getColor(R.color.color_table_active));
            if(iv.parseNullInteger(cli.getItem7()) == 1){

                holder.tvItem2.setText("");
                holder.tvItem3.setText(cli.getItem6());
            }else{
                holder.tvItem2.setText(cli.getItem5());
                holder.tvItem3.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(cli.getItem6())));
            }
        }else{
            holder.rlContainer.setBackgroundColor(context.getResources().getColor(R.color.color_table));
            holder.tvItem2.setText("");
            holder.tvItem3.setText("");
        }

        holder.rlContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog alert = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Apakah anda yakin ingin mengubah meja ke " + cli.getItem2())
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateMeja(cli);
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

    private void updateMeja(final CustomItem item) {

        JSONObject penjualanJSON = new JSONObject();

        try {
            penjualanJSON.put("kdmeja", item.getItem1());
            penjualanJSON.put("nomeja", item.getItem2());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nobukti", noBukti);
            jBody.put("penjualan", penjualanJSON);
            jBody.put("status", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.updatePenjualan(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        message = "Meja berhasil diubah";
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        MainRiwayatPemesanan.changeMeja(item.getItem2());

                    }else{

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan saat memproses, silahkan ulangi kembali", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(context, "Terjadi kesalahan saat memproses, silahkan ulangi kembali", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}