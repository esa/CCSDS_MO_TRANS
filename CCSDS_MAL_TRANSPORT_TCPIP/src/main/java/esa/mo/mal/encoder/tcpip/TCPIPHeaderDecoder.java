package esa.mo.mal.encoder.tcpip;

import java.io.InputStream;
import java.util.List;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.UInteger;

import esa.mo.mal.encoder.binary.fixed.FixedBinaryDecoder;

public class TCPIPHeaderDecoder extends FixedBinaryDecoder {
	
	protected TCPIPHeaderDecoder(java.io.InputStream is) {
		super(new TCPIPBufferHolder(is, null, 0, 0));
	}
	
	public TCPIPHeaderDecoder(final BufferHolder srcBuffer) {
		super(srcBuffer);
	}
	
	public org.ccsds.moims.mo.mal.MALListDecoder createListDecoder(final List list) throws MALException {
		return new TCPIPHeaderListDecoder(list, sourceBuffer);
	}

	@Override
	public String decodeString() throws MALException {
		
	      return sourceBuffer.getString();
	}
	
	public Long decodeMALLong() throws MALException {
		
		return sourceBuffer.getSignedLong();
	}
	
	public UInteger decodeUInteger() throws MALException {
		
		return new UInteger(sourceBuffer.getUnsignedInt());
	}
	
	@Override
	public Identifier decodeNullableIdentifier() throws MALException {
		
		// decode presence flag
		boolean isNotNull = decodeBoolean();
		
		System.out.println("Decoding identifier. Is null: " + !isNotNull);
		
		// decode one element, or add null if presence flag indicates no element
		if (isNotNull) {
			return decodeIdentifier();
		}

		return null;
	}
	
	@Override
	public Integer decodeInteger() throws MALException {
		
		return ((TCPIPBufferHolder)sourceBuffer).get32();
	}
	
	/**
	 * Internal class that implements the fixed length field decoding.
	 */
	protected static class TCPIPBufferHolder extends FixedBufferHolder {

		public TCPIPBufferHolder(InputStream is, byte[] buf, int offset, int length) {
			super(is, buf, offset, length);
		}

		@Override
		public String getString() throws MALException {
			
			final long len = getUnsignedInt();
			System.out.print("Decode string: length " + len);

			if (len > Integer.MAX_VALUE) {
				throw new MALException("Value is too big to decode! Please provide a string with a length lower than INT_MAX");
			}
			
			if (len >= 0) {
				checkBuffer((int) len);

				final String s = new String(buf, offset, (int) len, UTF8_CHARSET);
				offset += len;
				System.out.println(" val " + s);
				return s;
			}
			
			return null;
		}
		
		@Override
		public int getUnsignedInt() throws MALException {
			
			int value = 0;
			int i = 0;
			int b;
			while (((b = get8()) & 0x80) != 0) {
				value |= (b & 0x7F) << i;
				i += 7;
			}
			return value | (b << i);
		}
		
		public int get32() throws MALException {
			
			checkBuffer(4);

			final int i = shiftOffsetAndReturnPrevious(4);
			return java.nio.ByteBuffer.wrap(getBuf(), i, 4).getInt() & 0xFFFFFFF;
		}
	}
}