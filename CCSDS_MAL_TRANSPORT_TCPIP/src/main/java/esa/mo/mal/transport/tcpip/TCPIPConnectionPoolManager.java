package esa.mo.mal.transport.tcpip;

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
		
		int hash = getSocketHash(type, localHost, localPort, remoteHost, remotePort);
		System.out.println("ConnectionPool: get -> hash: " + hash);
		
		if (type == SOCKET_TYPE.LOCAL) {
			return localSockets.get(hash);
		} else if (type == SOCKET_TYPE.REMOTE) {
			return remoteSockets.get(hash);
		}
		
		RLOGGER.warning("The socket type provided doesn't exist! Use either 'LOCAL' or 'REMOTE'.");

		return null;
	}
	
	public int getSocketHash(SOCKET_TYPE type, String localHost, int localPort, String remoteHost, int remotePort) {
		
		if (type == SOCKET_TYPE.LOCAL) {
			return Integer.toString(localPort).hashCode();
		}
		
		return (localHost+localPort+remoteHost+remotePort).hashCode();		
	}
}
