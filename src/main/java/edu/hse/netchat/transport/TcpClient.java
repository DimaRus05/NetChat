package edu.hse.netchat.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** TCP client connector. */
public final class TcpClient {

    public PeerConnection connect(InetSocketAddress peerAddress, int connectTimeoutMillis)
            throws IOException {
        Socket socket = new Socket();
        socket.connect(peerAddress, connectTimeoutMillis);
        return new PeerConnection(socket);
    }
}
