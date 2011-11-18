package com.github.slashmili.Zendroid
import android.app.Activity
import android.view.View

case class TypedResource[T](id: Int)
object TR {
  val listViewseverity3 = TypedResource[android.widget.TextView](R.id.listViewseverity3)
  val severity3 = TypedResource[android.widget.TextView](R.id.severity3)
  val txtZenossURL = TypedResource[android.widget.EditText](R.id.txtZenossURL)
  val ok = TypedResource[android.widget.Button](R.id.ok)
  val linearLayout1 = TypedResource[android.widget.LinearLayout](R.id.linearLayout1)
  val tableRow10 = TypedResource[android.widget.TableRow](R.id.tableRow10)
  val listViewerrorBOXSeverity3 = TypedResource[android.widget.LinearLayout](R.id.listViewerrorBOXSeverity3)
  val WidgetBox = TypedResource[android.widget.LinearLayout](R.id.WidgetBox)
  val textView1 = TypedResource[android.widget.TextView](R.id.textView1)
  val tableRow1 = TypedResource[android.widget.TableRow](R.id.tableRow1)
  val textView4 = TypedResource[android.widget.TextView](R.id.textView4)
  val spnOnWarning = TypedResource[android.widget.Spinner](R.id.spnOnWarning)
  val spnUpdateEvery = TypedResource[android.widget.Spinner](R.id.spnUpdateEvery)
  val tableRow12 = TypedResource[android.widget.TableRow](R.id.tableRow12)
  val severity3Img = TypedResource[android.widget.ImageView](R.id.severity3Img)
  val spnOnCritical = TypedResource[android.widget.Spinner](R.id.spnOnCritical)
  val tableRow9 = TypedResource[android.widget.TableRow](R.id.tableRow9)
  val chkInvalidSSL = TypedResource[android.widget.CheckBox](R.id.chkInvalidSSL)
  val listViewerrorBOXSeverity5 = TypedResource[android.widget.LinearLayout](R.id.listViewerrorBOXSeverity5)
  val listViewSeverity4Img = TypedResource[android.widget.ImageView](R.id.listViewSeverity4Img)
  val listViewSeverity5Img = TypedResource[android.widget.ImageView](R.id.listViewSeverity5Img)
  val editText4 = TypedResource[android.widget.EditText](R.id.editText4)
  val txtZenossUser = TypedResource[android.widget.EditText](R.id.txtZenossUser)
  val textView2 = TypedResource[android.widget.TextView](R.id.textView2)
  val tableRow8 = TypedResource[android.widget.TableRow](R.id.tableRow8)
  val tableRow11 = TypedResource[android.widget.TableRow](R.id.tableRow11)
  val deviceErrorListView = TypedResource[android.widget.ExpandableListView](R.id.deviceErrorListView)
  val txtMatchDevice = TypedResource[android.widget.EditText](R.id.txtMatchDevice)
  val WidgetErrorBox = TypedResource[android.widget.LinearLayout](R.id.WidgetErrorBox)
  val severity5 = TypedResource[android.widget.TextView](R.id.severity5)
  val tableRow7 = TypedResource[android.widget.TableRow](R.id.tableRow7)
  val severity5Box = TypedResource[android.widget.LinearLayout](R.id.severity5Box)
  val listViewseverity5 = TypedResource[android.widget.TextView](R.id.listViewseverity5)
  val tableRow5 = TypedResource[android.widget.TableRow](R.id.tableRow5)
  val txtEventConsoleLastTime = TypedResource[android.widget.TextView](R.id.txtEventConsoleLastTime)
  val severity4 = TypedResource[android.widget.TextView](R.id.severity4)
  val widgetError = TypedResource[android.widget.TextView](R.id.widgetError)
  val tableLayout1 = TypedResource[android.widget.TableLayout](R.id.tableLayout1)
  val textView11 = TypedResource[android.widget.TextView](R.id.textView11)
  val severity3Box = TypedResource[android.widget.LinearLayout](R.id.severity3Box)
  val tableRow3 = TypedResource[android.widget.TableRow](R.id.tableRow3)
  val listViewErrorIcon = TypedResource[android.widget.ImageView](R.id.listViewErrorIcon)
  val widgetHolder = TypedResource[android.widget.LinearLayout](R.id.widgetHolder)
  val listViewErrorBOX = TypedResource[android.widget.LinearLayout](R.id.listViewErrorBOX)
  val severity4Img = TypedResource[android.widget.ImageView](R.id.severity4Img)
  val btnDeleteAccount = TypedResource[android.widget.Button](R.id.btnDeleteAccount)
  val textView5 = TypedResource[android.widget.TextView](R.id.textView5)
  val tableRow4 = TypedResource[android.widget.TableRow](R.id.tableRow4)
  val textView6 = TypedResource[android.widget.TextView](R.id.textView6)
  val textView7 = TypedResource[android.widget.TextView](R.id.textView7)
  val severity5Img = TypedResource[android.widget.ImageView](R.id.severity5Img)
  val severity4Box = TypedResource[android.widget.LinearLayout](R.id.severity4Box)
  val listViewerrorBOXSeverity4 = TypedResource[android.widget.LinearLayout](R.id.listViewerrorBOXSeverity4)
  val tvChild = TypedResource[android.widget.TextView](R.id.tvChild)
  val textView9 = TypedResource[android.widget.TextView](R.id.textView9)
  val listViewSeverity3Img = TypedResource[android.widget.ImageView](R.id.listViewSeverity3Img)
  val listViewServerName = TypedResource[android.widget.TextView](R.id.listViewServerName)
  val spnOnError = TypedResource[android.widget.Spinner](R.id.spnOnError)
  val textView3 = TypedResource[android.widget.TextView](R.id.textView3)
  val btnSaveSettings = TypedResource[android.widget.Button](R.id.btnSaveSettings)
  val txtZenossPass = TypedResource[android.widget.EditText](R.id.txtZenossPass)
  val LinearLayout01 = TypedResource[android.widget.LinearLayout](R.id.LinearLayout01)
  val listViewseverity4 = TypedResource[android.widget.TextView](R.id.listViewseverity4)
  val tableRow2 = TypedResource[android.widget.TableRow](R.id.tableRow2)
  val textView8 = TypedResource[android.widget.TextView](R.id.textView8)
}
trait TypedViewHolder {
  def view: View
  def findView[T](tr: TypedResource[T]) = view.findViewById(tr.id).asInstanceOf[T]  
}
trait TypedView extends View with TypedViewHolder { def view = this }
trait TypedActivityHolder {
  def activity: Activity
  def findView[T](tr: TypedResource[T]) = activity.findViewById(tr.id).asInstanceOf[T]
}
trait TypedActivity extends Activity with TypedActivityHolder { def activity = this }
object TypedResource {
  implicit def view2typed(v: View) = new TypedViewHolder { def view = v }
  implicit def activity2typed(act: Activity) = new TypedActivityHolder { def activity = act }
}
