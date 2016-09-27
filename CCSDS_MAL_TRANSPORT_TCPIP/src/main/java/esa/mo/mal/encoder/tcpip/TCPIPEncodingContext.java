package esa.mo.mal.encoder.tcpip;

import java.util.Map;

import org.ccsds.moims.mo.mal.MALOperation;
import org.ccsds.moims.mo.mal.encoding.MALEncodingContext;
import org.ccsds.moims.mo.mal.transport.MALMessageHeader;

public class TCPIPEncodingContext extends MALEncodingContext {

	public TCPIPEncodingContext(MALMessageHeader header,
			MALOperation operation, int bodyElementIndex,
			Map endpointQosProperties, Map messageQosProperties) {
		super(header, operation, bodyElementIndex, endpointQosProperties,
				messageQosProperties);
		// TODO Auto-generated constructor stub
	}

}
