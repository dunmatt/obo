package com.github.dunmatt.obo.android.components.examples.createVoiceControl

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.{ Button, ListView }
import org.slf4j.LoggerFactory

class VoiceControlActivity extends Activity with View.OnClickListener {
  implicit val context = this
  protected val log = LoggerFactory.getLogger(getClass)

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    // val vh: TypedViewHolder.main = TypedViewHolder.setContentView(this, TR.layout.voice_control_main)
    val speakButton = findViewById(R.id.speak_button).asInstanceOf[Button]
    // vh.speak_button.setOnClickListener(this)
  }
}

object VoiceControlActivity {
  val VOICE_RECOGNITION_REQUEST_CODE = 1234
}
