
# SE320 Homework 3: Object Dependencies
## Fall 2021

- Late Policy: standard for the course, see the syllabus

### Overview
In this assignment you'll put into practice some material on testing with dependencies.  There are two steps to this homework:

1. Refactor the code to expose the dependency on the collaborator
2. Use the [Mockito](http://site.mockito.org/) mocking framework to test the code's interaction
   with its dependency.


### Refactoring
The code you've been given depends on a network connection implementation, which knows the details of some protocol
to request files from a server.  The details of this protocol are not important, because you're going to
test *without access to the real implementation*.  What is relevant is that the protocl includes
explicit connect, request, and close operations, and an iterator-like pair of methods
```moreBytes()``` and ```read()```.  See the javadoc in ```ServerConnection.java```.

We'd like to test the functionality independent of the actual dependency, as we've discussed in class, using a mock of the ```ServerConnection```. 
This corresponds to situations such as:

- Your colleagues aren't finished implementing the server connection
- The ```ServerConnection``` interface is actually going to be used to abstract over multiple connection channels, but you don't yet know which and you want to write tests that check what your abstraction is supposed to do

Pick an approach discussed in class (or an adaptation of the collaborator factory version to another
creational pattern, if you like).  Refactor the code appropriately.  In your write-up, identify
which approach you took (especially if you used another creational pattern), and explain why you
picked it.

Once you've refactored, change the test ```testServerConnectionFailureGivesNull```: the test
already constructs a mock, so change the test to get the mock ```ServerConnection``` where it needs
to be.  The test should pass with your changes, but the only changes you should make to that method are to pass the mock in the appropriate place.  Do not modify the construction of the mock (call to ```mock```), the call to ```when```, or the call to ```assertNull```.

### Mocking
For this assignment, we'll be using [Mockito](http://site.mockito.org/), a popular Java mocking
framework.  As usual, all of the relevant dependencies are already pulled in by the Gradle build
file.

Mockito is a very substantial framework.  We'll only scratch the surface.

You'll probably want to peruse the [documentation for Mockito](https://javadoc.io/static/org.mockito/mockito-core/3.1.0/org/mockito/Mockito.html).  For this assignment, you'll really only need sections 1-7, 10, and 12 (don't worry, they're short, and mostly examples).  If you use this (or a similar framework) in a real project, there are lots of bells and whistles for trickier tests or to make using many of the same mock easier; we won't use them for this homework.


We'd like you to write tests for the following 10 behaviors:

1. Test that if the attempt to ```connectTo(...)``` the server fails, the client code calls no
   further methods on the connection.
2. Test that if the connection succeeds but there is no valid file of that name, the client code calls no
   further methods on the connection except ```closeConnection```.  That is, the client code *is*
   expected to call ```closeConnection``` exactly once, but should not call other methods after it is known the
   file name is invalid.
3. Test that if the connection succeeds and the file is valid and non-empty, that the connection asks for at
   least some part of the file.
4. Test that if the connection succeeds and the file is valid but empty, the client returns an
   empty string.
5. Test that if the client successfully reads *part* of a file, and *then* an ```IOException```
   occurs before the file is fully read (i.e., ```moreBytes()``` has not returned false), the
   client still returns null to indicate an error, rather than returning a partial result.
6. Test that if the initial server connection succeeds, then if a ```IOException``` occurs while
   retrieving the file (requesting, or reading bytes, either one) the client still explicitly
   closes the server connection.
7. Test that the client simply returns unmodified the contents if it reads a file from the server
   whose contents start with "override",  i.e., it doesn't interpret a prefix of "override" as a trigger for some weird other behavior.
    + If you'd like a cute example of why this is interesting, see Ken Thompson's Turing Award
      Lecture, "Reflections on Trusting Trust." (You don't have to read this for the assignment.)
8. If the server returns the file in four pieces (i.e., four calls to ```read()``` must be executed), the client concatenates them in the correct order).
9. If ```read()``` ever returns ```null```, the client treats this as the empty string.
    + This stands in contrast to appending "null" to the file contents read thus far, which is the default if you simply append null. In Java, ```"asdf"+null``` evaluates to "asdfnull".
10. Test that if any of the connection operations fails the first time it is executed with an ```IOException```, the client
    returns null.


You should end up with 14 tests (one each for 1-9, plus 5 for item 10 --- since there are 5 methods
in ```ServerConnection```), each with a slightly different mock.  One or more of the tests should fail, because the client code has bugs!  You'll have to find out which --- if all of your tests pass, then one or more of your tests do not reflect the tests requested above.  You do not have to fix the client code (and in fact, please *do not fix the client code*), as this will simplify grading.

An example test from the Mockito documentation is in ```MockTests.java``` in the test directory.
You should add your tests there.  (You're welcome to delete the example test, but it's worth
running once to make sure everything is working for you.)

Document which test is which in your test file!  Do more than simply naming your tests cleverly ---
write explicit comments above each test indicating which of the above cases the test is for.  *You
earn points by writing this documentation, even if the actual test is incorrect.*


### Grading

- 20% refactoring and explanation
    + 10% refactoring
    + 10% explanation
- 80% testing with mocks (and/or stubs and fakes)
    + 70% tests (both the property, and whether they pass/fail correctly)
        - 5 per test (14 tests)
    + 10% documentation of which test is for which case

This assignment is shorter than the previous homeworks.  Thus you have only one week to complete
it, and it is worth 10% of your term grade.

You should hand in a .zip or .tar.gz/.tgz containing:

- All of your source code:
    + Refactored program
    + Mockito-based tests (and documentation)
    + Short README describing your choice of refactoring.
        - As usual, in the directory with the code is recommended. In this case, a comment at the point of the refactoring works, too.
