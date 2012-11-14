package com.github.slashmili.Zendroid.settings

import _root_.android.os.Bundle
import _root_.android.view.{View, Menu, MenuItem}
import _root_.android.widget.Toast
import _root_.android.content.{Intent, res, DialogInterface, Context, SharedPreferences}
import _root_.android.net.Uri
import _root_.android.preference.{PreferenceActivity, PreferenceManager, EditTextPreference}
import _root_.android.app.{Activity, ProgressDialog, AlertDialog}

import _root_.com.androidsnippets.SimpleCrypto

import com.github.slashmili.Zendroid._
import Services.{ZenossUpdateService, ServiceRunner}
import utils.ZenossEvents._
import utils.ZenossAPI


object ZenroidSettings {

  val ENCRYPT_KEY              = "THISKEYNEVERCOMITTOGIT!"
  val PREFIX_KEY_URL           = "prefix_url"
  val PREFIX_KEY_USER          = "prefix_user"
  val PREFIX_KEY_PASS          = "prefix_pass"
  val PREFIX_KEY_MATCH         = "prefix_match"
  val PREFIX_KEY_ON_CRITICAL   = "prefix_oncritical"
  val PREFIX_KEY_ON_ERROR      = "prefix_onerror"
  val PREFIX_KEY_ON_WARNING    = "prefix_onwarning"
  val PREFIX_KEY_INVALID_HTTPS = "prefix_invalidhttps"
  val PREFIX_KEY_STATE         = "prefix_state"
  val PREFIX_KEY_PERFORM_SYNCING = "prefix_performsyncing"
  val PREFIX_KEY_SYNCINGINTERVAL = "prefix_syncinginterval"
  val PREFIX_KEY_SYNCOVERWIFI  = "prefix_syncoverwifi"

  def clear(content: Context) = {
    val e = ZenroidSettings.prefs(content).edit
    e.clear
    e.commit
  }
  /* handle old config */
  def mvOldConfig(context: Context) = {
    val oldConf = ZendroidPreferences.loadPref(context)
    val  sp=PreferenceManager.getDefaultSharedPreferences(context).edit

    sp.putString(ZenroidSettings.PREFIX_KEY_URL, oldConf.get("url").toString)
    sp.putString(ZenroidSettings.PREFIX_KEY_USER, oldConf.get("user").toString)
    ZenroidSettings.encryptZenossPass(context, oldConf.get("pass").toString)
    sp.putBoolean(ZenroidSettings.PREFIX_KEY_INVALID_HTTPS, if(oldConf.get("invalid_ssl").toString.toInt == 0) false else true)
    sp.putString(ZenroidSettings.PREFIX_KEY_STATE, "")
    sp.putString(ZenroidSettings.PREFIX_KEY_ON_CRITICAL, oldConf.get("on_critical").toString)
    sp.putString(ZenroidSettings.PREFIX_KEY_ON_ERROR, oldConf.get("on_error").toString)
    sp.putString(ZenroidSettings.PREFIX_KEY_ON_WARNING, oldConf.get("on_warning").toString)
    sp.putString(ZenroidSettings.PREFIX_KEY_MATCH, oldConf.get("match").toString)
    sp.putBoolean(ZenroidSettings.PREFIX_KEY_PERFORM_SYNCING, if(oldConf.get("update").toString.toInt == 0) false else true )
    sp.putString(ZenroidSettings.PREFIX_KEY_SYNCINGINTERVAL, if(oldConf.get("update").toString.toInt == 0) "300000" else oldConf.get("update").toString)
    sp.putBoolean(ZenroidSettings.PREFIX_KEY_SYNCOVERWIFI, false)
    sp.commit
  }


