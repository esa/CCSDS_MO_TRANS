package esa.mo.mal.transport.tcpip;

import java.io.OutputStream;
import java.util.Map;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALOperation;
import org.ccsds.moims.mo.mal.encoding.MALElementOutputStream;
import org.ccsds.moims.mo.mal.encoding.MALElementStreamFactory;

import esa.mo.mal.transport.gen.GENMessage;
import esa.mo.mal.transport.gen.GENMessageHeader;

public class TCPIPMessage extends GENMessage {

	public TCPIPMessage(boolean wrapBodyParts, boolean readHeader,
			GENMessageHeader header, Map qosProperties, byte[] packet,
			MALElementStreamFactory encFactory) throws MALException {
		super(wrapBodyParts, readHeader, header, qosProperties, packet, encFactory);
		// TODO Auto-generated constructor stub
	}
	
	public TCPIPMessage(boolean wrapBodyParts, GENMessageHeader header, Map qosProperties, MALOperation operation,
	          MALElementStreamFactory encFactory, Object... body) throws MALInteractionException {
		super(wrapBodyParts, header, qosProperties, operation, encFactory, body);
		System.out.println("TCPIPMessage (constructor)");		
	}
	
	@Override
	public void encodeMessage(final MALElementStreamFactory streamFactory,
			final MALElementOutputStream enc,
			final OutputStream lowLevelOutputStream, final boolean writeHeader)
			throws MALException {
		System.out.println("TCPIPMessage.encodeMessage()");
	}

}
