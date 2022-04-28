PACKAGE = de.markusfisch.android.barcodescannerviewdemo

all: debug install start

debug:
	./gradlew assembleDebug

lint:
	./gradlew :barcodescannerview:lintDebug

aar: clean lint
	./gradlew :barcodescannerview:assembleRelease

install:
	adb $(TARGET) install -r app/build/outputs/apk/debug/app-debug.apk

start:
	adb $(TARGET) shell 'am start -n $(PACKAGE)/.activity.MainActivity'

uninstall:
	adb $(TARGET) uninstall $(PACKAGE)

clean:
	./gradlew clean
