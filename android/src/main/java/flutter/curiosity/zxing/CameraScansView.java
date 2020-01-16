package flutter.curiosity.zxing;

import android.content.Context;
import android.graphics.ImageFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.Map;

import flutter.curiosity.CuriosityPlugin;
import flutter.curiosity.utils.Utils;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class CameraScansView implements PlatformView, LifecycleOwner, EventChannel.StreamHandler, MethodChannel.MethodCallHandler {
//    private static final String TAG = "CameraScansView";

    private LifecycleRegistry lifecycleRegistry;
    private TextureView textureView;
    private boolean isPlay;
    private EventChannel.EventSink eventSink;
    private long lastCurrentTimestamp = 0L;//最后一次的扫描
    private Preview mPreview;

    CameraScansView(Context context, BinaryMessenger messenger, int i, Object object) {
        Map map = (Map) object;
        isPlay = (Boolean) map.get("isPlay");
//        isPlay = _isPlay == Boolean.TRUE;
        new EventChannel(messenger, CuriosityPlugin.cameraScansView + "_" + i + "/event")
                .setStreamHandler(this);
        MethodChannel methodChannel = new MethodChannel(messenger, CuriosityPlugin.cameraScansView + "_" + i + "/method");
        methodChannel.setMethodCallHandler(this);
        textureView = new TextureView(context);
        lifecycleRegistry = new LifecycleRegistry(this);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(outMetrics);
//        Log.d(TAG, "CameraScansView: " + outMetrics.toString());
        mPreview = buildPreView(outMetrics.widthPixels, outMetrics.heightPixels);
        CameraX.bindToLifecycle(this, mPreview, buildImageAnalysis());

    }

    @Override
    public View getView() {
        if (lifecycleRegistry.getCurrentState() != Lifecycle.State.RESUMED) {
            lifecycleRegistry.markState(Lifecycle.State.RESUMED);
        }
        return textureView;
    }

    @Override
    public void dispose() {
        Log.d("CameraX", "dispose");
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);
        CameraX.unbindAll();
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        this.eventSink = eventSink;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "startScan":
                isPlay = true;
                result.success(null);
                break;
            case "stopScan":
                isPlay = false;
                result.success(null);
                break;
            case "setFlashMode":
                boolean isOpen = methodCall.argument("isOpen");
                mPreview.enableTorch(isOpen);
                result.success(true);
                break;
            case "getFlashMode":
                result.success(mPreview.isTorchOn());
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onCancel(Object o) {
        Log.d("CameraX", "onCancel");
        eventSink = null;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    private Preview buildPreView(int width, int height) {
        PreviewConfig config = new PreviewConfig.Builder()
                .setTargetAspectRatio(Rational.parseRational(width + ":" + height))
                .setTargetResolution(new Size(width, height))
                .build();
        Preview preview = new Preview(config);
        preview.setOnPreviewOutputUpdateListener(output -> {
            if (textureView != null) {
                textureView.setSurfaceTexture(output.getSurfaceTexture());
            }
        });
        return preview;
    }

    private UseCase buildImageAnalysis() {
        ImageAnalysisConfig config = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();

        ImageAnalysis analysis = new ImageAnalysis(config);
        analysis.setAnalyzer(new QRCodeAnalyzer());

        return analysis;
    }


    private class QRCodeAnalyzer implements ImageAnalysis.Analyzer {
        private static final String TAG = "QRCodeAnalyzer";
        private MultiFormatReader reader = new MultiFormatReader();

        @Override
        public void analyze(ImageProxy image, int rotationDegrees) {
            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp - lastCurrentTimestamp >= 1L && isPlay == Boolean.TRUE) {
                if (ImageFormat.YUV_420_888 != image.getFormat()) {
                    Log.d(TAG, "analyze: " + image.getFormat());
                    return;
                }
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] array = new byte[buffer.remaining()];
                buffer.get(array);
                int height = image.getHeight();
                int width = image.getWidth();
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(array,
                        width,
                        height,
                        0,
                        0,
                        width,
                        height,
                        false);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    final Result decode = reader.decode(binaryBitmap);
                    if (decode != null && eventSink != null) {
                        textureView.post(() -> {
                            if (eventSink != null)
                                eventSink.success(Utils.toMap(decode));
                        });
                    }
                } catch (Exception e) {
                    buffer.clear();
//                    Log.d(TAG, "analyze: error ");
                }
                lastCurrentTimestamp = currentTimestamp;
            }
        }
    }
}
