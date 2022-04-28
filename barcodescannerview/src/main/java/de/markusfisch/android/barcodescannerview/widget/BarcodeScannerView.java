package de.markusfisch.android.barcodescannerview.widget;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;

import java.util.HashSet;
import java.util.List;

import de.markusfisch.android.cameraview.widget.CameraView;
import de.markusfisch.android.zxingcpp.ZxingCpp;
import de.markusfisch.android.zxingcpp.ZxingCpp.Format;
import de.markusfisch.android.zxingcpp.ZxingCpp.Result;

public class BarcodeScannerView extends CameraView {
	public interface OnBarcodeListener {
		boolean onBarcodeRead(String result);
	}

	public final Rect cropRect = new Rect();
	public final HashSet<Format> formats = new HashSet<>();

	private OnBarcodeListener onBarcodeListener;
	private boolean decoding = true;

	public BarcodeScannerView(Context context) {
		super(context);
		init();
	}

	public BarcodeScannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BarcodeScannerView(
			Context context,
			AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void setOnBarcodeListener(OnBarcodeListener listener) {
		onBarcodeListener = listener;
	}

	public void setDecoding(boolean enable) {
		decoding = enable;
	}

	public boolean isDecoding() {
		return decoding;
	}

	private void init() {
		formats.add(Format.QR_CODE);
		setUseOrientationListener(true);
		setOnCameraListener(new OnCameraListener() {
			@Override
			public void onConfigureParameters(Camera.Parameters parameters) {
				List<String> modes = parameters.getSupportedSceneModes();
				if (modes != null) {
					for (String mode : modes) {
						if (Camera.Parameters.SCENE_MODE_BARCODE.equals(mode)) {
							parameters.setSceneMode(mode);
							break;
						}
					}
				}
				setAutoFocus(parameters);
			}

			@Override
			public void onCameraError() {
			}

			@Override
			public void onCameraReady(Camera camera) {
				int frameWidth = getFrameWidth();
				int frameHeight = getFrameHeight();
				int yStride = (int) Math.ceil(frameWidth / 16.0) * 16;
				cropRect.set(0, 0, frameWidth, frameHeight);
				camera.setPreviewCallback((data, camera1) -> {
					if (decoding) {
						Result result = ZxingCpp.INSTANCE.readByteArray(
								data,
								yStride,
								cropRect,
								0,
								formats,
								false,
								true);
						if (result != null && onBarcodeListener != null) {
							decoding = onBarcodeListener.onBarcodeRead(
									result.getText());
						}
					}
				});
			}

			@Override
			public void onPreviewStarted(Camera camera) {
			}

			@Override
			public void onCameraStopping(Camera camera) {
				camera.setPreviewCallback(null);
			}
		});
	}
}
