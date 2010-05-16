package org.openvoice;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class VoicemailsActivity extends Activity {
	
  public static final String EXTRA_TRANSCRIPTION = "org.openvoice.extra.TRANSCRIPTION";
  public static final String EXTRA_FILENAME = "org.openvoice.extra.FILENAME";
  
  private SharedPreferences mPrefs;

  private Context mContext;
  private ListView mVoicemailsListView;
  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
    setContentView(R.layout.voicemails);
    mVoicemailsListView = (ListView) findViewById(R.id.VoicemailsListView); 
    mContext = getApplicationContext();
    mPrefs = mContext.getSharedPreferences(MessagingsActivity.PREFERENCES_NAME, MODE_WORLD_READABLE);
    handleUserVoicemail();    
	}

  private void handleUserVoicemail() {
    new VoicemailDownloadTask(getApplicationContext(), this).execute();	  
  }

  void showVoicemails(final List<Map<String, String>> voicemails) {
  	String[] from = {"caller_id", "time", "direction", "caller_name", "transcription"};
  	int[] to= {R.id.vm_caller_id, R.id.vm_datetime, R.id.vm_direction, R.id.vm_caller_name, R.id.transcription};
    mVoicemailsListView.setAdapter(new SimpleAdapter(this, voicemails, R.layout.voicemail_row, from, to));
    mVoicemailsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View arg1, int position, long row_id) {
        Intent vmDetailsIntent = new Intent(getApplicationContext(), VoicemailDetailsActivity.class);
        Map<String, String> voicemail = (Map<String, String>)voicemails.get(position);
				String to = voicemail.get("caller_id").toString();
        String transcription = voicemail.get("transcription").toString();
        String fileName = voicemail.get("filename").toString();
        vmDetailsIntent.putExtra(MessagingsActivity.EXTRA_TO, to);
        vmDetailsIntent.putExtra(EXTRA_TRANSCRIPTION, transcription);
        vmDetailsIntent.putExtra(EXTRA_FILENAME,fileName);
        startActivity(vmDetailsIntent);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.voicemail_menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.refresh:
      	handleUserVoicemail();
    }
    return false;
  }  
}
