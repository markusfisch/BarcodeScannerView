package de.markusfisch.android.barcodescannerview.widget;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.HashSet;
import java.util.List;

import de.markusfisch.android.cameraview.widget.CameraView;
import de.markusfisch.android.zxingcpp.ZxingCpp;
import de.markusfisch.android.zxingcpp.ZxingCpp.Binarizer;
import de.markusfisch.android.zxingcpp.ZxingCpp.DecodeHints;
import de.markusfisch.android.zxingcpp.ZxingCpp.Format;
import de.markusfisch.android.zxingcpp.ZxingCpp.Result;

public class BarcodeScannerView extends CameraView {
	public interface OnBarcodeListener {
		boolean onBarcodeRead(Result result);
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
	private boolean showOverlay = true;
	private boolean tryRotate = true;
	private boolean tryInvert = true;
	private boolean tryDownscale = true;
	private boolean useLocalAverage = false;
	private float cropRatio = 0f;

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

	/**
	 * Set listener for recognized barcodes.
	 *
	 * @param listener listener for barcodes
	 */
	public void setOnBarcodeListener(OnBarcodeListener listener) {
		onBarcodeListener = listener;
	}

	/**
	 * Set listener to modify the crop rectangle.
	 *
	 * @param listener listener to modify crop rectangle
	 */
	public void setOnSetCropRectListener(OnSetCropRectListener listener) {
		onSetCropRectListener = listener;
	}

	/**
	 * Set if codes that are rotated by 90 degree should be read.
	 * Default is true.
	 *
	 * @param enable true if rotated codes should be read
	 */
	public void setTryRotate(boolean enable) {
		tryRotate = enable;
	}

	/**
	 * Set if inverted codes should be read. Default is true.
	 *
	 * @param enable true if inverted codes should be read
	 */
	public void setTryInvert(boolean enable) {
		tryInvert = enable;
	}

	/**
	 * Set if the camera frame should also be tried downscaled to improve
	 * detection of very big or distorted codes. Default is true.
	 *
	 * @param enable true if inverted codes should be read
	 */
	public void setTryDownscale(boolean enable) {
		tryDownscale = enable;
	}

	/**
	 * Set the ratio between the size of the crop rectangle and the shorter
	 * dimension of the view.
	 *
	 * @param ratio a value from 0 to 1
	 */
	public void setCropRatio(float ratio) {
		cropRatio = Math.max(0f, Math.min(1f, ratio));
	}

	/**
	 * Return current ratio between crop rectangle and view size.
	 */
	public float getCropRatio() {
		return cropRatio;
	}

	/**
	 * Enable or disable decoding barcodes.
	 *
	 * @param enable true if scanning should be active
	 */
	public void setDecoding(boolean enable) {
		decoding = enable;
	}

	/**
	 * Returns true if scanning is active.
	 */
	public boolean isDecoding() {
		return decoding;
	}

	/**
	 * Change visibility of the crop rectangle overlay.
	 *
	 * @param enable true if the crop rectangle should be visible
	 */
	public void setShowOverlay(boolean enable) {
		showOverlay = enable;
	}

	/**
	 * Return true if the crop rectangle is visible.
	 */
	public boolean showOverlay() {
		return showOverlay;
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
				int width = getFrameWidth();
				int height = getFrameHeight();
				int orientation = getFrameOrientation();
				calculateCropRect(width, height);
				if (onSetCropRectListener != null) {
					onSetCropRectListener.onSetCropRect(cropRect);
				}
				if (showOverlay) {
					overlayView = new OverlayView(context);
					overlayView.updateTransformationMatrix(
							width,
							height,
							orientation,
							previewRect,
							cropRect);
					addView(overlayView);
					overlayView.layout(
							previewRect.left,
							previewRect.top,
							previewRect.right,
							previewRect.bottom);
				}
				DecodeHints decodeHints = new DecodeHints();
				decodeHints.setTryRotate(tryRotate);
				decodeHints.setTryInvert(tryInvert);
				decodeHints.setTryDownscale(tryDownscale);
				decodeHints.setMaxNumberOfSymbols(1);
				decodeHints.setFormats(TextUtils.join(",", formats));
				camera.setPreviewCallback((data, camera1) -> {
					if (!decoding) {
						return;
					}
					// By default, ZXing uses LOCAL_AVERAGE, but
					// this does not work well with inverted
					// barcodes on low-contrast backgrounds.
					useLocalAverage ^= true;
					decodeHints.setBinarizer(useLocalAverage
							? Binarizer.LOCAL_AVERAGE
							: Binarizer.GLOBAL_HISTOGRAM);
					List<Result> results = ZxingCpp.INSTANCE.readByteArray(
							data,
							width,
							cropRect,
							orientation,
							decodeHints);
					if (results == null || results.size() < 1) {
						return;
					}
					Result result = results.get(0);
					if (overlayView != null) {
						overlayView.show(result.getPosition());
					}
					if (onBarcodeListener == null) {
						return;
					}
					decoding = onBarcodeListener.onBarcodeRead(result);
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

	private void calculateCropRect(int width, int height) {
		if (cropRatio > 0f) {
			int minPreview = Math.min(previewRect.width(),
					previewRect.height());
			int sizeInPreviewRect = (int) (minPreview * cropRatio);
			int minFrame = Math.min(width, height);
			int sizeInFrame = Math.round(
					(float) minFrame / minPreview * sizeInPreviewRect);
			int left = (width - sizeInFrame) / 2;
			int top = (height - sizeInFrame) / 2;
			cropRect.set(left, top, left + sizeInFrame,
					top + sizeInFrame);
		} else {
			cropRect.set(0, 0, width, height);
		}
	}
}
