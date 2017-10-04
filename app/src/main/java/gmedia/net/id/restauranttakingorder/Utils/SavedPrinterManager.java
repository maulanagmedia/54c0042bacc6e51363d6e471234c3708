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
	public static final String TAG_JENIS1 = "JENIS1";
	public static final String TAG_JENIS2 = "JENIS2";

	// Constructor
	public SavedPrinterManager(Context context){
		this.context = context;
		pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create login session
	 * */
	public void saveLastPrinter(String printer1, String printer2, String jenis1, String jenis2){

		editor.putString(TAG_PRINT1, printer1);
		editor.putString(TAG_PRINT2, printer2);
		editor.putString(TAG_JENIS1, jenis1);
		editor.putString(TAG_JENIS2, jenis2);

		editor.commit();
	}	

	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getLastPrinter(){

		HashMap<String, String> user = new HashMap<String, String>();
		user.put(TAG_PRINT1, pref.getString(TAG_PRINT1, null));
		user.put(TAG_PRINT2, pref.getString(TAG_PRINT2, null));
		user.put(TAG_JENIS1, pref.getString(TAG_JENIS1, null));
		user.put(TAG_JENIS2, pref.getString(TAG_JENIS2, null));
		return user;
	}

	public String getData(String key){
		return pref.getString(key, null);
	}
}
