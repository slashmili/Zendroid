package com.github.slashmili.Zendroid
import android.app.Activity
import android.view.View

case class TypedResource[T](id: Int)
object TR {
  val textView11 = TypedResource[android.widget.TextView](R.id.textView11)
  val severity3Box = TypedResource[android.widget.LinearLayout](R.id.severity3Box)
  val severity3 = TypedResource[android.widget.TextView](R.id.severity3)
  val txtZenossURL = TypedResource[android.widget.EditText](R.id.txtZenossURL)
  val ok = TypedResource[android.widget.Button](R.id.ok)
  val tableRow3 = TypedResource[android.widget.TableRow](R.id.tableRow3)
  val widgetHolder = TypedResource[android.widget.LinearLayout](R.id.widgetHolder)
  val severity4Img = TypedResource[android.widget.ImageView](R.id.severity4Img)
  val textView5 = TypedResource[android.widget.TextView](R.id.textView5)
  val tableRow4 = TypedResource[android.widget.TableRow](R.id.tableRow4)
  val tableRow10 = TypedResource[android.widget.TableRow](R.id.tableRow10)
  val textView6 = TypedResource[android.widget.TextView](R.id.textView6)
  val WidgetBox = TypedResource[android.widget.LinearLayout](R.id.WidgetBox)
  val textView1 = TypedResource[android.widget.TextView](R.id.textView1)
  val textView7 = TypedResource[android.widget.TextView](R.id.textView7)
  val tableRow1 = TypedResource[android.widget.TableRow](R.id.tableRow1)
  val severity5Img = TypedResource[android.widget.ImageView](R.id.severity5Img)
  val textView4 = TypedResource[android.widget.TextView](R.id.textView4)
  val textView10 = TypedResource[android.widget.TextView](R.id.textView10)
  val spnOnWarning = TypedResource[android.widget.Spinner](R.id.spnOnWarning)
  val severity4Box = TypedResource[android.widget.LinearLayout](R.id.severity4Box)
  val tableRow12 = TypedResource[android.widget.TableRow](R.id.tableRow12)
  val spnUpdateEvery = TypedResource[android.widget.Spinner](R.id.spnUpdateEvery)
  val spnOnCritical = TypedResource[android.widget.Spinner](R.id.spnOnCritical)
  val severity3Img = TypedResource[android.widget.ImageView](R.id.severity3Img)
  val chkInvalidSSL = TypedResource[android.widget.CheckBox](R.id.chkInvalidSSL)
  val tableRow9 = TypedResource[android.widget.TableRow](R.id.tableRow9)
  val editText4 = TypedResource[android.widget.EditText](R.id.editText4)
  val textView9 = TypedResource[android.widget.TextView](R.id.textView9)
  val txtZenossUser = TypedResource[android.widget.EditText](R.id.txtZenossUser)
  val tableRow8 = TypedResource[android.widget.TableRow](R.id.tableRow8)
  val textView2 = TypedResource[android.widget.TextView](R.id.textView2)
  val tableRow11 = TypedResource[android.widget.TableRow](R.id.tableRow11)
  val spnOnError = TypedResource[android.widget.Spinner](R.id.spnOnError)
  val textView3 = TypedResource[android.widget.TextView](R.id.textView3)
  val btnSaveSettings = TypedResource[android.widget.Button](R.id.btnSaveSettings)
  val txtMatchDevice = TypedResource[android.widget.EditText](R.id.txtMatchDevice)
  val WidgetErrorBox = TypedResource[android.widget.LinearLayout](R.id.WidgetErrorBox)
  val severity5 = TypedResource[android.widget.TextView](R.id.severity5)
  val tableRow7 = TypedResource[android.widget.TableRow](R.id.tableRow7)
  val txtZenossPass = TypedResource[android.widget.EditText](R.id.txtZenossPass)
  val severity5Box = TypedResource[android.widget.LinearLayout](R.id.severity5Box)
  val tableRow5 = TypedResource[android.widget.TableRow](R.id.tableRow5)
  val severity4 = TypedResource[android.widget.TextView](R.id.severity4)
  val widgetError = TypedResource[android.widget.TextView](R.id.widgetError)
  val tableRow2 = TypedResource[android.widget.TableRow](R.id.tableRow2)
  val textView8 = TypedResource[android.widget.TextView](R.id.textView8)
  val tableLayout1 = TypedResource[android.widget.TableLayout](R.id.tableLayout1)
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
