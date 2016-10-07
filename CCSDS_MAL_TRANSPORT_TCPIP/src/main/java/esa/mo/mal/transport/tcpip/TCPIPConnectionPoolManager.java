package esa.mo.mal.transport.tcpip;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

enum SOCKET_TYPE {
	LOCAL,
	REMOTE
};

public enum TCPIPConnectionPoolManager {
	INSTANCE;
	
	/**
	 * Logger
	 */
	public static final java.util.logging.Logger RLOGGER = Logger.getLogger("org.ccsds.moims.mo.mal.transport.tcpip");

	private Map<Integer, Socket> localSockets = new HashMap<Integer, Socket>();
	private Map<Integer, Socket> remoteSockets = new HashMap<Integer, Socket>();
	
	public void put(SOCKET_TYPE type, Socket s) {
		
		String remoteHost = "";
		int remotePort = -1;
		if (s.getInetAddress() != null) {
			remoteHost = s.getInetAddress().getCanonicalHostName();
			remotePort = s.getPort();
		}
		String localHost = s.getLocalAddress().getCanonicalHostName();
		int localPort = s.getLocalPort();
		int hash = getSocketHash(type, localHost, localPort, remoteHost, remotePort);
		
		System.out.println("ConnectionPool: put -> hash: " + hash);
		
		if (type == SOCKET_TYPE.LOCAL) {
			localSockets.put(hash, s);
		} else if (type == SOCKET_TYPE.REMOTE) {
			remoteSockets.put(hash, s);
		}
	}
	
	public Socket get(SOCKET_TYPE type, String localHost, int localPort, String remoteHost, int remotePort) {
		
		Socket s = null;
		int hash = getSocketHash(type, localHost, localPort, remoteHost, remotePort);
		System.out.println("ConnectionPool: get -> hash: " + hash);
		
		if (type == SOCKET_TYPE.LOCAL) {
			s = localSockets.get(hash);
			if (s == null) {
				s = createSocket();
				RLOGGER.warning("The socket doesn't exist yet! Creating a new one.");
			}
		} else if (type == SOCKET_TYPE.REMOTE) {
			s = remoteSockets.get(hash);
		} else {	
			RLOGGER.warning("The socket type provided doesn't exist! Use either 'LOCAL' or 'REMOTE'.");
		}

		return s;
	}
	
	public int getSocketHash(SOCKET_TYPE type, String localHost, int localPort, String remoteHost, int remotePort) {
		
		if (type == SOCKET_TYPE.LOCAL) {
			return Integer.toString(localPort).hashCode();
		}
		
		return (localHost+localPort+remoteHost+remotePort).hashCode();		
	}
	
	public Socket createSocket() {
		
		Socket s = new Socket();
		try {
			s.bind(null);			
			put(SOCKET_TYPE.LOCAL, s);
			
			RLOGGER.fine("Created a socket at port " + s.getLocalPort());
		} catch (IOException e) {

			RLOGGER.warning("Failed to create a socket! " + e.getMessage());
			e.printStackTrace();
		}
		
		return s;
	}
}
