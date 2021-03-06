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

import _root_.android.util.{Log => AndroidLog}

private object Log {
  def d(b:String) ={
    AndroidLog.d("ZenossAPI", b)
  }
}

class ZenossAPI (url: String, username: String, password: String, acceptUntrustedSSL:Boolean = false){
  var cookie = ""
  def isAcceptUntrustedSSL = acceptUntrustedSSL

  def auth : Boolean = {
    val addrLogin="/zport/acl_users/cookieAuthHelper/login"
    val data = List(("__ac_name", username),
                    ("__ac_password", password),
                    ("submitted", "true"),
                    ("came_from", "%s%s".format(url, "/zport/dmd"))
              )

    Log.d("ZenossAPI.auth: I'm going to submit user info to web page")
    val res = HttpClient.Post("%s%s" format(url,addrLogin), data, List(), acceptUntrustedSSL)
    if (res != None)
      res.get._2 filter {
        h=> h._1.toUpperCase == "Set-Cookie".toUpperCase
      } foreach {
        case(hName, hValue) => cookie = hValue
      }

    Log.d("ZenossAPI.auth: I submit username and password let's check it")
    return authCheck
  }

  def authCheck : Boolean = {
    if ( cookie == "" )
      return false


    Log.d("ZenossAPI.authCheck: Ok then, I'm submiting test page wait...")
    val res = HttpClient.Post("%s%s".format(url,"/zport/dmd"), List(), List(("Cookie", cookie)), acceptUntrustedSSL)
    if (res == None)
      return false
    Log.d("ZenossAPI.authCheck: It returns something let's check it")
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
  implicit def MapEvents(zen: ZenossAPI) = new ZenossEvents(zen.getUrl, zen.getCookie, zen.isAcceptUntrustedSSL)
}

class ZenossEvents (url: String, cookie: String, acceptUntrustedSSL:Boolean = false) {
  val action = "EventsRouter"
  val actionType   = "rpc"
  val addrEventConsole = "/zport/dmd/evconsole_router"
  def eventsQuery(productStates: List[String]) : Option[JSONObject] = {
    var prodState = ""
    val lastVal = if(productStates != List())
        productStates.last
      else
        ""
    productStates.foreach( state =>{
        prodState += """{"prodState":"%s"}""".format(state)
        if(state != lastVal)
          prodState += ", "
      }
    )

    val l = """{"action":"EventsRouter","method":"query","data":[{"criteria":[%s],"start":0,"limit":100,"dir":"DESC","sort":"lastTime","params":"{\"severity\":[5,4,3],\"eventState\":[0,1]}"}],"type":"rpc","tid":1}""".format(prodState)
    Log.d("RRRRREQUESTING " + l)
    val res = HttpClient.Json("%s%s".format(url, addrEventConsole), new JSONObject(l), List(("Cookie", cookie)), acceptUntrustedSSL)
    Log.d(res.toString)
    if(res == None)
      return None
    return Some(res.get._1)

  }



  private def checkResult(res : Option[(JSONObject, List[(String, String)])]): Boolean = {
    if(res == None)
      return false
    val jsonRes = res.get._1
    if(jsonRes.has("result") && jsonRes.getJSONObject("result").has("success")){
      if(jsonRes.getJSONObject("result").getString("success") == "true")
          return true
      else
        return false
    }else if(jsonRes.has("type") && jsonRes.getString("type") == "exception"){
        throw new Exception(jsonRes.getString("message"))
    } else
      return false
    true
  }

  def eventsAcknowledge(evid: String): Boolean = {
    val query = """{"action":"EventsRouter","type":"rpc","data":[{"direction":"DESC","history":false,"evids":["%s"]}],"method":"acknowledge","tid":2}""".format(evid)
    val res = HttpClient.Json("%s%s".format(url, addrEventConsole), new JSONObject(query), List(("Cookie", cookie)), acceptUntrustedSSL)
    return checkResult(res)
  }

  def eventsUnacknowledge(evid: String): Boolean = {
    val query = """{"action":"EventsRouter","type":"rpc","data":[{"direction":"DESC","history":false,"evids":["%s"]}],"method":"unacknowledge","tid":2}""".format(evid)
    val res = HttpClient.Json("%s%s".format(url, addrEventConsole), new JSONObject(query), List(("Cookie", cookie)), acceptUntrustedSSL)
    return checkResult(res)
  }

