package com.github.slashmili.Zendroid.utils

import java.util.ArrayList
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.{TextView, ImageView, LinearLayout}

import com.github.slashmili.Zendroid.Services.Store
import com.github.slashmili.Zendroid.R

class CustomDeviceErrorListView (context: Context) extends BaseExpandableListAdapter {

  override def areAllItemsEnabled = true
  
  var devices: List[Store.ZenossDevice] = List()

  def addItem(zenDevice: Store.ZenossDevice) = {
    devices ::= zenDevice
  }

  override def getChild(groupPosition: Int, childPosition: Int) = {
    devices(childPosition)
  }

  override def getChildId(groupPosition: Int, childPosition: Int): Long = {
    childPosition
  }

  override def getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View, parent: ViewGroup): View = {
    var cv = convertView 
    if (convertView == null) {
      val infalInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
      cv = infalInflater.inflate(R.layout.event_error_custom_list_view, null)
    }
    val ev = devices(groupPosition).getEvent(childPosition) 
    val tv = cv.findViewById(R.id.tvChild).asInstanceOf[TextView]
    tv.setText(" "+ ev.getSummary)
    var icon = ev.getSeverity match {
      case 3 => R.drawable.severity3_notify
      case 4 => R.drawable.severity4_notify
      case 5 => R.drawable.severity5_notify
      case _ => 0
    }
    /*
    val ie = cv.findViewById(Ri.id.EventErrorCustomListViewIcon)asInstanceOf[ImageView]
    ie.setImageResource(icon)
    */
    tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    tv.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);

    cv
  }

  override def getChildrenCount(groupPosition: Int) = {
    devices(groupPosition).countEvents
  }

  override def getGroup(groupPosition: Int) = {
    devices(groupPosition)
  }

  override def getGroupCount = {
    devices.size
  }

  override def getGroupId(groupPosition: Int) = {
    groupPosition
  }

  override def getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View, parent: ViewGroup): View ={
    val d = devices(groupPosition)
    var cv = convertView
    if (convertView == null) {
      val infalInflater =context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
      cv = infalInflater.inflate(R.layout.device_error_custom_list_view, null)
    }
    val tv = cv.findViewById(R.id.listViewServerName).asInstanceOf[TextView]
    tv.setText(d.getName)

    val c = d.countCritical
    val tc = cv.findViewById(R.id.listViewseverity5).asInstanceOf[TextView]
    tc.setText(c.toString)
    val lc = cv.findViewById(R.id.listViewerrorBOXSeverity5).asInstanceOf[View]
    if(c == 0)
      lc.getBackground().setAlpha(100)
    else
      lc.getBackground().setAlpha(255)

    val e = d.countError
    val te = cv.findViewById(R.id.listViewseverity4).asInstanceOf[TextView]
    te.setText(e.toString)
    val le = cv.findViewById(R.id.listViewerrorBOXSeverity4).asInstanceOf[View]
    if( e == 0)
      le.getBackground().setAlpha(100)
    else
      le.getBackground().setAlpha(255)

    val w = d.countWarning
    val tw = cv.findViewById(R.id.listViewseverity3).asInstanceOf[TextView]
    tw.setText(w.toString)
    val lw = cv.findViewById(R.id.listViewerrorBOXSeverity3).asInstanceOf[View]
    if(w == 0)
      lw.getBackground().setAlpha(100)
    else
      lw.getBackground().setAlpha(255)

    val di = cv.findViewById(R.id.listViewErrorIcon).asInstanceOf[ImageView]
    val icon = d.getDeviceType match {
      case "linux"   => R.drawable.server_linux
      case "windows" => R.drawable.server_windows
      case "server"  => R.drawable.server
      case "router"  => R.drawable.router
      case "network" => R.drawable.network
      case _         => R.drawable.noicon
    }
    di.setImageResource(icon)
    return cv
  }

  override def hasStableIds = true
  override def isChildSelectable(arg0: Int, arg1: Int) = false
}


// vim: set ts=2 sw=2 et:
