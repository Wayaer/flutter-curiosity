package flutter.curiosity

import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel

class CuriosityEvent(binaryMessenger: BinaryMessenger) : EventChannel.StreamHandler {
    private var eventSink: EventChannel.EventSink? = null
    private val eventName = "curiosity/event"
    private var eventChannel: EventChannel? = null
    private val handler = Handler(Looper.getMainLooper())

    init {
        eventChannel = EventChannel(binaryMessenger, eventName)
        eventChannel!!.setStreamHandler(this)
    }


    fun sendEvent(arguments: Any?) {
        handler.post {
            eventSink?.success(arguments)
        }
    }

    fun dispose() {
        eventSink?.endOfStream()
        eventChannel?.setStreamHandler(null)
        eventChannel = null
    }

    override fun onListen(arguments: Any?, event: EventChannel.EventSink?) {
        eventSink = event
    }

    override fun onCancel(arguments: Any?) {
        eventSink?.endOfStream()
        eventSink = null
    }
}