  def eventsClose(evid: String): Boolean = {
    val query = """{"action":"EventsRouter","type":"rpc","data":[{"direction":"DESC","history":false,"evids":["%s"]}],"method":"close","tid":2}""".format(evid)
    val res = HttpClient.Json("%s%s".format(url, addrEventConsole), new JSONObject(query), List(("Cookie", cookie)), acceptUntrustedSSL)
    return checkResult(res)
  }

}


object HttpClient {
  val charset = "UTF-8"

  private def urlEncode(str: String) =  URLEncoder.encode(str, charset)

  def Json(url: String, data: JSONObject,  headers: List[(String, String)], acceptUntrustedSSL:Boolean = false): Option[(JSONObject, List[(String, String)])] = {
    val postHeader = ("Content-type", "application/json") :: ("Accept", "application/json") :: headers
    val req = Request(url, data.toString, postHeader, acceptUntrustedSSL)
    if (req != None )
      return Some(( new JSONObject(req.get._1), req.get._2 ))
    return None
  }

  def Post(url: String, data: List[(String, String)], headers: List[(String, String)], acceptUntrustedSSL:Boolean = false): Option[(String, List[(String, String)])] = {
    val postHeader = ("Content-type","application/x-www-form-urlencoded") :: headers
    var raw_data = ""
    raw_data = data.foldLeft(raw_data) { (raw_data, x) => raw_data +  x._1 + "=" + x._2 + "&" }
    Request(url, raw_data, postHeader, acceptUntrustedSSL)
  }

  def Get(url: String, data: List[(String, String)], headers: List[(String, String)], acceptUntrustedSSL:Boolean = false): Option[(String, List[(String, String)])] = {
    var raw_data = ""
    raw_data = data.foldLeft(raw_data) { (raw_data, x) => raw_data +  urlEncode(x._1) + "=" + urlEncode(x._2) + "&" }

    Request("%s?%s".format(url, raw_data), "", headers, acceptUntrustedSSL)
  }

  private def retry[T](n: Int)(fn: => T): T = {
    try {
      fn
    } catch {
      case e =>
        if (n > 1) retry(n - 1)(fn)
        else throw e
    }
  }

  def Request(url: String, data: String, headers: List[(String, String)], acceptUntrustedSSL:Boolean = false): Option[(String, List[(String, String)])] = {
    try
    {
      val httpclient = if (acceptUntrustedSSL == false){
        new DefaultHttpClient()
      }else {
        Log.d("asked me to accpet untrusted SSL connection")
        UntrustedHttpsClient.createHttpClient
      }
      Log.d("HttpClient.Request: I'm removing http redirect handler ")
      httpclient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false)
      val httpPostRequest = new HttpPost(url)
      val se = new StringEntity(data)

      httpPostRequest.setEntity(se);
      headers foreach {
        case(hname, hvalue) => httpPostRequest.setHeader(hname, hvalue)
      }
      httpPostRequest.setHeader("Accept-Encoding", "gzip")

      Log.d("HttpClient.Request: Executing Request")
      //TODO: set timeout
      //retry for sake of Android 2.3 owners http://goo.gl/uikUJ
      val response = retry(4) {
        httpclient.execute(httpPostRequest).asInstanceOf[HttpResponse]
      }
      Log.d("HttpClient.Request: I've got some hot data from remote server")

      //Get headers
      var resHeader: List[(String,String)] = List()

      Log.d("HttpClient.Request: I'm getting headers")
      response.getAllHeaders foreach {
        case(header) =>
        resHeader ::= (header.getName.toString , header.getValue.toString)
      }
      resHeader ::= ("StatusCode", response.getStatusLine().getStatusCode().toString)

      //Get body
      Log.d("HttpClient.Request: I'm reading body")
      val entity = response.getEntity()
      var instream = entity.getContent()
      if (response.containsHeader("Content-Encoding") == true && response.getFirstHeader("Content-Encoding").getValue == "gzip"){
        Log.d("HttpClient.Request: Opps! it's ziped, no worry I have my tools to decode it")
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
        e.printStackTrace();
        throw e
    }
    return None
  }
}

object exten {
implicit def d(s: String) = new JSONObject(s)
}
// vim: set ts=2 sw=2 et:
