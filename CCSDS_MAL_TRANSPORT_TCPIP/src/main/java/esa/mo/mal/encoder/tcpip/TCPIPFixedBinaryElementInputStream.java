package esa.mo.mal.encoder.tcpip;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.QoSLevel;
import org.ccsds.moims.mo.mal.structures.SessionType;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.structures.UShort;

import esa.mo.mal.encoder.binary.BinaryElementInputStream;
import esa.mo.mal.encoder.gen.GENDecoder;
import esa.mo.mal.transport.tcpip.TCPIPMessageHeader;

/**
 * Manage the decoding of an incoming TCPIP Message. Separate decoders are used for 
 * the message header and body. The header uses a custom implementation according to
 * MAL TCPIP Transport Binding specifications, and the body is split binary decoded.
 * 
 * @author Rian van Gijlswijk <r.vangijlswijk@telespazio-vega.de>
 *
 */
public class TCPIPFixedBinaryElementInputStream extends BinaryElementInputStream {
	
	public TCPIPFixedBinaryElementInputStream(final java.io.InputStream is) {
		super(new TCPIPFixedBinaryDecoder(is));
		System.out.println("TCPIPHeaderElementInputStream constructor 1");
	}
	
	protected TCPIPFixedBinaryElementInputStream(final byte[] src, final int offset) {
		super(new TCPIPFixedBinaryDecoder(src, offset));
		System.out.println("TCPIPHeaderElementInputStream constructor 2");
	}
	
	/**
	 * Read an element
	 */
	@Override
	public Object readElement(final Object element, final MALEncodingContext ctx)
			throws IllegalArgumentException, MALException {
		System.out.println("TCPIPHeaderElementInputStream.readElement()");

		if (element == ctx.getHeader()) {
			// header is decoded using custom tcpip decoder
			return decodeHeader(element);
		} else {
			// body is decoded using split binary decoder
			// Synchronize buffer reading offset as two decoders are used in parallel
//			int headerDecodingOffset = ((TCPIPHeaderDecoder)dec).getBufferOffset();
//			((TCPIPSplitBinaryDecoder)dec).setBufferOffset(headerDecodingOffset);

			
			return null;
		}
	}
	
	/**
	 * Decode the header
	 * 
	 * @param element
	 *            The header to decode
	 * @return The decoded header
	 * @throws MALException
	 */
	private Object decodeHeader(final Object element) throws MALException {
		
		if (!(element instanceof TCPIPMessageHeader)) {
			throw new MALException("Wrong header element supplied. Must be instance of TCPIPMessageHeader");
		}
		
		TCPIPMessageHeader header = (TCPIPMessageHeader)element;
				
		short versionAndSDU = dec.decodeUOctet().getValue();
		header.versionNumber = (versionAndSDU >> 0x5);
		short sduType = (short) (versionAndSDU & 0x1f);
		header.setServiceArea(new UShort(dec.decodeShort()));
		header.setService(new UShort(dec.decodeShort()));
		header.setOperation(new UShort(dec.decodeShort()));
		header.setAreaVersion(dec.decodeUOctet());

		short parts = dec.decodeUOctet().getValue();
		header.setIsErrorMessage((((parts & 0x80) >> 7) == 0x1));
		header.setQoSlevel(QoSLevel.fromOrdinal(((parts & 0x70) >> 4)));
		header.setSession(SessionType.fromOrdinal(parts & 0xF));
		Long transactionId = ((TCPIPFixedBinaryDecoder)dec).decodeMALLong();
		header.setTransactionId(transactionId);
		System.out.println("QOS DECODING: qos=" + header.getQoSlevel().getOrdinal() + " parts=" + parts);
		
		short flags = dec.decodeUOctet().getValue(); // flags
		boolean sourceIdFlag = (((flags & 0x80) >> 7) == 0x1);
		boolean destinationIdFlag = (((flags & 0x40) >> 6) == 0x1);
		boolean priorityFlag = (((flags & 0x20) >> 5) == 0x1);
		boolean timestampFlag = (((flags & 0x10) >> 4) == 0x1);
		boolean networkZoneFlag = (((flags & 0x8) >> 3) == 0x1);
		boolean sessionNameFlag = (((flags & 0x4) >> 2) == 0x1);
		boolean domainFlag = (((flags & 0x2) >> 1) == 0x1);
		boolean authenticationIdFlag = ((flags & 0x1) == 0x1);		
		
		header.setEncodingId(dec.decodeUOctet().getValue());
		int bodyLength = (int) dec.decodeInteger();
		header.setBodyLength(bodyLength);
		
		if (sourceIdFlag) {
			String sourceId = dec.decodeString();
			if (isURI(sourceId)) {
				header.setURIFrom(new URI(sourceId));
			} else {
				String from = header.getURIFrom() + sourceId;
				header.setURIFrom(new URI(from));
			}
		}
		if (destinationIdFlag) {
			String destinationId = dec.decodeString();
			String to = header.getURITo() + destinationId;
			header.setURITo(new URI(to));
		}
		if (priorityFlag) {
			header.setPriority(dec.decodeUInteger());
		}
		if (timestampFlag) {
			header.setTimestamp(dec.decodeTime());
		}
		if (networkZoneFlag) {
			header.setNetworkZone(dec.decodeIdentifier());
		}
		if (sessionNameFlag) {
			header.setSessionName(dec.decodeIdentifier());
		}
		if (domainFlag) {
			IdentifierList list = (IdentifierList) new IdentifierList().decode(dec);
			header.setDomain(list);
		}
		if (authenticationIdFlag) {
			header.setAuthenticationId(dec.decodeBlob());
		}	
		
		header.setInteractionType(sduType);
		header.setInteractionStage(sduType);
		
		header.decodedHeaderBytes = ((TCPIPFixedBinaryDecoder)dec).getBufferOffset();

		// debug information
		System.out.println("Decoded header:");
		System.out.println("---------------------------------------");
		System.out.println(element.toString());
		System.out.println("Decoded header bytes:");
		System.out.println(header.decodedHeaderBytes);
		System.out.println("---------------------------------------");		

		return header;
	}
	
	/**
	 * Is @param a URI?
	 * 
	 * @param uri
	 * @return
	 */
	private boolean isURI(String uri) {
		return uri.startsWith("maltcp://");
	}
}
