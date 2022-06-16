package de.markusfisch.android.barcodescannerview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import de.markusfisch.android.zxingcpp.ZxingCpp.Position;

public class OverlayView extends View {
	private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint cropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final RectF cropRectInView = new RectF();
	private final Matrix matrix = new Matrix();
	private final float[] coords = new float[16];
	private final Runnable clearPointsRunnable = () -> clear();

	private float dotRadius;
	private float cornerRadius;
	private int count = 0;

	public OverlayView(Context context) {
		super(context);
		init(context);
	}

	public OverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public OverlayView(
			Context context,
			AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public void updateTransformationMatrix(
			int imageWidth,
			int imageHeight,
			int imageRotation,
			Rect viewRect,
			Rect cropRect) {
		cropRectInView.set(cropRect);
		// Scale and rotate to map the crop rectangle from image
		// to view coordinates.
		matrix.setScale(1f / imageWidth, 1f / imageHeight);
		matrix.postRotate(imageRotation, .5f, .5f);
		matrix.postScale(viewRect.width(), viewRect.height());
		matrix.postTranslate(viewRect.left, viewRect.top);
		matrix.mapRect(cropRectInView);
		// Scale points relative to upright camera image.
		float uprightWidth, uprightHeight;
		switch (imageRotation) {
			case 90:
			case 270:
				uprightWidth = imageHeight;
				uprightHeight = imageWidth;
				break;
			default:
				uprightWidth = imageWidth;
				uprightHeight = imageHeight;
				break;
		}
		matrix.setScale(
				viewRect.width() / uprightWidth,
				viewRect.height() / uprightHeight);
		matrix.postTranslate(cropRectInView.left, cropRectInView.top);
	}

	public void show(Position position) {
		clear();
		if (position == null) {
			return;
		}
		addCoordinate(position.getTopLeft());
		addCoordinate(position.getTopRight());
		addCoordinate(position.getBottomRight());
		addCoordinate(position.getBottomLeft());
		matrix.mapPoints(coords, 0, coords, 0, count);
		removeCallbacks(clearPointsRunnable);
		postDelayed(clearPointsRunnable, 200);
	}

	private void clear() {
		invalidate();
		count = 0;
	}

	@Override
	public void onDraw(Canvas canvas) {
		drawClip(canvas, cropRectInView, cropPaint, cornerRadius);
		for (int i = 0; i < count; i += 2) {
			canvas.drawCircle(coords[i], coords[i + 1], dotRadius, dotPaint);
		}
	}

	private void init(Context context) {
		float dp = context.getResources().getDisplayMetrics().density;
		dotRadius = 6f * dp;
		cornerRadius = 8f * dp;

		dotPaint.setStyle(Paint.Style.FILL);
		dotPaint.setColor(0xc0ffffff);

		cropPaint.setStyle(Paint.Style.STROKE);
		cropPaint.setColor(0xffffffff);
		cropPaint.setStrokeWidth(dp);
	}

	private void addCoordinate(Point point) {
		coords[count++] = point.x;
		coords[count++] = point.y;
	}

	private static void drawClip(Canvas canvas, RectF roi, Paint roiPaint,
			float cornerRadius) {
		float minDist = Math.min(roi.width(), roi.height()) / 2f;
		if (minDist < 1f) {
			return;
		}
		// canvas.clipRect() doesn't work reliably below KITKAT.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			float radius = Math.min(minDist / 2f, cornerRadius);
			canvas.save();
			clipOutPathCompat(canvas,
					createRoundedRectPath(roi, radius, radius));
			canvas.drawColor(0x80000000);
			canvas.restore();
		} else {
			canvas.drawRect(roi, roiPaint);
		}
	}

	private static void clipOutPathCompat(Canvas canvas, Path path) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			canvas.clipOutPath(path);
		} else {
			canvas.clipPath(path, Region.Op.DIFFERENCE);
		}
	}

	private static Path createRoundedRectPath(RectF rect,
			float rx, float ry) {
		float widthMinusCorners = rect.width() - 2 * rx;
		float heightMinusCorners = rect.height() - 2 * ry;
		Path path = new Path();
		path.moveTo(rect.right, rect.top + ry);
		path.rQuadTo(0f, -ry, -rx, -ry);
		path.rLineTo(-widthMinusCorners, 0f);
		path.rQuadTo(-rx, 0f, -rx, ry);
		path.rLineTo(0f, heightMinusCorners);
		path.rQuadTo(0f, ry, rx, ry);
		path.rLineTo(widthMinusCorners, 0f);
		path.rQuadTo(rx, 0f, rx, -ry);
		path.rLineTo(0f, -heightMinusCorners);
		path.close();
		return path;
	}
}
