package com.github.slashmili.Zendroid

import _root_.android.content.Context
import _root_.android.appwidget.AppWidgetProvider;
import _root_.android.appwidget.AppWidgetManager;
import _root_.android.widget.RemoteViews;
import _root_.android.util.Log
import _root_.org.json.JSONObject

import android.app.Service;
import android.content.Intent;
import android.content.ComponentName;
import android.appwidget.AppWidgetManager;
import android.os.IBinder;


import utils._
import ZenossEvents._

import Storage.UserData
import Services._

class SmallWidget extends AppWidgetProvider {
  override def onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: Array[Int]) = {
    appWidgetIds.foreach ( UpdateServiceStore.appendWidgetId(_) )
    context.startService(new Intent(context, classOf[UpdateService]))
    Log.d("**************", appWidgetIds.toString)

  //  appWidgetManager.updateAppWidget(appWidgetIds, updateWidget(context));
  }

  override def onDeleted(context: Context, appWidgetIds: Array[Int]) = {
    appWidgetIds.foreach (  UpdateServiceStore.removeWidgetId(_) )
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

    return Some(Map("severity5" -> "5", "severity4" -> "5", "severity3" -> "5"))
    try 
    {
      val dh = new UserData(context)
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


