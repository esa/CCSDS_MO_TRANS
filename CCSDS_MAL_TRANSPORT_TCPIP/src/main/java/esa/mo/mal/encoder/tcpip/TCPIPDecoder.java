package esa.mo.mal.encoder.tcpip;

import org.ccsds.moims.mo.mal.MALException;

import esa.mo.mal.encoder.binary.BinaryDecoder;
import esa.mo.mal.encoder.binary.fixed.FixedBinaryDecoder;

public class TCPIPDecoder extends FixedBinaryDecoder {

	protected TCPIPDecoder(java.io.InputStream is) {
		super(is);
	}
	
	public String decodeMALString() throws MALException {
		
		return decodeString();
	}
	
	public Long decodeMALLong() throws MALException {
		
		return sourceBuffer.getSignedLong();
	}

}