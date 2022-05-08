package de.markusfisch.android.barcodescannerview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import de.markusfisch.android.zxingcpp.ZxingCpp.Position;

public class OverlayView extends View {
	private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint cropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final RectF cropRectInView = new RectF();
	private final Matrix matrix = new Matrix();
	private final float[] coords = new float[16];

	private float radius;
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
		// Scale points in position according to upright camera image.
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
		invalidate();
		count = 0;
		if (position == null) {
			return;
		}
		Point[] points = new Point[]{
				position.getTopLeft(),
				position.getTopRight(),
				position.getBottomRight(),
				position.getBottomLeft()
		};
		for (Point point : points) {
			coords[count++] = point.x;
			coords[count++] = point.y;
		}
		matrix.mapPoints(coords, 0, coords, 0, count);
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawRect(cropRectInView, cropPaint);
		for (int i = 0; i < count; i += 2) {
			canvas.drawCircle(coords[i], coords[i + 1], radius, dotPaint);
		}
	}

	private void init(Context context) {
		float dp = context.getResources().getDisplayMetrics().density;
		radius = 6f * dp;

		dotPaint.setStyle(Paint.Style.FILL);
		dotPaint.setColor(0xc0ffffff);

		cropPaint.setStyle(Paint.Style.STROKE);
		cropPaint.setColor(0xffffffff);
		cropPaint.setStrokeWidth(dp);
	}
}
