package com.github.slashmili.Zendroid

import _root_.android.app.{Activity, NotificationManager}
import _root_.android.os.{Bundle, Handler}
import _root_.android.view.View
import _root_.android.widget.{ExpandableListView, Toast}
import _root_.android.content.{Intent, Context}
import _root_.com.github.slashmili.Zendroid.Services.ServiceRunner
import _root_.android.util.Log
import _root_.android.view.{View, Menu, MenuItem}
import _root_.android.app.{ProgressDialog, AlertDialog}
import _root_.android.content.DialogInterface
import _root_.java.util.TimerTask

import utils.CustomDeviceErrorListView

class EventConsoleActivity extends Activity {
  var listView: ExpandableListView = _
  var mHandler = new Handler()

  val adapter = new CustomDeviceErrorListView(this)
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.event_console)
    listView = findViewById(R.id.deviceErrorListView).asInstanceOf[ExpandableListView]
    val config = ZendroidPreferences.loadPref(EventConsoleActivity.this)
      if(config == None){
        val conf = new Intent(EventConsoleActivity.this, classOf[GlobalConfiguration])
        startActivity(conf)
      }
  }


  def clearnotification = {
    val ns   = Context.NOTIFICATION_SERVICE;
    val nm   = getSystemService(ns).asInstanceOf[NotificationManager]
    ServiceRunner.EventStore.eventIDs.foreach(nm.cancel(_, 0) )
  }

  def refreshActivity = {
    val adapter = new CustomDeviceErrorListView(this)
    listView.setAdapter(adapter)
    ServiceRunner.EventStore.getDevices.foreach( x=> adapter.addItem(x._2) )
    adapter.notifyDataSetChanged()
  }
  
  /*
  var mUpdateTimeTask = new Runnable() {
    def run = {
      refreshActivity
      Log.d("EventConsoleActivity", "WWWWWWORKS")
      mHandler.postAtTime(this, System.currentTimeMillis() + 30000) //refresh Activity after .5 min
    }
  }
  */
  override def onResume = {
    super.onResume
    /*
    mHandler.removeCallbacks(mUpdateTimeTask)
    mHandler.postDelayed(mUpdateTimeTask, 100)
    */
    refreshActivity
    clearnotification
  }

  override def onPause = {
    super.onPause
    //mHandler.removeCallbacks(mUpdateTimeTask)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean ={
    val inflater = getMenuInflater()
    inflater.inflate(R.menu.config_menu, menu)
    return true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean ={
    item.getItemId match {
      case R.id.mnuLastStatus    => openLastStatusPopup
      case R.id.mnuAccount => {
        val conf = new Intent(EventConsoleActivity.this, classOf[GlobalConfiguration])
        startActivity(conf)
      }
      case R.id.mnuRefresh => {
        refreshActivity
      }
      case R.id.mnuAbout         => {
        val about = new Intent(EventConsoleActivity.this, classOf[MainActivity])
        startActivity(about)
      }
    }
    return super.onOptionsItemSelected(item)
  }


  def openLastStatusPopup() = {
    var lastRunStatus = if (ServiceRunner.started == false){
      "You haven't run Zendroid service yet"
    }
    else if (ServiceRunner.errorMessage == "" ){
      "Clean"
    }else {
      ServiceRunner.lastThrowableError.getMessage
    }
    lastRunStatus = "Last Error: " + lastRunStatus
    new AlertDialog.Builder(EventConsoleActivity.this)
    .setTitle("Last Status")
    .setMessage("Next Update: " + ServiceRunner.nextTime.format("%R") + "\n" + lastRunStatus)
    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which:Int) ={
          dialog.cancel
        }
      })
    .setPositiveButton("Send debug info !", new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which:Int)= {
           if (ServiceRunner.lastThrowableError != null ){
             var stackTrace = "Exception : " + ServiceRunner.lastThrowableError.toString + "\n"
             for (e <- ServiceRunner.lastThrowableError.getStackTrace){ 
               stackTrace = stackTrace + e.toString + "\n" 
             }
             val emailIntent = new Intent(Intent.ACTION_SEND)
             emailIntent.setType("plain/text")
             emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, Array("miliroid@gmail.com"))
             emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Zendroid Stack Error")
             emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, stackTrace )
             startActivity(Intent.createChooser(emailIntent, "Report Last Exception"))
           }else {
              val toastMessage = "There isn't any debug to send"
              val context = getApplicationContext()
              val toast = Toast.makeText(context, toastMessage , Toast.LENGTH_SHORT)
              toast.show()
          }
        }
      })
    .show()
  }
}
