/* ----------------------------------------------------------------------------
 * Copyright (C) 2014      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO TCP/IP Transport Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.mal.transport.tcpip;

import esa.mo.mal.encoder.binary.split.SplitBinaryStreamFactory;
import esa.mo.mal.encoder.tcpip.TCPIPMessageDecoderFactory;
import esa.mo.mal.transport.gen.GENEndpoint;
import esa.mo.mal.transport.gen.GENMessage;
import esa.mo.mal.transport.gen.GENTransport;
import esa.mo.mal.transport.gen.sending.GENMessageSender;
import esa.mo.mal.transport.gen.sending.GENOutgoingMessageHolder;
import esa.mo.mal.transport.gen.util.GENMessagePoller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALHelper;
import org.ccsds.moims.mo.mal.MALStandardError;
import org.ccsds.moims.mo.mal.broker.MALBrokerBinding;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.InteractionType;
import org.ccsds.moims.mo.mal.structures.QoSLevel;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.transport.MALEndpoint;
import org.ccsds.moims.mo.mal.transport.MALTransmitErrorException;
import org.ccsds.moims.mo.mal.transport.MALTransportFactory;

/**
 * The TCPIP MAL Transport implementation.
 *
 * The following properties configure the transport:
 *
 * org.ccsds.moims.mo.mal.transport.tcpip.debug 
 * 		== debug mode , affects logging 
 * org.ccsds.moims.mo.mal.transport.tcpip.host 
 * 		== adapter (host / IP Address) that the transport will use for incoming connections. 
 * 		In case of a pure client (i.e. not offering any services) this property should be omitted. 
 * org.ccsds.moims.mo.mal.transport.tcpip.port 
 * 		== port that the transport listens to. In case this is a pure client, this property should be omitted.
 *
 * The general logic is the following: The transport at first initializes the server listen port (if this is a server,
 * offering services). On receiving a request to send a MAL Message the transport tries to find if it has allocated some recourses
 * associated with the target URI (has already the means to exchange data with it) and if not, it creates
 * -numconnections- connections to the target server. If a client has already opened a connection to a server the server
 * will re-use that communication channel to send back data to the client.
 *
 * On the server, each incoming connection is handled separately by a different thread which on the first message
 * reception associates the remote URI with the connection (socket). This has the consequence that if the server wants
 * to either use a service, or reply to the remote URI, it will use on of these already allocated communication
 * resources.
 *
 * In the case of malformed MAL messages or communication errors, all resources related to the remote URI are released
 * and need to be reestablished.
 *
 * URIs:
 *
 * The TCPIP Transport generates URIs in the form of: {@code maltcp://<host>:<port or client ID>/<service id>}
 *
 * If a MAL instance does not offer any services then all of its endpoints get a Client URI. If a MAL instance offers at
 * least one service then all of its endpoints get a Server URI. A service provider communicates with a service consumer
 * with the communication channel that the service consumer initiated (uses bidirectional TCP/IP communication).
 *
 */
public class TCPIPTransport extends GENTransport {
	
	/**
	 * Logger
	 */
	public static final java.util.logging.Logger RLOGGER = Logger.getLogger("org.ccsds.moims.mo.mal.transport.tcpip");

	/**
	 * Port delimiter
	 */
	private static final char PORT_DELIMITER = ':';
	
	/**
	 * Service delimiter
	 */
	private static final char SERVICE_DELIMITER = '/';

	/**
	 * The server port that the TCP transport listens for incoming connections
	 */
	private final int serverPort;

	/**
	 * Server host, this can be one of the IP Addresses / hostnames of the host.
	 */
	private final String serverHost;

	/**
	 * Holds the server connection listener
	 */
	private TCPIPServerConnectionListener serverConnectionListener = null;

	/**
	 * Holds the list of data poller threads
	 */
	private final List<GENMessagePoller> pollerThreads = new ArrayList<GENMessagePoller>();

