import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_curiosity/scan/ScanResult.dart';

/// qr scan view controller .
/// can startScan or stopScan .
const scanView = 'scanView';

class ScanController extends ChangeNotifier {
  Stream stream;
  StreamSubscription subscription;
  ScanResult result;
  EventChannel channel;
  bool isPlay;
  MethodChannel methodChannel;

  ScanController({this.isPlay: true})
      : assert(isPlay != null),
        super();

  void attach(int id) {
    channel = EventChannel('${scanView}_$id/event');
    methodChannel = MethodChannel('${scanView}_$id/method');
    stream = channel.receiveBroadcastStream(
      {
        "isPlay": isPlay,
      },
    );
    subscription = stream.listen((data) {
      this.result = ScanResult.formMap(data);
      notifyListeners();
    });
  }

  //开始扫描
  Future<void> startScan() async {
    await methodChannel.invokeMethod('startScan');
  }

  //停止扫描
  Future<void> stopScan() async {
    await methodChannel.invokeMethod('stopScan');
  }

  /// flash mode open or close.
  ///
  /// [isOpen] if false will close flash mode.
  ///
  /// It will return is success.
  Future<bool> setFlashMode(bool isOpen) async => await methodChannel.invokeMethod('setFlashMode', {
        'isOpen': isOpen,
      });

  /// flash mode open or close.
  ///
  /// [isOpen] if false will close flash mode.
  ///
  /// It will return is success.
  Future<bool> getFlashMode() async => await methodChannel.invokeMethod('getFlashMode');

  void detach() {
    subscription?.cancel();
    notifyListeners();
  }
}
