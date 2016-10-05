package esa.mo.mal.transport.tcpip;

import org.ccsds.moims.mo.mal.structures.URI;

public class TCPIPPacketInfoHolder {
	
	private byte[] packetData;
	private URI tcpipFrom;
	private URI tcpipTo;
	
	public TCPIPPacketInfoHolder(byte[] packetData, URI from, URI to) {
		this.packetData = packetData;
		this.tcpipFrom = from;
		this.tcpipTo = to;
	}

	public byte[] getPacketData() {
		return packetData;
	}

	public void setPacketData(byte[] packetData) {
		this.packetData = packetData;
	}

	public URI getUriFrom() {
		return tcpipFrom;
	}

	public void setUriFrom(URI uriFrom) {
		this.tcpipFrom = uriFrom;
	}

	public URI getUriTo() {
		return tcpipTo;
	}

	public void setUriTo(URI uriTo) {
		this.tcpipTo = uriTo;
	}

}
