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





import _root_.android.widget.RemoteViews;
import _root_.android.util.Log
import _root_.org.json.JSONObject

import android.content.ComponentName;
import com.github.slashmili.Zendroid.utils._
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
      Log.d(TAG, "in actoooooooooooooooooooooooooooooooooor" )
        var appWidgetManager = AppWidgetManager.getInstance(this);
        loop {
          receive {
            case i: Int =>  {
              Log.d(TAG, "I know this event")
              val info = appWidgetManager.getAppWidgetInfo(i)
              val updateViews = updateWidget(this)             
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
              Log.d(TAG, "onStart>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"  + UpdateServiceStore.getWidgetsIds.toString )
         // }
          super.onStart(intent, startId)
          this ! "stop"

          val alarmManager = getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]

          var time = new Time();
          time.set(System.currentTimeMillis() + 180000)
          val nextUpdate = time.toMillis(false)

          val updateIntent = new Intent(ACTION_UPDATE_ALL)
          updateIntent.setClass(this, classOf[UpdateService])

          val pendingIntent = PendingIntent.getService(this, 0, updateIntent, 0);
          alarmManager.set(AlarmManager.RTC, nextUpdate, pendingIntent) 


        }
    }

    override def onCreate = {
      Log.d(TAG, "onCreate>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" )
      super.onCreate()
       stopSelf();
    }

    override def onDestroy = {
      Log.d(TAG, "Destroy<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
      super.onDestroy
    }

    override def onBind(intent: Intent) : IBinder = {
      Log.d(TAG, "onBind========================================");
      return null;
    }


  def updateWidget(context: Context): RemoteViews = {
   
    val events = getLastEvent(context)

    val remoteView = new RemoteViews(context.getPackageName(),R.layout.small_widget )

    if(events != None) {

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
    remoteView 
  }


  def getLastEvent(context: Context) : Option[Map[String, String]] = 
  { 

    try 
    {
      //val dh = new UserData(context)
      //val saved_date = dh.selectAll 
      //if(saved_date != List()){
      if( 1 == 1){
          Log.w("Widgets", "Login to Zenoss")
          //val zen = new ZenossAPI(saved_date(0)("url"), saved_date(0)("username"), saved_date(0)("password"))
          val zen = new ZenossAPI("https://monitoring.com", "milad", "")
          Log.w("Widgets", "**************************************Logininig to Zenoss")
          if(zen.auth == false) 
              return None
        Log.w("Widgets", "============*******************before request to Zenoss")
        val jOpt = zen.eventsQuery
        Log.w("Widgets", "============*******************after request to Zenoss")
        if (jOpt == None)
          return None
        val jEvent = jOpt.get
        var severity5 = 0
        var severity4 = 0
        var severity3 = 0
        
        if(jEvent.has("result") && jEvent.getJSONObject("result").has("events")){
          val events = jEvent.getJSONObject("result").getJSONArray("events")
          for(i <- 0 to  events.length -1 ){
            val JO = new JSONObject( events.get(i).toString) 
            if(JO.has("severity")){
              JO.getString("severity") match {
                case "5" => severity5 += 1
                case "4" => severity4 += 1
                case "3" => severity3 += 1
              }
            }
          }
            return Some(Map("severity5" -> severity5.toString, "severity4" -> severity4.toString, "severity3" -> severity3.toString))
          }
        
      }
    } catch { 
        case e => e.printStackTrace()
    }

          return None
    }

}
