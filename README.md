# RealmSpeak


## Simon's Update

I'm working on bringing the **RealmSpeak** source code up-to-date with the
use of Java features that were not available when Robin originally wrote 
the code.

But, before messing with the code per se, I thought it best to start by
simply writing javadocs and creating unit tests for the existing classes,
to confirm my understanding of the code. 

Once that has been done, and we have a full regression suite of tests, I will
feel more confident about refactoring the code without breaking any 
existing functionality.


### Code Structure

I sketched out a diagram of the base module dependencies here:

https://goo.gl/Ve6Jcy

I'll be updating this diagram as I work through the codebase (adding javadocs 
and unit tests), so you can track my progress if you wish.

(Note that this diagram does not show _every_ module; it was starting to 
get a little unwieldy, so I decided to focus on just the base modules for now).



### Web-based Game History

My ultimate plan is to write a new _HTML game generator module_ to create 
a more modern UI user-experience for navigating online game histories. But
it will be quite a while before I get to that stage, I'm sure...


~Simon



## Robin's Original Note

RealmSpeak is a java application that allows you to play Avalon Hill's 
Magic Realm the boardgame online with friends, or as a solitaire game.

Look in the Documents subfolder for instructions I wrote years ago about 
getting it all to build through "ant". Once you have ant and a java JDK 
installed, it's a simple two commands to get the RealmSpeakFull.jar.  
Then you just click run.bat, and you are off an running.  Not a good way to 
modify RealmSpeak, but a great way to get the final product when you are done 
debugging.

I use Eclipse IDE to do all my editing/debugging/compiling, but feel free to 
use whatever Java IDE you prefer.

To understand all the dependencies, take a look at the file 
`build/project-list.xml`.  Scroll to the very bottom, and note the project 
"RealmSpeakFull".  This is RealmSpeak in all it's glory!  You should be able 
to work backward from that to figure out how to setup projects in your 
favorite IDE.

IF YOU ARE NOT INTERESTED IN MODIFYING THE SOURCE CODE AND WANT TO PLAY THE GAME

Please check out the latest build (which matches this version of source) here:

  http://realmspeak.dewkid.com/downloads

To play the game, a good place to start is here:

  http://triremis.com.au/wiki-mr/pmwiki.php?n=Main.LearningTheGameUsingRealmSpeak

Also be sure to check out the FAQ:

  http://triremis.com.au/wiki-mr/pmwiki.php?n=Main.RealmSpeakFAQ
  
Robin
