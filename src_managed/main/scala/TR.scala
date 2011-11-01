package com.github.slashmili.Zendroid
import android.app.Activity
import android.view.View

case class TypedResource[T](id: Int)
object TR {
  val severity3 = TypedResource[android.widget.TextView](R.id.severity3)
  val txtZenossURL = TypedResource[android.widget.EditText](R.id.txtZenossURL)
  val ok = TypedResource[android.widget.Button](R.id.ok)
  val ok_button = TypedResource[android.widget.Button](R.id.ok_button)
  val tableRow10 = TypedResource[android.widget.TableRow](R.id.tableRow10)
  val widgetconf_match = TypedResource[android.widget.EditText](R.id.widgetconf_match)
  val WidgetBox = TypedResource[android.widget.LinearLayout](R.id.WidgetBox)
  val textView1 = TypedResource[android.widget.TextView](R.id.textView1)
  val tableRow1 = TypedResource[android.widget.TableRow](R.id.tableRow1)
  val textView4 = TypedResource[android.widget.TextView](R.id.textView4)
  val spnOnWarning = TypedResource[android.widget.Spinner](R.id.spnOnWarning)
  val widgetconf_user = TypedResource[android.widget.EditText](R.id.widgetconf_user)
  val spnUpdateEvery = TypedResource[android.widget.Spinner](R.id.spnUpdateEvery)
  val tableRow12 = TypedResource[android.widget.TableRow](R.id.tableRow12)
  val severity3Img = TypedResource[android.widget.ImageView](R.id.severity3Img)
  val spnOnCritical = TypedResource[android.widget.Spinner](R.id.spnOnCritical)
  val tableRow9 = TypedResource[android.widget.TableRow](R.id.tableRow9)
  val editText4 = TypedResource[android.widget.EditText](R.id.editText4)
  val txtZenossUser = TypedResource[android.widget.EditText](R.id.txtZenossUser)
  val textView2 = TypedResource[android.widget.TextView](R.id.textView2)
  val tableRow8 = TypedResource[android.widget.TableRow](R.id.tableRow8)
  val tableRow11 = TypedResource[android.widget.TableRow](R.id.tableRow11)
  val txtMatchDevice = TypedResource[android.widget.EditText](R.id.txtMatchDevice)
  val widget149 = TypedResource[android.widget.TextView](R.id.widget149)
  val severity5 = TypedResource[android.widget.TextView](R.id.severity5)
  val WidgetErrorBox = TypedResource[android.widget.LinearLayout](R.id.WidgetErrorBox)
  val widgetconf_url = TypedResource[android.widget.EditText](R.id.widgetconf_url)
  val tableRow7 = TypedResource[android.widget.TableRow](R.id.tableRow7)
  val severity5Box = TypedResource[android.widget.LinearLayout](R.id.severity5Box)
  val tableRow5 = TypedResource[android.widget.TableRow](R.id.tableRow5)
  val severity4 = TypedResource[android.widget.TextView](R.id.severity4)
  val widgetError = TypedResource[android.widget.TextView](R.id.widgetError)
  val tableLayout1 = TypedResource[android.widget.TableLayout](R.id.tableLayout1)
  val severity3Box = TypedResource[android.widget.LinearLayout](R.id.severity3Box)
  val tableRow3 = TypedResource[android.widget.TableRow](R.id.tableRow3)
  val widgetHolder = TypedResource[android.widget.LinearLayout](R.id.widgetHolder)
  val btnGetLastStatus = TypedResource[android.widget.Button](R.id.btnGetLastStatus)
  val severity4Img = TypedResource[android.widget.ImageView](R.id.severity4Img)
  val textView5 = TypedResource[android.widget.TextView](R.id.textView5)
  val tableRow4 = TypedResource[android.widget.TableRow](R.id.tableRow4)
  val textView6 = TypedResource[android.widget.TextView](R.id.textView6)
  val textView7 = TypedResource[android.widget.TextView](R.id.textView7)
  val severity5Img = TypedResource[android.widget.ImageView](R.id.severity5Img)
  val severity4Box = TypedResource[android.widget.LinearLayout](R.id.severity4Box)
  val widgetconf_pass = TypedResource[android.widget.EditText](R.id.widgetconf_pass)
  val cancel_button = TypedResource[android.widget.Button](R.id.cancel_button)
  val textView9 = TypedResource[android.widget.TextView](R.id.textView9)
  val widgetconf_update = TypedResource[android.widget.Spinner](R.id.widgetconf_update)
  val spnOnError = TypedResource[android.widget.Spinner](R.id.spnOnError)
  val textView3 = TypedResource[android.widget.TextView](R.id.textView3)
  val btnSaveSettings = TypedResource[android.widget.Button](R.id.btnSaveSettings)
  val txtZenossPass = TypedResource[android.widget.EditText](R.id.txtZenossPass)
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
