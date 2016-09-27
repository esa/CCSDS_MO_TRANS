package esa.mo.mal.encoder.tcpip;

import org.ccsds.moims.mo.mal.MALException;

import esa.mo.mal.transport.gen.GENMessage;
import esa.mo.mal.transport.gen.GENReceptionHandler;
import esa.mo.mal.transport.gen.GENTransport;
import esa.mo.mal.transport.gen.receivers.GENIncomingMessageDecoder;
import esa.mo.mal.transport.gen.receivers.GENIncomingMessageDecoderFactory;
import esa.mo.mal.transport.gen.receivers.GENIncomingMessageHolder;
import esa.mo.mal.transport.tcpip.TCPIPTransport;

public class TCPIPMessageDecoderFactory implements GENIncomingMessageDecoderFactory<byte[]>{

	@Override
	public GENIncomingMessageDecoder createDecoder(GENTransport transport,
			GENReceptionHandler receptionHandler, byte[] messageSource) {
		return new TCPIPMessageDecoder((TCPIPTransport) transport, messageSource);
	}
	
	public static final class TCPIPMessageDecoder implements GENIncomingMessageDecoder {
		
		private final TCPIPTransport transport;
		private final byte[] rawMessage;
		
		public TCPIPMessageDecoder(TCPIPTransport transport, byte[] messageSource) {
			this.transport = transport;
			this.rawMessage = messageSource;
		}

		@Override
		public GENIncomingMessageHolder decodeAndCreateMessage()
				throws MALException {

			GENTransport.PacketToString smsg = transport.new PacketToString(null);
			GENMessage msg = transport.createMessage(rawMessage);
			
			if (msg != null) {
				return new GENIncomingMessageHolder(msg.getHeader().getTransactionId(), msg, smsg);
			}
			
			return null;
		}
		
	}

}
