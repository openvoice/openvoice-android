package org.openvoice;

import java.net.URI;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class MessagingDownloadTask extends AsyncTask<String, Void, Boolean> {

	private org.openvoice.Main mMain;
  private Context mContext;
  private SharedPreferences mPrefs;
  private String[][] mMessages;
  
  public MessagingDownloadTask(Context context, org.openvoice.Main main) {
    mContext = context;
    mMain = main;
    mPrefs = context.getSharedPreferences(org.openvoice.Main.PREFERENCES_NAME, Context.MODE_WORLD_READABLE);    
  }  

  @Override
  protected Boolean doInBackground(String... params) {
    return new Boolean(downloadStatus());
  }

  @Override
  protected void onPostExecute(Boolean result) {
    if(result) {
      mMain.showMessages(mMessages);
    }
  }

  public boolean downloadStatus() {
    DefaultHttpClient client = new DefaultHttpClient();
    try {
      String user_id = mPrefs.getString(org.openvoice.Main.PREF_USER_ID, "");
      String token = mPrefs.getString(org.openvoice.Main.PREF_TOKEN, "");
      String addr = "/users/" + user_id + "/messagings?format=json&token=" + token; 
      URI uri = new URI(org.openvoice.Main.SERVER_URL + addr);
      HttpGet method = new HttpGet(uri);
      ResponseHandler<String> responseHandler = new BasicResponseHandler();
      String responseBody = client.execute(method, responseHandler);
      if( responseBody != null && responseBody != "") {
        try {
          JSONArray jsons = new JSONArray(responseBody);
          mMessages = new String[jsons.length()][2];
          for(int i=0; i<jsons.length(); i++) {
            JSONObject json = jsons.getJSONObject(i);            
            JSONObject message = json.getJSONObject("messaging");
            extract_status(i, message);
          }
        } catch(JSONException jsone) {
          try {
            JSONObject json = new JSONObject(responseBody);
            JSONArray ar = json.toJSONArray(json.names());
            JSONObject elem = ar.getJSONObject(0);
            mMessages = new String[1][2];
            extract_status(0, elem);
          } catch(JSONException e) {
            Log.e(getClass().getName(), e.getMessage());
          }
        }
//        String localStatus = StatusDBOpenHelper.getInstance(mContext).getCurrentStatus();
//        boolean shouldSync = !localStatus.equals(stat[0][0]);
//        if(shouldSync) {
//          StatusDBOpenHelper.getInstance(mContext).insertTwitterStatus(stat[0][0], stat[0][1]);
          return true;
//        }        
      }
    } catch (Exception e) {
      //Log.e(getClass().getName(), e.getMessage());
    } finally {
      // TODO do something here
    }
    return false;
  }

  private void extract_status(int i, JSONObject elem)
  throws JSONException {
    mMessages[i][0] = elem.getString("from");
    mMessages[i][1] = elem.getString("text");
  }
}