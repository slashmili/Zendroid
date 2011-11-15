package com.github.slashmili.Zendroid

import _root_.android.app.{Activity, NotificationManager, Dialog}
import _root_.android.os.{Bundle, Handler}
import _root_.android.view.View
import _root_.android.widget.{ExpandableListView, Toast, TextView}
import _root_.android.content.{Intent, Context}
import _root_.com.github.slashmili.Zendroid.Services.ServiceRunner
import _root_.com.github.slashmili.Zendroid.Services._
import _root_.android.util.Log
import _root_.android.view.{View, Menu, MenuItem}
import _root_.android.app.{ProgressDialog, AlertDialog}
import _root_.android.content.DialogInterface
import _root_.android.text.format.Time

import utils.CustomDeviceErrorListView
import com.github.slashmili.Zendroid.utils.{ZenossAPI, ZenossEvents}
import ZenossEvents._

class EventConsoleActivity extends Activity {
  var listView: ExpandableListView = _
  var txtLastTime: TextView  = _
  var mHandler = new Handler()

  var adapter = new CustomDeviceErrorListView(this)
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setTitle("Event Console")
    setContentView(R.layout.event_console)
    listView = findViewById(R.id.deviceErrorListView).asInstanceOf[ExpandableListView]

    listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
        def onChildClick(parent: ExpandableListView , v: View , groupPosition: Int , childPosition: Int, id: Long): Boolean = {
        val event = adapter.getChild(groupPosition, childPosition).asInstanceOf[Store.Event]
        val device = adapter.getGroup(groupPosition).asInstanceOf[Store.ZenossDevice]
        //doing some work for child
        showDetails(device, event)
        true
      }
    })
    txtLastTime = findViewById(R.id.txtEventConsoleLastTime).asInstanceOf[TextView]
    val config = ZendroidPreferences.loadPref(EventConsoleActivity.this)
      if(config == None){
        val conf = new Intent(EventConsoleActivity.this, classOf[GlobalConfiguration])
        startActivity(conf)
      }
  }


  def showDetails(device:Store.ZenossDevice,  event: Store.Event) = {
    val dialog = new Dialog(this)
    dialog.setContentView(R.layout.event_detail_dialog)
    dialog.setTitle("Event Detail")
    val txtEventDetailDeviceName = dialog.findViewById(R.id.txtEventDetailDeviceName).asInstanceOf[TextView]
    txtEventDetailDeviceName.setText(device.getName)
    val txtEventDetailComponent  = dialog.findViewById(R.id.txtEventDetailComponent).asInstanceOf[TextView]
    txtEventDetailComponent.setText(event.getComponent)
    val txtEventDetailStatus     = dialog.findViewById(R.id.txtEventDetailStatus).asInstanceOf[TextView]
    txtEventDetailStatus.setText(event.getEventState.toString)
    val txtEventDetailStartTime  = dialog.findViewById(R.id.txtEventDetailStartTime).asInstanceOf[TextView]
    txtEventDetailStartTime.setText(event.getFirstTime)
    val txtEventDetailLastSeen   = dialog.findViewById(R.id.txtEventDetailLastSeen).asInstanceOf[TextView]
    txtEventDetailLastSeen.setText(event.getLastTime)
    val txtEventDetailCount      = dialog.findViewById(R.id.txtEventDetailCount).asInstanceOf[TextView]
    txtEventDetailCount.setText(event.getCount.toString)
    val txtEventDetailMessage    = dialog.findViewById(R.id.txtEventDetailMessage).asInstanceOf[TextView]
    txtEventDetailMessage.setText(event.getSummary)
    val txtEventDetailSummary    = dialog.findViewById(R.id.txtEventDetailSummary).asInstanceOf[TextView]
    txtEventDetailSummary.setText(event.getSummary)


    dialog.findViewById(R.id.btnEventDetailClose).setOnClickListener(new View.OnClickListener() {
        def onClick(v: View): Unit = {
          dialog.cancel
        }
      }
    )

  dialog.findViewById(R.id.btnEventDetailAckUnck).setOnClickListener(new View.OnClickListener() {
      def onClick(v: View): Unit = {
        val config = ZendroidPreferences.loadPref(EventConsoleActivity.this)
        val url  = config.get("url").toString
        val user = config.get("user").toString
        val pass = config.get("pass").toString

        val invalidSSL = if (config.get("invalid_ssl").toString == "0"){
            false
          } else {
            true
        }

        val zen = new ZenossAPI(url, user, pass, invalidSSL)
        zen.auth
        if(event.getEventState == "New") {
          zen.eventsAcknowledge(event.getEvID)
          event.setEventState(Store.EventState.ACKNOWLEDGED)
        }else {
          zen.eventsUnacknowledge(event.getEvID)
          event.setEventState(Store.EventState.NEW)
        }
      }
    })
    dialog.findViewById(R.id.btnEventDetailClear).setOnClickListener(new View.OnClickListener() {
      def onClick(v: View): Unit = {
        val config = ZendroidPreferences.loadPref(EventConsoleActivity.this)
        val url  = config.get("url").toString
        val user = config.get("user").toString
        val pass = config.get("pass").toString
        val invalidSSL = if (config.get("invalid_ssl").toString == "0"){
            false
          } else {
            true
        }

        val zen = new ZenossAPI(url, user, pass, invalidSSL)
        zen.auth
        zen.eventsClose(event.getEvID)
        device.removeEvent(event.getEvID)
        dialog.cancel
        refreshActivity

     }
    })
    dialog.show()
  }

  def clearnotification = {
    val ns   = Context.NOTIFICATION_SERVICE;
    val nm   = getSystemService(ns).asInstanceOf[NotificationManager]
    ServiceRunner.EventStore.eventIDs.foreach(nm.cancel(_, 0) )
  }

  def refreshActivity = {
    adapter = new CustomDeviceErrorListView(this)
    listView.setAdapter(adapter)
    ServiceRunner.EventStore.getDevices.foreach( x=> adapter.addItem(x._2) )
    adapter.notifyDataSetChanged()
    if(ServiceRunner.lastTime != null)
        txtLastTime.setText("Last events fetch: " + ServiceRunner.lastTime.format("%R"))

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
      "You haven't run Zenroid service yet"
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
             emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Zenroid Stack Error")
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
