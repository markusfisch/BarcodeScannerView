package de.markusfisch.android.barcodescannerviewdemo.activity;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import de.markusfisch.android.barcodescannerview.widget.BarcodeScannerView;
import de.markusfisch.android.barcodescannerviewdemo.R;

public class MainActivity extends Activity {
	private static final int REQUEST_CAMERA = 1;

	private BarcodeScannerView scannerView;

	@Override
	public void onRequestPermissionsResult(
			int requestCode,
			String[] permissions,
			int[] grantResults) {
		if (requestCode == REQUEST_CAMERA &&
				grantResults.length > 0 &&
				grantResults[0] != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(this, R.string.error_camera,
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		checkPermissions();
		setContentView(R.layout.activity_main);

		TextView textView = findViewById(R.id.text);

		scannerView = findViewById(R.id.scanner);
		scannerView.setCropRatio(.75f);
		scannerView.setOnBarcodeListener(result -> {
			// This listener is called from the Camera thread.
			textView.post(() -> textView.setText(result.getText()));
			// Return true to keep scanning for barcodes.
			return true;
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		scannerView.openAsync(BarcodeScannerView.findCameraId(
				Camera.CameraInfo.CAMERA_FACING_BACK));
	}

	@Override
	public void onPause() {
		super.onPause();
		scannerView.close();
	}

	private void checkPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			String permission = android.Manifest.permission.CAMERA;
			if (checkSelfPermission(permission) !=
					PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{permission}, REQUEST_CAMERA);
			}
		}
	}
}
