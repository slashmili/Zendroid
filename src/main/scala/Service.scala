package com.github.slashmili.Zendroid.Services

import _root_.android.content.{Intent, Context}
import _root_.android.app.{Notification, NotificationManager, PendingIntent, AlarmManager, IntentService}
import _root_.android.util.Log
import _root_.android.text.format.Time
import _root_.android.appwidget.AppWidgetManager
import _root_.android.widget.RemoteViews

import _root_.org.json.JSONObject
import _root_.org.apache.http.conn.HttpHostConnectException
import _root_.org.apache.http.conn.ConnectTimeoutException
import _root_.javax.net.ssl.SSLException
import _root_.java.net.{UnknownHostException, SocketException}
import _root_.java.io.IOException

import com.github.slashmili.Zendroid.utils._
import ZenossEvents._
import com.github.slashmili.Zendroid.{R, GlobalConfiguration, ZendroidPreferences}

object ServiceRunner { 
  var alarmManager:AlarmManager = _
  var service:ZenossUpdateService = _
  var criticalEvent = 0
  var errorEvent    = 0 
  var warninigEvent = 0
  var errorMessage = ""
  var started      = false
  var nextTime = new Time()
  val TAG = "Zendroid.Services.ServiceRunner"

  def startService (context: Context) = {
    started = true
    context.startService(new Intent(context, classOf[ZenossUpdateService]))
  }

  object WdigetStore {
    var widgetsId:List[Int] = List()
      def appendWidgetId(wid: Int) = {
      if ( widgetsId.filter(_ == wid).length == 0)
        widgetsId ::= wid
      Log.d("UpdateService", "widget with id " + wid.toString + " is added")
    }

    def removeWidgetId(wid: Int) = {
      widgetsId = widgetsId.filter(_ != wid)
      Log.d("UpdateService", "widget with id " + wid.toString + " is removed")
    }

    def getWidgetsIds = widgetsId

    def isActive: Boolean = {
      if (widgetsId.length == 0)
        return false
      return true
    }

    def updateWidget(context: Context): RemoteViews = {
      def getLastEvent() : Option[Map[String, String]] = {
        return Some(Map("severity5" -> ServiceRunner.criticalEvent.toString, "severity4" -> ServiceRunner.errorEvent.toString, "severity3" -> ServiceRunner.warninigEvent.toString))
      }
      val zp = ZendroidPreferences.loadPref(context)
      if (zp == None){
        val errorRemoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget_error)
        errorRemoteView.setTextViewText(R.id.widgetError,"Loading ...")
        return errorRemoteView
      }
      var errorText = ""
      var events: Option[Map[String, String]] = None
      try {
        events = getLastEvent()
      }catch {
        case e :javax.net.ssl.SSLException => {
          errorText="Your domain doesn't have valid ssl"
        }
        case e:java.net.UnknownHostException => {
          errorText="Can't find host " + zp.get("url")
        }
        case e:org.apache.http.conn.HttpHostConnectException => {
          errorText="Conenction to " + zp.get("url") + " refused"
        }
        case e: org.apache.http.conn.ConnectTimeoutException => {
          Log.d(TAG, "It seems internet connection goes down, or server doesn't response")
        }
        case e: java.net.NoRouteToHostException => {
          Log.d(TAG, "It seems internet connection goes down")
        }
        case e: java.io.IOException => {
          Log.d(TAG, "IOException ! try again later")
        }
        case e: java.net.SocketException => {
          Log.d(TAG, "IOException ! try again later")
        }
        case e => {
          errorText="Unkown Error " + e.toString
        }
      }
      val remoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget )

