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

public class InboundCallActivity extends Activity {

  private SharedPreferences mPrefs;
  private ListView mCallListView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.calls);
      Context context = getApplicationContext();
      mPrefs = context.getSharedPreferences(MessagingsActivity.PREFERENCES_NAME, MODE_WORLD_READABLE);    
      mCallListView = (ListView) findViewById(R.id.CallListView);      
      new InboundCallDownloadTask(getApplicationContext(), this).execute();    
  }
	
  /**
   * builds adapter that binds inbound calls with voice_row.
   * 
   * @param calls
   */
  void showCalls(final List<Map<String, String>> calls) {
  	String[] from = {"caller_name", "caller_id", "time"};
  	int[] to= {R.id.inbound_caller_name, R.id.inbound_caller_id, R.id.inbound_datetime};
  	mCallListView.setAdapter(new SimpleAdapter(this, calls, R.layout.voice_row, from, to));
    mCallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View arg1, int position, long row_id) {
        Intent newMessageIntent = new Intent(getApplicationContext(), NewMessageActivity.class);
        String to = ((Map<String, String>)calls.get(position)).get("caller_id").toString();
        newMessageIntent.putExtra(MessagingsActivity.EXTRA_TO, to);
        startActivity(newMessageIntent);
      }
    });
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.new_call, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.new_call:
        startActivity(new Intent(this, NewCallActivity.class));
      case R.id.refresh:
      	new InboundCallDownloadTask(getApplicationContext(), this).execute();
    }
    return false;
  }  
}
