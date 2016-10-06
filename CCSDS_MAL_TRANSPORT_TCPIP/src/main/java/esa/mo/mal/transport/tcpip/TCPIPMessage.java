package esa.mo.mal.transport.tcpip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.logging.Logger;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALOperation;
import org.ccsds.moims.mo.mal.encoding.MALElementOutputStream;
import org.ccsds.moims.mo.mal.encoding.MALElementStreamFactory;

import esa.mo.mal.encoder.binary.split.SplitBinaryElementOutputStream;
import esa.mo.mal.encoder.binary.split.SplitBinaryStreamFactory;
import esa.mo.mal.transport.gen.GENMessage;
import esa.mo.mal.transport.gen.GENMessageHeader;

public class TCPIPMessage extends GENMessage {
	
	/**
	 * Logger
	 */
	public static final java.util.logging.Logger RLOGGER = Logger.getLogger("org.ccsds.moims.mo.mal.transport.tcpip");

	public TCPIPMessage(boolean wrapBodyParts,
			GENMessageHeader header, Map qosProperties, byte[] packet,
			MALElementStreamFactory encFactory) throws MALException {
		super(wrapBodyParts, true, header, qosProperties, packet, encFactory);
		// TODO Auto-generated constructor stub
		System.out.println("TCPIPMessage (constructor 1)");		
	}
	
	public TCPIPMessage(boolean wrapBodyParts, GENMessageHeader header, Map qosProperties, MALOperation operation,
	          MALElementStreamFactory encFactory, Object... body) throws MALInteractionException {
		super(wrapBodyParts, header, qosProperties, operation, body);
		System.out.println("TCPIPMessage (constructor 2)");		
	}
	
	@Override
	public void encodeMessage(final MALElementStreamFactory streamFactory,
			final MALElementOutputStream enc,
			final OutputStream lowLevelOutputStream, final boolean writeHeader)
			throws MALException {
		System.out.println("TCPIPMessage.encodeMessage()");
		System.out.println("TCPIPMessageBody: " + this.getBody().toString());

		// encode header and body using TCPIPEncoder class
		ByteArrayOutputStream hdrBaos = new ByteArrayOutputStream();
		ByteArrayOutputStream bodyBaos = new ByteArrayOutputStream();
		MALElementOutputStream headerEncoder = streamFactory.createOutputStream(hdrBaos);
		MALElementStreamFactory bodyStreamFactory = new SplitBinaryStreamFactory();
		MALElementOutputStream bodyEncoder = bodyStreamFactory.createOutputStream(bodyBaos);

		super.encodeMessage(streamFactory, headerEncoder, hdrBaos, true);
		super.encodeMessage(bodyStreamFactory, bodyEncoder, bodyBaos, false);
		
		int hdrSize = hdrBaos.size();

		byte[] hdrBuf = hdrBaos.toByteArray();	
		byte[] bodyBuf = bodyBaos.toByteArray();			

		// overwrite bodysize parameter
		int totalMessageLength = hdrBaos.size() + bodyBaos.size();
		int bodySize = totalMessageLength - 23;
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		b.putInt(bodySize);
		byte[] bodySizeBuf = b.array();
		
		System.arraycopy(bodySizeBuf, 0, hdrBuf, 19, 4);
		
//		System.out.println("buffer: sz: " + bodySize + ", buf: " + new String(hdrBuf));	
		System.out.println("Header: sz=" + hdrBuf.length + " contents=");
		for (byte b2 : hdrBuf) {
			System.out.print(Integer.toString(b2 & 0xFF, 10) + " ");
		}
		System.out.println("\nBody: sz=" + bodyBuf.length + " contents=");
		for (byte b2 : bodyBuf) {
			System.out.print(Integer.toString(b2 & 0xFF, 10) + " ");
		}
		System.out.println();

		try {
			lowLevelOutputStream.write(hdrBuf);
			if (this.getBody() != null) { 
				lowLevelOutputStream.write(bodyBuf);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return "TCPIPMessage {URIFrom:" 
			+ header.getURIFrom() 
			+ "URITo:" + header.getURITo()
			+ "}";		
	}
}
