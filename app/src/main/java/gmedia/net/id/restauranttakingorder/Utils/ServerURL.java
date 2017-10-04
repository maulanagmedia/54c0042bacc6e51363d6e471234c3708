package gmedia.net.id.restauranttakingorder.Utils;

/**
 * Created by Shinmaul on 10/2/2017.
 */

public class ServerURL {

    private static String baseURL = "http://192.168.12.181/resto/api/";
    public static String login = baseURL + "auth/login/";
    public static String getKategori = baseURL + "order/get_kategori/";
    public static String getMenu = baseURL + "order/get_menu/";
    public static String getMeja = baseURL + "order/get_meja/";
    public static String getNoBukti = baseURL + "order/generate_nobukti/";
    public static String saveOrder = baseURL + "order/save/";
}