      val intentGlobalConfiguration = new Intent(context, classOf[GlobalConfiguration])
      val pendingIntentGlobalConfiguration = PendingIntent.getActivity(context, 0, intentGlobalConfiguration, 0)
      if(events != None)
      {
        if(events.get("severity5") == "0"){
          remoteView.setInt(R.id.severity5Img, "setAlpha", 100);
          remoteView.setInt(R.id.severity5Box, "setBackgroundResource", R.drawable.severity5_background_noevent);
        }else {
          remoteView.setInt(R.id.severity5Img, "setAlpha", 255);
          remoteView.setInt(R.id.severity5Box, "setBackgroundResource", R.drawable.severity5_background);
        }
        remoteView.setTextViewText(R.id.severity5, events.get("severity5"))
        remoteView.setOnClickPendingIntent(R.id.severity5, pendingIntentGlobalConfiguration)

        if(events.get("severity4") == "0"){
            remoteView.setInt(R.id.severity4Img, "setAlpha", 100);
            remoteView.setInt(R.id.severity4Box, "setBackgroundResource", R.drawable.severity4_background_noevent);
        }else {
            remoteView.setInt(R.id.severity4Img, "setAlpha", 255);
            remoteView.setInt(R.id.severity4Box, "setBackgroundResource", R.drawable.severity4_background);
        }
        remoteView.setTextViewText(R.id.severity4, events.get("severity4"))
        remoteView.setOnClickPendingIntent(R.id.severity4, pendingIntentGlobalConfiguration)

        if(events.get("severity3") == "0"){
          remoteView.setInt(R.id.severity3Img, "setAlpha", 100);
          remoteView.setInt(R.id.severity3Box, "setBackgroundResource", R.drawable.severity3_background_noevent);
        }else {
          remoteView.setInt(R.id.severity3Img, "setAlpha", 255);
          remoteView.setInt(R.id.severity3Box, "setBackgroundResource", R.drawable.severity3_background);
        }
        remoteView.setTextViewText(R.id.severity3, events.get("severity3"))
        remoteView.setOnClickPendingIntent(R.id.severity3, pendingIntentGlobalConfiguration)
      }

      errorText = ServiceRunner.errorMessage
      if(errorText != ""){
        remoteView.setTextViewText(R.id.widgetError, "I got an error for last run for more info go to zendroid and check 'Last Status'")
      }
      else {
        remoteView.setTextViewText(R.id.widgetError, "")
      }
      remoteView.setOnClickPendingIntent(R.id.widgetError, pendingIntentGlobalConfiguration)
      remoteView
    }
  }

  object EventStore {
    var eventIDs:List[String] = List()
      def append(evid: String) = {
        eventIDs ::=evid
    }

    def contains(evid: String): Boolean  = {
      if ( eventIDs.filter( _ == evid).length == 1)
        return true
      else
        return false
    }
  }
}


class ZenossUpdateService extends IntentService ("ZenossUpdateService") {
  val ACTION_UPDATE_ALL = "com.github.slashmili.Zendroid.UPDATE_ALL"

  override def onHandleIntent(intent: Intent) {
    var appWidgetManager = AppWidgetManager.getInstance(this);
    try {
      ServiceRunner.errorMessage = ""
      requestLastEvent(this)
    }catch {
      case e => ServiceRunner.errorMessage = e.toString
    }

    //update other widgets
    ServiceRunner.WdigetStore.getWidgetsIds.foreach {
      val info = appWidgetManager.getAppWidgetInfo(_)
        val updateViews = ServiceRunner.WdigetStore.updateWidget(this)
        appWidgetManager.updateAppWidget(_, updateViews)
    }
  }
  
  def getLastEvent() : Option[Map[String, String]] = {
    return Some(Map("severity5" -> ServiceRunner.criticalEvent.toString, "severity4" -> ServiceRunner.errorEvent.toString, "severity3" -> ServiceRunner.warninigEvent.toString))
  }

