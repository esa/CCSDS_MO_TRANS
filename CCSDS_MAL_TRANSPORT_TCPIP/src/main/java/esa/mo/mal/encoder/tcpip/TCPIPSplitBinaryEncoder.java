/* ----------------------------------------------------------------------------
 * Copyright (C) 2013      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO Split Binary encoder
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.mal.encoder.tcpip;

import java.io.IOException;
import java.io.OutputStream;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.FineTime;
import org.ccsds.moims.mo.mal.structures.Time;

/**
 * Implements the MALEncoder and MALListEncoder interfaces for a split binary
 * encoding.
 */
public class TCPIPSplitBinaryEncoder extends esa.mo.mal.encoder.binary.BinaryEncoder {
	private int openCount = 1;

	/**
	 * Constructor.
	 *
	 * @param os
	 *            Output stream to write to.
	 */
	public TCPIPSplitBinaryEncoder(final OutputStream os) {
		super(new SplitStreamHolder(os));
	}

	protected TCPIPSplitBinaryEncoder(final StreamHolder os) {
		super(os);
	}

	@Override
	public org.ccsds.moims.mo.mal.MALListEncoder createListEncoder(
			final java.util.List value) throws MALException {
		++openCount;

		return super.createListEncoder(value);
	}

	/**
	 * A MAL string is encoded as follows: - String Length: UInteger -
	 * Character: UTF-8, variable size, multiple of octet The field 'string
	 * length' shall be assigned with the number of octets required to encode
	 * the character of the string
	 * 
	 * @param val
	 *            The string to encode
	 * @throws MALException
	 *             if the string to encode is too large
	 */
	@Override
	public void encodeString(String val) throws MALException {

		try {
			outputStream.addString(val);
		} catch (IOException e) {
			throw new MALException(ENCODING_EXCEPTION_STR, e);
		}
	}
	
	@Override
	public void encodeNullableString(String value) throws MALException {
		
		try {
			if (value != null) {
				// encode presence flag
				outputStream.addNotNull();
				// encode element as String
				encodeString(value);
			} else {
				// encode presence flag
				outputStream.addIsNull();
				
			}
		} catch (IOException e) {
			throw new MALException(ENCODING_EXCEPTION_STR, e);
		}
	}

	@Override
	public void encodeBoolean(final Boolean value) throws MALException {
		try {
			outputStream.addBool(value);
		} catch (IOException ex) {
			throw new MALException(ENCODING_EXCEPTION_STR, ex);
		}
	}

	@Override
	public void encodeTime(final Time value) throws MALException {
		try {
			((SplitStreamHolder) outputStream).addFixedUnsignedLong(value
					.getValue());
		} catch (IOException ex) {
			throw new MALException(ENCODING_EXCEPTION_STR, ex);
		}
	}

	@Override
	public void encodeFineTime(final FineTime value) throws MALException {
		try {
			((SplitStreamHolder) outputStream).addFixedUnsignedLong(value
					.getValue());
		} catch (IOException ex) {
			throw new MALException(ENCODING_EXCEPTION_STR, ex);
		}
	}

	@Override
	public void encodeNullableBoolean(final Boolean value) throws MALException {
		try {
			if (null != value) {
				outputStream.addNotNull();
				outputStream.addBool(value);
			} else {
				outputStream.addIsNull();
			}
		} catch (IOException ex) {
			throw new MALException(ENCODING_EXCEPTION_STR, ex);
		}
	}

	@Override
	public void encodeNullableBlob(final Blob value) throws MALException {

		try {
			if (value != null) {
				// encode presence flag
				outputStream.addNotNull();
				// encode element as String
				encodeBlob(value);
			} else {
				// encode presence flag
				outputStream.addIsNull();
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		--openCount;

		if (1 > openCount) {
			try {
				((SplitStreamHolder) outputStream).close();
			} catch (IOException ex) {
				// do nothing
			}
		}
	}

	/**
	 * Extends the StreamHolder class for handling splitting out the Boolean
	 * values.
	 */
	public static class SplitStreamHolder extends BinaryStreamHolder {
		private static final int BIT_BYTES_BLOCK_SIZE = 1024;
		private final java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		private byte[] bitBytes = new byte[BIT_BYTES_BLOCK_SIZE];
		private int bitBytesInUse = 0;
		private int bitIndex = 0;

		/**
		 * Constructor.
		 *
		 * @param outputStream
		 *            The output stream to encode into.
		 */
		public SplitStreamHolder(OutputStream outputStream) {
			super(outputStream);
		}

		@Override
		public void close() throws IOException {
			streamAddUnsignedInt(outputStream, bitBytesInUse);
			outputStream.write(bitBytes, 0, bitBytesInUse);
			baos.writeTo(outputStream);
		}
		
		@Override
		public void addString(String value) throws IOException {
			
			addBytes(value.getBytes(UTF8_CHARSET));
		}

		@Override
		public void addBool(boolean value) throws IOException {
			if (value) {
				setBit(bitIndex);
			}

			++bitIndex;
		}

		@Override
		public void addIsNull() throws IOException {
			++bitIndex;
		}

		@Override
		public void addNotNull() throws IOException {
			setBit(bitIndex);
			++bitIndex;
		}

		@Override
		public void directAdd(final byte[] val) throws IOException {
			baos.write(val);
		}

		@Override
		public void directAdd(final byte val) throws IOException {
			baos.write(val);
		}

		private static void streamAddUnsignedInt(java.io.OutputStream os, int value) throws IOException {
			while ((value & 0xFFFFFF80) != 0L) {
				os.write((value & 0x7F) | 0x80);
				value >>>= 7;
			}
			os.write(value & 0x7F);
		}
		
		@Override
		public void addBytes(final byte[] value) throws IOException {
			
			if (null == value) {
				streamAddUnsignedInt(baos, 0);
			} else {
				streamAddUnsignedInt(baos, value.length);
				baos.write(value);
			}
		}

		public void addUnsignedVarint4(int value) throws IOException {
			streamAddUnsignedInt(baos, value);
		}
		
		public void addFixedUnsignedLong(long value) throws IOException {
			directAdd(java.nio.ByteBuffer.allocate(8).putLong(value).array());
		}

		private void setBit(int bitIndex) {
			int byteIndex = bitIndex / 8;

			int bytesRequired = byteIndex + 1;
			if (bitBytesInUse < bytesRequired) {
				if (bitBytes.length < bytesRequired) {
					bitBytes = java.util.Arrays.copyOf(bitBytes,
							((bytesRequired / BIT_BYTES_BLOCK_SIZE) + 1)
									* BIT_BYTES_BLOCK_SIZE);
				}

				bitBytesInUse = bytesRequired;
			}

			bitIndex %= 8;
			bitBytes[byteIndex] |= (1 << bitIndex);
		}
	}
}
