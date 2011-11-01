package com.github.slashmili.Zendroid


import _root_.android.app.Activity
import _root_.android.os.Bundle
import _root_.android.view.View
import _root_.android.widget.{EditText, TextView, Button, Spinner, ArrayAdapter, AdapterView}
import _root_.android.content.Intent;
import _root_.android.widget.AdapterView.OnItemSelectedListener
import _root_.android.appwidget.AppWidgetManager
import _root_.android.app.{ProgressDialog, AlertDialog}
import _root_.android.content.DialogInterface
import _root_.android.util.Log

import com.github.slashmili.Zendroid.Services.{ZenossUpdateService, ServiceRunner}
import com.github.slashmili.Zendroid.utils._
import ZenossEvents._



class GlobalConfiguration extends Activity  {

  //static objects
  object UpdatePeriod {
    val EVERY_5_MINS = 300000
    val EVERY_10_MINS = 600000
    val EVERY_20_MINS = 1200000
    val EVERY_30_MINS = 1800000
    val EVERY_1_HOUR =  3600000
    val DISABLED = 0
  }

  object Alarm {
    val NO_ALARM=0;
    val ALARM_WITH_NOTIFICATION=1;
    val ALARM_WITH_NOTIFICATION_AND_SOUND=2;
  }


  //widgets
  var txtZenossURL:EditText = _
  var txtZenossUser:EditText = _
  var txtZenossPass:EditText = _
  var spnOnCritical:Spinner = _
  var spnOnError:Spinner = _
  var spnOnWarning:Spinner = _
  var spnUpdateEvery:Spinner = _
  var txtMatchDevice:EditText = _

