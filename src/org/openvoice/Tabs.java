package org.openvoice;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class Tabs extends TabActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      
      TabHost tabHost = getTabHost();
      TabHost.TabSpec spec;
      Intent intent = new Intent().setClass(this, MessagingsActivity.class);
      spec = tabHost.newTabSpec("messages").setIndicator("messages").setContent(intent);
      tabHost.addTab(spec);

      intent = new Intent().setClass(this, InboundCallActivity.class);
      spec = tabHost.newTabSpec("inbound").setIndicator("inbound").setContent(intent);
      tabHost.addTab(spec);

      intent = new Intent().setClass(this, InboundCallActivity.class);
      spec = tabHost.newTabSpec("outbound").setIndicator("outbound").setContent(intent);
      tabHost.addTab(spec);

      intent = new Intent().setClass(this, VoicemailsActivity.class);
      spec = tabHost.newTabSpec("voicemails").setIndicator("voicemails").setContent(intent);
      tabHost.addTab(spec);

      tabHost.setCurrentTab(0);
//      tabHost.setCurrentTabByTag(getIntent());
//      tabHost.addTab(tabHost.newTabSpec("Msg")
//              .setIndicator("list")
//              .setContent(new Intent(this, Main.class)));
//
//      tabHost.addTab(tabHost.newTabSpec("tab2")
//              .setIndicator("photo list")
//              .setContent(new Intent(this, List8.class)));

  }

}
