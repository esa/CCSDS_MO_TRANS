package esa.mo.mal.encoder.tcpip;

import java.io.OutputStream;

import esa.mo.mal.encoder.gen.GENElementOutputStream;
import esa.mo.mal.encoder.gen.GENEncoder;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;

public class TCPIPSplitBinaryElementOutputStream extends GENElementOutputStream {

	protected TCPIPSplitBinaryElementOutputStream(OutputStream os) {
		super(os);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected GENEncoder createEncoder(OutputStream arg0) {
		// TODO Auto-generated method stub
		System.out.println("TCPIPSplitBinaryElementOutputStream.createEncoder()");
		return null;
	}

	@Override
	public void writeElement(final Object element, final MALEncodingContext ctx)
			throws MALException {
		System.out.println("TCPIPSplitBinaryElementOutputStream.writeElement()");
	}
}
