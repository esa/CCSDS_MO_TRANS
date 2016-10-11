package esa.mo.mal.transport.tcpip;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public enum TCPIPConnectionPoolManager {
	INSTANCE;
	
	/**
	 * Logger
	 */
	public static final java.util.logging.Logger RLOGGER = Logger.getLogger("org.ccsds.moims.mo.mal.transport.tcpip");

	private Map<Integer, Socket> connections = new HashMap<Integer, Socket>();
	
	public void put(Socket s) {
		
		String remoteHost = "";
		int remotePort = -1;
		if (s.getInetAddress() != null) {
			remoteHost = s.getInetAddress().getCanonicalHostName();
			remotePort = s.getPort();
		}
		String localHost = s.getLocalAddress().getCanonicalHostName();
		int localPort = s.getLocalPort();
		int hash = getSocketHash(localHost, localPort, remoteHost, remotePort);
		
		System.out.println("ConnectionPool: put -> hash: " + hash);
		
		connections.put(hash, s);
	}
	
	public Socket getAny() {
		return get("", 0, "", 0);
	}
	
	public Socket get(String localHost, int localPort, String remoteHost, int remotePort) {
		
		Socket s = null;
		int hash = getSocketHash(localHost, localPort, remoteHost, remotePort);
		System.out.println("ConnectionPool: get -> hash: " + hash);
		
		s = connections.get(hash);
		if (s == null) {
			s = createSocket(localPort);
			RLOGGER.warning("The socket doesn't exist yet! Creating a new one.");
		}

		return s;
	}
	
	public int getSocketHash(String localHost, int localPort, String remoteHost, int remotePort) {
		
		return Integer.toString(localPort).hashCode();
	}
	
	public Socket createSocket(int localPort) {
		
		Socket s = new Socket();
		try {
			s.bind(new InetSocketAddress(localPort));
			
			RLOGGER.fine("Created a socket at port " + s.getLocalPort());
		} catch (IOException e) {
			try {
				s.bind(null);
			} catch (IOException e1) {
				RLOGGER.warning("Failed to create a socket! " + e.getMessage());
				e1.printStackTrace();
			}
			
			RLOGGER.warning("Failed to create a socket at port " + localPort + "! " + e.getMessage());
		}			
		put(s);
		
		return s;
	}
	
	public String toString() {
		
		StringBuffer result = new StringBuffer();
		result.append("LocalSockets:\n");
		for (int hash : connections.keySet()) {
			result.append(hash + ": " + connections.get(hash) + "\n");
		}
		
		return result.toString();
	}
}
