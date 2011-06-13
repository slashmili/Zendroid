package com.github.slashmili.Zendroid.Services

import android.app.Notification; 
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.app.Service;
import android.util.Log
import android.content.Intent;
import android.os.IBinder;
import android.content.Context
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.text.format.Time;
import scala.actors.Actor
import android.appwidget.AppWidgetManager;
import android.net.Uri
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.conn.ConnectTimeoutException
import javax.net.ssl.SSLException
import java.net.UnknownHostException
import java.io.IOException
import java.net.SocketException

import _root_.android.widget.RemoteViews;
import _root_.android.util.Log
import _root_.org.json.JSONObject

import android.content.ComponentName;
import com.github.slashmili.Zendroid.utils._
import com.github.slashmili.Zendroid.{ZenPreferences, AppWidgetConfigure}
import ZenossEvents._


import com.github.slashmili.Zendroid.R
import com.github.slashmili.Zendroid.MainActivity

object UpdateServiceStore {
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
}
class UpdateService extends Service   with  Actor {
    private val TAG = "Services.UpdateService" 
    val ACTION_UPDATE_ALL = "com.github.slashmili.Zendroid.UPDATE_ALL"
    def act() {
        var appWidgetManager = AppWidgetManager.getInstance(this);
        loop {
          receive {
            case i: Int =>  {
              val info = appWidgetManager.getAppWidgetInfo(i)
              val updateViews = updateWidget(this,i)
              appWidgetManager.updateAppWidget(i, updateViews);

            }
            case s: String => exit()
            case _ => Log.d(TAG, "I don't know this event")
          }
        }
    }
    override def onStart(intent: Intent, startId: Int) = {
        if(UpdateServiceStore.isActive == true) {
              this.start
            if(ACTION_UPDATE_ALL == intent.getAction()){
              Log.d(TAG, "updateing widgets : "  + UpdateServiceStore.getWidgetsIds.toString )
            }else {
            //TODO don't update this just
            }
              UpdateServiceStore.getWidgetsIds.foreach {
                this ! _
              }
         // }
          super.onStart(intent, startId)
          this ! "stop"


        }
    }

    override def onCreate = {
      super.onCreate()
       stopSelf();
    }

    override def onDestroy = {
      super.onDestroy
    }

    override def onBind(intent: Intent) : IBinder = {
      return null;
    }


  def updateWidget(context: Context, wId:Int): RemoteViews = {
    val zp = ZenPreferences.loadPref(context, wId)
    if ( zp == None){
      val errorRemoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget_error)
      errorRemoteView.setTextViewText(R.id.widgetError,"Waiting for initial loading")
      UpdateServiceStore.removeWidgetId(wId)
      return errorRemoteView
    }
    var events: Option[Map[String, String]] = None
    try {
      events = getLastEvent(context, wId)
    }catch {
      case e :javax.net.ssl.SSLException => {
        val errorRemoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget_error)
        errorRemoteView.setTextViewText(R.id.widgetError,"Your domain doesn't have valid ssl")
        UpdateServiceStore.removeWidgetId(wId)
        return errorRemoteView
      }
      case e:java.net.UnknownHostException => {
        val errorRemoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget_error)
        errorRemoteView.setTextViewText(R.id.widgetError,"Cann't find host " + zp.get.filter(_._1 =="url")(0)._2 + ", delete it and create new one!")
        UpdateServiceStore.removeWidgetId(wId)
        return errorRemoteView
      }
      case e:org.apache.http.conn.HttpHostConnectException => {
        val errorRemoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget_error)
        errorRemoteView.setTextViewText(R.id.widgetError,"Conenction to " + zp.get.filter(_._1 =="url")(0)._2 + " refused")
        UpdateServiceStore.removeWidgetId(wId)
        return errorRemoteView
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
      case _ => {
        val errorRemoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget_error)
        errorRemoteView.setTextViewText(R.id.widgetError,"Unknow error")
        UpdateServiceStore.removeWidgetId(wId)
        return errorRemoteView
      }
    }
    val remoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget )

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

      if(events.get("severity4") == "0"){
        remoteView.setInt(R.id.severity4Img, "setAlpha", 100);
        remoteView.setInt(R.id.severity4Box, "setBackgroundResource", R.drawable.severity4_background_noevent);
      }else {
        remoteView.setInt(R.id.severity4Img, "setAlpha", 255);
        remoteView.setInt(R.id.severity4Box, "setBackgroundResource", R.drawable.severity4_background);
      }
      remoteView.setTextViewText(R.id.severity4, events.get("severity4"))

      if(events.get("severity3") == "0"){
        remoteView.setInt(R.id.severity3Img, "setAlpha", 100);
        remoteView.setInt(R.id.severity3Box, "setBackgroundResource", R.drawable.severity3_background_noevent);
      }else {
        remoteView.setInt(R.id.severity3Img, "setAlpha", 255);
        remoteView.setInt(R.id.severity3Box, "setBackgroundResource", R.drawable.severity3_background);
      } 
      remoteView.setTextViewText(R.id.severity3, events.get("severity3"))
     
    }
    val alarmManager = getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]

    var time = new Time();
    val updateEvery = zp.get.filter(_._1 =="update")(0)._2.toInt
    time.set(System.currentTimeMillis() + updateEvery)
    val nextUpdate = time.toMillis(false)

    val updateIntent = new Intent(ACTION_UPDATE_ALL)
    updateIntent.setClass(this, classOf[UpdateService])

    val pendingIntent = PendingIntent.getService(this, 0, updateIntent, 0);
    alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent)



    remoteView 
  }


  def getLastEvent(context: Context, wId:Int) : Option[Map[String, String]] = 
  { 
    val zp = ZenPreferences.loadPref(context, wId)
    if ( zp == None)
    {
      UpdateServiceStore.removeWidgetId(wId)
      return Some(Map("severity5" -> "0", "severity4" -> "0", "severity3" -> "0"))
    }
    try 
    {
          val url  = zp.get.filter(_._1 =="url")(0)._2
          val user = zp.get.filter(_._1 =="user")(0)._2 
          val pass = zp.get.filter(_._1 =="pass")(0)._2 
           
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
        val match_d = zp.get.filter(_._1 =="match")(0)._2
        if(jEvent.has("result") && jEvent.getJSONObject("result").has("events")){
          val events = jEvent.getJSONObject("result").getJSONArray("events")
          for(i <- 0 to  events.length -1 ){
            val JO = new JSONObject( events.get(i).toString) 
            //TODO make a method for this shits
            if(JO.has("device") && JO.getJSONObject("device").has("text"))
            {
              var hostname = JO.getJSONObject("device").getString("text").trim
              if(match_d == "") {
                if(JO.has("severity")){
                  JO.getString("severity") match {
                    case "5" => severity5 += 1
                    case "4" => severity4 += 1
                    case "3" => severity3 += 1
                  }
                }
              }else {
                match_d.split(",").foreach {
                  case str => {
                    if ("""%s""".format(str.trim).r.findAllIn(hostname).toSeq.length != 0){
                      if(JO.has("severity")){
                        JO.getString("severity") match {
                          case "5" => severity5 += 1
                          case "4" => severity4 += 1
                          case "3" => severity3 += 1
                        }
                      }
                    }
                  }
                }
              }
            }
          }
            return Some(Map("severity5" -> severity5.toString, "severity4" -> severity4.toString, "severity3" -> severity3.toString))
          }
        
    } catch { 
        case e =>  throw e;
   }

          return None
    }

}
