package gmedia.net.id.restauranttakingorder.Order.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import gmedia.net.id.restauranttakingorder.Order.DetailOrder;
import gmedia.net.id.restauranttakingorder.Order.MainOpenOrder;
import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.Utils.ServerURL;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ListMejaAdapter extends RecyclerView.Adapter<ListMejaAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;
    private ServerURL serverURL;
    private SessionManager session;
    private List<CustomItem> listPenjualan;
    private boolean nonMeja = false;
    private AlertDialog alertDialogListAdmin;

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

    public ListMejaAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
        serverURL = new ServerURL(context);
        session = new SessionManager(context);
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


                loadWaitressList(cli);
                /*if(iv.parseNullInteger(lastSelectedItem.getItem7()) == 1){

                    checkStatus(lastSelectedItem, "");
                }else if(iv.parseNullInteger(lastSelectedItem.getItem7()) == 3){
                    checkStatus(lastSelectedItem, lastSelectedItem.getItem5());
                }
                else{

                    redirectToOrderDetail(lastSelectedItem, true);
                }*/
            }
        });
    }

    //region choose waiters
    private void loadWaitressList(final CustomItem cli) {

        serverURL = new ServerURL(context);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_list_admin, null);
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

        getAdminList(cli, lvAdmin, btnRefreshAdmin, pbLoadingAdmin);

        btnRefreshAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getAdminList(cli, lvAdmin, btnRefreshAdmin, pbLoadingAdmin);
                btnRefreshAdmin.setVisibility(View.GONE);
            }
        });

        alertDialogListAdmin = builder.create();

        alertDialogListAdmin.show();

        alertDialogListAdmin.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private void getAdminList(final CustomItem cli, final ListView lvAdmin, final Button btnRefreshAdmin, final ProgressBar pbLoadingAdmin){

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
                                    loadAdminPin(cli, selectedItem.getItem4());
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

    private void loadAdminPin(final CustomItem cli, final String username){

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
                validateAdmin(cli, username, tvPin.getText().toString());
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

    private void validateAdmin(final CustomItem cli, String username, String password) {

        if(username.length() == 0){

            Toast.makeText(context, "Username masih kosong", Toast.LENGTH_LONG).show();
            return;
        }

        if(password.length() == 0){

            Toast.makeText(context, "Pin harap diisi", Toast.LENGTH_LONG).show();
            return;
        }

        loginAdmin(cli, username, password);
    }

    private void loginAdmin(final CustomItem cli, final String username, final String password) {

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
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    String message = response.getJSONObject("metadata").getString("message");
                    if(iv.parseNullInteger(status) == 200){

                        progressDialog.dismiss();
                        JSONObject jo = response.getJSONObject("response");

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

                        if(iv.parseNullInteger(cli.getItem7()) == 1){

                            checkStatus(cli, "");
                        }else if(iv.parseNullInteger(cli.getItem7()) == 3){
                            checkStatus(cli, cli.getItem5());
                        }
                        else{

                            redirectToOrderDetail(cli, true);
                        }

                    }else{
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(context, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }
    //endregion

    private void redirectToOrderDetail(CustomItem cli, boolean newOrder) {
        Intent intent = new Intent(context, DetailOrder.class);

        intent.putExtra("kdmeja", cli.getItem1());
        intent.putExtra("nomeja", cli.getItem2());

        if(!newOrder){
            intent.putExtra("nobukti", cli.getItem3());
            intent.putExtra("urutan", cli.getItem5());
            intent.putExtra("printno", cli.getItem7());
            intent.putExtra("jmlplg", cli.getItem8());
        }

        context.startActivity(intent);
    }

    private void showAlertDialog(final CustomItem cli){

        AlertDialog alert = new AlertDialog.Builder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Opsi")
                .setMessage("Meja ini memiliki order yang masih diproses.")
                .setNegativeButton("Order Baru", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        redirectToOrderDetail(cli, true);
                    }
                })
                .setPositiveButton("Tambahkan ke Order Lama", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        loadList();
                    }
                })
                .setNeutralButton("Tutup", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    private void loadList(){

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_detail_meja, null);
        builder.setView(view);
        builder.setTitle("Order Saat Ini");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setNeutralButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setCancelable(false);

        final ListView lvDetailMeja = (ListView) view.findViewById(R.id.lv_detail_meja);

        lvDetailMeja.setAdapter(null);

        if(listPenjualan != null && listPenjualan.size() > 0){

            DetailMejaAdapter adapter = new DetailMejaAdapter((Activity) context, listPenjualan);
            lvDetailMeja.setAdapter(adapter);
            lvDetailMeja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
                    getPenjualan(item);
                }
            });
        }

        final android.support.v7.app.AlertDialog alert = builder.create();

        alert.show();

        alert.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private void checkStatus(final CustomItem cli, final String nobukti) {

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kdmeja", cli.getItem1());
            jBody.put("nobukti", nobukti);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getDetailMeja(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length() > 0){

                            listPenjualan = new ArrayList<>();
                            for(int i = 0; i < jsonArray.length(); i++){

                                JSONObject jo = jsonArray.getJSONObject(i);
                                listPenjualan.add(new CustomItem(jo.getString("kdmeja"), jo.getString("nmmeja"), jo.getString("nobukti"), jo.getString("usertgl"), jo.getString("urutan"), jo.getString("jml_item"), jo.getString("print_no"), jo.getString("jumlah_plg")));
                            }

                            if(nobukti.equals("")){

                                showAlertDialog(cli);
                            }else{
                                if(listPenjualan.size()> 0){

                                    getPenjualan(listPenjualan.get(0));
                                }else{
                                    Toast.makeText(context, "Order "+ nobukti +" telah selesai", Toast.LENGTH_LONG).show();
                                }
                            }

                        }else{

                            if(nobukti.equals("")){

                                redirectToOrderDetail(cli, true);
                            }else{
                                Toast.makeText(context, "Order "+ nobukti +" telah selesai", Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan saat mengakses data, mohon coba kembali", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(context, "Terjadi kesalahan saat mengakses data, mohon coba kembali", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getPenjualan(final CustomItem cli) {

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nobukti", cli.getItem3());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ApiVolley request = new ApiVolley(context, jBody, "POST", serverURL.getPenjualan(), "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        if(jsonArray.length() > 0){

                            redirectToOrderDetail(cli, false);
                        }else{
                            Toast.makeText(context, "Order "+ cli.getItem3() +" telah selesai, order akan disimpan sebagai order baru", Toast.LENGTH_LONG).show();
                            redirectToOrderDetail(cli, true);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan saat mengakses data, mohon coba kembali", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(context, "Terjadi kesalahan saat mengakses data, mohon coba kembali", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}