package org.openvoice;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MessagingsActivity extends Activity {

  private SharedPreferences mPrefs;
  public static final String PREFERENCES_NAME = "OpenVoice";
  public static final String PREF_USER_ID = "UserID";
  public static final String PREF_USER_LOGIN = "UserLogin";
  public static final String PREF_PHONE_NUMBER = "MyPhoneNumber";
  public static final String PREF_TOKEN = "Token";

  public static final String EXTRA_TO = "org.openvoice.extra.TO";
 
  private static final int LOGIN_DIALOG = 0;
  
  private ListView mMessageListView;  
  private Dialog mLoginDialog;
  
  /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messagings);
        Context context = getApplicationContext();
        mPrefs = context.getSharedPreferences(PREFERENCES_NAME, MODE_WORLD_READABLE);        
        mMessageListView = (ListView) findViewById(R.id.MessagesListView);      
        handleLogin();       
    }
    
    /**
     * builds adapter that binds message data with message_row and the list view of messages.
     * each row of message displays caller_id, time, and message body.
     * 
     * @param messages
     */
    void showMessages(final List<Map<String, String>> messageData) {
    	String[] from = {"caller_id", "time", "direction", "caller_name", "message_body"};
    	// to array contains ids from message_row.xml
    	int[] to= {R.id.caller_id, R.id.message_datetime, R.id.direction, R.id.caller_name, R.id.message_body};
      mMessageListView.setAdapter(new SimpleAdapter(this, messageData, R.layout.message_row, from, to));
      mMessageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long row_id) {
          Intent newMessageIntent = new Intent(getApplicationContext(), NewMessageActivity.class);
          String to = ((Map<String, String>)messageData.get(position)).get("caller_id").toString();
          newMessageIntent.putExtra(EXTRA_TO, to);
          startActivity(newMessageIntent);
        }
      });
    }
    
    private void handleUserMessaging() {
      new MessagingDownloadTask(getApplicationContext(), this).execute();	  
    }

    /**
     * displays the login dialog if to persistence token is saved.
     */
    private void handleLogin() {
    	if(mPrefs.getString(PREF_TOKEN, "").equals("")) {
    		showDialog(LOGIN_DIALOG);
    	} else {
      	handleUserMessaging();
    	}
    }
    
    /**
     * upon successful login, saves persistence token and user login so that we don't prompt user to login in the future
     * @param username
     * @param password
     * @return
     */
    private boolean login(String username, String password) {
    	DefaultHttpClient client = new DefaultHttpClient();
    	try {
    		String url = "/user_session?user_session[login]=" + username +"&user_session[password]=" + password + "&format=json";
    		URI uri = new URI(SettingsActivity.getServerUrl(getApplicationContext()) + url);
    		HttpPost method = new HttpPost(uri);
    		ResponseHandler<String>	responseHandler = new BasicResponseHandler();
    		String responseBody = client.execute(method, responseHandler);
    		JSONObject json = new JSONObject(responseBody);
        JSONObject userJson = json.getJSONObject("record").getJSONObject("user");
        SharedPreferences.Editor e = mPrefs.edit();
        e.putString(PREF_TOKEN, userJson.getString("persistence_token"));
        e.putString(PREF_USER_LOGIN, userJson.getString("login"));
        e.commit();
        
        registerPhoneNumber(userJson.getString("login"));
        
        return true;
    	} catch(Exception e) {
    		Log.e(getClass().getName(), e.getMessage());
    	}
  		return false;
    }
    
    /**
     * registers phone number to the user account if needed.
     * @return
     */
    private boolean registerPhoneNumber(String user_login) {
    	TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
  	  String phoneNumber = tm.getLine1Number(); 
      DefaultHttpClient client = new DefaultHttpClient();
      try {	
        String url = "/users/register_phone";
        String params = "?format=json&phone_number=" + phoneNumber + "&user_login=" + user_login;
        URI uri = new URI(SettingsActivity.getServerUrl(getApplicationContext()) + url + params);
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
        
      	handleUserMessaging();
      	
        return true;
      } catch (Exception e) {
        Log.e(getClass().getName(), e.getMessage());
      } finally {	
        // TODO fill in blanks 
      }    	
    	return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
      switch (id) {
        case LOGIN_DIALOG:
          mLoginDialog = new Dialog(MessagingsActivity.this);
          mLoginDialog.setTitle("Login");
          mLoginDialog.setContentView(R.layout.login);
          Button authenticateButton = (Button) mLoginDialog.findViewById(R.id.authenticate_button);
          authenticateButton.setOnClickListener(mLoginListener);
          return mLoginDialog;
      }
      return null;
    }

    private OnClickListener mLoginListener = new OnClickListener() {
      public void onClick(View v) {    	
      	String username = ((EditText)mLoginDialog.findViewById(R.id.ov_username)).getText().toString();
      	String password= ((EditText)mLoginDialog.findViewById(R.id.ov_password)).getText().toString();
        if(login(username, password)) {
        	mLoginDialog.dismiss();
        	Toast.makeText(getApplicationContext(), "Login suceeded", Toast.LENGTH_LONG).show();
        } else {
        	Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_LONG).show();        	
        }
      }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.new_message, menu);
      return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
        case R.id.new_message:
          startActivity(new Intent(this, NewMessageActivity.class));
          return true;
        case R.id.refresh:
        	handleUserMessaging();
        	return true;
        case R.id.login_item:
        	showDialog(LOGIN_DIALOG);
        	return true;
        case R.id.setting_item:
          startActivity(new Intent(this, SettingsActivity.class));
          return true;
      }
      return false;
    }    
}