package gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.Adapter.AccountAdapter;
import gmedia.net.id.restauranttakingorder.Order.MainOpenOrder;
import gmedia.net.id.restauranttakingorder.Order.MainOrder;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.MainRiwayatPemesanan;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MenuByTransaksiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;
    private ServerURL serverURL;
    private AlertDialog alertDialogListAdmin;
    private SessionManager session;
    private String idOrder = "", jmlLama = "", jmlBaru = "";
    private boolean isLoading = false;

    public class MyViewHolder0 extends RecyclerView.ViewHolder {

        public LinearLayout llNote;
        public TextView tvItem1, tvItem2, tvItem3;

        public MyViewHolder0(View view) {

            super(view);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
        }
    }

    public class MyViewHolder1 extends RecyclerView.ViewHolder {

        public LinearLayout llNote;
        public TextView tvItem1, tvItem2, tvItem3, tvNote;

        public MyViewHolder1(View view) {

            super(view);

            llNote = (LinearLayout) view.findViewById(R.id.ll_note);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
            tvItem2 = (TextView) view.findViewById(R.id.tv_item2);
            tvItem3 = (TextView) view.findViewById(R.id.tv_item3);
            tvNote = (TextView) view.findViewById(R.id.tv_note);
        }
    }

    public MenuByTransaksiAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
        session = new SessionManager(context);
        serverURL = new ServerURL(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = null ;
        if(viewType == 0 ){
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_menu_by_transaksi_title, parent, false);
            return new MyViewHolder0(itemView);
        }else{
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_menu_by_transaksi, parent, false);
            return new MyViewHolder1(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final CustomItem cli = masterList.get(position);

        if(holder.getItemViewType() == 0){

            MyViewHolder0 holder0 = (MyViewHolder0) holder;
            if(cli.getItem3().equals("1")){

                holder0.tvItem1.setText("Order pertama ("+ cli.getItem2()+")");
            }else{
                holder0.tvItem1.setText("Upselling "+ cli.getItem3() +" ("+ cli.getItem2()+")");
            }
        }else{

            MyViewHolder1 holder1 = (MyViewHolder1) holder;

            holder1.tvItem1.setText(cli.getItem2());
            holder1.tvItem2.setText(cli.getItem5());
            if(cli.getItem4().length()>0 || cli.getItem9().length() > 0){
                holder1.llNote.setVisibility(View.VISIBLE);
                holder1.tvItem3.setText((cli.getItem9().length() > 0 ? cli.getItem9()+ " ": "")+cli.getItem4());
            }else{
                holder1.llNote.setVisibility(View.GONE);
            }

            if(cli.getItem7().equals("0") || cli.getItem8().equals("0")){

                holder1.tvItem1.setTextColor(context.getResources().getColor(R.color.color_blue));
                holder1.tvItem2.setTextColor(context.getResources().getColor(R.color.color_blue));
                holder1.tvItem3.setTextColor(context.getResources().getColor(R.color.color_blue));
                holder1.tvNote.setTextColor(context.getResources().getColor(R.color.color_blue));
            }else{

                holder1.tvItem1.setTextColor(context.getResources().getColor(R.color.color_red));
                holder1.tvItem2.setTextColor(context.getResources().getColor(R.color.color_red));
                holder1.tvItem3.setTextColor(context.getResources().getColor(R.color.color_red));
                holder1.tvNote.setTextColor(context.getResources().getColor(R.color.color_red));
            }

            holder1.tvItem1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if(!isLoading) getReleasedMenu(cli);
                }
            });
        }
    }

    private void getReleasedMenu(final CustomItem item){

        isLoading = true;
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("id", item.getItem1());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getReleasedMenu(), "", "", 0, "", "", new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                isLoading = false;
                String message = "Terjadi kesalahan saat memuat data, harap ulangi kembai";
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");

                    if(iv.parseNullInteger(status) == 200){

                        String jml = response.getJSONObject("response").getString("jml");
                        String sisa = String.valueOf(iv.parseNullLong(item.getItem5()) - iv.parseNullLong(jml));
                        if(iv.parseNullLong(sisa) > 0){

                            loadVoidDialog(item, sisa);
                        }else{

                            Toast.makeText(context, "Pesanan yang telah release tidak dapat di batalkan", Toast.LENGTH_LONG).show();
                        }
                    }else{

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onError(String result) {

                isLoading = false;
                Toast.makeText(context, "Terjadi kesalahan saat memuat data, harap ulangi kembai", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadVoidDialog(final CustomItem item, final String jml) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_void, null);

        builder.setView(view);

        final EditText edtJumlah = (EditText) view.findViewById(R.id.edt_jumlah);
        final ImageView ivMin = (ImageView) view.findViewById(R.id.iv_jml_min);
        final ImageView ivPlus = (ImageView) view.findViewById(R.id.iv_jml_plus);
        final TextView tvSubtitle = (TextView) view.findViewById(R.id.tv_subtitle);
        final TextView tvName = (TextView) view.findViewById(R.id.tv_name);

        // 1. id
        // 2. nama
        // 3. harga
        // 4. gambar
        //  5. banyak
        // 6. satuan
        // 7. diskon
        // 8. catatan
        // 9. hargaDiskon
        // 10. tag meja
        // 11. type
        // 12. upselling
        // 13. print menu
        // 14. print rekap
        // 15. flag cetak
        // 16. Jenis Order (DN/TA)
        // 17. alias
        // 18. pilihan

        tvSubtitle.setText("Terdapat " +jml+ " pesanan "+item.getItem2()+" yang belum release");
        tvName.setText("Apakah anda yakin ingin membatalkan pesanan ?");
        edtJumlah.setText(jml);

        ivMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(iv.parseNullLong(edtJumlah.getText().toString()) > 1){

                    long hasil = iv.parseNullLong(edtJumlah.getText().toString()) - 1;
                    edtJumlah.setText(String.valueOf(hasil));

                }
            }
        });

        ivPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(iv.parseNullLong(edtJumlah.getText().toString()) < iv.parseNullLong(jml)){

                    long hasil = iv.parseNullLong(edtJumlah.getText().toString()) + 1;
                    edtJumlah.setText(String.valueOf(hasil));

                }
            }
        });

        AlertDialog builderDialog = builder
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        idOrder = item.getItem1();
                        jmlLama = item.getItem5();
                        jmlBaru = edtJumlah.getText().toString();

                        loadWaitressList();
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                })
                .create();

        builderDialog.show();
    }

    private void loadWaitressList() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_list_admin_void, null);
        builder.setView(view);
        builder.setTitle("Pramusaji");
        builder.setCancelable(false);
        builder.setNeutralButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final ListView lvAdmin = (ListView) view.findViewById(R.id.lv_admin);
        final Button btnRefreshAdmin = (Button) view.findViewById(R.id.btn_refresh);
        final ProgressBar pbLoadingAdmin = (ProgressBar) view.findViewById(R.id.pb_loading);

        getAdminList(lvAdmin, btnRefreshAdmin, pbLoadingAdmin);

        btnRefreshAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getAdminList(lvAdmin, btnRefreshAdmin, pbLoadingAdmin);
                btnRefreshAdmin.setVisibility(View.GONE);
            }
        });

        alertDialogListAdmin = builder.create();

        alertDialogListAdmin.show();

        alertDialogListAdmin.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

    }

    private void getAdminList(final ListView lvAdmin, final Button btnRefreshAdmin, final ProgressBar pbLoadingAdmin){

        pbLoadingAdmin.setVisibility(View.VISIBLE);
        ApiVolley request = new ApiVolley(context, new JSONObject(), "GET", serverURL.getAccount(), "", "", 0, "", "", new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    List<CustomItem> adminList = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        adminList = new ArrayList<>();
                        JSONArray jsonArray = response.getJSONArray("response");

                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            adminList.add(new CustomItem(jo.getString("nik"),jo.getString("nama"),jo.getString("bagian"),jo.getString("username")));
                        }

                        lvAdmin.setAdapter(null);

                        if(adminList != null && adminList.size() > 0){

                            AccountAdapter adapter = new AccountAdapter((Activity) context, adminList);
                            lvAdmin.setAdapter(adapter);

                            lvAdmin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);
                                    loadAdminPin(selectedItem.getItem4());
                                }
                            });
                        }
                    }else{
                        lvAdmin.setAdapter(null);
                        btnRefreshAdmin.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    lvAdmin.setAdapter(null);
                    btnRefreshAdmin.setVisibility(View.VISIBLE);
                }

                pbLoadingAdmin.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {
                pbLoadingAdmin.setVisibility(View.GONE);
                lvAdmin.setAdapter(null);
                btnRefreshAdmin.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadAdminPin(final String username){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_account_password, null);
        builder.setView(view);

        final TextView tvPin = (TextView) view.findViewById(R.id.tv_pin);
        final TextView tv1 = (TextView) view.findViewById(R.id.tv_1);
        final TextView tv2 = (TextView) view.findViewById(R.id.tv_2);
        final TextView tv3 = (TextView) view.findViewById(R.id.tv_3);
        final TextView tv4 = (TextView) view.findViewById(R.id.tv_4);
        final TextView tv5 = (TextView) view.findViewById(R.id.tv_5);
        final TextView tv6 = (TextView) view.findViewById(R.id.tv_6);
        final TextView tv7 = (TextView) view.findViewById(R.id.tv_7);
        final TextView tv8 = (TextView) view.findViewById(R.id.tv_8);
        final TextView tv9 = (TextView) view.findViewById(R.id.tv_9);
        final TextView tv0 = (TextView) view.findViewById(R.id.tv_0);
        final ImageView ivClear = (ImageView) view.findViewById(R.id.iv_clear);
        pinButtonListener(tvPin,tv1);
        pinButtonListener(tvPin,tv2);
        pinButtonListener(tvPin,tv3);
        pinButtonListener(tvPin,tv4);
        pinButtonListener(tvPin,tv5);
        pinButtonListener(tvPin,tv6);
        pinButtonListener(tvPin,tv7);
        pinButtonListener(tvPin,tv8);
        pinButtonListener(tvPin,tv9);
        pinButtonListener(tvPin,tv0);

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(tvPin.getText().length() > 0) tvPin.setText(tvPin.getText().toString().substring(0, tvPin.getText().length()-1));
            }
        });


        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(username.length() == 0){

                    Toast.makeText(context, "Username masih kosong", Toast.LENGTH_LONG).show();
                    return;
                }

                if(tvPin.getText().toString().length() == 0){

                    Toast.makeText(context, "Pin harap diisi", Toast.LENGTH_LONG).show();
                    return;
                }

                loginAdmin(username, tvPin.getText().toString());
            }
        });


        final Dialog alert = builder.create();

        /*DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        alert.getWindow().setLayout(width,  height);*/
        alert.show();

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private void loginAdmin(final String username, final String password) {

        final ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Custom_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("username", username);
            jBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.login(), "", "", 0, username, password, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                progressDialog.dismiss();
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        JSONObject jo = response.getJSONObject("response");

                        //update session
                        session.createLoginSession(jo.getString("nik"),jo.getString("nik"),jo.getString("nama"), username, password, "1","");
                        session = new SessionManager(context);
                        MainOpenOrder.updateStatus(context);
                        if(alertDialogListAdmin != null){
                            try {
                                if(alertDialogListAdmin.isShowing()){
                                    alertDialogListAdmin.dismiss();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        voidPesanan();
                    }else{
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void voidPesanan() {

        final ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Custom_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.show();

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("id", idOrder);
            jBody.put("jml_lama", jmlLama);
            jBody.put("jml_baru", jmlBaru);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.voidMenu(), "", "", 0, "", "", new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                progressDialog.dismiss();
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }else{
                        //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                    Snackbar.make(((Activity)context).findViewById(android.R.id.content), message,
                            Snackbar.LENGTH_INDEFINITE).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            }).show();

                    if(ListTransaksiAdapter.selectedItem != null){
                        MainRiwayatPemesanan.selectTransaksi(ListTransaksiAdapter.selectedItem);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void pinButtonListener(final TextView tvTarget, final TextView tv){

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (tv.getId()){
                    case R.id.tv_1:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"1");
                        break;
                    case R.id.tv_2:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"2");
                        break;
                    case R.id.tv_3:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"3");
                        break;
                    case R.id.tv_4:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"4");
                        break;
                    case R.id.tv_5:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"5");
                        break;
                    case R.id.tv_6:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"6");
                        break;
                    case R.id.tv_7:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"7");
                        break;
                    case R.id.tv_8:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"8");
                        break;
                    case R.id.tv_9:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"9");
                        break;
                    case R.id.tv_0:
                        if(tvTarget.getText().length() < 4) tvTarget.setText(tvTarget.getText()+"0");
                        break;
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {

        int hasil = 0;
        final CustomItem item = masterList.get(position);
        if(item.getItem1().equals("H")){
            hasil = 0;
        }else{
            hasil = 1;
        }
        return hasil;
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}