  def isEmpty(context: Context) = {
    if(getZenossURL(context) == "")
      true
    else
      false
  }
  def prefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
  def getZenossURL(context: Context)  = prefs(context).getString(ZenroidSettings.PREFIX_KEY_URL, "")
  def getZenossUser(context: Context) = prefs(context).getString(ZenroidSettings.PREFIX_KEY_USER, "")
  def getZenossPassRaw(context: Context) = prefs(context).getString(ZenroidSettings.PREFIX_KEY_PASS, "")
  def getZenossPass(context: Context): String = {
    val pass =prefs(context).getString(ZenroidSettings.PREFIX_KEY_PASS, "")
    try {
      return SimpleCrypto.decrypt(ZenroidSettings.ENCRYPT_KEY, pass)
    }catch {
      case e => {
        e.printStackTrace()
        return ""
      }
    }
  }
  def encryptZenossPass(context: Context, pass: String) = {
    val pr = ZenroidSettings.prefs(context).edit
    pr.putString(ZenroidSettings.PREFIX_KEY_PASS, SimpleCrypto.encrypt(ZenroidSettings.ENCRYPT_KEY, pass))
    pr.commit
  }
  def getAcceptInvalidHTTPS(context: Context) = prefs(context).getBoolean(ZenroidSettings.PREFIX_KEY_INVALID_HTTPS, false)
  def getMatchDevice(context: Context) = prefs(context).getString(ZenroidSettings.PREFIX_KEY_MATCH, "")
  def getOnCritical(context: Context) = prefs(context).getString(ZenroidSettings.PREFIX_KEY_ON_CRITICAL, "0").toInt
  def getOnError(context: Context) = prefs(context).getString(ZenroidSettings.PREFIX_KEY_ON_ERROR, "0").toInt
  def getOnWarning(context: Context) = prefs(context).getString(ZenroidSettings.PREFIX_KEY_ON_WARNING, "0").toInt

  def fixbug44(context: Context) = {
    val  sp=PreferenceManager.getDefaultSharedPreferences(context).edit
    sp.putString(ZenroidSettings.PREFIX_KEY_STATE, "")
    sp.commit
  }
  def getStates(context: Context): List[String] = {

    val v = prefs(context).getString(ZenroidSettings.PREFIX_KEY_STATE, "")
    if(v == "#ALL#"){
        fixbug44(context)
        return List()
    }
    if(v.split("::").toList == List(""))
      List()
    else
      v.split("::").toList
  }
  def getPerformSyncing(context: Context) = prefs(context).getBoolean(ZenroidSettings.PREFIX_KEY_PERFORM_SYNCING, false)
  def getSyncingInterval(context: Context) = prefs(context).getString(ZenroidSettings.PREFIX_KEY_SYNCINGINTERVAL, "30000").toInt
  def getSyncoverWIFI(context: Context) = prefs(context).getBoolean(ZenroidSettings.PREFIX_KEY_SYNCOVERWIFI, false)

}

class ZenroidSettings extends PreferenceActivity {

  var saveSettingsCheck = false
  var isDiscard = false
  var isRemoved = false
  var errorMessage = ""


  var zenossURL = ""
  var zenossUser = ""
  var zenossPass = ""
  var acceptInvalidHTTPS = false
  var deviceProductionState = ""
  var onCritical = "0"
  var onError    = "0"
  var onWarning  = "0"
  var matchDevice = ""
  var syncing      = false
  var syncInterval = "300000"
  var syncOverWIFI = false


  def setPreferences  = {
    val  sp=PreferenceManager.getDefaultSharedPreferences(ZenroidSettings.this)
    zenossURL  = sp.getString(ZenroidSettings.PREFIX_KEY_URL, "")
    zenossUser = sp.getString(ZenroidSettings.PREFIX_KEY_USER, "")
    zenossPass = sp.getString(ZenroidSettings.PREFIX_KEY_PASS, "")
    acceptInvalidHTTPS = sp.getBoolean(ZenroidSettings.PREFIX_KEY_INVALID_HTTPS, false)
    deviceProductionState = sp.getString(ZenroidSettings.PREFIX_KEY_STATE, "")
    onCritical = sp.getString(ZenroidSettings.PREFIX_KEY_ON_CRITICAL, "0")
    onError = sp.getString(ZenroidSettings.PREFIX_KEY_ON_ERROR, "0")
    onWarning = sp.getString(ZenroidSettings.PREFIX_KEY_ON_WARNING, "0")
    matchDevice = sp.getString(ZenroidSettings.PREFIX_KEY_MATCH, "")
    syncing      = sp.getBoolean(ZenroidSettings.PREFIX_KEY_PERFORM_SYNCING,false)
    syncInterval = sp.getString(ZenroidSettings.PREFIX_KEY_SYNCINGINTERVAL, "300000")
    syncOverWIFI = sp.getBoolean(ZenroidSettings.PREFIX_KEY_SYNCOVERWIFI, false)

  }

