package esa.mo.mal.encoder.tcpip;

import java.io.OutputStream;

import esa.mo.mal.encoder.binary.split.SplitBinaryEncoder;
import esa.mo.mal.encoder.gen.GENElementOutputStream;
import esa.mo.mal.encoder.gen.GENEncoder;
import esa.mo.mal.transport.tcpip.TCPIPMessageHeader;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;
import org.ccsds.moims.mo.mal.structures.Element;
import org.ccsds.moims.mo.mal.structures.UOctet;
import org.ccsds.moims.mo.mal.structures.URI;

public class TCPIPSplitBinaryElementOutputStream extends GENElementOutputStream {
	
	private OutputStream os;

	public TCPIPSplitBinaryElementOutputStream(OutputStream os) {
		super(os);
		this.os = os;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected GENEncoder createEncoder(OutputStream os) {
		// TODO Auto-generated method stub
		System.out.println("TCPIPSplitBinaryElementOutputStream.createEncoder()");
		this.os = os;		
		return null;
	}

	@Override
	public void writeElement(final Object element, final MALEncodingContext ctx)
			throws MALException {
		System.out.println("TCPIPSplitBinaryElementOutputStream.writeElement(Object, MALEncodingContext)");		

		// encode using our own encoder
		
		if (element == ctx.getHeader()) {

			this.enc = new TCPIPEncoder(dos);
			encodeHeader(element);

		} else {

			this.enc = new SplitBinaryEncoder(os);
			encodeBody(element);

		}
				
	}
	
	private void encodeHeader(final Object element) throws MALException {
		
		if (!(element instanceof TCPIPMessageHeader)) {
			throw new MALException("Wrong header element supplied. Must be instance of TCPIPMessageHeader");
		}
		
		
		TCPIPMessageHeader header = (TCPIPMessageHeader)element;
		
		System.out.println("TCPIPMessageHeader.encode()");

	    System.out.println("Header to encode:");
		System.out.println("---------------------------------------");
		System.out.println(header);
		System.out.println("---------------------------------------");

		// version number & sdu type
		byte versionAndSDU = (byte) (header.versionNumber << 5 | header.getSDUType());
		
		enc.encodeUOctet(new UOctet(versionAndSDU));
		enc.encodeShort((short)header.getServiceArea().getValue());
		enc.encodeShort((short)header.getService().getValue());
		enc.encodeShort((short)header.getOperation().getValue());
		enc.encodeUOctet(header.getAreaVersion());
		
		byte parts = (byte)((header.getIsErrorMessage() ? 0x1 : 0x0 ) | header.getQoSlevel().getOrdinal() << 4 | header.getSession().getOrdinal());

		enc.encodeUOctet(new UOctet(parts));
		((TCPIPEncoder)enc).encodeMALLong(header.getTransactionId());

		// set flags
		enc.encodeUOctet(getFlags(header));
		// set encoding id
		enc.encodeUOctet(new UOctet());
		
		// preset body length
		// TODO: set bodyLength after encoding whole message
		enc.encodeInteger(header.getBodyLength());
		
		// encode rest of header
		if (!header.getServiceFrom().isEmpty()) {
			((TCPIPEncoder)enc).encodeMALString(getLocalNamePart(header.getURIFrom()));
		}
		if (!header.getServiceTo().isEmpty()) {
			((TCPIPEncoder)enc).encodeMALString(getLocalNamePart(header.getURITo()));
		}
		if (header.getPriority() != null) {
			enc.encodeUInteger(header.getPriority());
		}
		if (header.getTimestamp() != null) {
			enc.encodeTime(header.getTimestamp());
		}
		if (header.getNetworkZone() != null) {
			enc.encodeIdentifier(header.getNetworkZone());
		}
		if (header.getSessionName() != null) {
			enc.encodeIdentifier(header.getSessionName());
		}
		if (header.getDomain() != null && header.getDomain().size() > 0) {
			// TODO: implement
		}
		if (header.getAuthenticationId() != null) {
			enc.encodeBlob(header.getAuthenticationId());
		}
	}
	
	private void encodeBody(final Object element) throws MALException {
		
		if (element instanceof Element) {
			enc.encodeElement((Element) element);
		}
	}
	
	private UOctet getFlags(TCPIPMessageHeader header) {
		
		short result = 0;
		if (!header.getServiceFrom().isEmpty()) {
			result |= (0x1 << 7);
		}
		if (!header.getServiceTo().isEmpty()) {
			result |= (0x1 << 6);
		}
		if (header.getPriority() != null) {
			result |= (0x1 << 5);
		}
		if (header.getTimestamp() != null) {
			result |= (0x1 << 4);
		}
		if (header.getNetworkZone() != null) {
			result |= (0x1 << 3);
		}
		if (header.getSessionName() != null) {
			result |= (0x1 << 2);
		}
		if (header.getDomain() != null && header.getDomain().size() > 0) {
			result |= (0x1 << 1);
		}
		if (header.getAuthenticationId() != null) {
			result |= 0x1;
		}
		
		return new UOctet(result);
	}
	
	private String getLocalNamePart(URI uri) {
		
		if (uri == null) {
			// TODO: log warning
			return "";
		}

		char serviceDelim = '/'; // TODO: retrieve from TCPIPTransport instance

		int idx = uri.toString().lastIndexOf(serviceDelim);
		if (uri.toString().length() > idx) {
			return uri.toString().substring(idx + 1);
		} else {
			return "";
		}
	}
}
