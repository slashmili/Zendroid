package com.github.slashmili.Zendroid

import _root_.android.content.Context
import _root_.android.appwidget.AppWidgetProvider;
import _root_.android.appwidget.AppWidgetManager;
import _root_.android.widget.RemoteViews;
import _root_.android.util.Log

import utils.ZenossAPI
import Storage.UserData

class SmallWidget extends AppWidgetProvider {
  
  override def onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: Array[Int]) = {
   appWidgetManager.updateAppWidget(appWidgetIds, updateWidget(context));
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


  def getLastEvent(context: Context) : Option[Map[String, String]] = { 
  try {
    val dh = new UserData(context)
    val saved_date = dh.selectAll 
    if(saved_date != List()){
        val zen = new ZenossAPI(saved_date(0)("url"))
        zen.auth(saved_date(0)("username"), saved_date(0)("password"))
        if(zen.authCheck == false)
            return None
      val events = zen.getEvents
      if(events == None )
        return None

      return events 
    }
    else {
        return None
    }
  } catch { 
    case e => e.printStackTrace()
  }
  return None
  }
}



