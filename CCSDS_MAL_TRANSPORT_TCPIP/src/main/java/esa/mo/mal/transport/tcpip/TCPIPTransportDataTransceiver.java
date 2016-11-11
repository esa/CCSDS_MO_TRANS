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

import esa.mo.mal.transport.gen.sending.GENMessageSender;
import esa.mo.mal.transport.gen.sending.GENOutgoingMessageHolder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Level;

import org.ccsds.moims.mo.mal.structures.URI;

import static esa.mo.mal.transport.tcpip.TCPIPTransport.RLOGGER;

/**
 * This class implements the low level data (MAL Message) transport protocol. The messages are encoded according to the
 * MAL TCPIP Transport Binding specification.
 * 
 * This class manages both the transmitting and receiving of messages.
 */
public class TCPIPTransportDataTransceiver implements esa.mo.mal.transport.gen.util.GENMessagePoller.GENMessageReceiver<TCPIPPacketInfoHolder>, GENMessageSender {

	protected final Socket socket;
	protected final DataOutputStream socketWriteIf;
	protected final DataInputStream socketReadIf;
	protected final int serverPort;
	
	/**
	 * Constructor.
	 *
	 * @param socket the TCPIP socket.
	 * @throws IOException if there is an error.
	 */
	public TCPIPTransportDataTransceiver(Socket socket, int serverPort) throws IOException {
		this.socket = socket;
		this.serverPort = serverPort;
		socketWriteIf = new DataOutputStream(socket.getOutputStream());
		socketReadIf = new DataInputStream(socket.getInputStream());
	}

	/**
	 * Send an encoded message out over the socket
	 * @param packetData
	 * 				The encoded message to send
	 */
	@Override
	public void sendEncodedMessage(GENOutgoingMessageHolder packetData) throws IOException {

		StringBuilder sb = new StringBuilder();
		sb.append("\nTCPIPTransportDataTransciever.sendEncodedMessage()");
		sb.append("\nWriting to socket:");
		sb.append("\n---------------------------------------");
		sb.append("\npacketData length: " + packetData.getEncodedMessage().length + "\n");
		for (byte b2 : packetData.getEncodedMessage()) {
			sb.append(Integer.toString(b2 & 0xFF, 10) + " ");
		}
		sb.append("\n---------------------------------------");
		RLOGGER.log(Level.FINEST, sb.toString());
		
		socketWriteIf.write(packetData.getEncodedMessage());
		socketWriteIf.flush();
	}

	/**
	 * Read an encoded message from the socket. The message is read into a byte
	 * array. The encoded message header contains the length of the
	 * variable-size body of the message. Therefore, this information is already
	 * extracted. The resulting length is used to extract a body message from
	 * the socket stream.
	 * 
	 * If this TCPIP transport instance is also a provider, it has initiated a
	 * server socket with a configuration-defined port. The incoming socket
	 * cannot have the same port, as it is not the same socket. Hence, the port
	 * of the URI-to parameter in the message is modified such that it equals
	 * the port of the server socket after the message is received. This ensures
	 * that the communication supports several sockets, one for provider-side
	 * and one for client-side, while still being compliant with the MAL
	 * restriction that every client/provider has exactly one unique address.
	 * @throws InterruptedException 
	 */
	@Override
	public TCPIPPacketInfoHolder readEncodedMessage() throws IOException {
		
		// get information
		String remoteHost = socket.getInetAddress().getHostAddress();
		int remotePort = socket.getPort();
		String localHost = socket.getLocalAddress().getHostAddress();
		int localPort = socket.getLocalPort();

		// figure out length according to mal message mapping to determine byte arr length, then read the rest.		
		final int headerSize = 23;
		byte[] rawHeader = new byte[headerSize];
		
		try{
			int bytesRead = socketReadIf.read(rawHeader, 0, headerSize);
			if (bytesRead < 1) {
				return null;
			}
		} catch (NullPointerException headerReadNullPointer) {
			RLOGGER.warning("NullpointerException occured while reading header! " + headerReadNullPointer.getMessage());
		} catch (IndexOutOfBoundsException headerReadOutOfBounds) {
			RLOGGER.warning("IndexOutOfBoundsException occured while reading header! " + headerReadOutOfBounds.getMessage());			
		} catch (SocketException socketExc) {
//			TCPIPTransport.RLOGGER.warning("SocketException occured while reading header! " + socketExc.getMessage());			
			return null;
		}
		
		byte[] bodyLengthParam = Arrays.copyOfRange(rawHeader, 19, 23);
		int bodyLength = byteArrayToInt(bodyLengthParam);
		
		// read body
	    byte[] bodyData = new byte[bodyLength];
	    try {
	    	socketReadIf.readFully(bodyData);
	    } catch (EOFException bodyReadEof) {
	    	RLOGGER.warning("EOF reached for input stream! " + bodyReadEof.getMessage());
	    } catch (IOException bodyReadIo) {
	    	RLOGGER.warning("Socket connection closed while reading!");
	    }
	
		// merge header and body
	    byte[] totalPacketData = new byte[headerSize + bodyLength];
	    System.arraycopy(rawHeader, 0, totalPacketData, 0, headerSize);
	    System.arraycopy(bodyData, 0, totalPacketData, headerSize, bodyLength);
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("\nTCPIPTransportDataTransciever.readEncodedMessage()");
	    sb.append("\nReading from socket:");
	    sb.append("\n---------------------------------------");
	    sb.append("\ntotalPacketData headerLength: " + rawHeader.length + ", BodyLength: " + bodyLength + ", length: " + totalPacketData.length + "\n");
		for (byte b2 : totalPacketData) {
			sb.append(Integer.toString(b2 & 0xFF, 10) + " ");
		}
	    sb.append("\n---------------------------------------");
		RLOGGER.log(Level.FINEST, sb.toString());
		
		// if this is also a provider, update the localport to equal the port of the server socket.
		if (serverPort > 0) {
			localPort = serverPort;
		}
		RLOGGER.log(Level.FINEST, "Local addr: " + localHost + ":" + localPort);
		RLOGGER.log(Level.FINEST, "Remote addr: " + remoteHost + ":" + remotePort);
		
		URI from = new URI("maltcp://" + remoteHost + ":" + remotePort);
		URI to = new URI("maltcp://" + localHost + ":" + localPort);
		
		return new TCPIPPacketInfoHolder(totalPacketData, from, to);
	}

	/**
	 * Close the socket
	 */
	@Override
	public void close() {
		
		RLOGGER.log(Level.FINE, "Closing client connection at port {0}", socket.getLocalPort());
		
		try {
			socket.close();
		} catch (IOException e) {
			RLOGGER.warning("An exception occured while trying to close the socket! "
					+ e.getMessage());
			e.printStackTrace();
		}
	}
  
	/**
	 * Convert a 4-byte byte array to an integer.
	 * 
	 * @param b
	 * 		The byte array to convert
	 * @return int
	 */
	public static int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
				| (b[0] & 0xFF) << 24;
	}
}
