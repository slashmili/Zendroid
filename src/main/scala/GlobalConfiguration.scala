package com.github.slashmili.Zendroid


import _root_.android.app.Activity
import _root_.android.os.Bundle
import _root_.android.view.{View, Menu, MenuItem}
import _root_.android.widget.{EditText, TextView, Button, Spinner, ArrayAdapter, AdapterView, Toast, CheckBox}
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
    val NO_ALARM=0
    val ALARM_WITH_NOTIFICATION=1
    val ALARM_WITH_NOTIFICATION_AND_SOUND=2
    val ALARM_WITH_NOTIFICATION_AND_SOUND_AND_VIBRATE=3
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
  var chkInvalidSSL:CheckBox = _

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
    findViewById(R.id.btnDeleteAccount).setOnClickListener(btnRemoveAccountOnClickListener)
    //config texts
    txtZenossURL   =  findViewById(R.id.txtZenossURL).asInstanceOf[EditText]
    txtZenossUser  =  findViewById(R.id.txtZenossUser).asInstanceOf[EditText]
    txtZenossPass  =  findViewById(R.id.txtZenossPass).asInstanceOf[EditText]
    txtMatchDevice =  findViewById(R.id.txtMatchDevice).asInstanceOf[EditText]
    chkInvalidSSL  =  findViewById(R.id.chkInvalidSSL).asInstanceOf[CheckBox]

    def getAlaramType(id: Int) ={
      id match {
          case 1 => Alarm.ALARM_WITH_NOTIFICATION
          case 2 => Alarm.ALARM_WITH_NOTIFICATION_AND_SOUND
          case 3 => Alarm.ALARM_WITH_NOTIFICATION_AND_SOUND_AND_VIBRATE
          case _ => Alarm.NO_ALARM
      }
    }
    val eventAdapter = ArrayAdapter.createFromResource(this, R.array.on_event_choice, android.R.layout.simple_spinner_item);
    eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
    //config spnOnCritical
    spnOnCritical = findViewById(R.id.spnOnCritical).asInstanceOf[Spinner]
    spnOnCritical.setAdapter(eventAdapter);
    //TODO: make class for handling Alarm Spinner
    spnOnCritical.setOnItemSelectedListener(new OnItemSelectedListener() {
        def onItemSelected (parrent: AdapterView[_], v: View, position: Int, id:Long) ={
          onCritical = getAlaramType(position)
        }

        def onNothingSelected(parrent: AdapterView[_]) = {
        }
    })

    //config spnOnError
    spnOnError = findViewById(R.id.spnOnError).asInstanceOf[Spinner]
    spnOnError.setAdapter(eventAdapter);
    spnOnError.setOnItemSelectedListener(new OnItemSelectedListener() {
        def onItemSelected (parrent: AdapterView[_], v: View, position: Int, id:Long) ={
          onError = getAlaramType(position)
        }

        def onNothingSelected(parrent: AdapterView[_]) = {
        }
      })   

    //config spnOnWarning
    spnOnWarning = findViewById(R.id.spnOnWarning).asInstanceOf[Spinner]
    spnOnWarning.setAdapter(eventAdapter);
    spnOnWarning.setOnItemSelectedListener(new OnItemSelectedListener() {
        def onItemSelected (parrent: AdapterView[_], v: View, position: Int, id:Long) ={
          onWarning = getAlaramType(position)
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
      val config = ZendroidPreferences.loadPref(GlobalConfiguration.this)
      if (updateEvery == 0){
        saveSettings
        runAndExit
      }
      else if(config != None && config.get("url").toString == url && config.get("user") == user && config.get("pass") == pass){
        saveSettings
        runAndExit
      }
      else {
        var acceptInvalidSSL = "0"
        if(chkInvalidSSL.isChecked == true){
            acceptInvalidSSL = "1"
        }
        new CheckSettings().execute(url, user, pass, acceptInvalidSSL)
      }
    }
  }


  val btnRemoveAccountOnClickListener = new View.OnClickListener() {
    def onClick(v: View): Unit = {
    new AlertDialog.Builder(GlobalConfiguration.this)
    .setTitle("Removing Account")
    .setMessage("Do you want to remove your account from Zenroid ?")
    .setNegativeButton("No", new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which:Int) ={
          dialog.cancel
        }
      })
    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which:Int) ={
          txtZenossURL.setText("")
          txtZenossUser.setText("")
          txtZenossPass.setText("")
          updateEvery = UpdatePeriod.DISABLED
          saveSettings
          runAndExit
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
      val invalidSSL = if ( chkInvalidSSL.isChecked){
        1
      } else {
        0
      }
      ZendroidPreferences.savePref(GlobalConfiguration.this, url, user, pass, onCritical, onError, onWarning, matchDevice, invalidSSL, updateEvery)
  }

  def runAndExit () ={
      ServiceRunner.startService(this);

      val toastMessage = if(updateEvery==0){
        "Stoping monitoring ..."
      }else{
        "Starting monitoring ..."
      }
      val context = getApplicationContext()
      val toast = Toast.makeText(context, toastMessage , Toast.LENGTH_SHORT)
      toast.show()
      finish()
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
      var invalidSSL = false
      if ( config.get("invalid_ssl").toString.toInt == 1)
        invalidSSL = true
      chkInvalidSSL.setChecked(invalidSSL)

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
      var acceptInvalidSSL = false
      if (zenConf(3).toString.toInt == 1)
        acceptInvalidSSL = true
      val zen = new ZenossAPI(zenConf(0), zenConf(1), zenConf(2), acceptInvalidSSL)
      saveSettingsCheck = false
      errorMessage = ""
      try {
        val tmp = txtMatchDevice.getText.toString.r.findAllIn("").toSeq.length
        saveSettingsCheck = true
      }catch {
        case _ => {
          errorMessage = "Invalid Regex in Match Device"
          saveSettingsCheck = false
          return "checked"
        }
      }
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
        saveSettings
        runAndExit
      }
    }
  }
}
