/* ----------------------------------------------------------------------------
 * Copyright (C) 2013      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO COM Test bed
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package org.ccsds.moims.mo.com.test.suite;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import org.ccsds.moims.mo.com.activitytracking.ActivityTrackingHelper;
import org.ccsds.moims.mo.com.archive.ArchiveHelper;
import org.ccsds.moims.mo.com.archive.consumer.ArchiveStub;
import org.ccsds.moims.mo.com.event.EventHelper;
import org.ccsds.moims.mo.com.event.consumer.EventStub;
import org.ccsds.moims.mo.com.event.provider.MonitorEventPublisher;
import org.ccsds.moims.mo.com.test.activity.MonitorEventPublisherSkeleton;
import org.ccsds.moims.mo.com.test.util.COMInterceptor;
import org.ccsds.moims.mo.com.test.util.MALPublishInteractionListenerImpl;
import org.ccsds.moims.mo.comprototype.COMPrototypeHelper;
import org.ccsds.moims.mo.comprototype.activityrelaymanagement.ActivityRelayManagementHelper;
import org.ccsds.moims.mo.comprototype.activityrelaymanagement.consumer.ActivityRelayManagementStub;
import org.ccsds.moims.mo.comprototype.activitytest.ActivityTestHelper;
import org.ccsds.moims.mo.comprototype.activitytest.consumer.ActivityTestStub;
import org.ccsds.moims.mo.comprototype.eventtest.EventTestHelper;
import org.ccsds.moims.mo.comprototype.eventtest.consumer.EventTestStub;
import org.ccsds.moims.mo.comprototype.archivetest.ArchiveTestHelper;
import org.ccsds.moims.mo.comprototype.archivetest.consumer.ArchiveTestStub;
import org.ccsds.moims.mo.mal.MALContextFactory;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.consumer.MALConsumer;
import org.ccsds.moims.mo.mal.provider.MALProvider;
import org.ccsds.moims.mo.mal.provider.MALProviderManager;
import org.ccsds.moims.mo.mal.structures.*;
import org.ccsds.moims.mo.testbed.suite.BaseLocalMALInstance;
import org.ccsds.moims.mo.testbed.transport.TransportInterceptor;
import org.ccsds.moims.mo.testbed.util.Configuration;
import org.ccsds.moims.mo.testbed.util.FileBasedDirectory;
import org.ccsds.moims.mo.testbed.util.LoggingBase;

public class LocalMALInstance extends BaseLocalMALInstance
{
  public static final String ACTIVITY_EVENT_NAME = "ActivityEvent";
  public static final String ARCHIVE_EVENT_NAME = "ArchiveEvent";
  private ActivityRelayManagementStub activityRelayManagementStub = null;
  private EventStub activityEventStub = null;
  private ArchiveStub archiveStub;
  private ArchiveTestStub archiveTestStub;
  private EventStub archiveEventStub;
  private MonitorEventPublisher monitorEventPublisher = null;
  private EventTestStub eventTestStub = null;
  private EventStub eventStub = null;
  private final HashMap<String, ActivityTestStub> activityTestStubs = new HashMap<String, ActivityTestStub>();
  private MALProvider eventPublisherProvider;

  public static LocalMALInstance instance() throws MALException
  {
    return (LocalMALInstance) binstance();
  }

  public LocalMALInstance() throws MALException
  {
    super();
  }

  protected String getProtocol()
  {
    return System.getProperty(Configuration.DEFAULT_PROTOCOL);
  }

  protected void initHelpers() throws MALException
  {
    org.ccsds.moims.mo.com.COMHelper.init(MALContextFactory.getElementFactoryRegistry());
    ActivityTrackingHelper.init(MALContextFactory.getElementFactoryRegistry());
    ArchiveHelper.init(MALContextFactory.getElementFactoryRegistry());
    EventHelper.init(MALContextFactory.getElementFactoryRegistry());

    COMPrototypeHelper.init(MALContextFactory.getElementFactoryRegistry());
    ActivityTestHelper.init(MALContextFactory.getElementFactoryRegistry());
    ActivityRelayManagementHelper.init(MALContextFactory.getElementFactoryRegistry());
    EventTestHelper.init(MALContextFactory.getElementFactoryRegistry());
    ArchiveTestHelper.init(MALContextFactory.getElementFactoryRegistry());

    TransportInterceptor.instance().setEndpointSendInterceptor(new COMInterceptor());
  }

  public synchronized ActivityTestStub activityTestStub() throws MALException
  {
    return activityTestStub("");
  }

  public synchronized ActivityTestStub activityTestStub(String extraNamePart) throws MALException
  {
    ActivityTestStub stub = activityTestStubs.get(extraNamePart);
    if (null == stub)
    {
      FileBasedDirectory.URIpair uris = FileBasedDirectory.loadURIs(ActivityTestHelper.ACTIVITYTEST_SERVICE_NAME.getValue());

      final IdentifierList domain = new IdentifierList();
      domain.add(new Identifier("esa"));
      domain.add(new Identifier("mission"));

      MALConsumer consumer = defaultConsumerMgr.createConsumer(
              "ActivityTestConsumer" + extraNamePart,
              uris.uri,
              uris.broker,
              ActivityTestHelper.ACTIVITYTEST_SERVICE,
              new Blob("".getBytes()),
              domain,
              new Identifier("networkZone"),
              SessionType.LIVE,
              new Identifier("LIVE"),
              QoSLevel.BESTEFFORT,
              new Hashtable(), new UInteger(0));

      stub = new ActivityTestStub(consumer);
      activityTestStubs.put(extraNamePart, stub);
    }
    return stub;
  }

  public synchronized ActivityRelayManagementStub activityRelayManagementStub() throws MALException
  {
    if (null == activityRelayManagementStub)
    {
      FileBasedDirectory.URIpair uris = FileBasedDirectory.loadURIs(ActivityRelayManagementHelper.ACTIVITYRELAYMANAGEMENT_SERVICE_NAME.getValue());

      MALConsumer consumer = defaultConsumerMgr.createConsumer(
              "ActivityRelayManagementConsumer",
              uris.uri,
              uris.broker,
              ActivityRelayManagementHelper.ACTIVITYRELAYMANAGEMENT_SERVICE,
              new Blob("".getBytes()),
              new IdentifierList(),
              new Identifier("networkZone"),
              SessionType.LIVE,
              new Identifier("LIVE"),
              QoSLevel.BESTEFFORT,
              new Hashtable(), new UInteger(0));

      activityRelayManagementStub = new ActivityRelayManagementStub(consumer);
    }

    return activityRelayManagementStub;
  }

  public synchronized EventStub activityEventStub(String serviceNameSuffix, IdentifierList domain) throws MALException
  {
    if (null == activityEventStub)
    {
      LoggingBase.logMessage("LocalMALInstance:event stub creating consumer " + serviceNameSuffix + " " + domain);
      FileBasedDirectory.URIpair uris = FileBasedDirectory.loadURIs(LocalMALInstance.ACTIVITY_EVENT_NAME + serviceNameSuffix);

      MALConsumer consumer = defaultConsumerMgr.createConsumer(
              "Activity Event Monitor consumer",
              uris.uri,
              uris.broker,
              EventHelper.EVENT_SERVICE,
              new Blob("".getBytes()),
              domain,
              new Identifier("GROUND"),
              SessionType.LIVE,
              new Identifier("LIVE"),
              QoSLevel.BESTEFFORT,
              new Hashtable(), new UInteger(0));

      activityEventStub = new EventStub(consumer);
    }

    return activityEventStub;
  }

  public synchronized ArchiveStub archiveStub() throws MALException
  {
    if (null == archiveStub)
    {
      FileBasedDirectory.URIpair uris = FileBasedDirectory.loadURIs(ArchiveHelper.ARCHIVE_SERVICE_NAME.getValue());

      MALConsumer consumer = defaultConsumerMgr.createConsumer(
              "ArchiveConsumer",
              uris.uri,
              uris.broker,
              ArchiveHelper.ARCHIVE_SERVICE,
              new Blob("".getBytes()),
              new IdentifierList(),
              new Identifier("networkZone"),
              SessionType.LIVE,
              new Identifier("LIVE"),
              QoSLevel.BESTEFFORT,
              new Hashtable(), new UInteger(0));

      archiveStub = new ArchiveStub(consumer);
    }

    return archiveStub;
  }

  public synchronized ArchiveTestStub archiveTestStub() throws MALException
  {
    if (null == archiveTestStub)
    {
      FileBasedDirectory.URIpair uris = FileBasedDirectory.loadURIs(ArchiveTestHelper.ARCHIVETEST_SERVICE_NAME.getValue());

      MALConsumer consumer = defaultConsumerMgr.createConsumer(
              "ArchiveTestConsumer",
              uris.uri,
              uris.broker,
              ArchiveTestHelper.ARCHIVETEST_SERVICE,
              new Blob("".getBytes()),
              new IdentifierList(),
              new Identifier("networkZone"),
              SessionType.LIVE,
              new Identifier("LIVE"),
              QoSLevel.BESTEFFORT,
              new Hashtable(), new UInteger(0));

      archiveTestStub = new ArchiveTestStub(consumer);
    }

    return archiveTestStub;
  }

  public synchronized EventStub archiveEventStub() throws MALException
  {
    if (null == archiveEventStub)
    {
      FileBasedDirectory.URIpair uris = FileBasedDirectory.loadURIs(ARCHIVE_EVENT_NAME);

      MALConsumer consumer = defaultConsumerMgr.createConsumer(
              "ArchiveEventConsumer",
              uris.uri,
              uris.broker,
              EventHelper.EVENT_SERVICE,
              new Blob("".getBytes()),
              new IdentifierList(),
              new Identifier("networkZone"),
              SessionType.LIVE,
              new Identifier("LIVE"),
              QoSLevel.BESTEFFORT,
              new Hashtable(), new UInteger(0));

      archiveEventStub = new EventStub(consumer);
    }

    return archiveEventStub;
  }

  public synchronized EventTestStub eventTestStub() throws MALException
  {
    if (null == eventTestStub)
    {
      FileBasedDirectory.URIpair uris = FileBasedDirectory.loadURIs(
              EventTestHelper.EVENTTEST_SERVICE_NAME.getValue());

      final IdentifierList domain = new IdentifierList();
      domain.add(new Identifier("esa"));
      domain.add(new Identifier("mission"));
      MALConsumer consumer = defaultConsumerMgr.createConsumer(
              "EventTestConsumer",
              uris.uri,
              uris.broker,
              EventTestHelper.EVENTTEST_SERVICE,
              new Blob("".getBytes()),
              domain,
              new Identifier("networkZone"),
              SessionType.LIVE,
              new Identifier("LIVE"),
              QoSLevel.BESTEFFORT,
              new Hashtable(), new UInteger(0));

      eventTestStub = new EventTestStub(consumer);
    }

    return eventTestStub;
  }

  public synchronized EventStub eventStub(IdentifierList domain) throws MALException
  {
    if (null == eventStub)
    {
      LoggingBase.logMessage("LocalMALInstance:event stub creating consumer " + domain);
      FileBasedDirectory.URIpair uris = FileBasedDirectory.loadURIs(EventHelper.EVENT_SERVICE_NAME.getValue());

      MALConsumer consumer = defaultConsumerMgr.createConsumer(
              "Event monitor consumer",
              uris.uri,
              uris.broker,
              EventHelper.EVENT_SERVICE,
              new Blob("".getBytes()),
              domain,
              new Identifier("GROUND"),
              SessionType.LIVE,
              new Identifier("LIVE"),
              QoSLevel.BESTEFFORT,
              new Hashtable(), new UInteger(0));

      eventStub = new EventStub(consumer);
    }

    return eventStub;
  }

  public synchronized MALProviderManager createProviderManager() throws MALException
  {
    return (defaultMal.createProviderManager());
  }

  protected void createMonitorEventPublisher(String relayName) throws MALInteractionException, MALException
  {
    LoggingBase.logMessage("LocalMAIInstance:createMonitorEventPublisher relay " + relayName);

    MonitorEventPublisherSkeleton monitorEventPublisherSkeleton = new MonitorEventPublisherSkeleton();
    FileBasedDirectory.URIpair uris = FileBasedDirectory.loadURIs(LocalMALInstance.ACTIVITY_EVENT_NAME + relayName);

    eventPublisherProvider = defaultProviderMgr.createProvider("Demo" + "CONSUMER-X",
            getProtocol(),
            EventHelper.EVENT_SERVICE,
            new Blob("".getBytes()),
            monitorEventPublisherSkeleton,
            new QoSLevel[]
    {
      QoSLevel.ASSURED
    },
            new UInteger(1),
            null,
            true,
            uris.broker);
    LoggingBase.logMessage("ActivityTestHandlerImpl:createMonitorEventPublisher - calling store UI\n");
    // FileBasedDirectory.storeURI(EventHelper.EVENT_SERVICE_NAME.getValue() + PROVIDER, malProvider.getURI(), malProvider.getBrokerURI());

    //monitorEventPublisherSkeleton.malInitialize(malProvider);

    final IdentifierList domain = new IdentifierList();
    domain.add(new Identifier("esa"));
    domain.add(new Identifier("mission"));

    monitorEventPublisher = monitorEventPublisherSkeleton.createMonitorEventPublisher(domain,
            new Identifier("GROUND"),
            SessionType.LIVE,
            new Identifier("LIVE"),
            QoSLevel.BESTEFFORT,
            null,
            new UInteger(0));
    final EntityKeyList lst = new EntityKeyList();
    lst.add(new EntityKey(new Identifier("*"), new Long(0), new Long(0), new Long(0)));

    monitorEventPublisher.register(lst, new MALPublishInteractionListenerImpl());
  }

  public MonitorEventPublisher getMonitorEventPublisher(String relay) throws MALInteractionException, MALException
  {
    if (monitorEventPublisher == null)
    {
      createMonitorEventPublisher(relay);
    }

    return monitorEventPublisher;
  }

  // Close all stubes
  public void close() throws MALInteractionException, MALException
  {
    if (activityRelayManagementStub != null)
    {
      activityRelayManagementStub.getConsumer().close();
      activityRelayManagementStub = null;
    }
    if (activityEventStub != null)
    {
      activityEventStub.getConsumer().close();
      activityEventStub = null;
    }
    if (eventStub != null)
    {
      eventStub.getConsumer().close();
      eventStub = null;
    }
    if (monitorEventPublisher != null)
    {
      monitorEventPublisher.close();
      monitorEventPublisher = null;
    }
    if (eventTestStub != null)
    {
      eventTestStub.getConsumer().close();
      eventTestStub = null;
    }

    Collection<ActivityTestStub> stubs = activityTestStubs.values();
    for (ActivityTestStub stub : stubs)
    {
      stub.getConsumer().close();
    }
    activityTestStubs.clear();

    if (eventPublisherProvider != null)
    {
      eventPublisherProvider.close();
      eventPublisherProvider = null;
    }
  }

  protected void createBrokers() throws MALException
  {
  }
}
