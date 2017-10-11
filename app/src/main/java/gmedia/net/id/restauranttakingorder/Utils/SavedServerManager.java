package gmedia.net.id.restauranttakingorder.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SavedServerManager {
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
	public static final String TAG_SERVER = "SERVERRTO";

	// Constructor
	public SavedServerManager(Context context){
		this.context = context;
		pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create login session
	 * */
	public void saveLastServer(String server){

		editor.putString(TAG_SERVER, server);
		editor.commit();
	}

	public String getServer(){
		return pref.getString(TAG_SERVER, "");
	}
}
