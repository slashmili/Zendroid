package com.github.slashmili.Zendroid.activities

import _root_.android.app.{Activity, NotificationManager, Dialog}
import _root_.android.os.{Bundle, Handler}
import _root_.android.widget.{ExpandableListView, Toast, TextView, ImageView, ProgressBar}
import _root_.android.content.{res, Intent, Context, IntentFilter, BroadcastReceiver}
import _root_.android.util.Log
import _root_.android.view.{View, Menu, MenuItem}
import _root_.android.app.{ProgressDialog, AlertDialog}
import _root_.android.content.DialogInterface
import _root_.android.text.format.Time
import _root_.android.content.DialogInterface.OnDismissListener



import com.github.slashmili.Zendroid._
import services.ServiceRunner
import services.store
import utils.{ZenossAPI, CustomDeviceErrorListView}
import utils.ZenossEvents._
import settings.{ZendroidPreferences, ZenroidSettings}

class EventConsoleActivity extends Activity {
  var listView: ExpandableListView = _
  var txtLastTime: TextView  = _
  var dlgShowDetails: Dialog = _
  var adapter = new CustomDeviceErrorListView(this)
  var expandedDevice:List[String] = List()
  var selectedEvent:store.Event = _
  var selectedDevice:store.ZenossDevice = _
  var pgbEventConsoleLastWaiting: ProgressBar = _

  val receiver = new BroadcastReceiver() {
    override def onReceive(context: Context, intent:Intent) ={
      refreshActivity
    }
  }

  def attachViews() {
    listView = findViewById(R.id.deviceErrorListView).asInstanceOf[ExpandableListView]
    pgbEventConsoleLastWaiting = findViewById(R.id.pgbEventConsoleLastWaiting).asInstanceOf[ProgressBar]
    txtLastTime = findViewById(R.id.txtEventConsoleLastTime).asInstanceOf[TextView]
  }
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setTitle("Event Console")
    setContentView(R.layout.event_console)

    attachViews

    listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
        def onChildClick(parent: ExpandableListView , v: View , groupPosition: Int , childPosition: Int, id: Long): Boolean = {
        val event = adapter.getChild(groupPosition, childPosition).asInstanceOf[store.Event]
        val device = adapter.getGroup(groupPosition).asInstanceOf[store.ZenossDevice]
        //doing some work for child
        selectedEvent  = event
        selectedDevice = device
        showDetails(device, event)
        true
      }
    })

    if(ZendroidPreferences.loadPref(this) != None){
      ZenroidSettings.mvOldConfig(this)
      ZendroidPreferences.clear(this)
    }
    if(ZenroidSettings.isEmpty(EventConsoleActivity.this) == true){
      val conf = new Intent(EventConsoleActivity.this, classOf[ZenroidSettings])
      startActivity(conf)
    }
  }

  def showDetails(device:store.ZenossDevice,  event: store.Event) = {
    dlgShowDetails = new Dialog(EventConsoleActivity.this)
    dlgShowDetails.setContentView(R.layout.event_detail_dialog)
    dlgShowDetails.setTitle("Event Detail")
    val txtEventDetailDeviceName = dlgShowDetails.findViewById(R.id.txtEventDetailDeviceName).asInstanceOf[TextView]
    txtEventDetailDeviceName.setText(device.getName)
    val txtEventDetailComponent  = dlgShowDetails.findViewById(R.id.txtEventDetailComponent).asInstanceOf[TextView]
    txtEventDetailComponent.setText(event.getComponent)
    val txtEventDetailStatus     = dlgShowDetails.findViewById(R.id.txtEventDetailStatus).asInstanceOf[TextView]
    txtEventDetailStatus.setText(event.getEventState.toString)
    val txtEventDetailStartTime  = dlgShowDetails.findViewById(R.id.txtEventDetailStartTime).asInstanceOf[TextView]
    txtEventDetailStartTime.setText(event.getFirstTime)
    val txtEventDetailLastSeen   = dlgShowDetails.findViewById(R.id.txtEventDetailLastSeen).asInstanceOf[TextView]
    txtEventDetailLastSeen.setText(event.getLastTime)
    val txtEventDetailCount      = dlgShowDetails.findViewById(R.id.txtEventDetailCount).asInstanceOf[TextView]
    txtEventDetailCount.setText(event.getCount.toString)
    val txtEventDetailMessage    = dlgShowDetails.findViewById(R.id.txtEventDetailMessage).asInstanceOf[TextView]
    txtEventDetailMessage.setText(event.getSummary)
    val txtEventDetailSummary    = dlgShowDetails.findViewById(R.id.txtEventDetailSummary).asInstanceOf[TextView]
    txtEventDetailSummary.setText(event.getSummary)

    var icon = event.getSeverity match {
      case 3 => R.drawable.severity3_notify
      case 4 => R.drawable.severity4_notify
      case 5 => R.drawable.severity5_notify
      case _ => 0
    }
    val imgEventDetailSeverityIcon = dlgShowDetails.findViewById(R.id.imgEventDetailSeverityIcon).asInstanceOf[ImageView]
    imgEventDetailSeverityIcon.setImageResource(icon)

    def prompt(header: String, body:String, action: String,evid: String, eventState: String) = {
      new AlertDialog.Builder(EventConsoleActivity.this)
      .setTitle(header)
      .setMessage(body)
      .setNegativeButton("No", new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, which:Int) ={
            dialog.cancel
          }
        })
      .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, which:Int)= {
              dialog.cancel
              new SendEventRequest().execute(action, evid, eventState)
            }
          }
        )
      .show()
    }

    dlgShowDetails.findViewById(R.id.btnEventDetailClose).setOnClickListener(new View.OnClickListener() {
        def onClick(v: View): Unit = {
          dlgShowDetails.dismiss
        }
      }
    )

  dlgShowDetails.findViewById(R.id.btnEventDetailAckUnck).setOnClickListener(new View.OnClickListener() {
      def onClick(v: View): Unit = {
        prompt("Sending Ack/Unack event","Do you want to send Ack/Unack request to server ?", "Acknowledge", event.getEvID, event.getEventState)
      }
    })

    dlgShowDetails.findViewById(R.id.btnEventDetailClear).setOnClickListener(new View.OnClickListener() {
      def onClick(v: View): Unit = {
        prompt("Sending Close event", "Do you want to send Close request to server ?", "Close", event.getEvID, event.getEventState)
     }
    })

    dlgShowDetails.setOnDismissListener(new OnDismissListener() {
      override def onDismiss(dialog: DialogInterface) = {
        refreshActivity
      }
    })

    expandedDevice = getExpandedDevice
    dlgShowDetails.show()
  }

  def clearNotification = {
    val ns   = Context.NOTIFICATION_SERVICE;
    val nm   = getSystemService(ns).asInstanceOf[NotificationManager]
    ServiceRunner.EventStore.eventIDs.foreach(nm.cancel(_, 0) )
  }

  def refreshActivity = {
    pgbEventConsoleLastWaiting.setVisibility(View.GONE)
    adapter = new CustomDeviceErrorListView(EventConsoleActivity.this)
    listView.setAdapter(adapter)
    ServiceRunner.EventStore.getDevices.foreach( x=> adapter.addItem(x._2) )
    adapter.notifyDataSetChanged()
    restoreExpandedState(expandedDevice)
    if(ServiceRunner.lastTime != null)
        txtLastTime.setText("Last events fetch: " + ServiceRunner.lastTime.format("%R"))

  }

  override def onResume = {
    super.onResume
    val filter = new IntentFilter
    filter.addAction("com.github.slashmili.Zendroid.REFRESHACTIVITY")
    registerReceiver(receiver, filter)
    refreshActivity
    clearNotification
  }

  override def onConfigurationChanged(newConfig: res.Configuration){
    super.onConfigurationChanged(newConfig)
  }

  override def onPause = {
    super.onPause
    unregisterReceiver(receiver)
    expandedDevice = getExpandedDevice
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean ={
    val inflater = getMenuInflater()
    inflater.inflate(R.menu.event_console_menu, menu)
    return true
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    if (ServiceRunner.started == false || ServiceRunner.errorMessage != "")
    {
      menu.findItem(R.id.mnuLastStatus).setIcon(R.drawable.ic_menu_connect)
    }else {
      menu.findItem(R.id.mnuLastStatus).setIcon(R.drawable.ic_menu_connected)
    }
    return true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean ={
    item.getItemId match {
      case R.id.mnuLastStatus    => openLastStatusPopup
      case R.id.mnuAccount => {
        val conf = new Intent(EventConsoleActivity.this, classOf[ZenroidSettings])
        startActivity(conf)
      }
      case R.id.mnuRefresh => {
        val toastMsg = if( ZenroidSettings.isEmpty(EventConsoleActivity.this))
            "First config Zenoss Settings"
          else{
            pgbEventConsoleLastWaiting.setVisibility(View.VISIBLE)
            ServiceRunner.startService(this, true)
            "Fetching events ..."
          }

        //save opend group view to open them later
        expandedDevice = getExpandedDevice
        val context = getApplicationContext()
        val toast = Toast.makeText(context, toastMsg , Toast.LENGTH_LONG)
        toast.show()
      }
      case R.id.mnuAbout         => {
        val about = new Intent(EventConsoleActivity.this, classOf[About])
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
    }
    else if (ZenroidSettings.getPerformSyncing(EventConsoleActivity.this) == false){
      "Zenroid is disabled"
    }
    else {
      try {
        ServiceRunner.lastThrowableError.getMessage
      } catch {
        case _ => ServiceRunner.errorMessage
      }
    }
    lastRunStatus = "Last Status : " + lastRunStatus
    new AlertDialog.Builder(EventConsoleActivity.this)
    .setTitle("Status")
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


  def getExpandedDevice : List[String] = {
    var expandedDevice:List[String] = List()
    if(adapter != null){
      for(i <- 0 to adapter.getGroupCount - 1){
        if(listView.isGroupExpanded(i))
          expandedDevice ::= adapter.getGroup(i).getName
      }
    }
    return expandedDevice
  }

  def restoreExpandedState(l: List[String]) = {
    if(l != List()){
      if(adapter != null){
        for(i <- 0 to adapter.getGroupCount -1){
          try {
            val id = adapter.getGroup(i).getName
            if(l.filter(_==id) != List())
              listView.expandGroup(i)
          }catch {
            case _ => {}
          }
        }
      }
    }
  }

  private class SendEventRequest extends MyAsyncTask {
    var exMessage = ""
    var result    = false
    var dialog:ProgressDialog = _
    override protected def doInBackground1(action: Array[String]): String = {
      val url  = ZenroidSettings.getZenossURL(EventConsoleActivity.this)
      val user = ZenroidSettings.getZenossUser(EventConsoleActivity.this)
      val pass = ZenroidSettings.getZenossPass(EventConsoleActivity.this)
      val invalidSSL = ZenroidSettings.getAcceptInvalidHTTPS(EventConsoleActivity.this)

      try {
        val zen = new ZenossAPI(url, user, pass, invalidSSL)
        zen.auth
        if(action(0) == "Acknowledge") {
          if(action(2) == store.EventState.NEW) {
            result = zen.eventsAcknowledge(action(1))
            selectedEvent.setEventState(store.EventState.ACKNOWLEDGED)
          }else {
            result = zen.eventsUnacknowledge(action(1))
            selectedEvent.setEventState(store.EventState.NEW)
          }
          dlgShowDetails.dismiss
        }else if (action(0) =="Close") {
          result = zen.eventsClose(action(1))
          ServiceRunner.EventStore.removeEvent(selectedDevice, selectedEvent)
          dlgShowDetails.dismiss
        }
      } catch {
        case e =>
        exMessage = e.getMessage
        //TODO:e = ServiceRunner.lastThrowableError
      }
      return "checked";
    }

    override protected def onPreExecute2 () = {
      dialog = new ProgressDialog(EventConsoleActivity.this)
      dialog.setMessage("sending request ...")
      dialog.show()
    }

    override protected def onPostExecute2(res: String) =  {
      if (result == false){
        if(exMessage == "")
            exMessage = "UnknowError"
        new AlertDialog.Builder(EventConsoleActivity.this)
        .setTitle("Error")
        .setMessage(exMessage)
        .setNegativeButton("close", new DialogInterface.OnClickListener() {
            def onClick(dialog: DialogInterface, which:Int) ={
              dialog.cancel
            }
          })
        .show()
      }
      dialog.dismiss()
    }
  }
}
