package com.github.slashmili.Zendroid.utils

import java.io._
import java.net._
import _root_.android.util.Log
import scala.util.parsing.json.JSON._
import com.devstream.http._
import org.json.JSONObject

import scala.util.matching.Regex

class ZenossAPI (url: String) {
    val addrLogin="/zport/acl_users/cookieAuthHelper/login"   
    val charset = "UTF-8"
    var cookie  = ""

    def auth(username: String, password: String) = {
        val data = "__ac_name=%s&__ac_password=%s&submitted=true&came_from=%s/zport/dmd" format(username, password, url)
        val headers = List(("content-type","application/x-www-form-urlencoded"))
        val req = new Request("%s%s".format(url,addrLogin),data, headers ,"POST");
        cookie = req.getHeaderField("Set-Cookie");
    }


    def authCheck : Boolean = {
        if(cookie == "")
          return false
        val headers = List(("content-type","application/x-www-form-urlencoded"), ("Cookie",cookie))
        val conn = new Request("%s%s".format(url,"/zport/dmd"),"",headers, "POST")
        if(conn.getResponseCode == 200)
            return true
        return false
    }

    def getEvents : Option[Map[String,String]] = {
        val l = """{"action":"EventsRouter","method":"query","data":[{"start":0,"limit":100,"dir":"DESC","sort":"severity","params":"{\"severity\":[5,4,3],\"eventState\":[0,1]}"}],"type":"rpc","tid":1}"""

        val r5 =new Regex("\"severity\": \"5")
        val r4 =new Regex("\"severity\": \"4")
        val r3 =new Regex("\"severity\": \"3")


        try {
            val res = HttpClient.SendHttpPost("%s%s".format(url,"/zport/dmd/evconsole_router"), new JSONObject(l), cookie )


        return Some(Map("severity5" ->  r5.findAllIn(res).length.toString,
                    "severity4" -> r4.findAllIn(res).length.toString,
                    "severity3" -> r3.findAllIn(res).length.toString
                  ))

        }
        catch {
          case e => return None
        }
        return None
        /* TODO:CHEK WHY IT DOES'NT WORK
        try {
        var headers = List(("Cookie",cookie))
        headers = headers ::: List(("content-type", "application/json"))
        headers = headers ::: List(("Accept-Encoding", "text/plain"))

        val req = new Request("%s%s".format(url,"/zport/dmd/evconsole_router"), l ,headers, "POST")

        if(req.getResponseCode != 200)
            return None

          val res = parseFull(res_str)
          Log.w("******************" ,res.toString)
          if(res == None)
              return None

          val eventsOption = res.get.asInstanceOf[Map[String,Any]].get("result")
          if (eventsOption == None )
              return None

          
          val events = eventsOption.get.asInstanceOf[Map[String,Any]].get("events")
          if ( events == None )
              return None
          return Some(events.get.asInstanceOf[List[Map[String,String]]])
        }catch {
          case e => e.printStackTrace
        }
        return None
        */
    }

}

private class Request (url: String, data: String, headers: List[(String, String)], method: String ) {

    var charset = "UTF-8"
    var urlObj =  new URL(url);
    var connection   = urlObj.openConnection.asInstanceOf[HttpURLConnection]
    headers.reverse.foreach{
        case (name, value) =>
        connection.setRequestProperty(name, value)
    }
    connection.setDoOutput(true)
    connection.setInstanceFollowRedirects(false)
    connection.connect
    connection.getOutputStream.write(data.getBytes(charset))

    def getResponseCode = connection.getResponseCode
    def getHeaderField(hname: String) = connection.getHeaderField(hname)

    def asString  = {
        val is = connection.getInputStream()
        val in = new InputStreamReader(is, charset)
        val bos = new StringBuilder
        val ba = new Array[Char](4096)
        
        def readOnce {
            val len = in.read(ba)
            if (len > 0) bos.appendAll(ba)
            if (len >= 0) readOnce
        }
        readOnce
        bos.toString

    }

}

class ZenossEvent(url:String, cookie: String) {
    def sayhi = println(url + cookie)
}


object pushfunctions {
    implicit def pushfunctions(s: ZenossAPI) = new ZenossEvent("milad", s.cookie)
}
