wordladder
==========

Wordladder generator.

Given a dictionary with one word on a line, the program generates word ladders like 

good, food, fond, find, fine, five

from "good" to "five".

Note: the two words should differ by exactly one letter.

This program is implemented with both Java and C. The Java version can take advantage
of multiple cores and is more capable than the C version. For example, the Java version,
if required, can relate "good" with "god" since missing an "o" can also be thought as
differ by one letter.

The Java implementation is in directory 'wordladder', sample input and output has been
given.

The C implementation is in in directory 'wordladder_c'. Sample dictionary has been
provided, too.
