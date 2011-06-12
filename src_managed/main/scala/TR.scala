package com.github.slashmili.Zendroid
import android.app.Activity
import android.view.View

case class TypedResource[T](id: Int)
object TR {
  val severity3Box = TypedResource[android.widget.LinearLayout](R.id.severity3Box)
  val severity3 = TypedResource[android.widget.TextView](R.id.severity3)
  val ok = TypedResource[android.widget.Button](R.id.ok)
  val severity4Img = TypedResource[android.widget.ImageView](R.id.severity4Img)
  val ok_button = TypedResource[android.widget.Button](R.id.ok_button)
  val widgetconf_match = TypedResource[android.widget.EditText](R.id.widgetconf_match)
  val WidgetBox = TypedResource[android.widget.LinearLayout](R.id.WidgetBox)
  val severity5Img = TypedResource[android.widget.ImageView](R.id.severity5Img)
  val username = TypedResource[android.widget.EditText](R.id.username)
  val severity4Box = TypedResource[android.widget.LinearLayout](R.id.severity4Box)
  val widgetconf_user = TypedResource[android.widget.EditText](R.id.widgetconf_user)
  val url = TypedResource[android.widget.EditText](R.id.url)
  val cancel = TypedResource[android.widget.Button](R.id.cancel)
  val widgetconf_pass = TypedResource[android.widget.EditText](R.id.widgetconf_pass)
  val password = TypedResource[android.widget.EditText](R.id.password)
  val severity3Img = TypedResource[android.widget.ImageView](R.id.severity3Img)
  val cancel_button = TypedResource[android.widget.Button](R.id.cancel_button)
  val widgetconf_update = TypedResource[android.widget.Spinner](R.id.widgetconf_update)
  val widgetconf_url = TypedResource[android.widget.EditText](R.id.widgetconf_url)
  val WidgetErrorBox = TypedResource[android.widget.LinearLayout](R.id.WidgetErrorBox)
  val severity5 = TypedResource[android.widget.TextView](R.id.severity5)
  val widget149 = TypedResource[android.widget.TextView](R.id.widget149)
  val severity5Box = TypedResource[android.widget.LinearLayout](R.id.severity5Box)
  val every = TypedResource[android.widget.Spinner](R.id.every)
  val severity4 = TypedResource[android.widget.TextView](R.id.severity4)
  val widgetError = TypedResource[android.widget.TextView](R.id.widgetError)
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
