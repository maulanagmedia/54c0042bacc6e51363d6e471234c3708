package gmedia.net.id.restauranttakingorder.Utils;

import com.maulana.custommodul.ItemValidation;

/**
 * Created by Shinmaul on 11/15/2017.
 */

public class Status {

    private static ItemValidation iv = new ItemValidation();

    public static String getPenjualanStatus(String status){
        String hasil = status;
        switch (iv.parseNullInteger(status)){
            case 1:
                hasil = "Proses";
                break;
            case 5:
                hasil = "Selesai";
                break;
            case 7:
                hasil = "Closing";
                break;
            case 9:
                hasil = "Dibatalkan";
                break;
        }

        return hasil;
    }
}
