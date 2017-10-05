package gmedia.net.id.restauranttakingorder.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SavedPrinterManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "GmediaRTO";

	// All Shared Preferences Keys
	public static final String TAG_PRINT1 = "PRINTER1";
	public static final String TAG_PRINT2 = "PRINTER2";
	public static final String TAG_PRINT3 = "PRINTER3";
	public static final String TAG_JENIS1 = "JENIS1";
	public static final String TAG_JENIS2 = "JENIS2";
	public static final String TAG_JENIS3 = "JENIS3";
	public static final String TAG_IP1 = "IP1";
	public static final String TAG_IP2 = "IP2";
	public static final String TAG_IP3 = "IP3";

	// Constructor
	public SavedPrinterManager(Context context){
		this.context = context;
		pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create login session
	 * */
	public void saveLastPrinter(int number, String printer, String jenis, String ip){

		if(number == 1){
			editor.putString(TAG_PRINT1, printer);
			editor.putString(TAG_JENIS1, jenis);
			editor.putString(TAG_IP1, ip);
		}else if(number == 2){
			editor.putString(TAG_PRINT2, printer);
			editor.putString(TAG_JENIS2, jenis);
			editor.putString(TAG_IP2, ip);
		}else if(number == 3){
			editor.putString(TAG_PRINT3, printer);
			editor.putString(TAG_JENIS3, jenis);
			editor.putString(TAG_IP3, ip);
		}


		editor.commit();
	}

	public String getData(String key){
		return pref.getString(key, null);
	}
}
