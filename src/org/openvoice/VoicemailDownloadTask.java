package org.openvoice;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class VoicemailDownloadTask extends AsyncTask<String, Void, Boolean> {

	private VoicemailsActivity mVoicemailsActivity;
  private Context mContext;
  private SharedPreferences mPrefs;
  private String[][] mVoicemails1;
  private List<Map<String, String>> mVoicemails;
  
  public VoicemailDownloadTask(Context context, VoicemailsActivity voicemailsActivity) {
    mContext = context;
    mVoicemailsActivity = voicemailsActivity;
    mPrefs = context.getSharedPreferences(org.openvoice.MessagingsActivity.PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
    mVoicemails = new ArrayList<Map<String, String>>();
  }  

  @Override
  protected Boolean doInBackground(String... params) {
    return new Boolean(downloadStatus());
  }

  @Override
  protected void onPostExecute(Boolean result) {
    if(result) {
      mVoicemailsActivity.showVoicemails(mVoicemails);
    }
  }

  public boolean downloadStatus() {
    DefaultHttpClient client = new DefaultHttpClient();
    try {
      String user_id = mPrefs.getString(org.openvoice.MessagingsActivity.PREF_USER_ID, "");
      String token = mPrefs.getString(org.openvoice.MessagingsActivity.PREF_TOKEN, "");
      String addr = "/users/" + user_id + "/voicemails?format=json&token=" + token; 
      URI uri = new URI(SettingsActivity.getServerUrl(mContext) + addr);
      HttpGet method = new HttpGet(uri);
      ResponseHandler<String> responseHandler = new BasicResponseHandler();
      String responseBody = client.execute(method, responseHandler);
      if( responseBody != null && responseBody != "") {
        try {
          JSONArray jsons = new JSONArray(responseBody);
          mVoicemails1 = new String[jsons.length()][3];
          for(int i=0; i<jsons.length(); i++) {
            JSONObject json = jsons.getJSONObject(i);            
            JSONObject message = json.getJSONObject("voicemail");
            extract_status(i, message);
          }
        } catch(JSONException jsone) {
          try {
            JSONObject json = new JSONObject(responseBody);
            JSONArray ar = json.toJSONArray(json.names());
            JSONObject elem = ar.getJSONObject(0);
            mVoicemails1 = new String[1][3];
            extract_status(0, elem);
          } catch(JSONException e) {
            Log.e(getClass().getName(), e.getMessage());
          }
        }

        return true;
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
    mVoicemails1[i][0] = elem.getString("from");
    mVoicemails1[i][1] = elem.getString("text");    
    mVoicemails1[i][2] = elem.getString("filename");
    
    HashMap<String, String> md = new HashMap<String, String>();
    md.put("caller_id", elem.getString("from"));
    // for now all directions are set to from because we do not support support outgoing vm yet
   	md.put("direction", "From: ");
    md.put("caller_name", ContactManager.getInstance(mContext).getContactNameByPhoneNumber(md.get("caller_id").toString()));
    md.put("time", elem.getString("created_at"));
    md.put("transcription", elem.getString("text"));
    md.put("filename", elem.getString("filename"));
    mVoicemails.add(md);    
  }
}