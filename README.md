# Barcode Scanner View

A barcode scanner view for Android. Batteries included.

## How to include

### Gradle

Add the JitPack repository in your root build.gradle at the end of
repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Then add the dependency in your app/build.gradle:

	dependencies {
		implementation 'com.github.markusfisch:BarcodeScannerView:1.0.0'
	}

### Manually

Alternatively you may just download the latest `aar` from
[Releases](https://github.com/markusfisch/BarcodeScannerView/releases)
and put it into `app/libs` in your app.

Then make sure your `app/build.gradle` contains the following line in the
`dependencies` block:

	dependencies {
		implementation fileTree(dir: 'libs', include: '*')
		...
	}

## How to use

Add it to a layout:

````xml
	<de.markusfisch.android.barcodescanerview.widget.BarcodeScannerView
		xmlns:android="http://schemas.android.com/apk/res/android"
		android:id="@+id/barcode_scanner"
		android:layout_width="match_parent"
		android:layout_height="match_parent"/>
```

Or create it programmatically:

```java
import de.markusfisch.android.barcodescanerview.widget.BarcodeScannerView;

BarcodeScannerView scannerView = new BarcodeScannerView(context);
```

Run `BarcodeScannerView.openAsync()`/`.close()` in `onResume()`/`onPause()`
of your activity or fragment:

```java
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
```

## Demo

You can run the enclosed demo app to see if this widget is what you want.

Either import it into Android Studio or, if you're not on that thing from
Redmond, just type `make` to build, install and run.
