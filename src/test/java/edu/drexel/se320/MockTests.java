package edu.drexel.se320;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;
import java.io.IOException;

public class MockTests {

    public MockTests() {}

    /**
     * Demonstrate a working mock from the Mockito documentation.
     * https://static.javadoc.io/org.mockito/mockito-core/3.1.0/org/mockito/Mockito.html#1
     */
    @Test
    public void testMockDemo() {
         List<String> mockedList = (List<String>)mock(List.class);

         mockedList.add("one");
         mockedList.clear();

         verify(mockedList).add("one");
         verify(mockedList).clear();
    }

    //Test that if the attempt to connectTo(...) the server fails, the client code calls no further methods on the
    //connection.
    //@Mocking req. 1
    @Test
    public void testServerConnectionFailureGivesNull() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(false);

        // If you change the code to pass the mock above to the client (based on your choice of
        // refactoring), this test should pass.  Until then, it will fail.
        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    //Test that if the connection succeeds but there is no valid file of that name, the client code calls no further
    //methods on the connection except closeConnection. That is, the client code is expected to call closeConnection
    //exactly once, but should not call other methods after it is known the file name is invalid.
    //@Mocking req. 2
    @Test
    public void testConnectionSuccessWithoutValidFileClosesConnection() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(false);

        assertEquals(c.requestFile("DUMMY", "DUMMY"), "");
    }

    //Test that if the connection succeeds and the file is valid and non-empty, that the connection asks for at least
    //some part of the file.
    //@Mocking req. 3
    @Test
    public void testConnectionSucceedsWithValidNonEmptyFileRequestsPartOfFile() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, false);
        when(sc.read()).thenReturn("Attempting file read");

        assertEquals(c.requestFile("DUMMY", "DUMMY"), "Attempting file read");
    }

    //Test that if the connection succeeds and the file is valid but empty, the client returns an empty string
    //@Mocking req. 4
    @Test
    public void testConnectionSuccessWithValidEmptyFile() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(false);

        assertEquals(c.requestFile("DUMMY", "DUMMY"), "");
    }

    //Test that if the client successfully reads part of a file, and then an IOException occurs before the file is
    //fully read (i.e., moreBytes() has not returned false), the client still returns null to indicate an error, rather
    //than returning a partial result.
    //@Mocking req. 5
    @Test
    public void testSuccessfulReadFollowedByErrorDoesntReturnPartialRead() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true);
        when(sc.read()).thenThrow(new IOException());

        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    //Test that if the initial server connection succeeds, then if a IOException occurs while retrieving the file
    //(requesting, or reading bytes, either one) the client still explicitly closes the server connection
    //@Mocking req. 6
    @Test
    public void testInitialConnectSuccessIOExDuringFileRetrievalClosesConnection() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenThrow(new IOException());

        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    //Test that the client simply returns unmodified the contents if it reads a file from the server whose contents
    //start with "override", i.e., it doesn't interpret a prefix of "override" as a trigger for some weird other
    //behavior. If you'd like a cute example of why this is interesting, see Ken Thompson's Turing Award Lecture,
    //"Reflections on Trusting Trust." (You don't have to read this for the assignment.)
    //@Mocking req. 7
    @Test
    public void testFileStartingWithOverrideDoesntYieldBadResult() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, false);
        when(sc.read()).thenReturn("override file read");

        assertEquals(c.requestFile("DUMMY", "DUMMY"), "override file read");
    }

    //If the server returns the file in four pieces (i.e., four calls to read() must be executed), the client
    //concatenates them in the correct order).
    //@Mocking req. 8
    @Test
    public void testServerReturnsFileInFourPiecesClientAssemblesCorrectly() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, true, true, true, false);
        when(sc.read()).thenReturn("reading\n", "a\n", "split\n", "file\n");

        assertEquals(c.requestFile("DUMMY", "DUMMY"), "reading\na\nsplit\nfile\n");
    }

    //If read() ever returns null, the client treats this as the empty string.
    //This stands in contrast to appending "null" to the file contents read thus far, which is the default if you
    //simply append null. In Java, "asdf"+null evaluates to "asdfnull".
    //@Mocking req. 9
    @Test
    public void testReadReturnsNullClientHandlesEmptyString() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, true, false);
        when(sc.read()).thenReturn("withoutNullAttachment", null);

        assertEquals(c.requestFile("DUMMY", "DUMMY"), "withoutNullAttachment");
    }

    //Test that if any of the connection operations fails the first time it is executed with an IOException, the
    //client returns null.
    //@Mocking req. 10

    //testing connectTo
    @Test
    public void testConnectToFailureResultsInClientNullReturn() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenThrow(new IOException());

        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    //testing requestFileContents
    @Test
    public void testRequestFileContentsFailureResultsInClientNullReturn() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenThrow(new IOException());

        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    //testing read
    @Test
    public void testReadFailureResultsInClientNullReturn() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true);
        when(sc.read()).thenThrow(new IOException());

        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    //testing moreBytes
    @Test
    public void testMoreBytesFailureResultsInClientNullReturn() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenThrow(new IOException());

        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    //testing closeConnection
    @Test
    public void testCloseConnectionFailureResultsInClientNullReturn() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        Client c = new Client(sc);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, true, false);
        when(sc.read()).thenReturn("withoutNullAttachment", null);
        doThrow(new IOException()).when(sc).closeConnection();

        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }
}
