package org.openvoice;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Tabs extends TabActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      Resources res = getResources();
      
      TabHost tabHost = getTabHost();
      TabHost.TabSpec spec;
      Intent intent = new Intent().setClass(this, MessagingsActivity.class);
      spec = tabHost.newTabSpec("messages").setIndicator("messages", res.getDrawable(R.drawable.ic_menu_send)).setContent(intent);
      tabHost.addTab(spec);

      intent = new Intent().setClass(this, InboundCallActivity.class);
      spec = tabHost.newTabSpec("inbound").setIndicator("inbound", res.getDrawable(R.drawable.arrow_down_unselected)).setContent(intent);
      tabHost.addTab(spec);

      intent = new Intent().setClass(this, OutboundCallActivity.class);
      spec = tabHost.newTabSpec("outbound").setIndicator("outbound", res.getDrawable(R.drawable.arrow_up_unselected)).setContent(intent);
      tabHost.addTab(spec);

      intent = new Intent().setClass(this, VoicemailsActivity.class);
      spec = tabHost.newTabSpec("voicemails").setIndicator("voicemails", res.getDrawable(R.drawable.dialog)).setContent(intent);
      tabHost.addTab(spec);

      tabHost.setCurrentTab(0);
  }

}
