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
      mMain.showMessages();
    }
  }

  public boolean downloadStatus() {
    DefaultHttpClient client = new DefaultHttpClient();
    String[][] stat = null;
    try {
      String user_id = mPrefs.getString(org.openvoice.Main.PREF_USER_ID, "");
      String token = mPrefs.getString(org.openvoice.Main.PREF_TOKEN, "");
      String addr = "/users/" + user_id + "/messagings?format=json&token=" + token; 
      URI uri = new URI(org.openvoice.Main.SERVER_URL_DEV + addr);
      HttpGet method = new HttpGet(uri);
      ResponseHandler<String> responseHandler = new BasicResponseHandler();
      String responseBody = client.execute(method, responseHandler);
      if( responseBody != null && responseBody != "") {
        try {
          JSONArray jsons = new JSONArray(responseBody);
          stat = new String[jsons.length()][2];
          for(int i=0; i<jsons.length(); i++) {
            JSONObject json = jsons.getJSONObject(i);
            JSONArray ar = json.toJSONArray(json.names());
            JSONObject elem = ar.getJSONObject(0);
            extract_status(stat, i, elem);
          }
        } catch(JSONException jsone) {
          try {
            JSONObject json = new JSONObject(responseBody);
            JSONArray ar = json.toJSONArray(json.names());
            JSONObject elem = ar.getJSONObject(0);
            stat = new String[1][2];
            extract_status(stat, 0, elem);
          } catch(JSONException e) {
            Log.e(getClass().getName(), e.getMessage());
          }
        }
//        String localStatus = StatusDBOpenHelper.getInstance(mContext).getCurrentStatus();
//        boolean shouldSync = !localStatus.equals(stat[0][0]);
//        if(shouldSync) {
//          StatusDBOpenHelper.getInstance(mContext).insertTwitterStatus(stat[0][0], stat[0][1]);
//          return true;
//        }        
      }
    } catch (Exception e) {
      //Log.e(getClass().getName(), e.getMessage());
    } finally {
      // TODO do something here
    }
    return false;
  }

  private void extract_status(String[][] stat, int i, JSONObject elem)
  throws JSONException {
    stat[i][0] = elem.getString("from");
    stat[i][1] = new Long(elem.getLong("text")).toString();
  }
}