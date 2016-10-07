/*******************************************************************************
 * Copyright or © or Copr. CNES
 *
 * This software is a computer program whose purpose is to provide a 
 * framework for the CCSDS Mission Operations services.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package org.ccsds.moims.mo.mal.test.transport;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.structures.EntityKey;
import org.ccsds.moims.mo.mal.structures.EntityKeyList;
import org.ccsds.moims.mo.mal.structures.EntityRequest;
import org.ccsds.moims.mo.mal.structures.EntityRequestList;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.QoSLevel;
import org.ccsds.moims.mo.mal.structures.SessionType;
import org.ccsds.moims.mo.mal.structures.Subscription;
import org.ccsds.moims.mo.mal.structures.Time;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.URI;
import org.ccsds.moims.mo.mal.structures.UpdateHeader;
import org.ccsds.moims.mo.mal.structures.UpdateHeaderList;
import org.ccsds.moims.mo.mal.structures.UpdateType;
import org.ccsds.moims.mo.mal.test.patterns.pubsub.HeaderTestProcedure;
import org.ccsds.moims.mo.mal.test.suite.LocalMALInstance;
import org.ccsds.moims.mo.mal.test.util.AssertionHelper;
import org.ccsds.moims.mo.malprototype.iptest.consumer.IPTestAdapter;
import org.ccsds.moims.mo.malprototype.iptest.consumer.IPTestStub;
import org.ccsds.moims.mo.malprototype.iptest.structures.IPTestResult;
import org.ccsds.moims.mo.malprototype.iptest.structures.TestPublishRegister;
import org.ccsds.moims.mo.malprototype.iptest.structures.TestPublishUpdate;
import org.ccsds.moims.mo.malprototype.iptest.structures.TestUpdate;
import org.ccsds.moims.mo.malprototype.iptest.structures.TestUpdateList;
import org.ccsds.moims.mo.testbed.util.LoggingBase;

public class TransmitMultipleTestProcedure
{
  
  public static final Identifier NETWORK_ZONE = new Identifier("NetworkZone-TransmitMultiple");
  public static final SessionType SESSION = SessionType.LIVE;
  public static final Identifier SESSION_NAME = new Identifier("LIVE");
  public static final UInteger PRIORITY = new UInteger(1);
  public static final QoSLevel QOS_LEVEL = QoSLevel.ASSURED;
  
  public static final EntityKey A_ENTITY_KEY = new EntityKey(NETWORK_ZONE, null, null, null);
  
  private Subscription subscription;

  private IPTestStub ipTest1;
  private MonitorListener listener1;
  private IPTestStub ipTest2;
  private MonitorListener listener2;
  
  private IPTestResult result;
  
  public boolean createConsumers() throws Exception {
    LoggingBase.logMessage("TransmitMultipleTestProcedure.createConsumers()");
    Thread.sleep(2000);
    
    ipTest1 = LocalMALInstance.instance().newIPTestStub(null,
        HeaderTestProcedure.AUTHENTICATION_ID,
        HeaderTestProcedure.DOMAIN, 
        NETWORK_ZONE, 
        SESSION, SESSION_NAME, QOS_LEVEL, PRIORITY, false).getStub();
    
    ipTest2 = LocalMALInstance.instance().newIPTestStub(null, 
        HeaderTestProcedure.AUTHENTICATION_ID,
        HeaderTestProcedure.DOMAIN, 
        NETWORK_ZONE, 
        SESSION, SESSION_NAME, QOS_LEVEL, PRIORITY, false).getStub();
    
    EntityKeyList entityKeys = new EntityKeyList();
    entityKeys.add(A_ENTITY_KEY);
    Boolean onlyOnChange = false;
    EntityRequest entityRequest = new EntityRequest(
        null, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 
        onlyOnChange, entityKeys);
    EntityRequestList entityRequests = new EntityRequestList();
    entityRequests.add(entityRequest);
    subscription = new Subscription(HeaderTestProcedure.SUBSCRIPTION_ID, entityRequests);
    return true;
  }
  
  public boolean initiateInteraction() throws Exception {
    LoggingBase.logMessage("TransmitMultipleTestProcedure.initiateInteraction()");
    
    EntityKeyList registeredEntityKeyList = new EntityKeyList();
    registeredEntityKeyList.add(A_ENTITY_KEY);
    
    UInteger expectedErrorCode = new UInteger(999);
    TestPublishRegister testPublishRegister = 
      new TestPublishRegister(QOS_LEVEL, PRIORITY, 
          HeaderTestProcedure.DOMAIN, 
          NETWORK_ZONE, SESSION, SESSION_NAME, false, 
          registeredEntityKeyList, expectedErrorCode);
    ipTest1.publishRegister(testPublishRegister);
    
    listener1 = new MonitorListener();
    listener2 = new MonitorListener();
    
    ipTest1.monitorRegister(subscription, listener1);
    ipTest2.monitorRegister(subscription, listener2);
    
    UpdateHeaderList updateHeaderList = new UpdateHeaderList();
    updateHeaderList.add(new UpdateHeader(new Time(System.currentTimeMillis()), new URI(""), UpdateType.MODIFICATION, A_ENTITY_KEY));
    
    TestUpdateList updateList = new TestUpdateList();
    updateList.add(new TestUpdate(new Integer(0)));
    
    TestPublishUpdate testPublishUpdate = new TestPublishUpdate(
        QOS_LEVEL, PRIORITY, HeaderTestProcedure.DOMAIN, NETWORK_ZONE, 
        SESSION, SESSION_NAME, false, updateHeaderList, updateList, expectedErrorCode, false, null);
    ipTest1.testMultipleNotify(testPublishUpdate);
    
    return true;
  }
  
  public boolean CallTheOperationGetResult() throws MALInteractionException, MALException
  {
    LoggingBase.logMessage("HeaderTestProcedure.CallTheOperationGetResult()");
    result = ipTest1.getResult(null);
    return true;
  }
  
  public boolean theProviderAssertions()
  {
    LoggingBase.logMessage("TransmitMultipleTestProcedure.theProviderAssertions()");
    return AssertionHelper.checkAssertions(result.getAssertions());
  }
  
  static class MonitorListener extends IPTestAdapter
  {

  }
}
