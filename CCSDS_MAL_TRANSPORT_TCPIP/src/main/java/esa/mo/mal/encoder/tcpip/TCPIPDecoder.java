package esa.mo.mal.encoder.tcpip;

import java.io.InputStream;

import org.ccsds.moims.mo.mal.MALException;

import esa.mo.mal.encoder.binary.BinaryDecoder;
import esa.mo.mal.encoder.binary.fixed.FixedBinaryDecoder;

public class TCPIPDecoder extends FixedBinaryDecoder {

	protected TCPIPDecoder(java.io.InputStream is) {
		super(new TCPIPBufferHolder(is, null, 0, 0));
	}
	
	public String decodeMALString() throws MALException {
		
	      return sourceBuffer.getString();
	}
	
	public Long decodeMALLong() throws MALException {
		
		return sourceBuffer.getSignedLong();
	}
	
	/**
	 * Internal class that implements the fixed length field decoding.
	 */
	protected static class TCPIPBufferHolder extends FixedBufferHolder {

		public TCPIPBufferHolder(InputStream is, byte[] buf, int offset, int length) {
			super(is, buf, offset, length);
		}
		
		public String getString() throws MALException {
			final long len = getUnsignedLong32();

			if (len > Integer.MAX_VALUE) {
				throw new MALException("Value is too big to decode! Please provide a string with a length lower than INT_MAX");
			}
			
			if (len >= 0) {
				checkBuffer((int) len);

				final String s = new String(buf, offset, (int) len,
						UTF8_CHARSET);
				offset += len;
				return s;
			}
			
			return null;
		}

	}

}