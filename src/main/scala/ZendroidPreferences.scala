package com.github.slashmili.Zendroid


import _root_.android.content.{Context, SharedPreferences}
import _root_.com.androidsnippets.SimpleCrypto

object ZendroidPreferences {
  val PREFS_NAME              = "com.github.slashmili.Zendroid.ZenossConfig"
  val ENCRYPT_KEY             = "THISKEYNEVERCOMITTOGIT!"
  val PREFIX_KEY_URL          = "prefix_url"
  val PREFIX_KEY_USER         = "prefix_user"
  val PREFIX_KEY_PASS         = "prefix_pass"
  val PREFIX_KEY_MATCH        = "prefix_match"
  val PREFIX_KEY_ON_CRITICAL  = "prefix_oncritical"
  val PREFIX_KEY_ON_ERROR     = "prefix_onerror"
  val PREFIX_KEY_ON_WARNING   = "prefix_onwarning"
  val PREFIX_KEY_INVALID_SSL  = "prefix_invalidssl"
  val PREFIX_KEY_UPDATE_EVERY = "prefix_updateevery"
  val PREFIX_RUN_ON           = "prefix_syncover"

  def clear(context: Context) = {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
    prefs.clear
    prefs.commit
  }
  // Write the prefix to the SharedPreferences object for this widget
  def savePref(context: Context, url: String, user: String, pass: String, onCritical: Int, onError:Int, onWarning:Int, match_d: String, invalidSSL:Int, updateEvery: Int, syncOver:String) = {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
    //Save
    prefs.putString(PREFIX_KEY_URL, url)
    prefs.putString(PREFIX_KEY_USER, user)
    prefs.putString(PREFIX_KEY_PASS, SimpleCrypto.encrypt(ENCRYPT_KEY, pass))
    prefs.putString(PREFIX_KEY_MATCH, match_d)
    prefs.putString(PREFIX_KEY_ON_CRITICAL, onCritical.toString)
    prefs.putString(PREFIX_KEY_ON_ERROR, onError.toString)
    prefs.putString(PREFIX_KEY_ON_WARNING, onWarning.toString)
    prefs.putString(PREFIX_KEY_INVALID_SSL, invalidSSL.toString)
    prefs.putString(PREFIX_KEY_UPDATE_EVERY, updateEvery.toString)
    prefs.putString(PREFIX_RUN_ON, syncOver.toString)
    prefs.commit()
  }

  // Read the prefix from the SharedPreferences object for this widget.
  // If there is no preference saved, return None
  def loadPref(context: Context):Option[Map[String,Any]] = {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    //load
    val url         = prefs.getString(PREFIX_KEY_URL, null)
    val user        = prefs.getString(PREFIX_KEY_USER, null)
    var pass        = prefs.getString(PREFIX_KEY_PASS, null)
    val matchDevice = prefs.getString(PREFIX_KEY_MATCH, null)
    val onCritical  = prefs.getString(PREFIX_KEY_ON_CRITICAL, null)
    val onError     = prefs.getString(PREFIX_KEY_ON_ERROR, null)
    val onWarning   = prefs.getString(PREFIX_KEY_ON_WARNING, null)
    val invalidSSL  = prefs.getString(PREFIX_KEY_INVALID_SSL, null)
    val updateEvery = prefs.getString(PREFIX_KEY_UPDATE_EVERY, null)
    val syncOver    = prefs.getString(PREFIX_RUN_ON, "always")

    try {
      pass = SimpleCrypto.decrypt(ENCRYPT_KEY, pass)
    }catch {
      case e => pass = null
    }

    if(url == null || user == null || pass == null || updateEvery == null) 
      return None

    return Some(Map("url" -> url, "user" -> user, "pass" -> pass, "update" -> updateEvery, "match" -> matchDevice, "on_critical" -> onCritical, "on_error"->onError, "on_warning"->onWarning, "invalid_ssl"->invalidSSL, "sync_over"->syncOver))
  }
}