  def revertPreferences = {
    val  sp=PreferenceManager.getDefaultSharedPreferences(ZenroidSettings.this).edit
    sp.putString(ZenroidSettings.PREFIX_KEY_URL, zenossURL)
    sp.putString(ZenroidSettings.PREFIX_KEY_USER, zenossUser)
    sp.putString(ZenroidSettings.PREFIX_KEY_PASS, zenossPass)
    sp.putBoolean(ZenroidSettings.PREFIX_KEY_INVALID_HTTPS, acceptInvalidHTTPS)
    sp.putString(ZenroidSettings.PREFIX_KEY_STATE, deviceProductionState)
    sp.putString(ZenroidSettings.PREFIX_KEY_ON_CRITICAL, onCritical)
    sp.putString(ZenroidSettings.PREFIX_KEY_ON_ERROR, onError)
    sp.putString(ZenroidSettings.PREFIX_KEY_ON_WARNING, onWarning)
    sp.putString(ZenroidSettings.PREFIX_KEY_MATCH, matchDevice)
    sp.putBoolean(ZenroidSettings.PREFIX_KEY_PERFORM_SYNCING,syncing)
    sp.putString(ZenroidSettings.PREFIX_KEY_SYNCINGINTERVAL, syncInterval)
    sp.putBoolean(ZenroidSettings.PREFIX_KEY_SYNCOVERWIFI, syncOverWIFI)
    sp.commit
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    isDiscard = false
    saveSettingsCheck = false
    isRemoved = false
    setPreferences
    addPreferencesFromResource(R.xml.global_preferences)
  }


