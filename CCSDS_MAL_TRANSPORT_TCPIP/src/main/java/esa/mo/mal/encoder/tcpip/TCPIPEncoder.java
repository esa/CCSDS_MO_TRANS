package esa.mo.mal.encoder.tcpip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.Element;
import org.ccsds.moims.mo.mal.structures.UInteger;

import esa.mo.mal.encoder.binary.fixed.FixedBinaryEncoder;
import esa.mo.mal.encoder.binary.split.SplitBinaryEncoder;
import esa.mo.mal.encoder.gen.GENEncoder;

public class TCPIPEncoder extends FixedBinaryEncoder {
		
	public TCPIPEncoder(final OutputStream os) {
		super(new FixedStreamHolder(os));
	}

	private TCPIPEncoder(StreamHolder sh) {
		super(sh);
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

		long MAX_STRING_LENGTH = 2 * (long)Integer.MAX_VALUE + 1;
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
	
	public void encodeList(List<Element> elements) throws MALException {
		
		encodeUInteger(new UInteger(elements.size()));
		for (Element element : elements) {
			element.encode(this);
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
