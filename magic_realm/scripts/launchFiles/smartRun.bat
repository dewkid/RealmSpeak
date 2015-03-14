call FindJavaHome.bat
set path=%JAVA_HOME%;%path%
@start javaw -mx384m -cp mail.jar;activation.jar;RealmSpeak.jar com.robin.magic_realm.RealmSpeak.RealmSpeakFrame %1