  override def onBackPressed = {
    val newZenossURL  = ZenroidSettings.getZenossURL(this)
    val newZenossUser = ZenroidSettings.getZenossUser(this)
    val newZenossPass = ZenroidSettings.getZenossPassRaw(this)
    val newSyncing    = ZenroidSettings.getPerformSyncing(this)
    val newAcceptInvalidHTTPS = ZenroidSettings.getAcceptInvalidHTTPS(this)

    if(newZenossURL          != zenossURL ||
       newZenossUser         != zenossUser ||
       newZenossPass         != zenossPass ||
       newSyncing            != syncing ||
       newAcceptInvalidHTTPS != acceptInvalidHTTPS ){

        new AlertDialog.Builder(ZenroidSettings.this)
        .setTitle("Settings have been changed")
        .setMessage("Do you want to save new settings ?")
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            def onClick(dialog: DialogInterface, which:Int) ={
              dialog.cancel
              isDiscard = true
              finish
            }
          })
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            def onClick(dialog: DialogInterface, which:Int) ={
              dialog.cancel
              tryToSaveSettings
            }
          })
          .show()
    }else {
      tryToSaveSettings
      finish
    }

  }
  override def onCreateOptionsMenu(menu: Menu): Boolean ={
    val inflater = getMenuInflater()
    inflater.inflate(R.menu.settings_menu, menu)
    return true
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    return true
  }

  def tryToSaveSettings = {
    val newZenossURL  = ZenroidSettings.getZenossURL(this)
    val newZenossUser = ZenroidSettings.getZenossUser(this)
    var newZenossPass = ZenroidSettings.getZenossPass(this)
    if(newZenossPass == ""){
      newZenossPass = ZenroidSettings.getZenossPassRaw(this)
    }
    val newAcceptInvalidHTTPS = if(ZenroidSettings.getAcceptInvalidHTTPS(this) == false)
        "0"
      else
        "1"
    saveSettingsCheck = false
    if(mainSettingNotChanged){
      saveSettingsCheck = true
      runAndExit
    }else {
      new CheckSettings().execute(newZenossURL, newZenossUser, newZenossPass , newAcceptInvalidHTTPS)
    }
  }

  private def mainSettingNotChanged() ={
    val newZenossURL  = ZenroidSettings.getZenossURL(this)
    val newZenossUser = ZenroidSettings.getZenossUser(this)
    var newZenossPass = ZenroidSettings.getZenossPass(this)
    val newAcceptInvalidHTTPS = if(ZenroidSettings.getAcceptInvalidHTTPS(this) == false)
        "0"
      else
        "1"

    newZenossURL == zenossURL && newZenossUser == zenossUser && ZenroidSettings.getZenossPass(this) != "" && ZenroidSettings.getAcceptInvalidHTTPS(this) == acceptInvalidHTTPS
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean ={
    item.getItemId match {
      case R.id.mnuSave => {
        tryToSaveSettings
      }
      case R.id.mnuDiscard => {isDiscard = true ; finish }
      case R.id.mnuRemoveAccount=> {
        //showPopupError(ZenroidSettings.getStates(this).toString)
        new AlertDialog.Builder(ZenroidSettings.this)
        .setTitle("Removing Account")
        .setMessage("Do you want to remove your account from Zenroid ?")
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            def onClick(dialog: DialogInterface, which:Int) ={
              dialog.cancel
            }
          })
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            def onClick(dialog: DialogInterface, which:Int) ={
              ZenroidSettings.clear(ZenroidSettings.this)
              dialog.cancel
              isRemoved = true ;
              finish
            }
          })
          .show()
      }
      case _ => {}
    }
    return super.onOptionsItemSelected(item)
  }


  override def onConfigurationChanged(newConfig: res.Configuration){
    super.onConfigurationChanged(newConfig)

  }
  override def onStop = {
    val context = getApplicationContext()
    val toastMessage = if(isDiscard == true){
      revertPreferences
      ""
    }else if (saveSettingsCheck == true){
      "Setting is saved"
    }else if(isRemoved == true){
      "Account is removed"
    } else if (mainSettingNotChanged){
        tryToSaveSettings
      "Setting is saved"
    }else {
      revertPreferences
      "Settings isn't saved, you need to press menu and save the settings"
    }
    if(toastMessage != ""){
      val toast = Toast.makeText(context, toastMessage , Toast.LENGTH_SHORT)
      toast.show
    }

    super.onStop
  }

  override def onResume = {
    super.onResume
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
        val sp = PreferenceManager.getDefaultSharedPreferences(ZenroidSettings.this)
        val tmp  = sp.getString(ZenroidSettings.PREFIX_KEY_MATCH, "").r.findAllIn("").toSeq.length
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
      dialog = new ProgressDialog(ZenroidSettings.this)
      dialog.setMessage("Checking Settings ...")
      dialog.show()
    }

    override protected def onPostExecute2(res: String) =  {
      dialog.dismiss()
      if(saveSettingsCheck == false){
        showPopupError(errorMessage)
      }else {
        runAndExit
      }
    }
  }
  def showPopupError (error:String) = {
    new AlertDialog.Builder(ZenroidSettings.this)
    .setTitle("Error in applying config")
    .setMessage(error)
    .setNegativeButton("No", new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which:Int) ={
          dialog.cancel
        }
      })
    .show();
  }

  def runAndExit () ={
    if(ZenroidSettings.getZenossPass(this) == ""){
      ZenroidSettings.encryptZenossPass(this, ZenroidSettings.getZenossPassRaw(this))
    }
    ServiceRunner.startService(this)

      val newSyncing = ZenroidSettings.getPerformSyncing(this)
      val toastMessage = if(syncing == true && newSyncing == false ){
        "Stoping monitoring ..."
      }else if(syncing == false && newSyncing == true){
        "Starting monitoring ..."
      }else {
        ""
      }
      if(toastMessage != ""){
        val context = getApplicationContext()
        val toast = Toast.makeText(context, toastMessage , Toast.LENGTH_SHORT)
        toast.show()
      }
      finish()
  }

}
