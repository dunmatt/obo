package com.github.dunmatt.obo.android.components.examples.createVoiceControl

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.{ Button, ListView, Toast }
import com.github.dunmatt.obo.android.components.{ TR, TypedViewHolder }
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

// most of this code taken from http://stackoverflow.com/questions/11798337/how-to-voice-commands-into-an-android-application
class VoiceControlActivity extends Activity with View.OnClickListener {
  import VoiceControlActivity._
  implicit val context = this
  protected val log = LoggerFactory.getLogger(getClass)
  private var vh: TypedViewHolder.voice_control_main = null  // populated first in onCreate

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    log.info("Starting Voice Control Activity +++++++++++++++++++++++++++++++")
    vh = TypedViewHolder.setContentView(this, TR.layout.voice_control_main)
    vh.speak_button.setOnClickListener(this)
  }

  def startVoiceRecognitionActivity: Unit = {
    val intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo")
    startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
      val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
      matches.foreach { word =>
        Toast.makeText(context, word, Toast.LENGTH_LONG).show
      }
    }
  }

  override def onRequestPermissionsResult( code: Int
                                         , permissions: Array[String]
                                         , results: Array[Int]): Unit = {
    if (results(0) == PackageManager.PERMISSION_GRANTED) {
      startVoiceRecognitionActivity
    }
  }

  override def onClick(v: View): Unit = {
    val status = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
    if (status == PackageManager.PERMISSION_GRANTED) {
      startVoiceRecognitionActivity
    } else {
      ActivityCompat.requestPermissions(this, Array(android.Manifest.permission.RECORD_AUDIO), ARBITRARY_INT)
    }
  }
}

object VoiceControlActivity {
  val VOICE_RECOGNITION_REQUEST_CODE = 1234
  val ARBITRARY_INT = 18
}
