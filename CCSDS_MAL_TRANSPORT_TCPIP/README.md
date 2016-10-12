# MAL TCPIP Transport Binding
This is an implementation for a MAL transport binding using the TCP/IP protocol.

## Getting started
In order to start using this transport binding, one has to set the properties `org.ccsds.moims.mo.mal.transport.tcpip.host` and `org.ccsds.moims.mo.mal.transport.tcpip.port` in the configuration file that is used by a consumer or provider. Moreover, one has pass the protocol `maltcp://` when instantiating a consumer or provider, in order to ensure that the TCPIP transport is used.

## URI Routing
This transport binding makes use of the TCP/IP ip address and port number to route messages. Each message contains
a source and destination URI, which contain the ip address and port of the provider and consumer respectively. 

The protocol used is `maltcp://`. Message urls have the format `maltcp://ipaddr:port/serviceDescriptor`.

According to the socket implementation, providers create a server socket for accepting messages and a socket for communication with clients.
However, every service instance, be it a provider, consumer, or both, can only have one unique address. Therefore, URIs are routed 
to the actual port that reads incoming messages, but internally they are mapped to the port of the server socket, if the service
is a provider. When a new socket is created, it will be assigned a random available port.

This means that end users may have to configure their system to allow more ports than only the port that they set their server instance to.
Ensure that the application can open a new port if necessary.

## Configuration parameters
The following configuration parameters can be configured.

| Property name		| Description |
|:------------------|:------------|
| org.ccsds.moims.mo.mal.transport.tcpip.wrap, org.ccsds.moims.mo.mal.transport.tcpip.debug | Debug mode, affect logging |
| org.ccsds.moims.mo.mal.transport.tcpip.host | adapter (host / IP Address) that the transport will use for incoming connections. In case of a pure client (i.e. not offering any services) this property should be omitted. |
| org.ccsds.moims.mo.mal.transport.tcpip.port | port that the transport listens to. In case this is a pure client, this property should be omitted. |
