package com.github.slashmili.Zendroid

import _root_.android.app.Activity
import _root_.android.os.Bundle
import _root_.android.view.View
import _root_.android.widget.{TextView, Button, Spinner, ArrayAdapter}
import _root_.android.util.Log
import java.util.Date


import Storage.UserData


class MainActivity extends Activity  {
  var btnOK: Button  = _
  var btnCancel: Button  = _
  var txtUrl: TextView = _
  var txtUsername: TextView = _
  var txtPassword: TextView = _
  var spnCheckEvery: Spinner = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)

    /* load widget*/
    btnOK         = findViewById(R.id.ok).asInstanceOf[Button]
    btnCancel     = findViewById(R.id.cancel).asInstanceOf[Button]
    txtUrl        = findViewById(R.id.url).asInstanceOf[TextView]
    txtUsername   = findViewById(R.id.username).asInstanceOf[TextView]
    txtPassword   = findViewById(R.id.password).asInstanceOf[TextView]
    spnCheckEvery = findViewById(R.id.every).asInstanceOf[Spinner]

    /* load saved date */
    loadUserData

    /* set listeners */
    setListener

    val list = Array("Every 5 min", "Every 10 mins", "Every 20 mins", "Manual")
    var adapter = new ArrayAdapter[String](this, android.R.layout.simple_spinner_item, list)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spnCheckEvery.setAdapter(adapter);


  }

  private def setListener = {
    btnOK.setOnClickListener(new View.OnClickListener() {
        override def onClick(v:View) {
            saveUserDate(txtUrl.getText.toString, txtUsername.getText.toString, txtPassword.getText.toString, "Manual")

            finish();
        }
      })
  }

  private def saveUserDate(url: String, username:String, password: String, checkEvery: String) = {
    val dh = new UserData(this)
    dh.deleteAll
    dh.save(url, username, password, checkEvery)
  }
  private def loadUserData = {
    val dh = new UserData(this)
    val saved_date = dh.selectAll
    if(saved_date != List()){
      txtUrl.setText(saved_date(0)("url"))
      txtUsername.setText(saved_date(0)("username"))
      txtPassword.setText(saved_date(0)("password"))
      Log.w("Hi I have data", saved_date.toString)
     }
  }


}
