package org.openvoice;

import java.net.URI;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NewCallActivity extends Activity {

  // Request code for the contact picker activity
  private static final int PICK_CONTACT_REQUEST = 1;

  /**
   * An SDK-specific instance of {@link ContactAccessor}.  The activity does not need
   * to know what SDK it is running in: all idiosyncrasies of different SDKs are
   * encapsulated in the implementations of the ContactAccessor class.
   */
  private final ContactAccessor mContactAccessor = ContactAccessor.getInstance();
  
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
    
    // Install a click handler on the Pick Contact button
    Button pickContact = (Button)findViewById(R.id.pick_contact_button);
    pickContact.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
            pickContact();
        }
    });
  }

  /**
   * Click handler for the Pick Contact button.  Invokes a contact picker activity.
   * The specific intent used to bring up that activity differs between versions
   * of the SDK, which is why we delegate the creation of the intent to ContactAccessor.
   */
  protected void pickContact() {
      startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
  }

  /**
   * Invoked when the contact picker activity is finished. The {@code contactUri} parameter
   * will contain a reference to the contact selected by the user. We will treat it as
   * an opaque URI and allow the SDK-specific ContactAccessor to handle the URI accordingly.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
          loadContactInfo(data.getData());
      }
  }

  /**
   * Load contact information on a background thread.
   */
  private void loadContactInfo(Uri contactUri) {

      /*
       * We should always run database queries on a background thread. The database may be
       * locked by some process for a long time.  If we locked up the UI thread while waiting
       * for the query to come back, we might get an "Application Not Responding" dialog.
       */
      AsyncTask<Uri, Void, ContactInfo> task = new AsyncTask<Uri, Void, ContactInfo>() {

          @Override
          protected ContactInfo doInBackground(Uri... uris) {
              return mContactAccessor.loadContact(getContentResolver(), uris[0]);
          }

          @Override
          protected void onPostExecute(ContactInfo result) {
              bindView(result);
          }
      };

      task.execute(contactUri);
  }

  /**
   * Displays contact information: name and phone number.
   */
  protected void bindView(ContactInfo contactInfo) {
//      TextView displayNameView = (TextView) findViewById(R.id.display_name_text_view);
//      displayNameView.setText(contactInfo.getDisplayName());

      TextView phoneNumberView = (TextView) findViewById(R.id.callee_number);
      phoneNumberView.setText(contactInfo.getPhoneNumber());
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
        URI uri = new URI(SettingsActivity.getServerUrl(getApplicationContext()) + "/voice_calls/create?" + params);
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
