package esa.mo.mal.encoder.tcpip;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.encoding.MALElementInputStream;
import org.ccsds.moims.mo.mal.encoding.MALElementOutputStream;
import esa.mo.mal.encoder.binary.BinaryStreamFactory;

public class TCPIPSplitBinaryStreamFactory extends BinaryStreamFactory {

	@Override
	protected void init(String protocol, Map properties)
			throws IllegalArgumentException, MALException {
		System.out.println("TCPIPSplitBinaryStreamFactory.init()");
		// TODO Auto-generated method stub
	}

	@Override
	public MALElementInputStream createInputStream(InputStream is)
			throws IllegalArgumentException, MALException {
		System.out.println("TCPIPSplitBinaryStreamFactory.createInputStream(InputStream)");
		return new TCPIPSplitBinaryElementInputStream(is);
	}

	@Override
	public MALElementInputStream createInputStream(byte[] bytes, int offset) {
		System.out.println("TCPIPSplitBinaryStreamFactory.createInputStream(byte[], int)");
		// TODO Auto-generated method stub
		return new TCPIPSplitBinaryElementInputStream(bytes, offset);
	}

	@Override
	public MALElementOutputStream createOutputStream(OutputStream os)
			throws IllegalArgumentException, MALException {
		System.out.println("TCPIPSplitBinaryStreamFactory.createOutputStream(OutputStream)");
		return new TCPIPSplitBinaryElementOutputStream(os);
	}

}
