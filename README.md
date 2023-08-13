# Barcode Scanner View

A barcode scanner view for Android. Batteries included.
Requires `minSdkVersion` of 9 or higher.

This is an Open Source alternative for the
[Google code scanner](https://developers.google.com/ml-kit/code-scanner)
in Google's ML Kit. If you just need to scan a barcode and do not need to
embed the scanning view, you should also check out the ML Kit solution.

## How to include

### Gradle

Add the JitPack repository in your root build.gradle at the end of
repositories:

```groovy
allprojects {
	repositories {
		…
		maven { url 'https://jitpack.io' }
	}
}
```

Then add the dependency in your app/build.gradle:

```groovy
dependencies {
	implementation 'com.github.markusfisch:BarcodeScannerView:1.4.1'
}
```

### Manually

Alternatively you may just download the latest `aar` from
[Releases](https://github.com/markusfisch/BarcodeScannerView/releases)
and put it into `app/libs` in your app.

In this case you need to add the dependencies of this library to your
`app/build.gradle`, too:

```groovy
dependencies {
	implementation fileTree(dir: 'libs', include: '*')
	implementation 'com.github.markusfisch:CameraView:1.9.1'
	implementation 'com.github.markusfisch:zxing-cpp:v2.1.0.1'
	…
}
```

### Proguard/R8

Don't forget to add the following line to your `app/proguard-rules.pro` if
you are using minification:

```
-keep class de.markusfisch.android.zxingcpp.** { *; }
```

## How to use

Add it to a layout:

```xml
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

### Crop window

You can define a square crop window with `setCropRatio()`:

```java
scannerView.setCropRatio(0.75f);
```

The number is the ratio between the size of the crop square and the
shorter dimension of the view. For example, `0.75` means 75% of the view
width in portrait orientation.

A crop window can improve the user experience by providing a clear UI
and it also improves performance because there's less data to process.

### Set barcode formats

By default only `Format.QR_CODE` is set.

If you don't want to scan for QR Codes you can do:

```java
scannerView.formats.clear();
```

To scan for `Format.EAN_13` for example:

```java
scannerView.formats.add(de.markusfisch.android.zxingcpp.ZxingCpp.Format.EAN_13);
```

The following formats are supported:

* `AZTEC`
* `CODABAR`
* `CODE_39`
* `CODE_93`
* `CODE_128`
* `DATA_BAR`
* `DATA_BAR_EXPANDED`
* `DATA_MATRIX`
* `EAN_8`
* `EAN_13`
* `ITF`
* `MAXICODE`
* `PDF_417`
* `QR_CODE`
* `MICRO_QR_CODE`
* `UPC_A`
* `UPC_E`

## Demo

You can run the enclosed [demo app](app) to see if this widget is what you're
looking for.

Either import it into Android Studio or just type `make` to build, install
and run it on a connected device.
