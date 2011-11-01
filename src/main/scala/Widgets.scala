package com.github.slashmili.Zendroid

import _root_.android.content.{Context, Intent}
import _root_.android.appwidget.{AppWidgetProvider, AppWidgetManager}
import _root_.android.widget.RemoteViews
import _root_.android.util.Log
import _root_.org.json.JSONObject

import com.github.slashmili.Zendroid.Services.ServiceRunner

class SmallWidget extends AppWidgetProvider {
  override def onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: Array[Int]) = {
    appWidgetIds.foreach ( ServiceRunner.WdigetStore.appendWidgetId(_))

    if(ServiceRunner.started == false) {
      ServiceRunner.startService(context)
      Log.d("Widget-Zendroid","Service is not running -----------------")
    }else {
      Log.d("Widget-Zendroid","Service is running     +++++++++++++++++")

      val remoteView = ServiceRunner.WdigetStore.updateWidget(context)
      appWidgetIds.foreach (appWidgetManager.updateAppWidget(_, remoteView))
    }
  }

  override def onReceive(context: Context, intent: Intent) ={
    super.onReceive(context, intent)
  }

  override def onDeleted(context: Context, appWidgetIds: Array[Int]) = {
    appWidgetIds.foreach ( ServiceRunner.WdigetStore.removeWidgetId(_))
  }
}
