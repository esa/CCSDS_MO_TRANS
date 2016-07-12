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
package esa.mo.mal.encoder.binary.split;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ccsds.moims.mo.mal.MALException;

import esa.mo.mal.encoder.gen.GENEncoder;

/**
 * Implements the MALEncoder and MALListEncoder interfaces for a split binary encoding.
 */
public class SplitBinaryEncoderTest {
	
	private ByteArrayOutputStream os;
	private GENEncoder encoder;
	
	@Before
	public void setup() {
		os = new ByteArrayOutputStream();
		encoder = new SplitBinaryEncoder(os);
	}

	private byte[] doEncodeNullableString(String string)
			throws MALException {
		encoder.encodeNullableString(string);
		encoder.close();
		byte[] bytes = os.toByteArray();
		System.out.println(string + " -> " + Arrays.toString(bytes));
		return bytes;
	}

	@Test
	public void testNullableNonNullString() throws MALException {
		String string = "Test";
		byte[] bytes = doEncodeNullableString(string);
		Assert.assertNotEquals(1,bytes.length);
	}

	@Test
	public void testNullableNullString() throws MALException {
		byte[] bytes = doEncodeNullableString(null);
		// Only the *empty* presence flag (bitfield's length = 0)
		Assert.assertEquals(1,bytes.length);
	}

	@Test
	public void testNullableEmptyString() throws MALException {
		byte[] bytes = doEncodeNullableString("");
		// Presence flag (bitfield's length + bitfield) + string's length
		Assert.assertEquals(3,bytes.length);

	}
}
