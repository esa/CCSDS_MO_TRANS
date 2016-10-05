package esa.mo.mal.encoder.tcpip;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.Element;

import esa.mo.mal.encoder.binary.BinaryDecoder;
import esa.mo.mal.encoder.binary.fixed.FixedBinaryDecoder;
import esa.mo.mal.encoder.binary.split.SplitBinaryDecoder;
import esa.mo.mal.encoder.gen.GENDecoder;

public class TCPIPDecoder extends FixedBinaryDecoder {
	
	private GENDecoder sbDec;

	protected TCPIPDecoder(java.io.InputStream is) {
		super(new TCPIPBufferHolder(is, null, 0, 0));
		sbDec = new SplitBinaryDecoder(is);
	}
	
	public String decodeMALString() throws MALException {
		
	      return sourceBuffer.getString();
	}
	
	public Long decodeMALLong() throws MALException {
		
		return sourceBuffer.getSignedLong();
	}
	
//	public List<Element> decodeList() throws MALException {
//		
//		int sizeOfList = (int) decodeUInteger().getValue();
//		
//		List<Element> decodedElements = new ArrayList<Element>();
//	}
	
	public Object decodeBodyElement(Object element) throws MALException {
		
		if (element != null && element instanceof Element) {
			System.out.println("Element to decode: " + element.getClass().getCanonicalName() +", " + element.toString());
			return sbDec.decodeElement((Element) element);
		}
		return null;
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

				final String s = new String(buf, offset, (int) len, UTF8_CHARSET);
				offset += len;
				return s;
			}
			
			return null;
		}

	}

}