package esa.mo.mal.transport.tcpip;

import org.ccsds.moims.mo.mal.structures.URI;

public class TCPIPPacketInfoHolder {
	
	private byte[] packetData;
	private URI uriFrom;
	private URI uriTo;
	
	public TCPIPPacketInfoHolder(byte[] packetData, URI from, URI to) {
		this.packetData = packetData;
		this.uriFrom = from;
		this.uriTo = to;
	}

	public byte[] getPacketData() {
		return packetData;
	}

	public void setPacketData(byte[] packetData) {
		this.packetData = packetData;
	}

	public URI getUriFrom() {
		return uriFrom;
	}

	public void setUriFrom(URI uriFrom) {
		this.uriFrom = uriFrom;
	}

	public URI getUriTo() {
		return uriTo;
	}

	public void setUriTo(URI uriTo) {
		this.uriTo = uriTo;
	}

}
