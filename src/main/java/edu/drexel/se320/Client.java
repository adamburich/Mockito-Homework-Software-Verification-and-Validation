package edu.drexel.se320;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.StringBuilder;
import java.lang.UnsupportedOperationException;

public class Client {

    private String lastResult;
    StringBuilder sb = null;
    //Store the connection passed in constructor so we can use it in requestFile
    ServerConnection conn;

    //Added ServerConnection object to constructor so we can pass in mocked objects
    public Client(ServerConnection conn) {
        lastResult = "";
        //set Client.conn to passed in object
        this.conn = conn;
    }

    public String requestFile(String server, String file) {
        if (server == null)
            throw new IllegalArgumentException("Null server address");
        if (file == null)
            throw new IllegalArgumentException("Null file");
	// This ServerConnection is here only as a placeholder --- the real dependency
	// doesn't exist yet.  Your tests will need to make sure the code below this
	// definition of conn interacts with connections correctly despite not having
	// a real ServerConnection.
	//
	// To be clear: do NOT implement the methods below.  Instead, make it possible
	// to run the code below with a mock, rather than this dummy implementation.
        /**
         * Commented out since we have a dynamic definition for conn now that we can insert mocks into
         *
        ServerConnection conn = new ServerConnection() {
            public boolean connectTo(String address) throws IOException {
                throw new UnsupportedOperationException();
            }
            public boolean requestFileContents(String filename) throws IOException {
                throw new UnsupportedOperationException();
            }
            public String read() throws IOException {
                throw new UnsupportedOperationException();
            }
            public boolean moreBytes() throws IOException {
                throw new UnsupportedOperationException();
            }
            public void closeConnection() throws IOException {
                throw new UnsupportedOperationException();
            }
        };*/

	// We'll use a StringBuilder to construct large strings more efficiently
	// than repeated linear calls to string concatenation.
        sb = new StringBuilder();

        try {
            if (conn.connectTo(server)) {
                boolean validFile = conn.requestFileContents(file);
                if (validFile) {
                    while (conn.moreBytes()) {
                        String tmp = conn.read();
                        if (tmp != null) {
                            sb.append(tmp);
                        }
                    }
                    conn.closeConnection();
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        String result = sb.toString();
        lastResult = result;
        return result;
    }
}

