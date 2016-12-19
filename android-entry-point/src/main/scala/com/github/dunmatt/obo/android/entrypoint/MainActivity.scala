package com.github.dunmatt.obo.android.entrypoint

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.{ AdapterView, ArrayAdapter, TextView, Toast }
import com.github.dunmatt.obo.core.{ Component, Constants }
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import scala.io.Source

class MainActivity extends AppCompatActivity {
  // allows accessing `.value` on TR.resource.constants
  implicit val context = this
  protected val log = LoggerFactory.getLogger(getClass)

  def friendlyName(s: String): String = {
    val lastDot = s.lastIndexOf('.')
    val prevDot = s.lastIndexOf('.', lastDot - 1)
    s.substring(prevDot + 1)
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    val vh: TypedViewHolder.main = TypedViewHolder.setContentView(this, TR.layout.main)
    // vh.text.setText(s"Hello world, from ${TR.string.app_name.value}")

    val components = Source.fromInputStream(getAssets.open(classOf[Component].getName)).getLines.toArray
    val componentAdapter = new ArrayAdapter(context, R.layout.component_list_item, components.map(friendlyName))
    // try {
    val intent = new Intent(context, classOf[AndroidComponentService])
    vh.component_list.setOnItemClickListener(new AdapterView.OnItemClickListener {
      def onItemClick(lv: AdapterView[_], v: View, position: Int, id: Long): Unit = {
        intent.putExtra(Constants.COMPONENT_NAME_KEY, components(position))
        startService(intent)
      }
    })
    // } catch {
    //   case e: Throwable => Toast.makeText(context, s"$e", Toast.LENGTH_LONG).show
    // }
    vh.component_list.setAdapter(componentAdapter)
  }

  override def onDestroy: Unit = {
    super.onDestroy
    log.info("MainActivity is being destroyed!")
    stopService(new Intent(this, classOf[AndroidComponentService]))
  }
}
