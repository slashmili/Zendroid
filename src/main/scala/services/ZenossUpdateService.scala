package com.github.slashmili.Zendroid.services

import _root_.android.content.{Intent, Context}
import _root_.android.app.{Notification, NotificationManager, PendingIntent, AlarmManager, IntentService}
import _root_.android.util.Log
import _root_.android.text.format.Time
import _root_.android.appwidget.AppWidgetManager
import _root_.org.json.JSONObject
import _root_.android.net.ConnectivityManager


import store._
import com.github.slashmili.Zendroid._
import utils.ZenossEvents._
import utils.ZenossAPI
import activities.EventConsoleActivity
import settings.ZenroidSettings



class ZenossUpdateService extends IntentService ("ZenossUpdateService") {
  val ACTION_UPDATE_ALL = "com.github.slashmili.Zendroid.UPDATE_ALL"

  override def onHandleIntent(intent: Intent) {
    var appWidgetManager = AppWidgetManager.getInstance(this);
    try {
      ServiceRunner.errorMessage = ""
      val runOnce = if(intent.getStringExtra("Zendroid.Service.RunOnce") == "true")
          true
        else
          false

      //check if it should be worked only over wifi
      val conMan = getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
      val mWifi  = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
      if(runOnce == false
        && ZenroidSettings.getSyncoverWIFI(this) == true
        && mWifi.isConnected == false){
           Log.d("Zenroid.Service", "WI-FI connectivity is " + mWifi.isConnected().toString)
           throw new Exception("Zenroid is only available over WI-FI")
      }
      requestLastEvent(this, runOnce)
      val broadcast = new Intent
      broadcast.setAction("com.github.slashmili.Zendroid.REFRESHACTIVITY")
      sendBroadcast(broadcast)
    }catch {
      case e => {
        ServiceRunner.errorMessage = e.toString
        ServiceRunner.lastThrowableError = e
      }
    }

    //update other widgets
    ServiceRunner.WdigetStore.getWidgetsIds.foreach {
      val info = appWidgetManager.getAppWidgetInfo(_)
        val updateViews = ServiceRunner.WdigetStore.updateWidget(this)
        appWidgetManager.updateAppWidget(_, updateViews)
    }
  }

