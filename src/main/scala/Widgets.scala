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

import android.content.Intent
import com.github.slashmili.Zendroid.Services.ZenossUpdateService


import Storage.UserData
import Services._

class SmallWidget extends AppWidgetProvider {
  override def onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: Array[Int]) = {
    //appWidgetIds.foreach ( UpdateServiceStore.appendWidgetId(_) )
    //context.startService(new Intent(context, classOf[UpdateService]))
    
    appWidgetIds.foreach ( ServiceRunner.WdigetStore.appendWidgetId(_))
    
    if(ServiceRunner.started == false) {
        ServiceRunner.startService(context);
       //context.startService(new Intent(context, classOf[ZenossUpdateService]))
       Log.d("Widget-Zendroid","Service is not running-----------------")
    }else {
      Log.d("Widget-Zendroid","Service is running     +++++++++++++++++")
      
      val remoteView = ServiceRunner.WdigetStore.updateWidget(context)
      appWidgetIds.foreach (   appWidgetManager.updateAppWidget(_, remoteView) )
    }

    Log.d("**************", appWidgetIds.toString)

  //  appWidgetManager.updateAppWidget(appWidgetIds, updateWidget(context));
  }

  override def onReceive(context: Context, intent: Intent) ={
    if (intent.getAction() == "APPWIDGET_CONFIGURE"){
      Log.d("OPEEEEN ME ENOOOW", "COM ONNNNN")
    }
    super.onReceive(context, intent)
  }
  override def onDeleted(context: Context, appWidgetIds: Array[Int]) = {
    //appWidgetIds.foreach (  UpdateServiceStore.removeWidgetId(_) )
    appWidgetIds.foreach ( ServiceRunner.WdigetStore.removeWidgetId(_))
  }
}
