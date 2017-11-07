/* ----------------------------------------------------------------------------
 * Copyright (C) 2016      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO Split Binary encoder Test
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.UOctet;
import org.ccsds.moims.mo.mal.structures.Time;

import esa.mo.mal.encoder.gen.GENEncoder;

/**
 * Implements the MALEncoder and MALListEncoder interfaces for a split binary encoding.
 */
public class SplitBinaryEncoderTest {
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	
	private ByteArrayOutputStream os;
	private GENEncoder encoder;
	
	@Before
	public void setup() {
		os = new ByteArrayOutputStream();
		encoder = new SplitBinaryEncoder(os);
		System.out.println(ANSI_CYAN + "~~~ Start ~~~" + ANSI_RESET);
	}

    @After
    public void tearDown() throws IOException {
        //os.close();
        System.out.println(ANSI_CYAN +  "~~~  End  ~~~\n" + ANSI_RESET);
    }
    
	
	private byte[] doEncodeString(String string)
            throws MALException {
	    encoder.encodeString(string);
        encoder.close();
        byte[] bytes = os.toByteArray();
        System.out.println(ANSI_YELLOW + "String : [" + string + "]" + ANSI_RESET);
        return bytes;
    }
	
	private byte[] doEncodeNullableString(String string)
            throws MALException {
        encoder.encodeNullableString(string);
        encoder.close();
        byte[] bytes = os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable string : [" + string + "]" + ANSI_RESET);
        return bytes;
    }

	@Test
	public void testNonNullString() throws MALException {
		String string = "Test";
		byte[] bytes = doEncodeString(string);
		// Don't forget the presence flag length set to 0 (no presence flag)
		byte[] expectedBytes = new byte[]{0,4,'T','e','s','t'};
		checkResult(bytes, expectedBytes);
	}

	@Test
    public void testNullableNonNullString() throws MALException {
        String string = "Test";
        byte[] bytes = doEncodeNullableString(string);
        // 1 : size for presence flag
        // 1 : flag for non null string
        // 4 : string length
        // Test : real string
        byte[] expectedBytes = new byte[]{1,1,4,'T','e','s','t'};
        checkResult(bytes, expectedBytes);
    }

