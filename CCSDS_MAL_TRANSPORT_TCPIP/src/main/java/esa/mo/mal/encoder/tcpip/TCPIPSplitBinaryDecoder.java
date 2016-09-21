package esa.mo.mal.encoder.tcpip;

import java.util.List;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALListDecoder;

import esa.mo.mal.encoder.gen.GENDecoder;

public class TCPIPSplitBinaryDecoder extends GENDecoder {

	protected TCPIPSplitBinaryDecoder(BufferHolder sourceBuffer) {
		super(sourceBuffer);
		// TODO Auto-generated constructor stub
	}

	@Override
	public MALListDecoder createListDecoder(List list)
			throws IllegalArgumentException, MALException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getRemainingEncodedData() throws MALException {
		// TODO Auto-generated method stub
		return null;
	}

}