	/**
	 * Constructor.
	 *
	 * @param protocol
	 *            The protocol string.
	 * @param serviceDelim
	 *            The delimiter to use for separating the URL
	 * @param supportsRouting
	 *            True if routing is supported by the naming convention
	 * @param wrapBodyParts
	 *            True is body parts should be wrapped in BLOBs
	 * @param factory
	 *            The factory that created us.
	 * @param properties
	 *            The QoS properties.
	 * @throws MALException
	 *             On error.
	 */
	public TCPIPTransport(final String protocol, final char serviceDelim,
			final boolean supportsRouting, final MALTransportFactory factory,
			final java.util.Map properties) throws MALException {
		super(protocol, serviceDelim, supportsRouting, false, factory, properties);

		System.out.println("TCPIPTransport (constructor)");

		// decode configuration
		if (properties != null) {
			// host / ip adress
			if (properties.containsKey("org.ccsds.moims.mo.mal.transport.tcpip.host")) {
				this.serverHost = (String) properties.get("org.ccsds.moims.mo.mal.transport.tcpip.host");
			} else {
				this.serverHost = null; // this is only a client
			}

			// port
			if (properties.containsKey("org.ccsds.moims.mo.mal.transport.tcpip.port")) {
				this.serverPort = Integer.parseInt((String) properties.get("org.ccsds.moims.mo.mal.transport.tcpip.port"));
			} else {
				if (serverHost != null) {
					// this is a server, use default port
					this.serverPort = 61616;
				} else {
					// this is a client
					this.serverPort = 0; // 0 means this is a client
				}
			}
			
			// debug mode
			if (properties.containsKey("org.ccsds.moims.mo.mal.transport.tcpip.debug")) {
				String level = properties.get("org.ccsds.moims.mo.mal.transport.tcpip.debug").toString();
				try {
					Level parsedLevel = Level.parse(level);
					RLOGGER.setLevel(parsedLevel);
					RLOGGER.finest("Finest");
					RLOGGER.fine("fine");
					RLOGGER.info("info");
					RLOGGER.warning("warning");
					RLOGGER.severe("severe");
				} catch(IllegalArgumentException ex) {
					RLOGGER.log(Level.WARNING, "The debug level supplied by the parameter"
							+ "org.ccsds.moims.mo.mal.transport.tcpip.debug does not exist!"
							+ "Please provide a java-logging compatible debug level.");
				}
			} else {
				
			}
		} else {
			// default values
			this.serverPort = 0; // 0 means this is a client
			this.serverHost = null; // null means this is a client
		}

		RLOGGER.log(Level.INFO, "TCPIP Wrapping body parts set to  : {0}", this.wrapBodyParts);
	}

	/**
	 * Initialize a server socket, if this is a provider
	 */
	@Override
	public void init() throws MALException {
		super.init();
		System.out.println("TCPIPTransport.init()");

		if (serverHost != null) {
			// this is also a server (i.e. provides some services)
			RLOGGER.log(Level.INFO, "Starting TCPIP Server Transport on port {0}", serverPort);

			// start server socket on predefined port / interface
			try {
				InetAddress serverHostAddr = InetAddress.getByName(serverHost);
				ServerSocket serverSocket = new ServerSocket(serverPort, 0, serverHostAddr);

				// create thread that will listen for connections
				synchronized (this) {
					serverConnectionListener = new TCPIPServerConnectionListener(this, serverSocket);
					serverConnectionListener.start();
				}

				RLOGGER.log(Level.INFO, "Started TCPIP Server Transport on port {0}", serverPort);
			} catch (java.net.BindException ex) {
				String errorMsg = "The server socket cannot be created, because another process is already using this socket!"
						+ "\nEnsure that you are instantiating a MAL provider with a unique configuration file and a unique port set. "
						+ "Also, check that the port which you set in your configuration file, is not used by other processes.";
				RLOGGER.severe(errorMsg);
				throw new MALException(errorMsg);
			} catch (Exception ex) {
				throw new MALException("Error initialising TCP Server", ex);
			}
		}

	}

	@Override
	public MALBrokerBinding createBroker(final String localName,
			final Blob authenticationId, final QoSLevel[] expectedQos,
			final UInteger priorityLevelNumber, final Map defaultQoSProperties)
			throws MALException {
		System.out.println("TCPIPTransport.createBroker()");
		// not support by TCPIP transport
		return null;
	}

	@Override
	public MALBrokerBinding createBroker(final MALEndpoint endpoint,
			final Blob authenticationId, final QoSLevel[] qosLevels,
			final UInteger priorities, final Map properties)
			throws MALException {
		System.out.println("TCPIPTransport.createBroker() 2");
		// not support by TCPIP transport
		return null;
	}

