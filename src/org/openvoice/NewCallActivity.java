package org.openvoice;

import java.net.URI;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NewCallActivity extends Activity {
	private SharedPreferences mPrefs;

	private TextView mRecipientView; 
  private Button mCallButton;

  private String mUserID;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_call);

    mPrefs = getApplicationContext().getSharedPreferences(MessagingsActivity.PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
    mUserID = mPrefs.getString(MessagingsActivity.PREF_USER_ID, "");

    setContactInfo();
    
    mCallButton = (Button) findViewById(R.id.call_button);
    mCallButton.setOnClickListener(mCallClickListener);
  }

  private void setContactInfo() {
    mRecipientView = (TextView) findViewById(R.id.callee_number);    
    try {
    	String recipient_number = getIntent().getExtras().getString(MessagingsActivity.EXTRA_TO);
    	if(recipient_number != null) {
    		mRecipientView.setText(recipient_number);
    	}
    } catch(NullPointerException npe) {
    	Log.i(getClass().getName(), "creating new call without callee number in the intent extra");
    }
  }

  private View.OnClickListener mCallClickListener = new View.OnClickListener() {
    public void onClick(View view) {
      new CallTask().execute();
    	finish();
    }
  };
  
  class CallTask extends AsyncTask<String, Void, Void> {

		@Override
    protected Void doInBackground(String... params) {
	    initiateCall();
	    return null;
    }
		
		public void initiateCall() {
      DefaultHttpClient client = new DefaultHttpClient();
      String token = mPrefs.getString(org.openvoice.MessagingsActivity.PREF_TOKEN, "");
      try {
      	String recipientNumber = mRecipientView.getText().toString();
        String params = "&format=json&user_id=" + mUserID + "&token=" + token + "&voice_call[to]=" + recipientNumber;
        URI uri = new URI(MessagingsActivity.SERVER_URL + "/voice_calls/create?" + params);
        HttpPost method = new HttpPost(uri);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = client.execute(method, responseHandler);
//        System.out.println(responseBody);
      } catch (Exception e) {
        Log.e(getClass().getName(), e.getMessage() == null ? "Error placing call" : e.getMessage());
      } finally {
        // TODO cleanup
      }
    }
  	
  }
}
