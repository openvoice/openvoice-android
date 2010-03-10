package org.openvoice;

import java.net.URI;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MessagingsActivity extends Activity {
	
	protected static String SERVER_URL_DEV = "http://web1.tunnlr.com:10790";
	protected static String SERVER_URL = "http://tropovoice.heroku.com";
	
  private SharedPreferences mPrefs;
  public static final String PREFERENCES_NAME = "OpenVoice";
  public static final String PREF_USER_ID = "UserID";
  public static final String PREF_PHONE_NUMBER = "MyPhoneNumber";
  public static final String PREF_TOKEN = "Token";

  public static final String EXTRA_TO = "org.openvoice.extra.TO";
  
  private ListView mMessageListView;
  private String [] mMessages;
  
  /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messagings);
        Context context = getApplicationContext();
        mPrefs = context.getSharedPreferences(PREFERENCES_NAME, MODE_WORLD_READABLE);
        
        mMessageListView = (ListView) findViewById(R.id.MessagesListView);
      
        locatePhoneNumber();
        login();
        handleUserMessaging();
    }
    
    void showMessages(String[][] messages) {
    	final String [] condensedMessages = new String[messages.length];
    	int i = 0;
    	for(String[] m : messages) {
    		condensedMessages[i] = m[0] + ": " + m[1];
    		i++;
    	}
      mMessageListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, condensedMessages));
      mMessageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long row_id) {
          Intent newMessageIntent = new Intent(getApplicationContext(), NewMessageActivity.class);
//          TextView contactNameView = (TextView) ((RelativeLayout)arg1).findViewById(R.id.contact_name);
          String selected = condensedMessages[position];
          String to = selected.substring(0, selected.indexOf(":"));
          newMessageIntent.putExtra(EXTRA_TO, to);
          startActivity(newMessageIntent);
        }
      });
      //mMessageListView.setTextFilterEnabled(true);      
    }
    
    private void handleUserMessaging() {
      new MessagingDownloadTask(getApplicationContext(), this).execute();	  
    }

    private void locatePhoneNumber() {
    	// return immediately if we have a saved user_id
    	if(mPrefs.getString(PREF_USER_ID, "").length() > 0) {
    		return;
    	}
    	
    	// if we have a saved phone number but not user_id, then retrieve the user_id from server
    	String phoneNumber = mPrefs.getString(PREF_PHONE_NUMBER, ""); 
    	if(phoneNumber.length() > 0) {
    		locateCurrentUser(phoneNumber);
    		return;
    	}
    	
    	// if still no luck then we try to get phonenumber from api call
    	TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
  	  phoneNumber = tm.getLine1Number(); 	
//  	  if(phoneNumber == null || phoneNumber.equals("")) {
//  	  	// if we can't get the phone number from api, such as for brand new t-mobile sim card, we have to ask user to input it
//      	Toast.makeText(getApplicationContext(), "Unable to retrieve phone number, maybe your have a new service or SIM card.  Please input your number.", Toast.LENGTH_LONG).show();
//  			Intent intent = new Intent(Main.this, PhoneNumberActivity.class);
//  			startActivityForResult(intent, MY_PHONE_NUMBER_CODE);      	  	
//  	  } else {
  	  	// if we can get the phone number from api, then go to server to get user_id
  	  	locateCurrentUser(phoneNumber);
//  	  }
    }

    private void locateCurrentUser(String phoneNumber) {    
      DefaultHttpClient client = new DefaultHttpClient();
      try {	
        String url = "/phone_numbers/locate_user";
        String params = "?format=json&phone_number=" + phoneNumber;
//        URI uri = new URI(SERVER_URL + url + params);
        URI uri = new URI(SERVER_URL + url + params);
        HttpGet method = new HttpGet(uri);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = client.execute(method, responseHandler);
        JSONObject json = new JSONObject(responseBody);
        JSONObject jo = json.getJSONObject("user");
        if (jo != null) {
          SharedPreferences.Editor e = mPrefs.edit();
          e.putString(PREF_USER_ID, jo.getString("id"));
          e.putString(PREF_PHONE_NUMBER, phoneNumber);
          e.commit();
        }
      } catch (Exception e) {
        Log.e(getClass().getName(), e.getMessage());
      } finally {
        // TODO fill in blanks 
      }
    }

    private void login() {
    	DefaultHttpClient client = new DefaultHttpClient();
    	try {
    		String url = "/user_sessions/create?user_session[login]=zlu&user_session[password]=flute&format=json";
    		URI uri = new URI(SERVER_URL + url);
    		HttpPost method = new HttpPost(uri);
    		ResponseHandler<String>	responseHandler = new BasicResponseHandler();
    		String responseBody = client.execute(method, responseHandler);
    		JSONObject json = new JSONObject(responseBody);
        String token = json.getJSONObject("record").getJSONObject("user").getString("persistence_token");
        SharedPreferences.Editor e = mPrefs.edit();
        e.putString(PREF_TOKEN, token);
        e.commit();
    	} catch(Exception e) {
    		Log.e(getClass().getName(), e.getMessage());
    	}
    }

}