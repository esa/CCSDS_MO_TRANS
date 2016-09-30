package esa.mo.mal.encoder.tcpip;

import java.io.IOException;
import java.io.OutputStream;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.UInteger;

import esa.mo.mal.encoder.binary.BinaryEncoder.BinaryStreamHolder;
import esa.mo.mal.encoder.binary.fixed.FixedBinaryEncoder;

public class TCPIPEncoder extends FixedBinaryEncoder {
	
	public TCPIPEncoder(final OutputStream os) {
		super(new FixedStreamHolder(os));
	}

	protected TCPIPEncoder(StreamHolder os) {
		super(os);
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
	public void encodeMALString(String val) throws MALException {

		final long MAX_STRING_LENGTH = 2 * Integer.MAX_VALUE + 1;
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
		
	    try
	    {
	      outputStream.addSignedLong(val);
	    }
	    catch (IOException ex)
	    {
	      throw new MALException(ENCODING_EXCEPTION_STR, ex);
	    }
	}
	
	public void encodeMALShort(short val) throws MALException {
		try
	    {
	      outputStream.addUnsignedInt16(val);
	    }
	    catch (IOException ex)
	    {
	      throw new MALException(ENCODING_EXCEPTION_STR, ex);
	    }
	}
	
	public OutputStream getOutputStream() {
		return ((TCPIPStreamHolder)outputStream).getOutputStream();
	}
	
	public static class TCPIPStreamHolder extends FixedStreamHolder {
		
		public TCPIPStreamHolder(OutputStream outputStream) {
			super(outputStream);
		}

		public OutputStream getOutputStream() {
			return this.outputStream;
		}
	}
}
