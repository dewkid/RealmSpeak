# RealmSpeak
RealmSpeak is a java application that allows you to play Avalon Hill's Magic Realm the boardgame online with friends,
or as a solitaire game.

Look in the Documents subfolder for instructions I wrote years ago about getting it all to build through "ant".
Once you have ant and a java JDK installed, it's a simple two commands to get the RealmSpeakFull.jar.  Then you
just click run.bat, and you are off an running.  Not a good way to modify RealmSpeak, but a great way to get the
final product when you are done debugging.

I use Eclipse IDE to do all my editing/debugging/compiling, but feel free to use whatever Java IDE you prefer.

To understand all the dependencies, take a look at the file build/project-list.xml.  Scroll to the very bottom,
and note the project "RealmSpeakFull".  This is RealmSpeak in all it's glory!  You should be able to work backward
from that to figure out how to setup projects in your favorite IDE.
