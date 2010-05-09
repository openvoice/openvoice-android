package org.openvoice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class OutboundCallActivity extends Activity {

  private SharedPreferences mPrefs;

  private ListView mCallListView;
  private String [] mCalls;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.outbound_calls);
      Context context = getApplicationContext();
      mPrefs = context.getSharedPreferences(MessagingsActivity.PREFERENCES_NAME, MODE_WORLD_READABLE);
      
      mCallListView = (ListView) findViewById(R.id.CallListView);
      
      new OutboundCallDownloadTask(getApplicationContext(), this).execute();    
  }
	
  void showCalls(String[][] calls) {
  	final String [] condensedMessages = new String[calls.length];
  	int i = 0;
  	for(String[] m : calls) {
  		condensedMessages[i] = m[0] + ": " + m[1];
  		i++;
  	}
    mCallListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, condensedMessages));
//    mCallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//      public void onItemClick(AdapterView<?> arg0, View arg1, int position, long row_id) {
//        Intent newMessageIntent = new Intent(getApplicationContext(), NewMessageActivity.class);
////        TextView contactNameView = (TextView) ((RelativeLayout)arg1).findViewById(R.id.contact_name);
//        String selected = condensedMessages[position];
//        String to = selected.substring(0, selected.indexOf(":"));
//        //newMessageIntent.putExtra(EXTRA_TO, to);
//        startActivity(newMessageIntent);
//      }
//    });
  }  
}
