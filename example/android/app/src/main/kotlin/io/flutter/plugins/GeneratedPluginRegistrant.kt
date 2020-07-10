package io.flutter.plugins

import com.baseflow.permissionhandler.PermissionHandlerPlugin
import com.rhyme.r_scan.RScanPlugin
import flutter.curiosity.CuriosityPlugin
import io.flutter.embedding.engine.FlutterEngine

/**
 * Generated file. Do not edit.
 * This file is generated by the Flutter tool based on the
 * plugins that support the Android platform.
 */
object GeneratedPluginRegistrant {
    fun registerWith(flutterEngine: FlutterEngine) {
        flutterEngine.plugins.add(CuriosityPlugin())
        flutterEngine.plugins.add(PermissionHandlerPlugin())
        flutterEngine.plugins.add(RScanPlugin())
    }
}