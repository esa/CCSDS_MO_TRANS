package esa.mo.mal.encoder.tcpip;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.encoding.MALElementInputStream;
import org.ccsds.moims.mo.mal.encoding.MALElementOutputStream;
import org.ccsds.moims.mo.mal.encoding.MALElementStreamFactory;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;
import org.ccsds.moims.mo.mal.structures.Blob;

public class TCPIPSplitBinaryStreamFactory extends MALElementStreamFactory {

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
	public MALElementInputStream createInputStream(byte[] bytes, int offset)
			throws IllegalArgumentException, MALException {
		System.out.println("TCPIPSplitBinaryStreamFactory.createInputStream(byte[], int)");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MALElementOutputStream createOutputStream(OutputStream os)
			throws IllegalArgumentException, MALException {
		System.out.println("TCPIPSplitBinaryStreamFactory.createOutputStream(OutputStream)");
		// TODO Auto-generated method stub
		return new TCPIPSplitBinaryElementOutputStream(os);
	}

	@Override
	public Blob encode(Object[] elements, MALEncodingContext ctx)
			throws IllegalArgumentException, MALException {
		System.out.println("TCPIPSplitBinaryStreamFactory.encode()");
		// TODO Auto-generated method stub
		return null;
	}

}
