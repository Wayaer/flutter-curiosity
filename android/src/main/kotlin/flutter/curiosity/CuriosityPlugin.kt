package flutter.curiosity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.NonNull
import com.luck.picture.lib.config.PictureConfig
import flutter.curiosity.gallery.PicturePicker
import flutter.curiosity.scanner.CameraTools
import flutter.curiosity.scanner.ScannerTools
import flutter.curiosity.scanner.ScannerView
import flutter.curiosity.tools.AppInfo
import flutter.curiosity.tools.FileTools
import flutter.curiosity.tools.NativeTools
import flutter.curiosity.tools.Tools
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import io.flutter.view.TextureRegistry
import java.io.File


/**
 * CuriosityPlugin
 */
class CuriosityPlugin : MethodCallHandler, ActivityAware, FlutterPlugin, ActivityResultListener {
    private lateinit var curiosityChannel: MethodChannel
    private var scannerView: ScannerView? = null
    private lateinit var registry: TextureRegistry
    private lateinit var eventChannel: EventChannel

    companion object {
        var openSystemGalleryCode = 100
        var openSystemCameraCode = 101
        lateinit var context: Context
        lateinit var call: MethodCall
        lateinit var activity: Activity
        lateinit var channelResult: MethodChannel.Result
    }

    override fun onAttachedToEngine(@NonNull plugin: FlutterPluginBinding) {
        val curiosity = "Curiosity"

        curiosityChannel = MethodChannel(plugin.binaryMessenger, curiosity)
        curiosityChannel.setMethodCallHandler(this)
        context = plugin.applicationContext
        registry = plugin.textureRegistry
        eventChannel = EventChannel(plugin.binaryMessenger, "$curiosity/event")

    }

    ///主要是用于获取当前flutter页面所处的Activity.
    override fun onAttachedToActivity(plugin: ActivityPluginBinding) {
        activity = plugin.activity
        plugin.addActivityResultListener(this)
    }

    ///主要是用于获取当前flutter页面所处的Activity.
    override fun onDetachedFromActivity() {
    }

    ///Activity注销时
    override fun onReattachedToActivityForConfigChanges(plugin: ActivityPluginBinding) {
        plugin.removeActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        curiosityChannel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        curiosityChannel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
    }

    ///主要用于接收Flutter端对原生方法调用的实现.
    override fun onMethodCall(_call: MethodCall, _result: MethodChannel.Result) {
        channelResult = _result
        call = _call
        scanner()
        gallery()
        tools()
    }

    private fun tools() {
        when (call.method) {
            "installApp" -> channelResult.success(NativeTools.installApp())
            "getFilePathSize" -> channelResult.success(NativeTools.getFilePathSize())
            "unZipFile" -> channelResult.success(FileTools.unZipFile())
            "callPhone" -> channelResult.success(NativeTools.callPhone())
            "goToMarket" -> channelResult.success(NativeTools.goToMarket())
            "isInstallApp" -> channelResult.success(NativeTools.isInstallApp())
            "exitApp" -> NativeTools.exitApp()
            "getAppInfo" -> channelResult.success(AppInfo.getAppInfo())
            "systemShare" -> channelResult.success(NativeTools.systemShare())
            "getGPSStatus" -> channelResult.success(NativeTools.getGPSStatus())
            "jumpGPSSetting" -> NativeTools.jumpGPSSetting()
        }
    }

    private fun gallery() {
        when (call.method) {
            "openPicker" -> PicturePicker.openPicker()
            "openCamera" -> PicturePicker.openCamera()
            "deleteCacheDirFile" -> PicturePicker.deleteCacheDirFile()
            "openSystemGallery" -> NativeTools.openSystemGallery()
            "openSystemCamera" -> NativeTools.openSystemCamera()
        }
    }


    private fun scanner() {
        when (call.method) {
            "scanImagePath" -> ScannerTools.scanImagePath()
            "scanImageUrl" -> ScannerTools.scanImageUrl()
            "scanImageMemory" -> ScannerTools.scanImageMemory()
            "availableCameras" ->
                channelResult.success(CameraTools.getAvailableCameras(activity))
            "initializeCameras" -> {
                scannerView = ScannerView(registry.createSurfaceTexture())
                eventChannel.setStreamHandler(scannerView)
                scannerView!!.initCameraView()
            }
            "setFlashMode" -> {
                val status = call.argument<Boolean>("status")
                scannerView?.enableTorch(status === java.lang.Boolean.TRUE)
                channelResult.success("setFlashMode")
            }
            "disposeCameras" -> {
                scannerView?.dispose()
                eventChannel.setStreamHandler(null)
                channelResult.success("dispose")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PictureConfig.REQUEST_CAMERA || requestCode == PictureConfig.CHOOSE_REQUEST) {
                channelResult.success(PicturePicker.onResult(requestCode, intent))
            }
            if (requestCode == openSystemGalleryCode) {
                val uri: Uri? = intent?.data
                channelResult.success(Tools.getRealPathFromURI(uri));
            }
            if (requestCode == openSystemCameraCode) {
                val photoPath: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path.toString() + "/temp.JPG"
                } else {
                    intent?.data?.encodedPath.toString();
                }
                channelResult.success(photoPath);
            }
        }
        return true
    }


}

