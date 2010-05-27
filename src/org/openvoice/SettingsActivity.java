package org.openvoice;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
	private static final String OPT_SERVER_URL = "server_url";
	private static final String OPT_SERVER_URL_DEFAULT = "http://openvoice.heroku.com";
//	private static final String OPT_SERVER_URL_DEFAULT = "http://web1.tunnlr.com:10790";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	public static String getServerUrl(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(
				OPT_SERVER_URL, OPT_SERVER_URL_DEFAULT);
	}
}
