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

  override def onReceive(context: Context, intent: Intent) ={
    if (intent.getAction() == "APPWIDGET_CONFIGURE"){
      Log.d("OPEEEEN ME ENOOOW", "COM ONNNNN")
    }
    super.onReceive(context, intent)
  }
  override def onDeleted(context: Context, appWidgetIds: Array[Int]) = {
    appWidgetIds.foreach (  UpdateServiceStore.removeWidgetId(_) )
  }
}