	/**
	 * The MAL TCPIP binding supports SEND, SUBMIT, REQUEST, INVOKE and
	 * PROGRESS. PUBSUB is not supported. A MAL implementation layer has to
	 * support PUBSUB itself.
	 */
	@Override
	public boolean isSupportedInteractionType(final InteractionType type) {
		return InteractionType.PUBSUB.getOrdinal() != type.getOrdinal();
	}

	/**
	 * The MAL TCPIP binding supports all QoS levels.
	 */
	@Override
	public boolean isSupportedQoSLevel(final QoSLevel qos) {
		return true;
	}

	/**
	 * Close all pollers and socket connections and then close the transport
	 * itself.
	 */
	@Override
	public void close() throws MALException {

		RLOGGER.info("Closing TCPIPTransport...");
		
		synchronized (this) {
			
			TCPIPConnectionPoolManager.INSTANCE.close();
			
			for (GENMessagePoller entry : pollerThreads) {
				entry.close();
			}

			pollerThreads.clear();
		}

		super.close();

		synchronized (this) {
			if (null != serverConnectionListener) {
				serverConnectionListener.interrupt();

			}
		}
	}
	
	/**
	 * Internal method for encoding the message.
	 *
	 * @param destinationRootURI
	 *            The destination root URI.
	 * @param destinationURI
	 *            The complete destination URI.
	 * @param multiSendHandle
	 *            Handle for multi send messages.
	 * @param lastForHandle
	 *            true if last message in a multi send.
	 * @param targetURI
	 *            The target URI.
	 * @param msg
	 *            The message to send.
	 * @return The message holder for the outgoing message.
	 * @throws Exception
	 *             if an error.
	 */
	@Override
	protected GENOutgoingMessageHolder internalEncodeMessage(
			final String destinationRootURI, final String destinationURI,
			final Object multiSendHandle, final boolean lastForHandle,
			final String targetURI, final GENMessage msg) throws Exception {

		try {
			// try to encode the TCPIP Message
			final ByteArrayOutputStream lowLevelOutputStream = new ByteArrayOutputStream();
			((TCPIPMessage) msg).encodeMessage(getStreamFactory(), lowLevelOutputStream);
			byte[] data = lowLevelOutputStream.toByteArray();

			// message is encoded!
			LOGGER.log(Level.FINE, "GEN Sending data to {0} : {1}", new Object[] { targetURI, new PacketToString(data) });

			return new GENOutgoingMessageHolder(destinationRootURI, destinationURI, multiSendHandle, lastForHandle, msg, data);
		} catch (MALException ex) {
			LOGGER.log(Level.SEVERE, "GEN could not encode message!", ex);
			throw new MALTransmitErrorException(msg.getHeader(), new MALStandardError(MALHelper.BAD_ENCODING_ERROR_NUMBER, null), null);
		}
	}
  
	/**
	 * Create an endpoint. This endpoint has an url assigned, which is made of
	 * the base url plus the service identifier.
	 */
	@Override
	protected GENEndpoint internalCreateEndpoint(final String localName,
			final String routingName, final Map properties) throws MALException {
		System.out.println("TCPIPTransport.internalCreateEndpoint() with uri: " + uriBase);
		
		return new TCPIPEndpoint(this, localName, routingName, uriBase + routingName, wrapBodyParts);
	}

	/**
	 * Create a transport address.
	 * 
	 * If this is a pure client, create a new client socket. The local port that
	 * is automatically assigned to this socket (the ephemeral socket) will be
	 * used as the port number for this client.
	 * 
	 * If this is a provider, the host and port as defined in the configuration
	 * file are used.
	 */
	@Override
	protected String createTransportAddress() throws MALException {

		String addr;
		if (serverHost == null) {
			addr = getDefaultHost() + PORT_DELIMITER + getClientPort();
		} else {
			// this a server (and potentially a client)			
			addr = serverHost + PORT_DELIMITER + serverPort;
		}

		RLOGGER.info("Transport address created is " + addr);
		
		return addr;
	}  

