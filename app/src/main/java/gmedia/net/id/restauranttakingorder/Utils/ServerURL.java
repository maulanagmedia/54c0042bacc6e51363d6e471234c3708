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
        baseURL = "http://" + this.server + "/api/";
    }

    public String login(){ return baseURL + "auth/login/";}
    public String checkServer(){ return baseURL + "auth/cek_server/";}
    public static String loginAdmin = "auth/login/";
    public static String getListAdmin = "auth/get_admin/";
    public String getLatestVersion(){ return baseURL + "auth/get_latest_version/";}
    public String getKategori(){ return baseURL + "order/get_kategori/";}
    public String getMenu(){ return baseURL + "order/get_menu/";}
    public String getMenuGantung(){ return baseURL + "order/get_menu_gantung/";}
    public String getMenuBarcode(){ return baseURL + "order/get_menu_barcode/";}
    public String getMeja(){ return baseURL + "order/get_meja/";}
    public String getDetailMeja(){ return baseURL + "order/get_detail_meja/";}
    public String getNoBukti(){ return baseURL + "order/generate_nobukti/";}

    public String getUpselling(){ return baseURL + "order/get_upselling/";}
    public String saveOrder(){ return baseURL + "order/save/";}
    public String saveOrderPerOne(){ return baseURL + "order/save_by_order/";}
    public String getPenjualan(){ return baseURL + "order/get_penjualan_h/";}

    public String updatePenjualan(){ return baseURL + "order/update_penjualan/";}
    public String updatePrinterStatus(){ return baseURL + "order/update_printer_status/";}
    public String getMenuSoldOut(){ return baseURL + "order/get_menu_sold_out/";}
    public String getOrderProcess(){ return baseURL + "order/get_process_order/";}

    public String getRiwayatOrder(){ return baseURL + "riwayat/get_riwayat_transaksi/";}
    public String getDetailRiwayatOrder(){ return baseURL + "riwayat/get_detail_riwayat_transaksi/";}
    public String getProfile(){ return baseURL + "profile/get_profile/";}
    public String getAccount(){ return baseURL + "auth/get_account/";}
    public String getPrinter(){ return baseURL + "printer/get_printer/";}
    public String getSugestionCatatan(){ return baseURL + "order/get_sugestion_catatan/";}
}
