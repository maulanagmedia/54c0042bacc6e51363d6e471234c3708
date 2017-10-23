package gmedia.net.id.restauranttakingorder.Utils;

import android.content.Context;

/**
 * Created by Shinmaul on 10/2/2017.
 */

public class ServerURL {

    private Context context;
    private String server;
    private SavedServerManager serverManager;
    private String baseURL = "";

    public ServerURL(Context context){
        this.context = context;
        serverManager = new SavedServerManager(context);
        this.server = serverManager.getServer();
        baseURL = "http://" + this.server + "/resto/api/";
    }

    public String login(){ return baseURL + "auth/login/";}
    public String getKategori(){ return baseURL + "order/get_kategori/";}
    public String getMenu(){ return baseURL + "order/get_menu/";}
    public String getMenuBarcode(){ return baseURL + "order/get_menu_barcode/";}
    public String getMeja(){ return baseURL + "order/get_meja/";}
    public String getNoBukti(){ return baseURL + "order/generate_nobukti/";}
    public String saveOrder(){ return baseURL + "order/save/";}
    public String getRiwayatOrder(){ return baseURL + "riwayat/get_riwayat_transaksi/";}
    public String getDetailRiwayatOrder(){ return baseURL + "riwayat/get_detail_riwayat_transaksi/";}
    public String getProfile(){ return baseURL + "profile/get_profile/";}
    public String getAccount(){ return baseURL + "auth/get_account/";}
}