	/**
	 * Called for received messages.
	 * 
	 * The URI from and URI to parameters for the message header are set according to the address
	 * information. Later, during decoding, the full URL is formed from the information in the message
	 * header.
	 * 
	 * The raw message data is split up in a packet, describing the header, and a packet for the body.
	 * These are each decoded separately; the header is decoded using an implementation that follows the
	 * MAL TCPIP Transport Binding specification. The body is decoded using a split binary decoder.
	 */
	public GENMessage createMessage(final TCPIPPacketInfoHolder packetInfo) throws MALException {
		System.out.println("TCPIPTransport.createMessage() for decoding");

		String serviceDelimStr = Character.toString(serviceDelim);
		String from = packetInfo.getUriFrom().getValue();
		if (!from.endsWith(serviceDelimStr)) {
			from += serviceDelimStr;
		}
		String to = packetInfo.getUriTo().getValue();
		if (!to.endsWith(serviceDelimStr)) {
			to += serviceDelimStr;
		}
		
		// preset header
		TCPIPMessageHeader header = new TCPIPMessageHeader(new URI(from), new URI(to));
		
		// msg with decoded header and empty body
		byte[] packetData = packetInfo.getPacketData();
		TCPIPMessage msg = new TCPIPMessage(wrapBodyParts, header, qosProperties, packetData, getStreamFactory());
		
		int decodedHeaderBytes = ((TCPIPMessageHeader)msg.getHeader()).decodedHeaderBytes;
		int bodySize = ((TCPIPMessageHeader)msg.getHeader()).getBodyLength() + 23 - decodedHeaderBytes;
		
		// copy body to separate packet
		byte[] bodyPacketData = new byte[bodySize];
		System.arraycopy(packetData, decodedHeaderBytes, bodyPacketData, 0, bodySize);

		// debug information
		System.out.println("TCPIPTransport.createMessage() Header results:");
		System.out.println(msg.getHeader().toString());
		System.out.println("TCPIPTransport.createMessage() TRYING TO DECODE BODY");
		System.out.println("---------------------------------------");
		System.out.println("TCPIPTransport.createMessage() Total msg in bytes:");
		for (byte b2 : packetData) {
			System.out.print(Integer.toString(b2 & 0xFF, 10) + " ");
		}
		System.out.println("\nDecoded header bytes: " + decodedHeaderBytes);
		System.out.println("Body: sz=" + bodyPacketData.length + " contents=");
		for (byte b2 : bodyPacketData) {
			System.out.print(Integer.toString(b2 & 0xFF, 10) + " ");
		}
		System.out.println("\n---------------------------------------");
		System.out.println();
		
		// decode the body
		TCPIPMessage msg2 = new TCPIPMessage(wrapBodyParts, (TCPIPMessageHeader)msg.getHeader(), qosProperties,
				bodyPacketData, new SplitBinaryStreamFactory());
		
		return msg2;
	}

	/**
	 * Create a message sender which will send out the message passed to this
	 * method. If no socket exists yet on the port defined by the destination
	 * port of the message, a new socket will be created and stored in the
	 * connection pool.
	 * 
	 * Create also a data reader thread for this socket in order to read
	 * messages from it no need to register this as it will automatically
	 * terminate when the underlying connection is terminated.
	 */
	@Override
	protected GENMessageSender createMessageSender(GENMessage msg,
			String remoteRootURI) throws MALException,
			MALTransmitErrorException {
		System.out.println("TCPIPTransport.createMessageSender()");
		try {			

			URI from = msg.getHeader().getURIFrom();
			ConnectionTuple fromCt = getConnectionParts(from.toString());
			ConnectionTuple toCt = getConnectionParts(remoteRootURI);

			// create a message sender and receiver for the socket
			Socket s = TCPIPConnectionPoolManager.INSTANCE.get(fromCt.port);
			s.connect(new InetSocketAddress(toCt.host, toCt.port));
			TCPIPTransportDataTransceiver trans = createDataTransceiver(s);
		    System.out.println("transport.createMessageSender() SERVERSOCKET: "
		    		+ fromCt.host + ":" + s.getLocalPort() + " (was " + fromCt.port + ")");
			System.out.println("ConnectionPool @ transport.createMessageSender():");
			System.out.println(TCPIPConnectionPoolManager.INSTANCE.toString());
					    
		    RLOGGER.fine("Original message for sending: " + msg.toString());
		    
			GENMessagePoller rcvr = new GENMessagePoller<TCPIPPacketInfoHolder>(this, trans,
					trans, new TCPIPMessageDecoderFactory());
			rcvr.setRemoteURI(remoteRootURI);
			rcvr.start();

			pollerThreads.add(rcvr);

			return trans;
		} catch (NumberFormatException nfe) {
			LOGGER.log(Level.WARNING,
					"Have no means to communicate with client URI : {0}",
					remoteRootURI);
			throw new MALException(
					"Have no means to communicate with client URI : "
							+ remoteRootURI);
		} catch (UnknownHostException e) {
			LOGGER.log(Level.WARNING, "TCPIP could not find host :{0}",
					remoteRootURI);
			LOGGER.log(Level.FINE, "TCPIP could not find host :"
					+ remoteRootURI, e);
			throw new MALTransmitErrorException(msg.getHeader(),
					new MALStandardError(
							MALHelper.DESTINATION_UNKNOWN_ERROR_NUMBER, null),
					null);
		} catch (java.net.ConnectException e) {
			LOGGER.log(Level.WARNING, "TCPIP could not connect to :{0}",
					remoteRootURI);
			LOGGER.log(Level.FINE, "TCPIP could not connect to :"
					+ remoteRootURI, e);
			throw new MALTransmitErrorException(
					msg.getHeader(),
					new MALStandardError(
							MALHelper.DESTINATION_TRANSIENT_ERROR_NUMBER, null),
					null);
		} catch (IOException e) {
			// there was a communication problem, we need to clean up the
			// objects we created in the meanwhile
			communicationError(remoteRootURI, null);

			// rethrow for higher MAL leyers
			throw new MALException("IO Exception", e);
		}
	}

