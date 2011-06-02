package com.github.slashmili.Zendroid.utils

import java.io._
import java.net._
class ZenossAPI (url: String) {
    val addrLogin="/zport/acl_users/cookieAuthHelper/login"   
    val charset = "UTF-8"
    var cookie  = ""

    def auth(username: String, password: String) = {
        val data = "__ac_name=%s&__ac_password=%s&submitted=true&came_from=%s/zport/dmd" format(username, password, url)
        val headers = List(("content-type","application/x-www-form-urlencoded"))
        val conn = request("%s%s".format(url,addrLogin),data, headers ,"POST");
        cookie = conn.getHeaderField("Set-Cookie");
    }


    def authCheck : Boolean = {
        val headers = List(("content-type","application/x-www-form-urlencoded"), ("Cookie",cookie))
        val conn = request("%s%s".format(url,"/zport/dmd"),"",headers, "POST")
        if(conn.getResponseCode == 200)
            return true
        return false
    }

    private def readAsString(conn: HttpURLConnection)  = {
        val is = conn.getInputStream()
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

    private def request(url :String,data: String, headers: List[(String,String)],  method: String) = {
        var urlObj =  new URL(url);
        var conn   = urlObj.openConnection.asInstanceOf[HttpURLConnection]
        headers.reverse.foreach{
            case (name, value) =>
            conn.setRequestProperty(name, value)
        }
        conn.setDoOutput(true)
        conn.setInstanceFollowRedirects(false)
        conn.connect
        conn.getOutputStream.write(data.getBytes(charset))
        conn
    }
}

private class ZenossLogin {

}
