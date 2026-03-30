package edu.hse.netchat.transport;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/** TCP server that accepts a single connection (base project scope). */
public final class TcpServer implements Closeable {

  private final ServerSocket serverSocket;

  public TcpServer(InetSocketAddress listenAddress) throws IOException {
    this.serverSocket = new ServerSocket();
    serverSocket.bind(listenAddress);
  }

  public InetSocketAddress boundAddress() {
    return new InetSocketAddress(serverSocket.getInetAddress(), serverSocket.getLocalPort());
  }

  public PeerConnection accept() throws IOException {
    Socket socket = serverSocket.accept();
    return new PeerConnection(socket);
  }

  @Override
  public void close() throws IOException {
    serverSocket.close();
  }
}
