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
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

import org.ccsds.moims.mo.mal.structures.URI;

/**
 * This class implements the low level data (MAL Message) transport protocol. In order to differentiate messages with
 * each other, the protocol has a very simple format: |size|message|
 *
 * If the protocol uses a different message encoding this class can be replaced in the TCPIPTransport.
 *
 */
public class TCPIPTransportDataTransceiver implements esa.mo.mal.transport.gen.util.GENMessagePoller.GENMessageReceiver<TCPIPPacketInfoHolder>, GENMessageSender {
	
	public static final java.util.logging.Logger RLOGGER = Logger .getLogger("org.ccsds.moims.mo.mal.transport.tcpip");
	protected final Socket socket;
	protected final DataOutputStream socketWriteIf;
	protected final DataInputStream socketReadIf;
	
	/**
	 * Constructor.
	 *
	 * @param socket the TCPIP socket.
	 * @throws IOException if there is an error.
	 */
	public TCPIPTransportDataTransceiver(Socket socket) throws IOException {
		this.socket = socket;
		socketWriteIf = new DataOutputStream(socket.getOutputStream());
		socketReadIf = new DataInputStream(socket.getInputStream());
	}
	
//	public TCPIPTransportDataTransceiver(String host, int port) {
//		this.socket = new Socket(host, port);
//	}

	@Override
	public void sendEncodedMessage(GENOutgoingMessageHolder packetData) throws IOException {
		
		System.out.println("TCPIPTransportDataTransciever.sendEncodedMessage()");
		System.out.println("Writing to socket:");
		System.out.println("---------------------------------------");
		System.out.println("packetData length: " + packetData.getEncodedMessage().length);
		System.out.write(packetData.getEncodedMessage());
		System.out.println("---------------------------------------");
		
		socketWriteIf.write(packetData.getEncodedMessage());
		socketWriteIf.flush();
	}

	@Override
	public TCPIPPacketInfoHolder readEncodedMessage() throws IOException {

		// figure out length according to mal message mapping to determine byte arr length, then read the rest.
		
		final int headerSize = 23;
		byte[] rawHeader = new byte[headerSize];
		socketReadIf.read(rawHeader, 0, headerSize);
		byte[] bodyLengthParam = Arrays.copyOfRange(rawHeader, 19, 23);
		int bodyLength = byteArrayToInt(bodyLengthParam);
		
		// read body
	    byte[] bodyData = new byte[bodyLength];
		int bytesRead = socketReadIf.read(bodyData);
	
		// merge header and body
		// TODO: replace by system.arraycopy.
	    byte[] totalPacketData = new byte[headerSize + bodyLength];
	    for (int i = 0; i < headerSize + bodyLength; i++) {
	    	totalPacketData[i] = (i < headerSize ? rawHeader[i] : bodyData[i-headerSize]);
	    }
	    
		System.out.println("TCPIPTransportDataTransciever.readEncodedMessage()");
		System.out.println("Reading from socket:");
		System.out.println("---------------------------------------");
		System.out.println("totalPacketData headerLength: " + rawHeader.length + ", BodyLength: " + bodyLength + ", bytesRead: " + (headerSize+bytesRead) + ", length: " + totalPacketData.length);
		System.out.write(totalPacketData);
		System.out.println("\n---------------------------------------");

		String remoteHost = socket.getInetAddress().getCanonicalHostName();
		int remotePort = socket.getPort();
		String localHost = socket.getLocalAddress().getCanonicalHostName();
		int localPort = socket.getLocalPort();
		System.out.println("Remote addr: " + remoteHost + ":" + remotePort);
		
		URI from = new URI("maltcp://" + remoteHost + ":" + remotePort);
		URI to = new URI("maltcp://" + localHost + ":" + localPort);
		
		return new TCPIPPacketInfoHolder(totalPacketData, from, to);
  }

  @Override
  public void close()
  {
    try
    {
      socket.close();
    }
    catch (IOException e)
    {
      // ignore
    }
  }
  
  public static int byteArrayToInt(byte[] b) {
      return   b[3] & 0xFF |
              (b[2] & 0xFF) << 8 |
              (b[1] & 0xFF) << 16 |
              (b[0] & 0xFF) << 24;
  }
}