  def requestLastEvent(context: Context, runOnce: Boolean = false) : Option[Map[String, String]] = {
    if(ZenroidSettings.isEmpty(this) == true){
      ServiceRunner.criticalEvent = 0
      ServiceRunner.errorEvent = 0
      ServiceRunner.warninigEvent = 0
      return Some(Map("severity5" -> "0", "severity4" -> "0", "severity3" -> "0"))
    }
    if(ZenroidSettings.getPerformSyncing(this) == false && runOnce == false){
      ServiceRunner.errorMessage = "Zenroid is disabled"
      return None
    }

    val updateIntent = new Intent(ACTION_UPDATE_ALL)
    updateIntent.setClass(this, classOf[ZenossUpdateService])
    val pendingIntent = PendingIntent.getService(this, 0, updateIntent, 0);
    if(runOnce == false){
      //remove previous alarm
      if(ServiceRunner.alarmManager != Nil){
      try {
          ServiceRunner.alarmManager.cancel(pendingIntent)
        }catch {
          case e => Log.d("Service", "Tried to cancel alaram but faild, " + e.toString)
        }
      }
    }
    try {
      val url  = ZenroidSettings.getZenossURL(this)
      val user = ZenroidSettings.getZenossUser(this)
      val pass = ZenroidSettings.getZenossPass(this)

      val invalidSSL = ZenroidSettings.getAcceptInvalidHTTPS(this)

      Log.w("Widgets", "Login to Zenoss")
      val zen = new ZenossAPI(url, user, pass, invalidSSL)
      if(zen.auth == false)
        throw new Exception("Wrong username or password")
      val jOpt = zen.eventsQuery(ZenroidSettings.getStates(this))
      if (jOpt == None)
        return None
      val jEvent = jOpt.get
      var severity5 = 0
      var severity4 = 0
      var severity3 = 0
      val match_d = ZenroidSettings.getMatchDevice(this)
      if(jEvent.has("result") && jEvent.getJSONObject("result").has("events")){
        val events = jEvent.getJSONObject("result").getJSONArray("events")
        ServiceRunner.EventStore.cleanDevices
        val oldEventIds = ServiceRunner.EventStore.eventIDs
        var fetchedEventIds:List[String] = List()
        for(i <- 0 to  events.length -1 ){
          val JO = new JSONObject( events.get(i).toString)
          //val JO = events.get(i).asInstanceOf[JSONObject]
            //TODO make a method for this shits

          val uid        = if(JO.has("device") && JO.getJSONObject("device").has("uid"))
              JO.getJSONObject("device").getString("uid")
            else
              ""
          val hostname   = if(JO.has("device") && JO.getJSONObject("device").has("text"))
              JO.getJSONObject("device").getString("text").trim
            else
              ""
          val errorText  = if(JO.has("summary") == true)
              JO.getString("summary")
            else
              ""
          val eventId    = JO.getString("evid")
          val countError = JO.getString("count").toInt
          val severity   = JO.getString("severity").toInt
          val eventState = JO.getString("eventState")
          val firstTime  = JO.getString("firstTime")
          val lastTime   = JO.getString("lastTime")
          val component  = if(JO.has("component"))
              JO.getJSONObject("component").getString("text")
            else
              ""
          fetchedEventIds ::= eventId
          if(JO.has("device") && JO.getJSONObject("device").has("text")){
            if(match_d == "") {
              if(JO.has("severity")){
                JO.getString("severity") match {
                  case "5" => severity5 += 1 ; showNotification(eventId, hostname,errorText, 5);
                  case "4" => severity4 += 1 ; showNotification(eventId, hostname,errorText, 4);
                  case "3" => severity3 += 1 ; showNotification(eventId, hostname,errorText, 3);
                }
              }
              ServiceRunner.EventStore.appendEvent(uid, hostname, eventId, errorText, severity, countError, eventState, firstTime, lastTime, component)
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
                      ServiceRunner.EventStore.appendEvent(uid, hostname, eventId, errorText, severity, countError, eventState, firstTime, lastTime, component)
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
        Log.d("ZenossUpdateService.getLastEvent", "severity5: " + severity5.toString + " | severity4: " + severity4.toString + " | severity3: " + severity3.toString)
        ServiceRunner.lastTime.set(System.currentTimeMillis())
        clearOldNotification(fetchedEventIds, oldEventIds)
        return Some(Map("severity5" -> severity5.toString, "severity4" -> severity4.toString, "severity3" -> severity3.toString))
      }

    }catch {
      case e =>  throw e;
    }finally {
      if(runOnce == false){
        //set new alarm
        ServiceRunner.alarmManager = getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
        ServiceRunner.nextTime = new Time()
        val updateEvery = ZenroidSettings.getSyncingInterval(this)
        ServiceRunner.nextTime.set(System.currentTimeMillis() + updateEvery)
        val nextUpdate = ServiceRunner.nextTime.toMillis(false)
        ServiceRunner.alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent)
        ServiceRunner.started = true
        Log.d("ZenossUpdateService.getLastEvent", " will be called Later (" + ServiceRunner.nextTime.format("%R") + ")")
      }
    }
    return None
  }

  def clearOldNotification(newEventIds: List[String], oldEventIds : List[String]) = {
    oldEventIds.diff(newEventIds).foreach(
      getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager].cancel(_, 0)
    )
  }
  def showNotification (eventId: String,hostname: String, summary: String, severity: Int ) = {
    if (ZenroidSettings.isEmpty(this) == false){
      var notificationState = severity match {
        case 5 => ZenroidSettings.getOnCritical(this)
        case 4 => ZenroidSettings.getOnError(this)
        case 3 => ZenroidSettings.getOnWarning(this)
        case _ => 0
      }
      if (notificationState != 0){
        if (!ServiceRunner.EventStore.contains(eventId)){
          var now  = new Time()
          now.set(System.currentTimeMillis())
          val ns   = Context.NOTIFICATION_SERVICE;
          val nm   = getSystemService(ns).asInstanceOf[NotificationManager]
          val host = "Zenroid: " + hostname
          val sum  = "(@" + now.format("%R")   + ") " + summary.trim
          var icon = severity match {
            case 3 => R.drawable.severity3_notify
            case 4 => R.drawable.severity4_notify
            case 5 => R.drawable.severity5_notify
            case _ => 0
          }

          val notification = new Notification(icon, sum, System.currentTimeMillis());
          val contentIntent = PendingIntent.getActivity(this, 0,
          new Intent(this, classOf[EventConsoleActivity]), 0)
          notification.setLatestEventInfo(this, host, sum, contentIntent);
          //if set to make sound
          if(notificationState == 2 || notificationState == 3)
            notification.defaults |= Notification.DEFAULT_SOUND
          if(notificationState == 3){
            notification.defaults |= Notification.DEFAULT_VIBRATE
            notification.defaults |= Notification.DEFAULT_LIGHTS
          }
          nm.notify(eventId, 0, notification);
          ServiceRunner.EventStore.append(eventId)
        }
      }
    }
  }
}
