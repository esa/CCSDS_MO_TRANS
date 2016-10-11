package esa.mo.mal.encoder.tcpip;

import java.io.OutputStream;

import esa.mo.mal.encoder.binary.split.SplitBinaryEncoder;
import esa.mo.mal.encoder.gen.GENElementOutputStream;
import esa.mo.mal.encoder.gen.GENEncoder;
import esa.mo.mal.transport.tcpip.TCPIPMessageHeader;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.UOctet;
import org.ccsds.moims.mo.mal.structures.URI;

public class TCPIPSplitBinaryElementOutputStream extends GENElementOutputStream {
	
	private GENEncoder hdrEnc;

	public TCPIPSplitBinaryElementOutputStream(OutputStream os) {
		super(os);
		this.hdrEnc = createHeaderEncoder(os);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected GENEncoder createEncoder(OutputStream os) {
		// TODO Auto-generated method stub
		System.out.println("TCPIPSplitBinaryElementOutputStream.createEncoder()");
		return new SplitBinaryEncoder(os);
	}
	
	private GENEncoder createHeaderEncoder(OutputStream os) {
		System.out.println("TCPIPSplitBinaryElementOutputStream.createHeaderEncoder()");
		return new TCPIPHeaderEncoder(os);
	}

	@Override
	public void writeElement(final Object element, final MALEncodingContext ctx)
			throws MALException {
		System.out.println("TCPIPSplitBinaryElementOutputStream.writeElement(Object, MALEncodingContext)");		
				
		if (element == ctx.getHeader()) {
			// header is encoded using tcpip custom encoder
			encodeHeader(element);
		} else {
			// body is encoded using default split binary encoder
//			super.writeElement(element, ctx);
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
		
		UOctet test = new UOctet(versionAndSDU);
		hdrEnc.encodeUOctet(test);
		hdrEnc.encodeShort((short)header.getServiceArea().getValue());
		hdrEnc.encodeShort((short)header.getService().getValue());
		hdrEnc.encodeShort((short)header.getOperation().getValue());
		hdrEnc.encodeUOctet(header.getAreaVersion());
		
		short parts = (short)(((header.getIsErrorMessage() ? 0x1 : 0x0 ) << 7) 
				| (header.getQoSlevel().getOrdinal() << 4) 
				| header.getSession().getOrdinal());
		
		System.out.println("QOS ENCODING: qos=" + header.getQoSlevel().getOrdinal() + " parts=" + parts);

		hdrEnc.encodeUOctet(new UOctet(parts));
		((TCPIPHeaderEncoder)hdrEnc).encodeMALLong(header.getTransactionId());

		// set flags
		hdrEnc.encodeUOctet(getFlags(header));
		// set encoding id
		hdrEnc.encodeUOctet(new UOctet(header.getEncodingId()));
		
		// preset body length. Allocate four bytes.
		// TODO: set bodyLength after encoding whole message
		hdrEnc.encodeInteger(0);
		
		// encode rest of header
		if (!header.getServiceFrom().isEmpty()) {
			// TODO ensure that urifrom is also correctly encoded in ScalAgent RID case
			hdrEnc.encodeString(header.getURIFrom().toString());
		}
		if (!header.getServiceTo().isEmpty()) {
			hdrEnc.encodeString(getLocalNamePart(header.getURITo()));
		}
		if (header.getPriority() != null) {
			hdrEnc.encodeUInteger(header.getPriority());
		}
		if (header.getTimestamp() != null) {
			hdrEnc.encodeTime(header.getTimestamp());
		}
		if (header.getNetworkZone() != null) {
			hdrEnc.encodeIdentifier(header.getNetworkZone());
		}
		if (header.getSessionName() != null) {
			hdrEnc.encodeIdentifier(header.getSessionName());
		}
		if (header.getDomain() != null && header.getDomain().size() > 0) {
			header.getDomain().encode(hdrEnc);
		}
		if (header.getAuthenticationId().getLength() > 0) {
			hdrEnc.encodeBlob(header.getAuthenticationId());
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
		if (header.getAuthenticationId().getLength() > 0) {
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
