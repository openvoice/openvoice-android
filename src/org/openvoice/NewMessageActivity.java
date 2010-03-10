package org.openvoice;

import java.net.URI;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewMessageActivity extends Activity {
	private SharedPreferences mPrefs;
	
  private Button mSendButton;
  private Button mCancelButton;
  private Button mCallButton;

  private String mUserID;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_message);

    mPrefs = getApplicationContext().getSharedPreferences(MessagingsActivity.PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
    mUserID = mPrefs.getString(MessagingsActivity.PREF_USER_ID, "");

    setContactInfo();
    
    mSendButton = (Button) findViewById(R.id.reply_friend_send);
    mSendButton.setOnClickListener(mSendClickListener);

    mCancelButton = (Button) findViewById(R.id.reply_friend_cancel);
    mCancelButton.setOnClickListener(mCancelClickListener);
    
    mCallButton = (Button) findViewById(R.id.call_button);
    mCallButton.setOnClickListener(mCallClickListener);
  }

  private void setContactInfo() {
    TextView contactNameView = (TextView) findViewById(R.id.reply_friend_name);    
    contactNameView.setText("To: " + getIntent().getExtras().getString(MessagingsActivity.EXTRA_TO));
//    TextView contactStatusView = (TextView) findViewById(R.id.reply_friend_status);
//    contactStatusView.setText("Re: " + getIntent().getExtras().getString(Status.EXTRA_FRIEND_STATUS));
  }

  private View.OnClickListener mSendClickListener = new View.OnClickListener() {
    public void onClick(View view) {
      EditText editText = (EditText) findViewById(R.id.reply_text_edit);
      String text = editText.getText().toString(); 
      if(!text.equals("")) {
        new CreateReplyTask().execute(text);
        finish();
      } else {
        Toast.makeText(getApplicationContext(), "Please write a reply", Toast.LENGTH_SHORT).show();
      }
    }
  };

  private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
    public void onClick(View view) {
      finish();
    }
  };

  private View.OnClickListener mCallClickListener = new View.OnClickListener() {
    public void onClick(View view) {
      new CallTask().execute();
    	finish();
    }
  };
  
  class CreateReplyTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
      createReply(params[0]);
      return null;
    }

    public void createReply(String text) {
      DefaultHttpClient client = new DefaultHttpClient();
      String token = mPrefs.getString(org.openvoice.MessagingsActivity.PREF_TOKEN, "");
      try {
      	String to = getIntent().getExtras().getString(MessagingsActivity.EXTRA_TO);
        String params = "&format=json&messaging[user_id]=" + mUserID + "&messaging[text]=" + Uri.encode(text) + "&token=" + token + "&messaging[to]=" + to;
        URI uri = new URI(MessagingsActivity.SERVER_URL + "/messagings/create?" + params);
        HttpPost method = new HttpPost(uri);
//        HttpResponse response = client.execute(method);
        
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = client.execute(method, responseHandler);
        
        System.out.println(responseBody);
      } catch (Exception e) {
        Log.e(getClass().getName(), e.getMessage());
      } finally {
        // TODO cleanup
      }
    }
  }
  
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
      	String to = getIntent().getExtras().getString(MessagingsActivity.EXTRA_TO);
        String params = "&format=json&user_id=" + mUserID + "&token=" + token + "&voice_call[to]=" + to;
        URI uri = new URI(MessagingsActivity.SERVER_URL + "/voice_calls/create?" + params);
        HttpPost method = new HttpPost(uri);
//        HttpResponse response = client.execute(method);
        
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = client.execute(method, responseHandler);
        
        System.out.println(responseBody);
      } catch (Exception e) {
        Log.e(getClass().getName(), e.getMessage());
      } finally {
        // TODO cleanup
      }
    }
  	
  }
}