  //variables
  var updateEvery: Int = UpdatePeriod.EVERY_5_MINS
  var onCritical:Int = Alarm.ALARM_WITH_NOTIFICATION_AND_SOUND
  var onError:Int    = Alarm.ALARM_WITH_NOTIFICATION_AND_SOUND
  var onWarning:Int  = Alarm.ALARM_WITH_NOTIFICATION_AND_SOUND
  var saveSettingsCheck = false
  var finishedSavingProcess = false 
  var errorMessage = ""

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.global_configuration)
    findViewById(R.id.btnSaveSettings).setOnClickListener(btnSaveSettingsOnClickListener)
    findViewById(R.id.btnGetLastStatus).setOnClickListener(btnGetLastStatusOnClickListener)

    //config texts
    txtZenossURL   =  findViewById(R.id.txtZenossURL).asInstanceOf[EditText]
    txtZenossUser  =  findViewById(R.id.txtZenossUser).asInstanceOf[EditText]
    txtZenossPass  =  findViewById(R.id.txtZenossPass).asInstanceOf[EditText]
    txtMatchDevice =  findViewById(R.id.txtMatchDevice).asInstanceOf[EditText]

    val eventAdapter = ArrayAdapter.createFromResource(this, R.array.on_event_choice, android.R.layout.simple_spinner_item);
    eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
    //config spnOnCritical
    spnOnCritical = findViewById(R.id.spnOnCritical).asInstanceOf[Spinner]
    spnOnCritical.setAdapter(eventAdapter);
    //TODO: make class for handling Alarm Spinner
    spnOnCritical.setOnItemSelectedListener(new OnItemSelectedListener() {
        def onItemSelected (parrent: AdapterView[_], v: View, position: Int, id:Long) ={
          onCritical = position match  {
            case 0 => Alarm.NO_ALARM
            case 1 => Alarm.ALARM_WITH_NOTIFICATION
            case 2 => Alarm.ALARM_WITH_NOTIFICATION_AND_SOUND
          }
        }

        def onNothingSelected(parrent: AdapterView[_]) = {
        }
      })

    //config spnOnError
    spnOnError = findViewById(R.id.spnOnError).asInstanceOf[Spinner]
    spnOnError.setAdapter(eventAdapter);
    spnOnError.setOnItemSelectedListener(new OnItemSelectedListener() {
        def onItemSelected (parrent: AdapterView[_], v: View, position: Int, id:Long) ={
          onError = position match  {
            case 0 => Alarm.NO_ALARM
            case 1 => Alarm.ALARM_WITH_NOTIFICATION
            case 2 => Alarm.ALARM_WITH_NOTIFICATION_AND_SOUND
          }
        }

        def onNothingSelected(parrent: AdapterView[_]) = {
        }
      })   

    //config spnOnWarning
    spnOnWarning = findViewById(R.id.spnOnWarning).asInstanceOf[Spinner]
    spnOnWarning.setAdapter(eventAdapter);
    spnOnWarning.setOnItemSelectedListener(new OnItemSelectedListener() {
        def onItemSelected (parrent: AdapterView[_], v: View, position: Int, id:Long) ={
          onWarning = position match  {
            case 0 => Alarm.NO_ALARM
            case 1 => Alarm.ALARM_WITH_NOTIFICATION
            case 2 => Alarm.ALARM_WITH_NOTIFICATION_AND_SOUND
          }
        }

        def onNothingSelected(parrent: AdapterView[_]) = {
        }
      })  

    val updateAdapter = ArrayAdapter.createFromResource(this, R.array.update_every_choice, android.R.layout.simple_spinner_item);
    updateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spnUpdateEvery = findViewById(R.id.spnUpdateEvery).asInstanceOf[Spinner]
    spnUpdateEvery.setAdapter(updateAdapter);    
    spnUpdateEvery.setOnItemSelectedListener(new OnItemSelectedListener() {
        def onItemSelected (parrent: AdapterView[_], v: View, position: Int, id:Long) ={
          updateEvery = position match  {
            case 0 => UpdatePeriod.EVERY_5_MINS
            case 1 => UpdatePeriod.EVERY_10_MINS
            case 2 => UpdatePeriod.EVERY_20_MINS
            case 3 => UpdatePeriod.EVERY_30_MINS
            case 4 => UpdatePeriod.EVERY_1_HOUR
            case 5 => UpdatePeriod.DISABLED
          }
        }

        def onNothingSelected(parrent: AdapterView[_]) = {
        }
      })  
    //load setting if saved before
    loadSettings
  }

  val btnSaveSettingsOnClickListener = new View.OnClickListener() {
    def onClick(v: View): Unit = {
      val url = txtZenossURL.getText().toString()
      val user = txtZenossUser.getText().toString()
      val pass = txtZenossPass.getText().toString()
      if (updateEvery == 0){
        saveSettings()
      }
      else {
        new CheckSettings().execute(url, user, pass)
      }
    }
  }

  val btnGetLastStatusOnClickListener = new View.OnClickListener() {
    def onClick(v: View): Unit = {
      val lastRunStatus = if (ServiceRunner.started == false){
        "Last Run Error: You haven't run Zendroid service yet"
      }
      else if (ServiceRunner.errorMessage == "" ){
        "Last Run Error: Clean"
      }else {
        "Last Run Error: "  + ServiceRunner.errorMessage
      }
      new AlertDialog.Builder(GlobalConfiguration.this)
      .setTitle("Last Status")
      .setMessage("Next Update: " + ServiceRunner.nextTime.format("%R") + "\n" + lastRunStatus)
      .setNegativeButton("Close", new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, which:Int) ={
            dialog.cancel
          }
        })
      .show()
    }
  }


  def showPopupError (error:String) = {
    new AlertDialog.Builder(GlobalConfiguration.this)
    .setTitle("Error in applying config")
    .setMessage(error)
    .setNegativeButton("No", new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which:Int) ={ 
          dialog.cancel
        }
      })
    .show();   
  }

  def saveSettings() = {
    val url = txtZenossURL.getText().toString()
      val user = txtZenossUser.getText().toString()
      val pass = txtZenossPass.getText().toString()
      val matchDevice = txtMatchDevice.getText().toString()
      ZendroidPreferences.savePref(GlobalConfiguration.this, url, user, pass, onCritical, onError, onWarning, matchDevice, 0, updateEvery)
      ServiceRunner.startService(this);
  }

  def loadSettings() = {
    val config = ZendroidPreferences.loadPref(GlobalConfiguration.this)
    if(config != None){
      txtZenossURL.setText(config.get("url").toString)
      txtZenossUser.setText(config.get("user").toString)
      txtZenossPass.setText(config.get("pass").toString)
      txtMatchDevice.setText(config.get("match").toString)
      spnOnCritical.setSelection(config.get("on_critical").toString.toInt)
      spnOnError.setSelection(config.get("on_error").toString.toInt)
      spnOnWarning.setSelection(config.get("on_warning").toString.toInt)

      val updateEvery = config.get("update").toString.toInt
      updateEvery match {
        case UpdatePeriod.EVERY_10_MINS => spnUpdateEvery.setSelection(1)
        case UpdatePeriod.EVERY_20_MINS => spnUpdateEvery.setSelection(2)
        case UpdatePeriod.EVERY_30_MINS => spnUpdateEvery.setSelection(3)
        case UpdatePeriod.EVERY_1_HOUR  => spnUpdateEvery.setSelection(4)
        case UpdatePeriod.DISABLED      => spnUpdateEvery.setSelection(5)
        case _ => spnUpdateEvery.setSelection(0)
      }
    }
  }

  private class CheckSettings extends MyAsyncTask {
    var dialog:ProgressDialog = _ 
    override protected def doInBackground1(zenConf: Array[String]): String = {
      val zen = new ZenossAPI(zenConf(0), zenConf(1), zenConf(2))
      saveSettingsCheck = false
      errorMessage = ""
      try {
        if(zen.auth == false){
          errorMessage = "Wrong username or password"
          saveSettingsCheck = false
        }else {
          saveSettingsCheck = true
        }
      }catch {
        case e=> {
          errorMessage = e.toString
          saveSettingsCheck = false
        }
      }
      return "checked";
    }

    override protected def onPreExecute2 () = {
      dialog = new ProgressDialog(GlobalConfiguration.this)
      dialog.setMessage("Checking Settings ...")
      dialog.show()
    }

    override protected def onPostExecute2(res: String) =  {
      dialog.dismiss()
      if(saveSettingsCheck == false){
        showPopupError(errorMessage)
      }else {
        saveSettings()
      }
    }
  }
}
