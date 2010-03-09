package org.openvoice;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class VoicemailsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView textview = new TextView(this);
		textview.setText("This is the voicemails tab");
		setContentView(textview);
	}

}
