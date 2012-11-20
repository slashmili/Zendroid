package com.github.slashmili.Zendroid.services

import _root_.android.text.format.Time
import _root_.android.content.{Intent, Context}
import _root_.android.app.{PendingIntent, AlarmManager}
import _root_.android.util.Log
import _root_.android.widget.RemoteViews
import _root_.android.os.Build


import com.github.slashmili.Zendroid._
import services.store._
import utils.ZenossEvents._
import activities.EventConsoleActivity
import settings.ZenroidSettings

object ServiceRunner {
  var alarmManager:AlarmManager = _
  var service:ZenossUpdateService = _
  var lastThrowableError:java.lang.Throwable = _
  var criticalEvent = 0
  var errorEvent    = 0
  var warninigEvent = 0
  var errorMessage = ""
  var started      = false
  var nextTime = new Time()
  val lastTime = new Time()
  val TAG = "Zendroid.services.ServiceRunner"

  def startService (context: Context, runOnce:Boolean = false) = {
    if (runOnce == false)
      started = true
    val s = new Intent(context, classOf[ZenossUpdateService])
    s.putExtra("Zendroid.Service.RunOnce", runOnce.toString)
    context.startService(s)
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

    def getLastEvent() : Option[Map[String, String]] = {
      return Some(Map("severity5" -> ServiceRunner.criticalEvent.toString, "severity4" -> ServiceRunner.errorEvent.toString, "severity3" -> ServiceRunner.warninigEvent.toString))
    }

    def updateWidget(context: Context): RemoteViews = {
      try {
        val intentGlobalConfiguration = new Intent(context, classOf[EventConsoleActivity])
        val pendingIntentGlobalConfiguration = PendingIntent.getActivity(context, 0, intentGlobalConfiguration, 0)

        if (ZenroidSettings.isEmpty(context) == true){
          val errorRemoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget_error)
          errorRemoteView.setTextViewText(R.id.widgetError,"Click here to configure Zenoss connection")
          errorRemoteView.setOnClickPendingIntent(R.id.widgetError, pendingIntentGlobalConfiguration)

          return errorRemoteView
        }
        var errorText = ""
        var events: Option[Map[String, String]] = None
        events = getLastEvent()
        def getRoundBackground(mainBackground: Int, patchedBackground: Int) = {
          //fore more info see http://code.google.com/p/android/issues/detail?id=9161
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mainBackground
          else
            patchedBackground
        }
        val remoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget )
        if(events != None)
        {
          if(events.get("severity5") == "0"){
            remoteView.setInt(R.id.severity5Img, "setAlpha", 100)
            remoteView.setInt(R.id.severity5Box, "setBackgroundResource", getRoundBackground(R.drawable.severity5_background_noevent, R.drawable.severity5_background_noevent_patch))
          }else {
            remoteView.setInt(R.id.severity5Img, "setAlpha", 255)
            remoteView.setInt(R.id.severity5Box, "setBackgroundResource", getRoundBackground(R.drawable.severity5_background, R.drawable.severity5_background_patch))
          }
          remoteView.setTextViewText(R.id.severity5, events.get("severity5"))
          remoteView.setOnClickPendingIntent(R.id.severity5, pendingIntentGlobalConfiguration)
          remoteView.setOnClickPendingIntent(R.id.severity5Box, pendingIntentGlobalConfiguration)

          if(events.get("severity4") == "0"){
            remoteView.setInt(R.id.severity4Img, "setAlpha", 100)
            remoteView.setInt(R.id.severity4Box, "setBackgroundResource", R.drawable.severity4_background_noevent)
          }else {
            remoteView.setInt(R.id.severity4Img, "setAlpha", 255)
            remoteView.setInt(R.id.severity4Box, "setBackgroundResource", R.drawable.severity4_background)
          }
          remoteView.setTextViewText(R.id.severity4, events.get("severity4"))
          remoteView.setOnClickPendingIntent(R.id.severity4, pendingIntentGlobalConfiguration)
          remoteView.setOnClickPendingIntent(R.id.severity4Box, pendingIntentGlobalConfiguration)

          if(events.get("severity3") == "0"){
            remoteView.setInt(R.id.severity3Img, "setAlpha", 100)
            remoteView.setInt(R.id.severity3Box, "setBackgroundResource", getRoundBackground(R.drawable.severity3_background_noevent, R.drawable.severity3_background_noevent_patch))
          }else {
            remoteView.setInt(R.id.severity3Img, "setAlpha", 255)
            remoteView.setInt(R.id.severity3Box, "setBackgroundResource", getRoundBackground(R.drawable.severity3_background, R.drawable.severity3_background_patch))
          }
          remoteView.setTextViewText(R.id.severity3, events.get("severity3"))
          remoteView.setOnClickPendingIntent(R.id.severity3, pendingIntentGlobalConfiguration)
          remoteView.setOnClickPendingIntent(R.id.severity3Box, pendingIntentGlobalConfiguration)
        }

        Log.d("ServiceRunner.updateWidget", "severity5: " + events.get("severity5") + " | severity4: " + events.get("severity4") +  " | severity3: " + events.get("severity3"))
        Log.d("ServiceRunner.updateWidget", "Error : " + ServiceRunner.errorMessage)

        errorText = ServiceRunner.errorMessage
        if(errorText != ""){

          val msg = if(ZenroidSettings.getPerformSyncing(context) == false){
            "    Zenroid is disabled    "
          }else {
            var now  = new Time()
            now.set(System.currentTimeMillis())
            "   Couldn't fetch data at " + now.format("%R") + "   "
          }
          remoteView.setTextViewText(R.id.widgetError, msg)
        }
        else {
          remoteView.setTextViewText(R.id.widgetError, "")
        }
        remoteView.setOnClickPendingIntent(R.id.widgetError, pendingIntentGlobalConfiguration)
        remoteView
      } catch {
        //save error for debug
        case e => {
          ServiceRunner.lastThrowableError = e
          throw e
        }
      }
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



    var devices: Map[String, ZenossDevice] = Map()
    def getDevices = devices
    def cleanDevices = { devices = Map() }
    def appendEvent(uid: String, name: String, evid: String, summary: String, severity: Int, count: Int, eventState: String, firstTime: String, lastTime: String, component: String) = {
      val zd: ZenossDevice= devices.get(uid).orElse(Some(new ZenossDevice(uid, name))).get
      zd.addEvent(evid, summary, severity, count, eventState, firstTime, lastTime, component)
      devices += (uid -> zd)
    }

    def countCritical              = devices.foldLeft(0)(_ + _._2.countCritical)
    def countCritical(uid: String) = devices.get(uid).get.countCritical
    def countError                 = devices.foldLeft(0)(_ + _._2.countError)
    def countError(uid: String)    = devices.get(uid).get.countError
    def countWarning               = devices.foldLeft(0)(_ + _._2.countWarning)
    def countWarning(uid: String)  = devices.get(uid).get.countWarning
    def removeEvent(from: ZenossDevice, event: Event) = {
      if(from.getEvents.length == 1){
        devices = devices.filter(x => from.getName != x._2.getName)
      }else {
        from.removeEvent(event.getEvID)
      }
    }
  }
}



// vim: set ts=2 sw=2 et:
