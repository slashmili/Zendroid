package com.github.slashmili.Zendroid.services.store

class Event (evid: String, summary: String, severity: Int, count: Int, var eventState: String, firstTime: String, lastTime: String, component: String){
  def getSeverity = severity
  def getSummary  = summary
  def getEvID     = evid
  def getCount    = count
  def getEventState = eventState
  def setEventState(s: String) = { eventState = s }
  def getFirstTime = firstTime
  def getLastTime = lastTime
  def getComponent = component
}

// vim: set ts=2 sw=2 et:
