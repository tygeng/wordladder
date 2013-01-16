CS2506 Project 1 - Word Ladder Generator

===========================================================================
Author:	Tianyu Geng
PID:	tony1
Email:	tony1@vt.edu

Language used:
java version "1.6.0_24"
OpenJDK Runtime Environment (IcedTea6 1.11.4) (6b24-1.11.4-1ubuntu0.12.04.1)
OpenJDK Server VM (build 20.0-b12, mixed mode)

Tested on rlogin cluster with version:
java version "1.7.0_01"
Java(TM) SE Runtime Environment (build 1.7.0_01-b08)
Java HotSpot(TM) 64-Bit Server VM (build 21.1-b02, mixed mode)

===========================================================================
How to compile and run

Please finish reading the whole document before running the program.

This is a command line program. It does NOT have a GUI. To compile and run
the program, follow the steps below:

* Put the project1.tar into an empty directory and unzip the project1.tar
  file with command:
  
    tar -xvf project1.tar

  Now there should be 5 *.java files.

* Compile the source code files with the following command:

    javac *.java

* Put the dictionary file into the current directiory and  name it
  "dictionary.txt". Put the input file called "input.txt" into the current
  directory and name it "input.txt". Then, run the program with the
  commands:

    java WordLadder

With the "short+dictionary.txt" The program should run for less than one
minute before it finishes both part 1 and part 2. 
However, be aware that the program has a preparation process that will 
generate a hash map to rapidly finish part 2. Therefore, this preparation 
time may take a few seconds before the program finishes part 1, which also 
uses the hash map for fast look up.
===========================================================================
Important notice:

This program may pause during execution. If this happens, please press
Ctrl+Z to temporarily stop the execution. Then, use command "fg" to switch
back to the program and the program should continue to run.
If pausing happens again, just Ctrl+Z and "fg" back again.

However, please wait patiently after part 2b is finished. The following
message should be displayed at that time:

  Cleaning cache, please wait ...

The cause for this strange issue is not clear yet. When running on my own
machine with Java SE 1.6.0 24, this issue has never appeared. However,
considering I am using multiple threads for part 2b, so I may have some
issue with synchronization between the threads. 

By experimenting with different settings and slightly changed source codes,
I found this issue is closely related to StringBuilder used to create the
formatted ladders. A very interesting thing is, if I put several separate
StringBuilder objects into different threads, the chance the program pause
increases significantly.

By now, I haven't found a cure for this issue.
===========================================================================
A little extra stuff

If you use some words as the command arguments when running WordLadder, the
program will output the shortest ladder with both letter permutation and
+/- word length variation. Use the long dictionary as an example. If you
type the following commands after the program has been compiled:

  java WordLadder hello world what is going on

This would be the output:

  ..
  hello, hollo, holl, hold, wold, world
  what, hat, at, as, is
  going, gong, gon, on

You can specify a certain .txt file as the dictionary by using "-d"
parameter like this:

  java WordLadder -d medium+dictionary.txt

Then, the program will create a serialized hashMap file on the disk called
".map". If you don't specify "-d" parameter, then, the next time you run the
program with more than two words as command line arguments, the program will
consult the dictionary used during the previous execution.

Have fun with it. With the long dictioanry, almost any two words can be
connected by a word ladder.








