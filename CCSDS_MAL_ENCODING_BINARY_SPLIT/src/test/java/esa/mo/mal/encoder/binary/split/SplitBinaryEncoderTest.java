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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.URI;

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
		System.out.println("~~~ Start ~~~");
	}

    @After
    public void tearDown() throws IOException {
        //os.close();
        System.out.println("~~~  End  ~~~");
    }
    
	
	private byte[] doEncodeString(String string)
            throws MALException {
	    encoder.encodeString(string);
        encoder.close();
        byte[] bytes = os.toByteArray();
        System.out.println("String : [" + string + "] -> " + Arrays.toString(bytes));
        return bytes;
    }
	
	private byte[] doEncodeNullableString(String string)
            throws MALException {
        encoder.encodeNullableString(string);
        encoder.close();
        byte[] bytes = os.toByteArray();
        System.out.println("Nullable string : [" + string + "] -> " + Arrays.toString(bytes));
        return bytes;
    }

	@Test
	public void testNonNullString() throws MALException {
		String string = "Test";
		byte[] bytes = doEncodeString(string);
		// Don't forget the presence flag length set to 0 (no presence flag)
		byte[] expectedBytes = new byte[]{0,4,'T','e','s','t'};
		System.out.println("expected -> " + Arrays.toString(expectedBytes));
		Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
    }

    @Test
    public void testNullableNullString() throws MALException {
        byte[] bytes = doEncodeNullableString(null);
        // Only the *empty* presence flag (bitfield's length = 1 + bitfield = 0)
        byte[] expectedBytes = new byte[]{1,0};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
    }
    
    @Test
    public void testURI() throws MALException {
        URI uri = new URI("tcp://127.0.0.1:5421/Demo");
        encoder.encodeURI(uri);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println("URI : [" + uri + "] -> " + Arrays.toString(bytes));
        byte[] expectedBytes = new byte[]{0,25,'t','c','p',':','/','/','1','2','7','.','0','0','1',':','5','4','2','1',
                                          '/','D','e','m','o'};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableURI() throws MALException {
        URI uri = new URI("tcp://127.0.0.1:5421/Demo");
        encoder.encodeNullableURI(uri);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println("Nullable URI : [" + uri + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 1
        byte[] expectedBytes = new byte[]{1,1,25,'t','c','p',':','/','/','1','2','7','.','0','0','1',':','5','4','2','1',
                                          '/','D','e','m','o'};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
    }

    @Test
    public void testBoolean() throws MALException {
        Boolean imTrue = true;
        Boolean imFalse = false;
        encoder.encodeBoolean(imFalse);
        encoder.encodeBoolean(imTrue);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println("Boolean : [" + imTrue + imFalse + "] -> " + Arrays.toString(bytes));
        // Boolean are encoded in the byte field
        byte[] expectedBytes = new byte[]{(byte)1,(byte)2};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
    }
    
    @Test
    public void testNullableBoolean() throws MALException {
        Boolean imTrue = true;
        Boolean imFalse = false;
        encoder.encodeNullableBoolean(imFalse);
        encoder.encodeNullableBoolean(imTrue);
        encoder.close();
        byte[] bytes = this.os.toByteArray();
        System.out.println("Nullable Boolean : [" + imTrue + imFalse + "] -> " + Arrays.toString(bytes));
        // Boolean are encoded in the byte field
        // present, present, false, true -> on a byte 0000 1101 (0x0d)
        byte[] expectedBytes = new byte[]{(byte)1,(byte)0x0d};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Integer : [" + i1 + "," + i2 + "," + i3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // switch left 1 bit to get sign 
        byte[] expectedBytes = new byte[]{1,0,0,32,(byte)0x06,(byte)0x40};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Nullable Integer : [" + i1 + "," + i2 + "," + i3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        byte[] expectedBytes = new byte[]{1,(byte)0x07,0,32,(byte)0x06,(byte)0x40};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("UInteger : [" + ui1 + "," + ui2 + "," + ui3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // Decale 1 bit to get sign 
        byte[] expectedBytes = new byte[]{1,0,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Nullable UInteger : [" + ui1 + "," + ui2 + "," + ui3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        byte[] expectedBytes = new byte[]{1,(byte)0x07,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Double : [" + d1 + "," + d2 + "," + d3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // Decale 1 bit to get sign
        // FIXME get the good encoding value
        byte[] expectedBytes = new byte[]{1,0,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Nullable Double : [" + d1 + "," + d2 + "," + d3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // FIXME get the good encoding value
        byte[] expectedBytes = new byte[]{1,(byte)0x07,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Long : [" + l1 + "," + l2 + "," + l3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // Decale 1 bit to get sign
        // FIXME get the good encoding value
        byte[] expectedBytes = new byte[]{1,0,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Nullable Long : [" + l1 + "," + l2 + "," + l3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // FIXME get the good encoding value
        byte[] expectedBytes = new byte[]{1,(byte)0x07,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Float : [" + f1 + "," + f2 + "," + f3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // Decale 1 bit to get sign
        // FIXME get the good encoding value
        byte[] expectedBytes = new byte[]{1,0,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Nullable Float : [" + f1 + "," + f2 + "," + f3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // FIXME get the good encoding value
        byte[] expectedBytes = new byte[]{1,(byte)0x07,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Short : [" + s1 + "," + s2 + "," + s3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // Decale 1 bit to get sign
        // FIXME get the good encoding value
        byte[] expectedBytes = new byte[]{1,0,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
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
        System.out.println("Nullable Short : [" + s1 + "," + s2 + "," + s3 + "] -> " + Arrays.toString(bytes));
        // byte field length 1
        // byte field 0
        // FIXME get the good encoding value
        byte[] expectedBytes = new byte[]{1,(byte)0x07,0,16,(byte)0x03,(byte)0x20};
        System.out.println("expected -> " + Arrays.toString(expectedBytes));
        Assert.assertArrayEquals(bytes, expectedBytes);
    }
    
    
}
