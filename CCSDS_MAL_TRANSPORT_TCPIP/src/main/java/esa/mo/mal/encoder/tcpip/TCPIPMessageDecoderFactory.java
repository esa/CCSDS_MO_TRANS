package esa.mo.mal.encoder.tcpip;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.URI;

import esa.mo.mal.transport.gen.GENMessage;
import esa.mo.mal.transport.gen.GENReceptionHandler;
import esa.mo.mal.transport.gen.GENTransport;
import esa.mo.mal.transport.gen.receivers.GENIncomingMessageDecoder;
import esa.mo.mal.transport.gen.receivers.GENIncomingMessageDecoderFactory;
import esa.mo.mal.transport.gen.receivers.GENIncomingMessageHolder;
import esa.mo.mal.transport.tcpip.TCPIPPacketInfoHolder;
import esa.mo.mal.transport.tcpip.TCPIPTransport;

public class TCPIPMessageDecoderFactory implements GENIncomingMessageDecoderFactory<TCPIPPacketInfoHolder>{

	@Override
	public GENIncomingMessageDecoder createDecoder(GENTransport transport,
			GENReceptionHandler receptionHandler, TCPIPPacketInfoHolder packetInfo) {
		return new TCPIPMessageDecoder((TCPIPTransport) transport, packetInfo);
	}
	
	public static final class TCPIPMessageDecoder implements GENIncomingMessageDecoder {
		
		private final TCPIPTransport transport;
		private final TCPIPPacketInfoHolder packetInfo;
		
		public TCPIPMessageDecoder(TCPIPTransport transport, TCPIPPacketInfoHolder packetInfo) {
			this.transport = transport;
			this.packetInfo = packetInfo;
		}

		@Override
		public GENIncomingMessageHolder decodeAndCreateMessage()
				throws MALException {
			
			System.out.println("TCPIPMessageDecoder.decodeAndCreateMessage()");

			GENTransport.PacketToString smsg = transport.new PacketToString(null);
			GENMessage msg = transport.createMessage(packetInfo);
			String serviceDelim = Character.toString(transport.getServiceDelim());
			
			if (msg != null) {
				return new GENIncomingMessageHolder(msg.getHeader().getTransactionId(), msg, smsg);
			}
			
			return null;
		}
		
	}

}
