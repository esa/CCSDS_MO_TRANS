package esa.mo.mal.encoder.tcpip;

import java.io.InputStream;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;
import org.ccsds.moims.mo.mal.structures.Element;
import org.ccsds.moims.mo.mal.structures.QoSLevel;
import org.ccsds.moims.mo.mal.structures.SessionType;
import org.ccsds.moims.mo.mal.structures.ULong;
import org.ccsds.moims.mo.mal.structures.UOctet;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.structures.UShort;

import esa.mo.mal.encoder.binary.split.SplitBinaryDecoder;
import esa.mo.mal.encoder.gen.GENDecoder;
import esa.mo.mal.encoder.gen.GENElementInputStream;
import esa.mo.mal.transport.tcpip.TCPIPMessageHeader;

public class TCPIPSplitBinaryElementInputStream extends GENElementInputStream {
	
	private InputStream is;

	public TCPIPSplitBinaryElementInputStream(final java.io.InputStream is) {
		super(new TCPIPDecoder(is));
		this.is = is;
	}
	
	protected TCPIPSplitBinaryElementInputStream(GENDecoder pdec) {
		super(pdec);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Object readElement(final Object element, final MALEncodingContext ctx)
			throws IllegalArgumentException, MALException {
		System.out.println("TCPIPSplitBinaryElementInputStream.readElement()");

		if (element == ctx.getHeader()) {
			return decodeHeader(element);
		} else {
			return decodeBody(element, (TCPIPMessageHeader) ctx.getHeader());
		}
	}
	
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
		header.setIsErrorMessage(((byte) parts >> 7) == 0x1);
		header.setQoSlevel(QoSLevel.fromOrdinal((parts >> 4) & 0x6));
		header.setSession(SessionType.fromOrdinal(parts & 0xf));
		Long transactionId = ((TCPIPDecoder)dec).decodeMALLong();
		header.setTransactionId(transactionId);
		
		short flags = dec.decodeUOctet().getValue(); // flags
		boolean sourceIdFlag = (((flags & 0x80) >> 7) == 0x1);
		boolean destinationIdFlag = (((flags & 0x40) >> 6) == 0x1);
		boolean priorityFlag = (((flags & 0x20) >> 5) == 0x1);
		boolean timestampFlag = (((flags & 0x10) >> 4) == 0x1);
		boolean networkZoneFlag = (((flags & 0x8) >> 3) == 0x1);
		boolean sessionNameFlag = (((flags & 0x4) >> 2) == 0x1);
		boolean domainFlag = (((flags & 0x2) >> 1) == 0x1);
		boolean authenticationIdFlag = ((flags & 0x1) == 0x1);		
		
		dec.decodeOctet(); // encoding id
		int bodyLength = dec.decodeInteger();
		header.setBodyLength(bodyLength);
		
		if (sourceIdFlag) {
			String sourceId = dec.decodeString();
			String from = header.getURIFrom() + sourceId;
			header.setURIFrom(new URI(from));
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
			// TODO: implement
		}
		if (authenticationIdFlag) {
			header.setAuthenticationId(dec.decodeBlob());
		}	
		
		header.setInteractionType(sduType);
		header.setInteractionStage(sduType);

		System.out.println("Decoded header:");
		System.out.println("---------------------------------------");
		System.out.println(element.toString());
		System.out.println("---------------------------------------");
		

		return header;
	}
	
	private Object decodeBody(final Object element, TCPIPMessageHeader header) throws MALException {
		
		return null;

////		if (header.getRemainingEncodedData().length == 0) {
////			return null;
////		}
//		
//		SplitBinaryDecoder sbDec = new SplitBinaryDecoder(is);
//		try {
//			Element el = sbDec.decodeElement((Element) element);
//			return el;
//		} catch (IllegalArgumentException | MALException e) {
//			// TODO Auto-generated catch block
//			throw new MALException(e.getMessage());
//		}
	}

}
