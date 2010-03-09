package org.openvoice;

import java.io.IOException;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VoicemailsActivity extends ListActivity {
	
	private SharedPreferences mPrefs;
  private String [] mVoicemails;
  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
    Context context = getApplicationContext();
    mPrefs = context.getSharedPreferences(MessagingsActivity.PREFERENCES_NAME, MODE_WORLD_READABLE);
    handleUserVoicemail();    
	}

  private void handleUserVoicemail() {
    new VoicemailDownloadTask(getApplicationContext(), this).execute();	  
  }
  
  void showVoicemails(String[][] voicemails) {
//  	String [] condensedVoicemails = new String[voicemails.length];
//  	int i = 0;
//  	for(String[] m : voicemails) {
//  		condensedVoicemails[i] = m[i];
//  		i++;
//  	}
    
    setListAdapter(new VoicemailListAdapter(this, voicemails));


  }

  /**
   * A sample ListAdapter that presents content from arrays of speeches and
   * text.
   * 
   */
  private class VoicemailListAdapter extends BaseAdapter {
      public VoicemailListAdapter(Context context, String[][] voicemails) {
          mContext = context;
          mFrom = new String[voicemails.length];
          mPath = new String[voicemails.length];
          mPlay = new String[voicemails.length];
          for(int i=0; i<mFrom.length; i++) {
          	mFrom[i] = voicemails[i][0];
          	mPath[i] = voicemails[i][1];
          	mPlay[i] = "Play";
          }
      }

      /**
       * The number of items in the list is determined by the number of speeches
       * in our array.
       * 
       * @see android.widget.ListAdapter#getCount()
       */
      public int getCount() {
          return mFrom.length;
      }

      /**
       * Since the data comes from an array, just returning the index is
       * sufficent to get at the data. If we were using a more complex data
       * structure, we would return whatever object represents one row in the
       * list.
       * 
       * @see android.widget.ListAdapter#getItem(int)
       */
      public Object getItem(int position) {
          return position;
      }

      /**
       * Use the array index as a unique id.
       * 
       * @see android.widget.ListAdapter#getItemId(int)
       */
      public long getItemId(int position) {
          return position;
      }

      /**
       * Make a SpeechView to hold each row.
       * 
       * @see android.widget.ListAdapter#getView(int, android.view.View,
       *      android.view.ViewGroup)
       */
      public View getView(int position, View convertView, ViewGroup parent) {
          VoicemailView sv;
          if (convertView == null) {
              sv = new VoicemailView(mContext, mFrom[position], mPath[position], mPlay[position]);
          } else {
              sv = (VoicemailView) convertView;
              sv.setTitle(mFrom[position]);
              sv.setPath(mPath[position]);
              sv.setDialogue(mPlay[position]);
          }

          return sv;
      }

      private Context mContext;
      private String[] mFrom;
      private String[] mPath;
      private String[] mPlay;
  }
  
  /**
   * We will use a SpeechView to display each speech. It's just a LinearLayout
   * with two text fields.
   *
   */
  private class VoicemailView extends LinearLayout {
      public VoicemailView(Context context, String title, String path, String words) {
          super(context);
          this.setOrientation(VERTICAL);
          mContext = context;
          // Here we build the child views in code. They could also have
          // been specified in an XML file.
          mFrom = new TextView(context);
          mFrom.setText(title);
          addView(mFrom, new LinearLayout.LayoutParams(
                  LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

          mPath = path;
          
          mPlayButton = new Button(context);
          mPlayButton.setText(words);
          mPlayButton.setOnClickListener(mPlayClickListener);
          addView(mPlayButton, new LinearLayout.LayoutParams(
                  LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      }
      
      private View.OnClickListener mPlayClickListener = new View.OnClickListener() {
        public void onClick(View view) {
          MediaPlayer mp = new MediaPlayer();
          
          Uri path = Uri.parse(mPath);
          try {
      	    mp.setDataSource(mContext, path);
      	    mp.prepare();
      	    mp.start();
          } catch (IllegalArgumentException e) {
      	    // TODO Auto-generated catch block
      	    e.printStackTrace();
          } catch (SecurityException e) {
      	    // TODO Auto-generated catch block
      	    e.printStackTrace();
          } catch (IllegalStateException e) {
      	    // TODO Auto-generated catch block
      	    e.printStackTrace();
          } catch (IOException e) {
      	    // TODO Auto-generated catch block
      	    e.printStackTrace();
          }
        }
      };

      public void setPath(String path) {
      	mPath = path;
      }
      
      /**
       * Convenience method to set the title of a SpeechView
       */
      public void setTitle(String title) {
          mFrom.setText(title);
      }

      /**
       * Convenience method to set the dialogue of a SpeechView
       */
      public void setDialogue(String words) {
          mPlayButton.setText(words);
      }

      private Context mContext;
      private TextView mFrom;
      private String mPath;
      private Button mPlayButton;
  }
 
}
