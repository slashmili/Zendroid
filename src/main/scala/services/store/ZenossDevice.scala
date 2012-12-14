package com.github.slashmili.Zendroid.services.store

class ZenossDevice(uid: String, name: String){
  var events:List[Event] = List()
  def getName = name
  def getDeviceType: String = {
    if(uid.contains("Linux")){
        return "linux"
    }else if(uid.contains("Wind")){
        return "windows"
    }else if(uid.contains("Server")){
        return "server"
    }else if(uid.contains("Router")){
        return "router"
    }else if(uid.contains("Switch")){
        return "network"
    }
    return "unknown"
  }

  def countEvents = events.size
  def removeEvent(evid: String) = {
    events = events.filter(_.getEvID != evid)
  }
  def getEvent(id: Int) = events(id)
  def getEvents = events
  def addEvent(evid: String, summary: String, severity: Int, count: Int, eventState: String, firstTime: String, lastTime: String, component: String) ={
    events ::= new Event(evid, summary, severity, count, eventState, firstTime, lastTime, component)
  } 
  def countSeverity(severity: Int) = 
    events.foldLeft(0){
      (sum ,ev) => {
        if (ev.getSeverity== severity)
          sum + 1
        else
          sum
      }
    }

  def countCritical = countSeverity(Severity.CRITICAL)
  def countError    = countSeverity(Severity.ERROR)   
  def countWarning  = countSeverity(Severity.WARNING)

  def cleanEvents = {
    events = List()
  }
}

// vim: set ts=2 sw=2 et:
