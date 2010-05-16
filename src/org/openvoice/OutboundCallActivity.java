package org.openvoice;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class OutboundCallActivity extends Activity {

  private SharedPreferences mPrefs;

  private ListView mCallListView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.outbound_calls);
      Context context = getApplicationContext();
      mPrefs = context.getSharedPreferences(MessagingsActivity.PREFERENCES_NAME, MODE_WORLD_READABLE);     
      mCallListView = (ListView) findViewById(R.id.OutboundCallListView);      
      new OutboundCallDownloadTask(getApplicationContext(), this).execute();    
  }
	
  void showCalls(List<Map<String, String>> calls) {
  	String[] from = {"caller_name", "caller_id", "time"};
  	int[] to= {R.id.outbound_caller_name, R.id.outbound_caller_id, R.id.outbound_datetime};
  	mCallListView.setAdapter(new SimpleAdapter(this, calls, R.layout.outbound_voice_row, from, to));
  }  
}
