package esa.mo.mal.encoder.tcpip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALListEncoder;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.UInteger;

import esa.mo.mal.encoder.binary.fixed.FixedBinaryEncoder;

public class TCPIPHeaderEncoder extends FixedBinaryEncoder {
		
	public TCPIPHeaderEncoder(final OutputStream os) {
		super(new TCPIPStreamHolder(os));
	}

	private TCPIPHeaderEncoder(StreamHolder sh) {
		super(sh);
	}
	
	@Override
	public MALListEncoder createListEncoder(List list) throws IllegalArgumentException, MALException {
		
		// encode number of elements
		encodeUInteger(new UInteger(list.size()));
		
		return this;		
	}
	
	/**
	 * A MAL string is encoded as follows:
	 * - String Length: UInteger
	 * - Character: UTF-8, variable size, multiple of octet
	 * The field 'string length' shall be assigned with the number of octets required to
	 * encode the character of the string
	 * @param val The string to encode
	 * @throws MALException if the string to encode is too large
	 */
	@Override
	public void encodeString(String val) throws MALException {

		long MAX_STRING_LENGTH = 2 * (long) Integer.MAX_VALUE + 1;
		byte[] output = val.getBytes(UTF8_CHARSET);
		
		if (output.length > MAX_STRING_LENGTH) {
			throw new MALException("The string length is greater than 2^32 -1 bytes! Please provide a shorter string.");
		}

		encodeUInteger(new UInteger(output.length));
		try {
			outputStream.directAdd(output);
		} catch (IOException ex) {
			throw new MALException(ENCODING_EXCEPTION_STR, ex);
		}
	}
	
	public void encodeMALLong(Long val) throws MALException {
		
		try {
			outputStream.addSignedLong(val);
		} catch (IOException ex) {
			throw new MALException(ENCODING_EXCEPTION_STR, ex);
		}
	}
	
	public void encodeMALShort(short val) throws MALException {
		
		try {
			outputStream.addUnsignedInt16(val);
		} catch (IOException ex) {
			throw new MALException(ENCODING_EXCEPTION_STR, ex);
		}
	}
	
	@Override
	public void encodeUInteger(final UInteger value) throws MALException {
		
		try {
			((TCPIPStreamHolder) outputStream).addUnsignedVarint4((int) value.getValue());
		} catch (IOException ex) {
			throw new MALException(ENCODING_EXCEPTION_STR, ex);
		}
	}

	@Override
	public void encodeNullableIdentifier(final Identifier value) throws MALException {
		
		if (value != null) {
			// encode presence flag
			encodeBoolean(true);
			// encode element as String
			encodeIdentifier(value);
		} else {
			// encode presence flag
			encodeBoolean(false);
		}
	}
	
	@Override
	public void encodeIdentifier(final Identifier value) throws MALException {
		
		encodeString(value.getValue());
	}
	
	@Override
	public void encodeBlob(final Blob value) throws MALException {
		
		encodeUInteger(new UInteger(value.getLength()));
		
		if (value.getLength() > 0) {
			try {
				outputStream.addBytes(value.getValue());
			} catch (IOException ex) {
				throw new MALException(ENCODING_EXCEPTION_STR, ex);
			}
		}
	}
	
	public OutputStream getOutputStream() {
		
		return ((TCPIPStreamHolder)outputStream).getOutputStream();
	}
	
	public static class TCPIPStreamHolder extends FixedStreamHolder {
		
		public TCPIPStreamHolder(OutputStream outputStream) {
			super(outputStream);
		}
		
		public void addUnsignedVarint4(int value) throws IOException {
			
			while ((value & 0xFFFFFF80) != 0L) {
				outputStream.write((value & 0x7F) | 0x80);
				value >>>= 7;
			}
			outputStream.write(value & 0x7F);
		}

		public OutputStream getOutputStream() {
			return this.outputStream;
		}
	}
}
