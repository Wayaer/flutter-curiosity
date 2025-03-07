import CoreLocation
import Flutter

public class CuriosityPlugin: NSObject, FlutterPlugin {
    var channel: FlutterMethodChannel

    var keyboardStatus = false

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "Curiosity", binaryMessenger: registrar.messenger())
        let plugin = CuriosityPlugin(channel)
        registrar.addMethodCallDelegate(plugin, channel: channel)
    }

    init(_ channel: FlutterMethodChannel) {
        self.channel = channel
        super.init()
        let center = NotificationCenter.default
        center.addObserver(self, selector: #selector(didShow), name: UIResponder.keyboardDidShowNotification, object: nil)
        center.addObserver(self, selector: #selector(didShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        center.addObserver(self, selector: #selector(didHide), name: UIResponder.keyboardWillHideNotification, object: nil)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "exitApp":
            exit(0)
        case "getGPSStatus":
            // 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
            result(CLLocationManager.locationServicesEnabled())
        case "saveBytesImageToGallery":
            let arguments = call.arguments as! [String: Any]
            let bytes = (arguments["bytes"] as! FlutterStandardTypedData).data
            var image = UIImage(data: bytes)
            let quality = arguments["quality"] as? Int
            if image != nil, quality != nil {
                let newImage = image!.jpegData(compressionQuality: CGFloat(quality! / 100))
                if newImage != nil {
                    let newUIImage = UIImage(data: newImage!)
                    if newUIImage != nil {
                        image = newUIImage
                    }
                }
            }
            if image != nil {
                ImageGalleryTools.shared.saveImage(result, image!)
            } else {
                result(false)
            }
        case "saveFilePathToGallery":
            let arguments = call.arguments as! [String: Any]
            let path = arguments["filePath"] as! String
            if ImageGalleryTools.shared.isImageFile(filename: path) {
                ImageGalleryTools.shared.saveImageAtFileUrl(result, path)
            } else {
                if UIVideoAtPathIsCompatibleWithSavedPhotosAlbum(path) {
                    ImageGalleryTools.shared.saveVideo(result, path)
                } else {
                    result(false)
                }
            }
        default:
            result(FlutterMethodNotImplemented)
        }
    }

    public func detachFromEngine(for registrar: FlutterPluginRegistrar) {
        channel.setMethodCallHandler(nil)
    }

    @objc func didShow() {
        if !keyboardStatus {
            keyboardStatus = true
            channel.invokeMethod("keyboardStatus", arguments: keyboardStatus)
        }
    }

    @objc func didHide() {
        if keyboardStatus {
            keyboardStatus = false
            channel.invokeMethod("keyboardStatus", arguments: keyboardStatus)
        }
    }
}