    @Test
    public void testNullableNullString() throws MALException {
        byte[] bytes = doEncodeNullableString(null);
        // Only the *empty* presence flag (bitfield's length = 1 + bitfield = 0)
        byte[] expectedBytes = new byte[]{1,0};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testURI() throws MALException {
        URI uri = new URI("tcp://127.0.0.1:5421/Demo");
        encoder.encodeURI(uri);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "URI : [" + uri + "]" + ANSI_RESET);
        byte[] expectedBytes = new byte[]{0,25,'t','c','p',':','/','/','1','2','7','.','0','.','0','.','1',':','5','4','2','1',
                                          '/','D','e','m','o'};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableURI() throws MALException {
        URI uri = new URI("tcp://127.0.0.1:5421/Demo");
        encoder.encodeNullableURI(uri);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable URI : [" + uri + "]" + ANSI_RESET);
        // byte field length 1
        // byte field 1
        byte[] expectedBytes = new byte[]{1,1,25,'t','c','p',':','/','/','1','2','7','.','0','.','0','.','1',':','5','4','2','1',
                                          '/','D','e','m','o'};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testIdentifier() throws MALException {
        Identifier id1 = new Identifier("Id1");
        Identifier id2 = new Identifier("Id2");
        encoder.encodeIdentifier(id1);
        encoder.encodeIdentifier(id2);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Identifier : [" + id1 + "," + id2 + "]" + ANSI_RESET);
        byte[] expectedBytes = new byte[]{0,3,'I','d','1',3,'I','d','2'};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableIdentifier() throws MALException {
        Identifier id1 = new Identifier("Id1");
        Identifier id2 = new Identifier("Id2");
        encoder.encodeNullableIdentifier(id1);
        encoder.encodeNullableIdentifier(id2);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable Identifier : [" + id1 + "," + id2 + "]" + ANSI_RESET);
        // byte field length 1
        // byte field 0000 0011 (3)
        byte[] expectedBytes = new byte[]{1,3,3,'I','d','1',3,'I','d','2'};
        checkResult(bytes, expectedBytes);
    }

    @Test
    public void testBoolean() throws MALException {
        Boolean imTrue = true;
        Boolean imFalse = false;
        encoder.encodeBoolean(imFalse);
        encoder.encodeBoolean(imTrue);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Boolean : [" + imFalse + imTrue + "]" + ANSI_RESET);
        // Boolean are encoded in the byte field
        byte[] expectedBytes = new byte[]{(byte)1,(byte)2};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableBoolean() throws MALException {
        Boolean imTrue = true;
        Boolean imFalse = false;
        encoder.encodeNullableBoolean(imFalse);
        encoder.encodeNullableBoolean(imTrue);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable Boolean : [" + imFalse + imTrue + "]" + ANSI_RESET);
        // Boolean are encoded in the byte field
        // present, present, false, true -> on a byte 0000 1101 (0x0d)
        byte[] expectedBytes = new byte[]{(byte)1,(byte)0x0d};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testInteger() throws MALException {
        Integer i1 = 0;
        Integer i2 = 16;
        Integer i3 = 800;
        encoder.encodeInteger(i1);
        encoder.encodeInteger(i2);
        encoder.encodeInteger(i3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Integer : [" + i1 + "," + i2 + "," + i3 + "]" + ANSI_RESET);
        // byte field length 0
        // switch left 1 bit to get sign
        // 0 = 00000000 = 0x00
        // 16 = 00010000 sign => 00100000 = 0x20
        // 800 = 00000011 00100000 sign + split => 11000000 00001100 = 0xc0 0x0c 
        byte[] expectedBytes = new byte[]{0,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x0c};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableInteger() throws MALException {
        Integer i1 = 0;
        Integer i2 = 16;
        Integer i3 = 800;
        encoder.encodeNullableInteger(i1);
        encoder.encodeNullableInteger(i2);
        encoder.encodeNullableInteger(i3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable Integer : [" + i1 + "," + i2 + "," + i3 + "]" + ANSI_RESET);
        // byte field length 1
        // byte field 0000 0111
        // 0 = 00000000 = 0x00
        // 16 = 00010000 sign => 00100000 = 0x20
        // 800 = 00000011 00100000 sign + split => 11000000 00001100 = 0xc0 0x0c
        byte[] expectedBytes = new byte[]{1,(byte)0x07,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x0c};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testUnsignedInteger() throws MALException {
        UInteger ui1 = new UInteger(0);
        UInteger ui2 = new UInteger(16);
        UInteger ui3 = new UInteger(800);
        encoder.encodeUInteger(ui1);
        encoder.encodeUInteger(ui2);
        encoder.encodeUInteger(ui3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "UInteger : [" + ui1 + "," + ui2 + "," + ui3 + "]" + ANSI_RESET);
        // byte field length 0
        // do not take care of the sign
        // 0 = 00000000 = 0x00
        // 16 = 00010000 = 0x10
        // 800 = 00000011 00100000 split => 101000000 00000110 = 0xa0 0x06  
        byte[] expectedBytes = new byte[]{0,(byte)0x00,(byte)0x10,(byte)0xa0,(byte)0x06};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableUnsignedInteger() throws MALException {
        UInteger ui1 = new UInteger(0);
        UInteger ui2 = new UInteger(16);
        UInteger ui3 = new UInteger(800);
        encoder.encodeNullableUInteger(ui1);
        encoder.encodeNullableUInteger(ui2);
        encoder.encodeNullableUInteger(ui3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable UInteger : [" + ui1 + "," + ui2 + "," + ui3 + "]" + ANSI_RESET);
        // byte field length 1
        // byte field 0000 0111 = 0x07 (all values present)
        // do not take care of the sign
        // 0 = 00000000 = 0x00
        // 16 = 00010000 = 0x10
        // 800 = 00000011 00100000 split => 101000000 00000110 = 0xa0 0x06  
        byte[] expectedBytes = new byte[]{1,(byte)0x07,(byte)0x00,(byte)0x10,(byte)0xa0,(byte)0x06};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testDouble() throws MALException {
        Double d1 = 0.0;
        Double d2 = 16.0;
        Double d3 = 800.0;
        encoder.encodeDouble(d1);
        encoder.encodeDouble(d2);
        encoder.encodeDouble(d3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Double : [" + d1 + "," + d2 + "," + d3 + "]" + ANSI_RESET);
        // byte field length 0
        // 0 = 0x00
        // 16 = 01000000 00110000 00000000 00000000 00000000 00000000 00000000 00000000 
        // split + sign = 10000000 10000000 10000000 10000000 10000000 10000000 10000000 10110000 10000000 00000001 
        // = 0x80 0x80 0x80 0x80 0x80 0x80 0x80 0xb0 0x80 0x01
        // 800 = 01000000 10001001 00000000 00000000 00000000 00000000 00000000 00000000
        // split + sign = 10000000 10000000 10000000 10000000 10000000 10000000 10000000 10001001 10000001 00000001
        // = 0x80 0x80 0x80 0x80 0x80 0x80 0x80 0x89 0x81 0x01

        byte[] expectedBytes = new byte[]{0,(byte)0x00,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0xb0,(byte)0x80,(byte)0x01,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x89,(byte)0x81,(byte)0x01};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableDouble() throws MALException {
        Double d1 = 0.0;
        Double d2 = 16.0;
        Double d3 = 800.0;
        encoder.encodeNullableDouble(d1);
        encoder.encodeNullableDouble(d2);
        encoder.encodeNullableDouble(d3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable Double : [" + d1 + "," + d2 + "," + d3 + "]" + ANSI_RESET);
        // byte field length 1
        // byte field 0000 0111 = 0x07
        // 0 = 0x00
        // 16 = 01000000 00110000 00000000 00000000 00000000 00000000 00000000 00000000 
        // split + sign = 10000000 10000000 10000000 10000000 10000000 10000000 10000000 11000000 01000001 
        // = 0x80 0x80 0x80 0x80 0x80 0x80 0x80 0xc0 0x41
        // 800 = 01000000 00110000 00000000 00000000 00000000 00000000 00000000 00000000 
        // split + sign = 10000000 10000000 10000000 10000000 10000000 10000000 10000000 10100100 01000100 
        // = 0x80 0x80 0x80 0x80 0x80 0x80 0x80 0xa4 0x44
        // 8000 8080 8080 8080 83b0 0001 8080 8080 8080 80c8 8801
        byte[] expectedBytes = new byte[]{1,(byte)0x07,(byte)0x00,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0xb0,(byte)0x80,(byte)0x01,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x89,(byte)0x81,(byte)0x01};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testLong() throws MALException {
        long l1 = 0;
        long l2 = 16;
        long l3 = 800;
        encoder.encodeLong(l1);
        encoder.encodeLong(l2);
        encoder.encodeLong(l3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Long : [" + l1 + "," + l2 + "," + l3 + "]" + ANSI_RESET);
        // byte field length 0
        // Decale 1 bit to get sign
        // 0 = 00000000 = 0x00
        // 16 = 00010000 sign => 00100000 = 0x20
        // 800 = 00000011 00100000 sign + split => 11000000 00001100 = 0xc0 0x0c  
        byte[] expectedBytes = new byte[]{0,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x0c};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableLong() throws MALException {
        long l1 = 0;
        long l2 = 16;
        long l3 = 800;
        encoder.encodeNullableLong(l1);
        encoder.encodeNullableLong(l2);
        encoder.encodeNullableLong(l3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable Long : [" + l1 + "," + l2 + "," + l3 + "]" + ANSI_RESET);
        // byte field length 1
        // byte field 0000 0111 = 0x07
        // 0 = 00000000 = 0x00
        // 16 = 00010000 sign => 00100000 = 0x20
        // 800 = 00000011 00100000 sign + split => 11000000 00001100 = 0xc0 0x0c 
        byte[] expectedBytes = new byte[]{1,(byte)0x07,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x0c};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testFloat() throws MALException {
        float f1 = 0.0f;
        float f2 = 16.0f;
        float f3 = 800.0f;
        encoder.encodeFloat(f1);
        encoder.encodeFloat(f2);
        encoder.encodeFloat(f3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Float : [" + f1 + "," + f2 + "," + f3 + "]" + ANSI_RESET);
        // byte field length 0
        // Decale 1 bit to get sign
        // 0 = 0x00
        // 16 = 01000001 10000000 00000000 00000000 
        // split + sign = 10000000 10000000 10000000 10011000 00001000 = 0x80 0x80 0x80 0x98 0x08
        // 800 = 01000100 01001000 00000000 00000000
        // split + sign = 10000000 10000000 11000000 11000100 00001000 = 0x80 0x80 0xc0 0xc4 0x08
        byte[] expectedBytes = new byte[]{0,(byte)0x00,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x98,(byte)0x08,
        		(byte)0x80,(byte)0x80,(byte)0xc0,(byte)0xc4,(byte)0x08};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableFloat() throws MALException {
        float f1 = 0;
        float f2 = 16;
        float f3 = 800;
        encoder.encodeNullableFloat(f1);
        encoder.encodeNullableFloat(f2);
        encoder.encodeNullableFloat(f3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable Float : [" + f1 + "," + f2 + "," + f3 + "]" + ANSI_RESET);
        // byte field length 1
        // byte field 0000 0111 = 0x07
        // 0 = 0x00
        // 16 = 01000001 10000000 00000000 00000000 
        // split + sign = 10000000 10000000 10000000 10011000 00001000 = 0x80 0x80 0x80 0x98 0x08
        // 800 = 01000100 01001000 00000000 00000000
        // split + sign = 10000000 10000000 11000000 11000100 00001000 = 0x80 0x80 0xc0 0xc4 0x08
        byte[] expectedBytes = new byte[]{1,(byte)0x07,(byte)0x00,
        		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x98,(byte)0x08,
        		(byte)0x80,(byte)0x80,(byte)0xc0,(byte)0xc4,(byte)0x08};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testShort() throws MALException {
        short s1 = 0;
        short s2 = 16;
        short s3 = 800;
        encoder.encodeShort(s1);
        encoder.encodeShort(s2);
        encoder.encodeShort(s3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Short : [" + s1 + "," + s2 + "," + s3 + "]" + ANSI_RESET);
        // byte field length 0
        // switch left 1 bit to get sign
        // 0 = 00000000 = 0x00
        // 16 = 00010000 sign => 00100000 = 0x20
        // 800 = 00000011 00100000 sign + split => 11000000 00001100 = 0xc0 0x0c
        byte[] expectedBytes = new byte[]{0,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x0c};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableShort() throws MALException {
        short s1 = 0;
        short s2 = 16;
        short s3 = 800;
        encoder.encodeNullableShort(s1);
        encoder.encodeNullableShort(s2);
        encoder.encodeNullableShort(s3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable Short : [" + s1 + "," + s2 + "," + s3 + "]" + ANSI_RESET);
        // byte field length 1
        // byte field 0000 0111 = 0x07
        // 0 = 00000000 = 0x00
        // 16 = 00010000 sign => 00100000 = 0x20
        // 800 = 00000011 00100000 sign + split => 11000000 00001100 = 0xc0 0x0c
        byte[] expectedBytes = new byte[]{1,(byte)0x07,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x0c};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testUOctet() throws MALException {
        UOctet uo1 = new UOctet((short)0);
        UOctet uo2 = new UOctet((short)16);
        UOctet uo3 = new UOctet((short)80);
        encoder.encodeUOctet(uo1);
        encoder.encodeUOctet(uo2);
        encoder.encodeUOctet(uo3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "UOctet : [" + uo1 + "," + uo2 + "," + uo3 + "]" + ANSI_RESET);
        // byte field length 0
        // Decale 1 bit to get sign
        // unsigned 8 bit integer encoding
        byte[] expectedBytes = new byte[]{0,0,16,80};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableUOctet() throws MALException {
        UOctet uo1 = new UOctet((short)0);
        UOctet uo2 = new UOctet((short)16);
        UOctet uo3 = new UOctet((short)80);
        encoder.encodeNullableUOctet(uo1);
        encoder.encodeNullableUOctet(uo2);
        encoder.encodeNullableUOctet(uo3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable UOctet : [" + uo1 + "," + uo2 + "," + uo3 + "]" + ANSI_RESET);
        // byte field length 1
        // byte field 0000 0111
        // unsigned 8 bit integer encoding
        byte[] expectedBytes = new byte[]{1,(byte)0x07,0,16,80};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testTime() throws MALException {
        Time uo1 = new Time((short)0);
        Time uo2 = new Time((short)16);
        Time uo3 = new Time((short)80);
        encoder.encodeTime(uo1);
        encoder.encodeTime(uo2);
        encoder.encodeTime(uo3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Time : [" + uo1 + "," + uo2 + "," + uo3 + "]" + ANSI_RESET);
        // byte field length 0
        // Decale 1 bit to get sign
        // unsigned 8 bit integer encoding
        byte[] expectedBytes = new byte[]{0,0,16,80};
        checkResult(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableTime() throws MALException {
        Time uo1 = new Time((short)0);
        Time uo2 = new Time((short)16);
        Time uo3 = new Time((short)80);
        encoder.encodeNullableTime(uo1);
        encoder.encodeNullableTime(uo2);
        encoder.encodeNullableTime(uo3);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println(ANSI_YELLOW + "Nullable Time : [" + uo1 + "," + uo2 + "," + uo3 + "]"+ ANSI_RESET);
        // byte field length 1
        // byte field 0000 0111
        // unsigned 8 bit integer encoding
        byte[] expectedBytes = new byte[]{1,(byte)0x07,0,16,80};
        checkResult(bytes, expectedBytes);
    }
    
    private void checkResult(byte[] encoded, byte[] expected)
    {
    	System.out.println("encoded  -> " + Arrays.toString(encoded));
    	System.out.println("expected -> " + Arrays.toString(expected));
    	String result = (Arrays.equals(encoded, expected)) ? ANSI_GREEN + "success" + ANSI_RESET : ANSI_RED + "failed" + ANSI_RESET;
    	System.out.println(result);
        Assert.assertArrayEquals(encoded, expected);
    }
}
