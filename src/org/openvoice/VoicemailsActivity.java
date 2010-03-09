package org.openvoice;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class VoicemailsActivity extends Activity {
	
	private SharedPreferences mPrefs;
  private ListView mVoicemailListView;
  private String [] mVoicemails;
  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
    setContentView(R.layout.voicemails);
    Context context = getApplicationContext();
    mPrefs = context.getSharedPreferences(MessagingsActivity.PREFERENCES_NAME, MODE_WORLD_READABLE);
    
    mVoicemailListView = (ListView) findViewById(R.id.VoicemailsListView);
    
    handleUserVoicemail();
	}

  private void handleUserVoicemail() {
    new VoicemailDownloadTask(getApplicationContext(), this).execute();	  
  }
  
  void showVoicemails(String[][] voicemails) {
  	String [] condensedVoicemails = new String[voicemails.length];
  	int i = 0;
  	for(String[] m : voicemails) {
  		condensedVoicemails[i] = m[0] + ": " + m[1];
  		i++;
  	}
    mVoicemailListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, condensedVoicemails));
    //mMessageListView.setTextFilterEnabled(true);      
  }  
}
