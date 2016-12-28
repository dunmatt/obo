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
import com.github.dunmatt.obo.core.Constants
import com.github.dunmatt.obo.android.components.{ TR, TypedViewHolder }
import java.util.UUID
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import scala.collection.JavaConversions._

// most of this code taken from http://stackoverflow.com/questions/11798337/how-to-voice-commands-into-an-android-application
class VoiceControlActivity extends Activity with View.OnClickListener {
  import VoiceControlActivity._
  import CreateVoiceControlComponent._
  implicit val context = this
  protected val log = LoggerFactory.getLogger(getClass)
  private val killSignal = zctx.socket(ZMQ.PULL)
  private val socket = zctx.socket(ZMQ.PUSH)
  socket.setSendTimeOut(500)

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    val componentId = getIntent.getStringExtra(Constants.COMPONENT_ID_KEY)
    // connect back to the component via inproc
    new Thread(new Runnable {
      def run {
        socket.connect(s"inproc://$componentId")
        log.info(s"Connecting to inproc://$componentId")
        killSignal.connect(s"inproc://$componentId/kill")
        killSignal.recv  // this is a blocking call until the kill signal is sent
        killSignal.close
        socket.close
        log.info("Stopping VoiceControlActivity")
        context.finish  // closes this activity
      }
    }).start
    
    val vh = TypedViewHolder.setContentView(this, TR.layout.voice_control_main)
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
      new Thread(new Runnable {
        def run {
          matches.foreach { word =>
            log.info(s"""Sending: "$word" back to the component.""")
            socket.send(word)
          }
        }
      }).start
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
