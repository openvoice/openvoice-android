package org.openvoice;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class InboundCallActivity extends Activity {

  private SharedPreferences mPrefs;

  private ListView mCallListView;
  private String [] mCalls;

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
	
  void showCalls(String[][] calls) {
  	final String [] condensedMessages = new String[calls.length];
  	int i = 0;
  	for(String[] m : calls) {
  		condensedMessages[i] = m[0] + ": " + m[1];
  		i++;
  	}

  	mCallListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, condensedMessages));
    mCallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View arg1, int position, long row_id) {
        Intent newCallIntent = new Intent(getApplicationContext(), NewCallActivity.class);
        String selected = condensedMessages[position];
        int firstColon = selected.indexOf(":");
        int secondColon = selected.indexOf(":", firstColon+1);
        String sipAddress = selected.substring(0, secondColon);
        newCallIntent.putExtra(MessagingsActivity.EXTRA_TO, sipAddress);
        startActivity(newCallIntent);
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
