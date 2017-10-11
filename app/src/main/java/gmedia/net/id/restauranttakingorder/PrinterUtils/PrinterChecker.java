package gmedia.net.id.restauranttakingorder.PrinterUtils;

import android.app.Activity;
import android.content.Context;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.Log;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.maulana.custommodul.ItemValidation;

import gmedia.net.id.restauranttakingorder.Utils.SavedPrinterManager;

/**
 * Created by Shinmaul on 10/10/2017.
 */

public class PrinterChecker implements ReceiveListener {

    private Context context;
    private Printer mPrinter = null;
    private ItemValidation iv = new ItemValidation();
    private final String TAG = "PrinterChecker";

    public PrinterChecker(Context context){
        this.context = context;
    }

    public boolean checkPinter(String target){

        try {
            Log.setLogSettings(context, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 1, Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "setLogSettings", context);
            android.util.Log.d(TAG, "setLogSettings " + e.toString());
        }

        return runSequence(target);
    }

    public void clearPrinter(String target){
        if (!initializeObject()) {
            return;
        }

        if(connectPrinter(target)){
            finalizeObject();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    disconnectPrinter();
                }
            }).start();
        }
    }

    private boolean runSequence(String target) {

        if (!initializeObject()) {
            android.util.Log.d(TAG, "initializeObject");
            return false;
        }

        if (!printData(target)) {
            android.util.Log.d(TAG, "initializeObject");
            finalizeObject();
            return false;
        }

        finalizeObject();
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnectPrinter();
            }
        }).start();

        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private boolean initializeObject() {
        try {
            mPrinter = new Printer(Printer.TM_U220,
                    Printer.MODEL_ANK,
                    context);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "Printer", context);
            android.util.Log.d(TAG, "initializeObject: " + e.toString());
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }

    private boolean printData(String target) {

        if (mPrinter == null) {
            android.util.Log.d(TAG, "printData null");
            return false;
        }

        if (!connectPrinter(target)) {
            android.util.Log.d(TAG, "printData connectPrinter");
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        if (!isPrintable(status)) {
            //ShowMsg.showMsg(makeErrorMessage(status), context);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            android.util.Log.d(TAG, "printData isPrintable");
            return false;
        }

        return true;
    }

    private boolean connectPrinter(String ip) {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            android.util.Log.d(TAG, "connectPrinter null");
            return false;
        }

        try {
            mPrinter.connect(ip, Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            //ShowMsg.showException(e, "connect", context);
            android.util.Log.d(TAG, "coonect : " + e.toString());
            return false;
        }

        return true;
    }

    private boolean isPrintable(PrinterStatusInfo status) {

        if (status == null) {
            android.util.Log.d(TAG, "isPrintable null");
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            android.util.Log.d(TAG, "isPrintable getConnection");
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            android.util.Log.d(TAG, "isPrintable getOnline");
            return false;
        }
        else {
            ;//print available
        }

        return true;
    }

    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    //ShowMsg.showException(e, "endTransaction", context);
                    android.util.Log.d(TAG, "endTransaction : " + e.toString());
                }
            });
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    //ShowMsg.showException(e, "disconnect", context);
                    android.util.Log.d(TAG, "disconnect : " + e.toString());
                }
            });
        }

        finalizeObject();
    }

    @Override
    public void onPtrReceive(Printer printer, int i, PrinterStatusInfo printerStatusInfo, String s) {

        finalizeObject();
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnectPrinter();
            }
        }).start();
    }
}