  def requestLastEvent(context: Context) : Option[Map[String, String]] = {
    val zp = ZendroidPreferences.loadPref(context)
    if(zp == None){
      ServiceRunner.criticalEvent = 0
      ServiceRunner.errorEvent = 0
      ServiceRunner.warninigEvent = 0    
      return Some(Map("severity5" -> "0", "severity4" -> "0", "severity3" -> "0"))
    }
    if(zp.get("update").toString.toInt == 0){
      ServiceRunner.errorMessage = "Service Disabled"
      return None
    }

    //remove previous alarm
    val updateIntent = new Intent(ACTION_UPDATE_ALL)
    updateIntent.setClass(this, classOf[ZenossUpdateService])
    val pendingIntent = PendingIntent.getService(this, 0, updateIntent, 0);
    if(ServiceRunner.alarmManager != Nil){
      try {
        ServiceRunner.alarmManager.cancel(pendingIntent)
      }catch {
        case e => Log.d("Service", "Tried to cancel alaram but faild, " + e.toString)
      }
    }
    try {
      val url  = zp.get("url").toString
      val user = zp.get("user").toString
      val pass = zp.get("pass").toString

      Log.w("Widgets", "Login to Zenoss")
      val zen = new ZenossAPI(url, user, pass)
      if(zen.auth == false)
        return None
      val jOpt = zen.eventsQuery
      if (jOpt == None)
        return None
      val jEvent = jOpt.get
      var severity5 = 0
      var severity4 = 0
      var severity3 = 0
      val match_d = zp.get("match").toString
      if(jEvent.has("result") && jEvent.getJSONObject("result").has("events")){
        val events = jEvent.getJSONObject("result").getJSONArray("events")
        for(i <- 0 to  events.length -1 ){
          val JO = new JSONObject( events.get(i).toString)
            //TODO make a method for this shits
          if(JO.has("device") && JO.getJSONObject("device").has("text")){
            val hostname   = JO.getJSONObject("device").getString("text").trim
            val errorText  = JO.getString("summary")
            val eventId    = JO.getString("evid")
            if(match_d == "") {
              if(JO.has("severity")){
                JO.getString("severity") match {
                  case "5" => severity5 += 1 ; showNotification(eventId, hostname,errorText, 5);
                  case "4" => severity4 += 1 ; showNotification(eventId, hostname,errorText, 4);
                  case "3" => severity3 += 1 ; showNotification(eventId, hostname,errorText, 3);
                }
              }
            }else {
              match_d.split(",").foreach {
                case str => {
                  if ("""%s""".format(str.trim).r.findAllIn(hostname).toSeq.length != 0){
                    if(JO.has("severity")){
                      JO.getString("severity") match {
                        case "5" => severity5 += 1; showNotification(eventId, hostname,errorText, 5);
                        case "4" => severity4 += 1; showNotification(eventId, hostname,errorText, 4);
                        case "3" => severity3 += 1; showNotification(eventId, hostname,errorText, 3);
                      }
                    }
                  }
                }
              }
            }
          }
        }
        ServiceRunner.criticalEvent = severity5
        ServiceRunner.errorEvent = severity4
        ServiceRunner.warninigEvent = severity3
        return Some(Map("severity5" -> severity5.toString, "severity4" -> severity4.toString, "severity3" -> severity3.toString))
      }

    }catch {
      case e =>  throw e;
    }finally {
      //set new alarm
      ServiceRunner.alarmManager = getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
      ServiceRunner.nextTime = new Time();
      val updateEvery = zp.get("update").toString.toInt
      ServiceRunner.nextTime.set(System.currentTimeMillis() + updateEvery)
      val nextUpdate = ServiceRunner.nextTime.toMillis(false)
      ServiceRunner.alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent)
      Log.d("UpdateService", " will be called Later (" + ServiceRunner.nextTime.format("%R") + ")")
    }
    return None
  }

  def showNotification (eventId: String,hostname: String, summary: String, severity: Int ) = {
    val zp = ZendroidPreferences.loadPref(this)
    if ( zp != None){
      var notificationState = severity match {
        case 5 => zp.get("on_critical").toString.toInt
        case 4 => zp.get("on_error").toString.toInt
        case 3 => zp.get("on_warning").toString.toInt
        case _ => 0
      }
      if (notificationState != 0){
        if (!ServiceRunner.EventStore.contains(eventId)){
          val ns = Context.NOTIFICATION_SERVICE;
          val nm = getSystemService(ns).asInstanceOf[NotificationManager]
          val host = "Zendroid: " + hostname
          val sum  = hostname + ": " + summary
          var icon = 0
          if (severity == 3)
            icon  = R.drawable.severity3_notify
          if (severity  == 4)
            icon   = R.drawable.severity4_notify
          if (severity == 5)
            icon   = R.drawable.severity5_notify

          val notification = new Notification(icon, sum, System.currentTimeMillis());
          val contentIntent = PendingIntent.getActivity(this, 0,
          new Intent(this, classOf[ZenossUpdateService]), 0)
          notification.setLatestEventInfo(this, host, sum, contentIntent);
          //if set to make sound
          if(notificationState == 2)
            notification.defaults |= Notification.DEFAULT_SOUND
          nm.notify(eventId, severity, notification);
          ServiceRunner.EventStore.append(eventId)
        }
      }
    }
  }
}
