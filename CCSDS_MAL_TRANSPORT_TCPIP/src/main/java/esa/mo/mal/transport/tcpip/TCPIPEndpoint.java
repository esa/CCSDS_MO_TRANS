package esa.mo.mal.transport.tcpip;

import java.util.Map;

import esa.mo.mal.transport.gen.GENEndpoint;
import esa.mo.mal.transport.gen.GENMessageHeader;
import esa.mo.mal.transport.gen.GENTransport;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALOperation;
import org.ccsds.moims.mo.mal.structures.*;
import org.ccsds.moims.mo.mal.transport.MALEncodedBody;
import org.ccsds.moims.mo.mal.transport.MALMessage;

public class TCPIPEndpoint extends GENEndpoint {

	public TCPIPEndpoint(GENTransport transport, String localName,
			String routingName, String uri, boolean wrapBodyParts) {
		super(transport, localName, routingName, uri, wrapBodyParts);
		// TODO Auto-generated constructor stub
	}

	@Override
	public MALMessage createMessage(final Blob authenticationId,
			final URI uriTo, final Time timestamp, final QoSLevel qosLevel,
			final UInteger priority, final IdentifierList domain,
			final Identifier networkZone, final SessionType session,
			final Identifier sessionName,
			final InteractionType interactionType,
			final UOctet interactionStage, final Long transactionId,
			final UShort serviceArea, final UShort service,
			final UShort operation, final UOctet serviceVersion,
			final Boolean isErrorMessage, final Map qosProperties,
			final Object... body) throws MALException {
				return null;

	}
	
	@Override
	public MALMessage createMessage(final Blob authenticationId,
			final URI uriTo, final Time timestamp, final QoSLevel qosLevel,
			final UInteger priority, final IdentifierList domain,
			final Identifier networkZone, final SessionType session,
			final Identifier sessionName,
			final InteractionType interactionType,
			final UOctet interactionStage, final Long transactionId,
			final UShort serviceArea, final UShort service,
			final UShort operation, final UOctet serviceVersion,
			final Boolean isErrorMessage, final Map qosProperties,
			final MALEncodedBody body) throws MALException {
				return null;

	}
	
	@Override
	public MALMessage createMessage(final Blob authenticationId,
	          final URI uriTo,
	          final Time timestamp,
	          final QoSLevel qosLevel,
	          final UInteger priority,
	          final IdentifierList domain,
	          final Identifier networkZone,
	          final SessionType session,
	          final Identifier sessionName,
	          final Long transactionId,
	          final Boolean isErrorMessage,
	          final MALOperation op,
	          final UOctet interactionStage,
	          final Map qosProperties,
	          final MALEncodedBody body) throws MALException
	  {
		return null;
		
	  }
	
	@Override
	public MALMessage createMessage(final Blob authenticationId,
			final URI uriTo, final Time timestamp, final QoSLevel qosLevel,
			final UInteger priority, final IdentifierList domain,
			final Identifier networkZone, final SessionType session,
			final Identifier sessionName, final Long transactionId,
			final Boolean isErrorMessage, final MALOperation op,
			final UOctet interactionStage, final Map qosProperties,
			final Object... body) throws MALException {
		
		GENMessageHeader hdr = createMessageHeader(getURI(), authenticationId,
				uriTo, timestamp, qosLevel, priority, domain, networkZone,
				session, sessionName, op.getInteractionType(),
				interactionStage, transactionId, op.getService().getArea().getNumber(), 
				op.getService().getNumber(), op.getNumber(), op.getService().getArea().getVersion(),
				isErrorMessage, qosProperties);
		System.out.println("TCPIPEndpoint.createMessage() uriFrom: " + getURI() + " uriTo: " + uriTo);
		try {
			return new TCPIPMessage(false, hdr, qosProperties, op, transport.getStreamFactory(), body);
		} catch (MALInteractionException e) {
			// TODO Auto-generated catch block
			throw new MALException("Error creating message", e);
		}
	}
	  
	  public GENMessageHeader createMessageHeader(final URI uriFrom,
	          final Blob authenticationId,
	          final URI uriTo,
	          final Time timestamp,
	          final QoSLevel qosLevel,
	          final UInteger priority,
	          final IdentifierList domain,
	          final Identifier networkZone,
	          final SessionType session,
	          final Identifier sessionName,
	          final InteractionType interactionType,
	          final UOctet interactionStage,
	          final Long transactionId,
	          final UShort serviceArea,
	          final UShort service,
	          final UShort operation,
	          final UOctet serviceVersion,
	          final Boolean isErrorMessage,
	          final Map qosProperties)
	  {
		return new TCPIPMessageHeader(uriFrom, authenticationId, uriTo, timestamp, qosLevel, priority, domain, networkZone, session, sessionName, interactionType, interactionStage, transactionId, serviceArea, service, operation, serviceVersion, isErrorMessage);
		  
	  }

}
