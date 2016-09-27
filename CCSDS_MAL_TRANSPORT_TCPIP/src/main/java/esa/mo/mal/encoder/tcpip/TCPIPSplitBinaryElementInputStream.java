package esa.mo.mal.encoder.tcpip;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;
import org.ccsds.moims.mo.mal.structures.QoSLevel;
import org.ccsds.moims.mo.mal.structures.SessionType;
import org.ccsds.moims.mo.mal.structures.ULong;
import org.ccsds.moims.mo.mal.structures.UOctet;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.structures.UShort;

import esa.mo.mal.encoder.gen.GENDecoder;
import esa.mo.mal.encoder.gen.GENElementInputStream;
import esa.mo.mal.transport.tcpip.TCPIPMessageHeader;

public class TCPIPSplitBinaryElementInputStream extends GENElementInputStream {

	public TCPIPSplitBinaryElementInputStream(final java.io.InputStream is) {
		super(new TCPIPDecoder(is));
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
			return decodeBody(element);
		}
	}
	
	private Object decodeHeader(final Object element) throws MALException {
		
		if (!(element instanceof TCPIPMessageHeader)) {
			throw new MALException("Wrong header element supplied. Must be instance of TCPIPMessageHeader");
		}
		
//		enc.encodeUOctet(new UOctet(versionAndSDU));
//		enc.encodeUShort(header.getServiceArea());
//		enc.encodeUShort(header.getService());
//		enc.encodeUShort(header.getOperation());
//		enc.encodeUOctet(header.getAreaVersion());
//		enc.encodeUOctet(new UOctet(parts));
//		enc.encodeLong(header.getTransactionId());
//		enc.encodeUOctet(new UOctet()); // flags
//		enc.encodeUOctet(new UOctet());
//		enc.encodeInteger(header.getBodyLength());
		
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
//		boolean sourceIdFlag = dec.decodeBoolean();
//		boolean destinationIdFlag = dec.decodeBoolean();
		dec.decodeOctet(); // flags
		dec.decodeOctet(); // encoding id
		int bodyLength = dec.decodeInteger();
		header.setBodyLength(bodyLength);
		
		if (true) {
			String sourceId = dec.decodeString();
			header.setURIFrom(new URI(sourceId));
		}
		if (true) {
			String destinationId = dec.decodeString();
			header.setURITo(new URI(destinationId));
		}
		
		header.setInteractionType(sduType);
		header.setInteractionStage(sduType);

		System.out.println("Decoded header:");
		System.out.println("---------------------------------------");
		System.out.println(element.toString());
		System.out.println("---------------------------------------");
		
		return header;
	}
	
	private Object decodeBody(final Object element) {

//		SplitBinaryDecoder sbDec = new SplitBinaryDecoder(dec.g)
		
		return null;
		
	}

}
