package esa.mo.mal.transport.tcpip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALOperation;
import org.ccsds.moims.mo.mal.encoding.MALElementOutputStream;
import org.ccsds.moims.mo.mal.encoding.MALElementStreamFactory;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;

import esa.mo.mal.encoder.binary.fixed.FixedBinaryElementOutputStream;
import esa.mo.mal.encoder.tcpip.TCPIPEncoder;
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

		// encode header and body using TCPIPEncoder class
		ByteArrayOutputStream msgBaos = new ByteArrayOutputStream();
		MALElementOutputStream messageEncoder = streamFactory.createOutputStream(msgBaos);

		super.encodeMessage(streamFactory, messageEncoder, msgBaos, true);		
		
		byte[] msgBuf = msgBaos.toByteArray();			

		// overwrite bodysize parameter
		int totalMessageLength = msgBaos.size();
		int bodySize = totalMessageLength - 23;
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		b.putInt(bodySize);
		byte[] bodySizeBuf = b.array();
		
		System.arraycopy(bodySizeBuf, 0, msgBuf, 19, 4);
		
		System.out.println("buffer: " + new String(msgBuf));	

		try {
			lowLevelOutputStream.write(msgBuf);
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
