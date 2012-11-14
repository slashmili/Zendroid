package com.github.slashmili.Zendroid.activities

import _root_.android.app.Activity
import _root_.android.os.Bundle
import _root_.android.view.View
import _root_.android.widget.Button
import _root_.android.content.Intent;
import _root_.android.net.Uri
import java.util.Date

import com.github.slashmili.Zendroid._


class About extends Activity  {
  var btnOK: Button  = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    /* load widget*/
    btnOK         = findViewById(R.id.ok).asInstanceOf[Button]
    /* set listeners */
    btnOK.setOnClickListener(new View.OnClickListener() {
      override def onClick(v:View) {
        val donate_url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=RXU9ZCHJM9XX4&lc=MY&item_name=Zendroid&item_number=Buy%20me%20a%20beer&currency_code=USD&bn=PP%2dDonationsBF%3awsojko%2epng%3aNonHosted"
        val browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(donate_url));
        startActivity(browserIntent)
        finish();
      }
    })
  }
}
