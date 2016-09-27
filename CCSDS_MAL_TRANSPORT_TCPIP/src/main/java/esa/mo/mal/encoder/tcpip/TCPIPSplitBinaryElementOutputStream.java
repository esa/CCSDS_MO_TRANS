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

public class TCPIPSplitBinaryElementOutputStream extends GENElementOutputStream {

	public TCPIPSplitBinaryElementOutputStream(OutputStream os) {
		super(os);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected GENEncoder createEncoder(OutputStream os) {
		// TODO Auto-generated method stub
		System.out.println("TCPIPSplitBinaryElementOutputStream.createEncoder()");		
		return new TCPIPEncoder(os);
	}

	@Override
	public void writeElement(final Object element, final MALEncodingContext ctx)
			throws MALException {
		System.out.println("TCPIPSplitBinaryElementOutputStream.writeElement(Object, MALEncodingContext)");		

		// encode using our own encoder
		if (null == enc) {
			this.enc = createEncoder(dos);
		}
		
		if (element == ctx.getHeader()) {
				
			encodeHeader(element);
			
		} else {
			
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
		enc.encodeBoolean(header.getURIFrom() != null);
		enc.encodeBoolean(header.getURITo() != null);
		enc.encodeBoolean(!header.getPriority().equals(0));
		enc.encodeBoolean(false);
		enc.encodeBoolean(false);
		enc.encodeBoolean(false);
		enc.encodeBoolean(false);
		enc.encodeBoolean(false);

		// set encoding id
		enc.encodeUOctet(new UOctet());
		
		// preset body length
		// TODO: set bodyLength after encoding whole message
		enc.encodeInteger(header.getBodyLength());
		
		// encode rest of header
		String uriFromStr = header.getURIFrom().toString();
		String uriToStr = header.getURITo().toString();
		enc.encodeString(uriFromStr);
		enc.encodeString(uriToStr);
	}
	
	private void encodeBody(final Object element) throws MALException {
		
		SplitBinaryEncoder sbEnc = new SplitBinaryEncoder(dos);
		
		if (element instanceof Element) {
			sbEnc.encodeElement((Element) element);
			
			// TODO: after successful body generation, write body length param to header part
		}
	}
}
