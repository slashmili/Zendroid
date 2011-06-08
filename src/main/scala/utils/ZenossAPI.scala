package com.github.slashmili.Zendroid.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import java.net.{URLEncoder, URLDecoder}
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.json.JSONObject
import org.apache.http.client.params.ClientPNames;

import _root_.android.util.Log

class ZenossAPI (url: String, username: String, password: String){
  var cookie = ""

  def auth : Boolean = {
    val addrLogin="/zport/acl_users/cookieAuthHelper/login"
    val data = List(("__ac_name", username),
                    ("__ac_password", password),
                    ("submitted", "true"),
                    ("came_from", "%s%s".format(url, "/zport/dmd"))
              )

    Log.w("ZenossAPI", "I'm going to submit user info to web page")
    val res = HttpClient.Post("%s%s" format(url,addrLogin), data, List())
    if (res != None)
      res.get._2 filter {
        h=> h._1.toUpperCase == "Set-Cookie".toUpperCase
      } foreach {
        case(hName, hValue) => cookie = hValue
      }

    Log.w("ZenossAPI", "I submit username and password let's check it")
    return authCheck
  }

  def authCheck : Boolean = {
    if ( cookie == "" )
      return false


    Log.w("ZenossAPI", "Ok then, I'm submiting test page wait...")
    val res = HttpClient.Post("%s%s".format(url,"/zport/dmd"), List(), List(("Cookie", cookie)))
    if (res == None)
      return false
    Log.w("ZenossAPI", "It returns something let's check it")
    val resCode = res.get._2 filter {
      h => h._1 == "StatusCode" && h._2 == "200"
    }
    if (resCode.length == 1)
      return true
    return false

  }

  def getUrl = url
  def getCookie = cookie

}

object ZenossEvents {
  implicit def MapEvents(zen: ZenossAPI) = new ZenossEvents(zen.getUrl, zen.getCookie)
}

class ZenossEvents (url: String, cookie: String) {
  val action = "EventsRouter"
  val actionType   = "rpc"

  def eventsQuery : Option[JSONObject] = {
    val u = "/zport/dmd/evconsole_router"
    val l = """{"action":"EventsRouter","method":"query","data":[{"start":0,"limit":100,"dir":"DESC","sort":"severity","params":"{\"severity\":[5,4,3],\"eventState\":[0,1]}"}],"type":"rpc","tid":1}"""
    Log.w("ZenossEvents", l)
    val res = HttpClient.Json("%s%s".format(url, u), new JSONObject(l), List(("Cookie", cookie)))
    if(res == None)
      return None
    return Some(res.get._1)
 
  }

}


object HttpClient {
  val charset = "UTF-8"

  private def urlEncode(str: String) =  URLEncoder.encode(str, charset)

  def Json(url: String, data: JSONObject,  headers: List[(String, String)]): Option[(JSONObject, List[(String, String)])] = {
    val postHeader = ("Content-type", "application/json") :: ("Accept", "application/json") :: headers 
    Log.w("JSOOOOOOOOOn","Send Jso0000000000000n requesti %s".format(data.toString))
    Log.w("HttpClient", "I'm sending JSON request please wait...")
    val req = Request(url, data.toString, postHeader)

    if (req != None )
      return Some(( new JSONObject(req.get._1), req.get._2 ))
    return None
  }

  def Post(url: String, data: List[(String, String)], headers: List[(String, String)]): Option[(String, List[(String, String)])] = {
    val postHeader = ("Content-type","application/x-www-form-urlencoded") :: headers
    var raw_data = ""
    raw_data = data.foldLeft(raw_data) { (raw_data, x) => raw_data +  x._1 + "=" + x._2 + "&" }
    Request(url, raw_data, postHeader)
  }

  def Get(url: String, data: List[(String, String)], headers: List[(String, String)]): Option[(String, List[(String, String)])] = {
    var raw_data = ""
    raw_data = data.foldLeft(raw_data) { (raw_data, x) => raw_data +  urlEncode(x._1) + "=" + urlEncode(x._2) + "&" }

    Request("%s?%s".format(url, raw_data), "", headers)
  }

  def Request(url: String, data: String, headers: List[(String, String)]): Option[(String, List[(String, String)])] = {
    try 
    {
      Log.w("HttpClient", "I'm processing Request ... %s".format(data))
      val httpclient = new DefaultHttpClient()
      Log.w("HttpClient", "I'm removing http redirect handler ")
      httpclient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false)
      val httpPostRequest = new HttpPost(url)
      val se = new StringEntity(data)

      httpPostRequest.setEntity(se);
      headers foreach { 
        case(hname, hvalue) => httpPostRequest.setHeader(hname, hvalue)
      }
      httpPostRequest.setHeader("Accept-Encoding", "gzip")

      Log.w("HttpClient", "Executing Request")
      val response = httpclient.execute(httpPostRequest).asInstanceOf[HttpResponse]
      Log.w("HttpClient", "I've got some hot data from remote server")

      //Get headers
      var resHeader: List[(String,String)] = List()

      Log.w("HttpClient", "I'm getting headers")
      response.getAllHeaders foreach { 
        case(header) =>  
        resHeader ::= (header.getName.toString , header.getValue.toString) 
      }
      resHeader ::= ("StatusCode", response.getStatusLine().getStatusCode().toString)

      //Get body
      Log.w("HttpClient", "I'm reading body")
      val entity = response.getEntity()
      var instream = entity.getContent()
      if (response.getFirstHeader("Content-Encoding") != Nil && response.getFirstHeader("Content-Encoding").getValue == "gzip"){
        Log.w("HttpClient", "Opps! it's ziped, no worry I have my tools to decode it")
        instream = new GZIPInputStream(instream)
      }
      val reader = new BufferedReader(new InputStreamReader(instream))

      val bos = new StringBuilder
      val ba = new Array[Char](4096)

      def readOnce {
        val len = reader.read(ba)
        if (len > 0) bos.appendAll(ba)
        if (len >= 0) readOnce
      }
      readOnce

      return Some((bos.toString, resHeader))

    } catch {
      case e => 
        //throw e
 e.printStackTrace();
    }
    return None
  }
}

object exten {
implicit def d(s: String) = new JSONObject(s)
}
// vim: set ts=2 sw=2 et:
