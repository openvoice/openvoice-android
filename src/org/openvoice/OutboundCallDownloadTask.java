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

public class OutboundCallDownloadTask extends AsyncTask<String, Void, Boolean> {

	private org.openvoice.OutboundCallActivity mMain;
  private Context mContext;
  private SharedPreferences mPrefs;
  private String[][] mCalls;
  
  public OutboundCallDownloadTask(Context context, org.openvoice.OutboundCallActivity main) {
    mContext = context;
    mMain = main;
    mPrefs = context.getSharedPreferences(org.openvoice.MessagingsActivity.PREFERENCES_NAME, Context.MODE_WORLD_READABLE);    
  }  

  @Override
  protected Boolean doInBackground(String... params) {
    return new Boolean(downloadStatus());
  }

  @Override
  protected void onPostExecute(Boolean result) {
    if(result) {
      mMain.showCalls(mCalls);
    }
  }

  public boolean downloadStatus() {
    DefaultHttpClient client = new DefaultHttpClient();
    try {
      String user_id = mPrefs.getString(org.openvoice.MessagingsActivity.PREF_USER_ID, "");
      String token = mPrefs.getString(org.openvoice.MessagingsActivity.PREF_TOKEN, "");
      String addr = "/users/" + user_id + "/voice_calls?format=json&token=" + token; 
      URI uri = new URI(org.openvoice.MessagingsActivity.SERVER_URL + addr);
      HttpGet method = new HttpGet(uri);
      ResponseHandler<String> responseHandler = new BasicResponseHandler();
      String responseBody = client.execute(method, responseHandler);
      if( responseBody != null && responseBody != "") {
        try {
          JSONArray jsons = new JSONArray(responseBody);
          mCalls = new String[jsons.length()][2];
          for(int i=0; i<jsons.length(); i++) {
            JSONObject json = jsons.getJSONObject(i);            
            JSONObject message = json.getJSONObject("voice_call");
            extract_status(i, message);
          }
        } catch(JSONException jsone) {
          try {
            JSONObject json = new JSONObject(responseBody);
            JSONArray ar = json.toJSONArray(json.names());
            JSONObject elem = ar.getJSONObject(0);
            mCalls = new String[1][2];
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
    mCalls[i][0] = elem.getString("to");
    mCalls[i][1] = elem.getString("created_at");
  }
}