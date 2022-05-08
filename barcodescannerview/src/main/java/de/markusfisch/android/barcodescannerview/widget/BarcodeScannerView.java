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

	public interface OnSetCropRectListener {
		void onSetCropRect(Rect cropRect);
	}

	public final HashSet<Format> formats = new HashSet<>();

	private final Rect cropRect = new Rect();

	private OnBarcodeListener onBarcodeListener;
	private OnSetCropRectListener onSetCropRectListener;
	private OverlayView overlayView;
	private boolean decoding = true;
	private boolean useOverlay = true;

	public BarcodeScannerView(Context context) {
		super(context);
		init(context);
	}

	public BarcodeScannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public BarcodeScannerView(
			Context context,
			AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public void setOnBarcodeListener(OnBarcodeListener listener) {
		onBarcodeListener = listener;
	}

	public void setOnSetCropRectListener(OnSetCropRectListener listener) {
		onSetCropRectListener = listener;
	}

	public void setDecoding(boolean enable) {
		decoding = enable;
	}

	public boolean isDecoding() {
		return decoding;
	}

	public void setUseOverlay(boolean enable) {
		useOverlay = enable;
	}

	public boolean useOverlay() {
		return useOverlay;
	}

	private void init(Context context) {
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
				int frameOrientation = getFrameOrientation();
				cropRect.set(0, 0, frameWidth, frameHeight);
				if (onSetCropRectListener != null) {
					onSetCropRectListener.onSetCropRect(cropRect);
				}
				if (useOverlay) {
					overlayView = new OverlayView(context);
					overlayView.updateTransformationMatrix(
							frameWidth,
							frameHeight,
							frameOrientation,
							previewRect,
							cropRect);
					addView(overlayView);
					overlayView.layout(
							previewRect.left,
							previewRect.top,
							previewRect.right,
							previewRect.bottom);
				}
				int yStride = (int) Math.ceil(frameWidth / 16.0) * 16;
				camera.setPreviewCallback((data, camera1) -> {
					if (!decoding) {
						return;
					}
					Result result = ZxingCpp.INSTANCE.readByteArray(
							data,
							yStride,
							cropRect,
							frameOrientation,
							formats,
							false,
							true,
							false);
					if (result == null) {
						return;
					}
					if (overlayView != null) {
						overlayView.show(result.getPosition());
					}
					if (onBarcodeListener == null) {
						return;
					}
					decoding = onBarcodeListener.onBarcodeRead(
							result.getText());
				});
			}

			@Override
			public void onPreviewStarted(Camera camera) {
			}

			@Override
			public void onCameraStopping(Camera camera) {
				camera.setPreviewCallback(null);
				overlayView = null;
			}
		});
	}
}