  /**
   * Allows transport derived from this, where the message encoding is changed for example, to easily replace the
   * message transceiver without worrying about the TCPIP connection
   *
   * @param socket the TCPIP socket
   * @return the new transceiver
   * @throws IOException if there is an error
   */
  protected TCPIPTransportDataTransceiver createDataTransceiver(Socket socket) throws IOException
  {
		System.out.println("TCPIPTransport.createDataTransceiver()");
		return new TCPIPTransportDataTransceiver(socket, serverPort);
  }

  /**
   * Provide a default IP address for this host
   *
   * @return The transport specific address part.
   * @throws MALException On error
   */
	private String getDefaultHost() throws MALException {
		
		try {
			// Build url string
			final InetAddress addr = Inet4Address.getLocalHost();
			final StringBuilder hostAddress = new StringBuilder();
			if (addr instanceof Inet6Address) {
				RLOGGER.fine("TCPIP Address class is IPv6");
				hostAddress.append('[');
				hostAddress.append(addr.getHostAddress());
				hostAddress.append(']');
			} else {
				hostAddress.append(addr.getHostAddress());
			}

			return hostAddress.toString();
		} catch (UnknownHostException ex) {
			throw new MALException("Could not determine local host address", ex);
		}
	}

	/**
	 * Get a client port by first creating a socket. The socket is bound to an ephemeral port
	 * which is returned so that it can be used by this client. The socket is stored in the
	 * connection pool manager for later use.
	 * @return the unique port, bound to an existing client socket
	 */
	private int getClientPort() {
		
		// pre-allocate a socket
		Socket s = TCPIPConnectionPoolManager.INSTANCE.getAny();
		return s.getLocalPort();
	}
  
	public char getServiceDelim() {
		return this.serviceDelim;
	}
	
	/**
	 * Get a tuple from a URI which contains the host and port information.
	 * 
	 * @param addr
	 * @return
	 * @throws MALException
	 */
	private ConnectionTuple getConnectionParts(String addr) throws MALException {
		
		// decode address
		String targetAddress = addr.replaceAll(protocol + protocolDelim, "");
		targetAddress = targetAddress.replaceAll(protocol, "");
		
		// remove service URI part, i.e. the part after the service delimiter
		int serviceIdx = targetAddress.indexOf(serviceDelim);
		if (serviceIdx >= 0) {
			targetAddress = targetAddress.substring(0, serviceIdx);
		}
		targetAddress = targetAddress.replaceAll(Character.toString(serviceDelim), "");

		if (!targetAddress.contains(":")) {
			// malformed URI
			throw new MALException("Malformed URI:" + addr);
		}

		String host = targetAddress.split(":")[0];
		int port = Integer.parseInt(targetAddress.split(":")[1]);
		return new ConnectionTuple(host, port);
	}
	
	/**
	 * A container class storing the host and port of some address.
	 * @author Rian van Gijlswijk <r.vangijlswijk@telespazio-vega.de>
	 *
	 */
	public static class ConnectionTuple {
		public String host;
		public int port;
		public ConnectionTuple(String h, int p) {
			this.host = h;
			this.port = p;
		}
	}
}
