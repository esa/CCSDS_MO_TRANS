package esa.mo.mal.encoder.tcpip;

import java.io.InputStream;

import esa.mo.mal.encoder.binary.split.SplitBinaryDecoder;

/**
 * A split binary decoder for decoding the message body of TCPIP Messages.
 * Allows retrieving how many bytes have been read so far. This information
 * is used 
 *  
 * @author Rian van Gijlswijk <r.vangijlswijk@telespazio-vega.de>
 *
 */
public class TCPIPSplitBinaryDecoder extends SplitBinaryDecoder {
	
	public TCPIPSplitBinaryDecoder(BufferHolder bh) {
		super(bh);
	}
	
	public TCPIPSplitBinaryDecoder(InputStream is) {
		super(new TCPIPSplitBufferHolder(is, null, 0, 0));
	}

	public TCPIPSplitBinaryDecoder(byte[] src, int offset) {
		super(src, offset);
	}
	
	/**
	 * Retrieve how many bytes have been read from the buffer so far. This 
	 * information is used to determine boundary between the variable-size 
	 * header part, containing optional fields, and the message body.
	 * 
	 * @return int
 * 				buffer offset pointer
	 */
	public int getBufferOffset() {
		return ((TCPIPSplitBufferHolder)this.sourceBuffer).getOffset();
	}
	
	public void setBufferOffset(int newOffset) {
		((TCPIPSplitBufferHolder)this.sourceBuffer).setOffset(newOffset);
	}
	
	protected static class TCPIPSplitBufferHolder extends SplitBufferHolder {

		public TCPIPSplitBufferHolder(InputStream is, byte[] buf, int offset,
				int length) {
			super(is, buf, offset, length);
		}
		
		public int getOffset() {
			return this.offset;
		}
		
		public void setOffset(int newOffset) {
			this.offset = newOffset;
		}
	}
}