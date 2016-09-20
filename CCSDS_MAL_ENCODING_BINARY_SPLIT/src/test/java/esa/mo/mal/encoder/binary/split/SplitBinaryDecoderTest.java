/* ----------------------------------------------------------------------------
 * Copyright (C) 2016      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO Split Binary Decoder Test
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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import esa.mo.mal.encoder.gen.GENDecoder;

import org.ccsds.moims.mo.mal.MALException;

public class SplitBinaryDecoderTest {
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	
	@Before
	public void setup() {
		System.out.println(ANSI_CYAN + "~~~ Start ~~~" + ANSI_RESET);
	}

    @After
    public void tearDown() throws IOException {
    	System.out.println(ANSI_CYAN +  "~~~  End  ~~~\n" + ANSI_RESET);
    }
    
    @Test
	public void testNonNullString() throws MALException {
		byte[] testBytes = new byte[]{0,4,'T','e','s','t'};
		SplitBinaryDecoder decoder = new SplitBinaryDecoder(testBytes);
		String result = decoder.decodeString();
		String expected = "Test";
		System.out.println(ANSI_YELLOW + "String" + ANSI_RESET);
		System.out.println("Decoded  -> [" + result + "]\nExpected -> [" + expected + "]");
		Assert.assertEquals(result, expected);
	}

	@Test
    public void testNullableNonNullString() throws MALException {
		byte[] testBytes = new byte[]{1,1,4,'T','e','s','t'};
		SplitBinaryDecoder decoder = new SplitBinaryDecoder(testBytes);
		String result = decoder.decodeNullableString();
		String expected = "Test";
		System.out.println(ANSI_YELLOW + "Nullable String" + ANSI_RESET);
		System.out.println("Decoded  -> [" + result + "]\nExpected -> [" + expected + "]");
		Assert.assertEquals(result, expected);
    }

    @Test
    public void testNullableNullString() throws MALException {
    	// Only the *empty* presence flag (bitfield's length = 1 + bitfield = 0)
    	byte[] testBytes = new byte[]{1,0};
		SplitBinaryDecoder decoder = new SplitBinaryDecoder(testBytes);
		String result = decoder.decodeNullableString();
		String expected = null;
		System.out.println(ANSI_YELLOW + "Null Nullable String" + ANSI_RESET);
		System.out.println("Decoded  -> [" + result + "]\nExpected -> [" + expected + "]");
		Assert.assertEquals(result, expected);
    }
    
    @Test
	public void testInteger() throws MALException {
    	// byte field 0 + 0 + 16 + 800
    	byte[] testBytes = new byte[]{0,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x0c};
		SplitBinaryDecoder decoder = new SplitBinaryDecoder(testBytes);
		Integer i1 = decoder.decodeInteger();
		Integer i2 = decoder.decodeInteger();
		Integer i3 = decoder.decodeInteger();
		Integer e1 = 0;
		Integer e2 = 16;
		Integer e3 = 800;
		System.out.println(ANSI_YELLOW + "Integer"+ ANSI_RESET);
		System.out.println("Decoded  -> [" + i1 + "," + i2 + "," + i3 + "]\nExpected -> [" + e1 + "," + e2 + "," + e3 + "]");
		Assert.assertEquals(i1, e1);
		Assert.assertEquals(i2, e2);
		Assert.assertEquals(i3, e3);
	}
    
    @Test
    public void testNullableInteger() throws MALException {
    	// byte field 1 + presence flag 0000 1101 + 0 + null + 16 + 800
    	byte[] testBytes = new byte[]{1,(byte)0x0d,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x0c};
		SplitBinaryDecoder decoder = new SplitBinaryDecoder(testBytes);
		Integer i1 = decoder.decodeNullableInteger();
		Integer i2 = decoder.decodeNullableInteger();
		Integer i3 = decoder.decodeNullableInteger();
		Integer i4 = decoder.decodeNullableInteger();
		Integer e1 = 0;
		Integer e2 = null;
		Integer e3 = 16;
		Integer e4 = 800;
		System.out.println(ANSI_YELLOW + "Nullable Integer"+ ANSI_RESET);
		System.out.println("Decoded  -> [" + i1 + "," + i2 + "," + i3 + "," + i4 + "]\nExpected -> [" + e1 + "," + e2 + "," + e3 + "," + e4 + "]");
		Assert.assertEquals(i1, e1);
		Assert.assertEquals(i2, e2);
		Assert.assertEquals(i3, e3);
		Assert.assertEquals(i4, e4);
	}
    
    @Test
	public void testFloat() throws MALException {
    	// byte field 0 + 0 + 16 + 800
    	byte[] testBytes = new byte[]{0,(byte)0x00,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x98,(byte)0x08,
        		(byte)0x80,(byte)0x80,(byte)0xc0,(byte)0xc4,(byte)0x08};
		SplitBinaryDecoder decoder = new SplitBinaryDecoder(testBytes);
		Float f1 = decoder.decodeFloat();
		Float f2 = decoder.decodeFloat();
		Float f3 = decoder.decodeFloat();
		Float e1 = 0.0f;
		Float e2 = 16.0f;
		Float e3 = 800.0f;
		System.out.println(ANSI_YELLOW + "Float"+ ANSI_RESET);
		System.out.println("Decoded  -> [" + f1 + "," + f2 + "," + f3 + "]\nExpected -> [" + e1 + "," + e2 + "," + e3 + "]");
		Assert.assertEquals(f1, e1);
		Assert.assertEquals(f2, e2);
		Assert.assertEquals(f3, e3);
	}
    
    @Test
    public void testNullableFloat() throws MALException {
    	// byte field 1 + presence flag 0000 1011 + 0 + 16 + null + 800
    	byte[] testBytes = new byte[]{1,(byte)0x0b,(byte)0x00,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x98,(byte)0x08,
        		(byte)0x80,(byte)0x80,(byte)0xc0,(byte)0xc4,(byte)0x08};
		SplitBinaryDecoder decoder = new SplitBinaryDecoder(testBytes);
		Float f1 = decoder.decodeNullableFloat();
		Float f2 = decoder.decodeNullableFloat();
		Float f3 = decoder.decodeNullableFloat();
		Float f4 = decoder.decodeNullableFloat();
		Float e1 = 0.0f;
		Float e2 = 16.0f;
		Float e3 = null;
		Float e4 = 800.0f;
		System.out.println(ANSI_YELLOW + "Nullable Float"+ ANSI_RESET);
		System.out.println("Decoded  -> [" + f1 + "," + f2 + "," + f3 + "," + f4 + "]\nExpected -> [" + e1 + "," + e2 + "," + e3 + "," + e4 + "]");
		Assert.assertEquals(f1, e1);
		Assert.assertEquals(f2, e2);
		Assert.assertEquals(f3, e3);
		Assert.assertEquals(f4, e4);
	}
    
    @Test
	public void testDouble() throws MALException {
    	// byte field 0 + 0 + 16 + 800
    	byte[] testBytes = new byte[]{0,(byte)0x00,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0xb0,(byte)0x80,(byte)0x01,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x89,(byte)0x81,(byte)0x01};
		SplitBinaryDecoder decoder = new SplitBinaryDecoder(testBytes);
		Double d1 = decoder.decodeDouble();
		Double d2 = decoder.decodeDouble();
		Double d3 = decoder.decodeDouble();
		Double e1 = 0.0;
		Double e2 = 16.0;
		Double e3 = 800.0;
		System.out.println(ANSI_YELLOW + "Double"+ ANSI_RESET);
		System.out.println("Decoded  -> [" + d1 + "," + d2 + "," + d3 + "]\nExpected -> [" + e1 + "," + e2 + "," + e3 + "]");
		Assert.assertEquals(d1, e1);
		Assert.assertEquals(d2, e2);
		Assert.assertEquals(d3, e3);
	}
    
    @Test
    public void testNullableDouble() throws MALException {
    	// byte field 1 + presence flag 0000 1110 + null + 0 + 16 + 800
    	byte[] testBytes = new byte[]{1,(byte)0x0e,(byte)0x00,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0xb0,(byte)0x80,(byte)0x01,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x89,(byte)0x81,(byte)0x01};
		SplitBinaryDecoder decoder = new SplitBinaryDecoder(testBytes);
		Double d1 = decoder.decodeNullableDouble();
		Double d2 = decoder.decodeNullableDouble();
		Double d3 = decoder.decodeNullableDouble();
		Double d4 = decoder.decodeNullableDouble();
		Double e1 = null;
		Double e2 = 0.0;
		Double e3 = 16.0;
		Double e4 = 800.0;
		System.out.println(ANSI_YELLOW + "Nullable Double"+ ANSI_RESET);
		System.out.println("Decoded  -> [" + d1 + "," + d2 + "," + d3 + "," + d4 + "]\nExpected -> [" + e1 + "," + e2 + "," + e3 + "," + e4 + "]");
		Assert.assertEquals(d1, e1);
		Assert.assertEquals(d2, e2);
		Assert.assertEquals(d3, e3);
		Assert.assertEquals(d4, e4);
	}

}
