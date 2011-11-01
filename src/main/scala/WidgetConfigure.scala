package com.github.slashmili.Zendroid

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import _root_.android.widget.{EditText, Button, Spinner, ArrayAdapter,AdapterView}

import _root_.android.app.ProgressDialog
import _root_.android.util.Log

import java.util.ArrayList;


import Services._
import com.github.slashmili.Zendroid.utils.ZenossAPI


import scala.actors.Actor

class AppWidgetConfigure extends Activity with  AdapterView.OnItemSelectedListener {

  val TAG = "Zenoss.AppWidgetConfigure"
  var mAppWidgetId =  AppWidgetManager.INVALID_APPWIDGET_ID
  var urlWidget:EditText = _
  var userWidget:EditText = _
  var passWidget:EditText = _
  var updateWidget:Spinner = _
  var matchWidget:EditText = _

  var updateEvery: Int = 300000

  val PREFS_NAME = "com.github.slashmili.Zendroid.AppWidgetConfigure"
  val PREF_PREFIX_KEY = "prefix_"
  override def onCreate(icicle: Bundle) = {
    super.onCreate(icicle)
    // Set the result to CANCELED.  This will cause the widget host to cancel
    // out of the widget placement if they press the back button.
//    setResult(Activity.RESULT_CANCELED);

    // Set the view layout resource to use.
    setContentView(R.layout.widget_configure);

   
    // Bind the action for the save button.
    findViewById(R.id.ok_button).setOnClickListener(mOnClickListener);

    // Find the widget id from the intent. 
    val intent = getIntent()
    val extras  = intent.getExtras()
    if (extras != null) {
      mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
      //UpdateServiceStore.removeWidgetId(mAppWidgetId)
    }

    // If they gave us an intent without the widget id, just bail.
    if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish();
    }

    // Find the EditTextm
    urlWidget = findViewById(R.id.widgetconf_url).asInstanceOf[EditText]
    userWidget = findViewById(R.id.widgetconf_user).asInstanceOf[EditText]
    passWidget = findViewById(R.id.widgetconf_pass).asInstanceOf[EditText]
    updateWidget = findViewById(R.id.widgetconf_update).asInstanceOf[Spinner]
    matchWidget =  findViewById(R.id.widgetconf_match).asInstanceOf[EditText] 
 
    val getDetail = ZenPreferences.loadPref(AppWidgetConfigure.this, mAppWidgetId)
    if(getDetail != None ){
      urlWidget.setText(getDetail.get.filter(_._1 =="url")(0)._2 )
      userWidget.setText(getDetail.get.filter(_._1 =="user")(0)._2 )
      passWidget.setText(getDetail.get.filter(_._1 =="pass")(0)._2 )
      matchWidget.setText(getDetail.get.filter(_._1 =="match")(0)._2 )
    }
    val list = Array("Every 5 minutes", "Every 10 minutes", "Every 20 minutes","Every 1 hour")
    val adapter = ArrayAdapter.createFromResource(this, R.array.update_every,  android.R.layout.simple_spinner_item)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    updateWidget.setAdapter(adapter)
    updateWidget.setOnItemSelectedListener(this)
  }

  def onNothingSelected(parrent: AdapterView[_]) = {
  }
  def onItemSelected(parrent: AdapterView[_], v: View, position: Int, id:Long) = {
    if (position == 1 ) 
      updateEvery = 600000
    else if (position == 2)
      updateEvery = 1200000
    else if (position == 3)
      updateEvery = 1800000
    else if (position == 4)
      updateEvery  = 3600000

  }
  val mOnClickListener = new View.OnClickListener() {
     def onClick(v: View): Unit = {
       /*
       val  context = AppWidgetConfigure.this

       val url = urlWidget.getText().toString()
       val user = userWidget.getText().toString()
       val pass = passWidget.getText().toString()
       val match_d = matchWidget.getText().toString()

        // Push widget update to surface with newly set prefix
        val appWidgetManager = AppWidgetManager.getInstance(context)

        ZenPreferences.savePref(context, mAppWidgetId, url, user, pass, updateEvery.toString, match_d)

        UpdateServiceStore.appendWidgetId(mAppWidgetId)
        context.startService(new Intent(context, classOf[UpdateService]))
        //AppWidgetProvider.updateWidget(context, appWidgetManager,   mAppWidgetId, titlePrefix);

        // Make sure we pass back the original appWidgetId
        val resultValue = new Intent();

        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(Activity.RESULT_OK, resultValue);
        */
        finish();
      }
   }

   // Write the prefix to the SharedPreferences object for this widget
//   def saveTitlePref(context: Context, appWidgetId: Int, text: String) = {
   // Read the prefix from the SharedPreferences object for this widget.
   // If there is no preference saved, get the default from a resource
//   def loadTitlePref(context: Context, appWidgetId: Int):String = {

}



object ZenPreferences {
  val PREFS_NAME = "com.github.slashmili.Zendroid.AppWidgetConfigure"
  val URL_PREFIX_KEY = "prefix_url"
  val USER_PREFIX_KEY = "prefix_user"
  val PASS_PREFIX_KEY = "prefix_pass"
  val UPDATE_PREFIX_KEY = "prefix_update"
  val MATCH_PREFIX_KEY  = "prefix_match"
  
   // Write the prefix to the SharedPreferences object for this widget
  def savePref(context: Context, appWidgetId: Int, url: String, user: String, pass: String, update: String, match_d: String) = {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
    prefs.putString(URL_PREFIX_KEY + appWidgetId, url);
    prefs.putString(USER_PREFIX_KEY + appWidgetId, user);
    prefs.putString(PASS_PREFIX_KEY + appWidgetId, pass);
    prefs.putString(UPDATE_PREFIX_KEY + appWidgetId, update);
    prefs.putString(MATCH_PREFIX_KEY + appWidgetId, match_d);
    prefs.commit();
  }

  // Read the prefix from the SharedPreferences object for this widget.
  // If there is no preference saved, return None
  def loadPref(context: Context, appWidgetId: Int):Option[List[(String,String)]] = {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    val url = prefs.getString(URL_PREFIX_KEY + appWidgetId, null);
    val user = prefs.getString(USER_PREFIX_KEY + appWidgetId, null)
    val pass = prefs.getString(PASS_PREFIX_KEY + appWidgetId, null)
    val update = prefs.getString(UPDATE_PREFIX_KEY + appWidgetId, null)

    if(url == null || user == null || pass == null || update == null) 
      return None

    val match_d = prefs.getString(MATCH_PREFIX_KEY + appWidgetId, null)

    return Some(List("url" -> url, "user" -> user, "pass" -> pass, "update" -> update, "match" -> match_d))
  }
}
