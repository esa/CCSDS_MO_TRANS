package esa.mo.mal.encoder.tcpip;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;

import esa.mo.mal.encoder.gen.GENDecoder;
import esa.mo.mal.encoder.gen.GENElementInputStream;

public class TCPIPSplitBinaryElementInputStream extends GENElementInputStream {

	protected TCPIPSplitBinaryElementInputStream(GENDecoder pdec) {
		super(pdec);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Object readElement(final Object element, final MALEncodingContext ctx)
			throws IllegalArgumentException, MALException {
		return null;

	}

}
