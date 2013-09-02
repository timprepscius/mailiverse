/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.mailbox;

import java.util.Collection;

import org.apache.james.mailbox.mock.MockMailboxSession;
import org.junit.Assert;
import org.junit.Test;

/**
 * Abstract base class to test {@link SubscriptionManager} implementations
 * 
 *
 */
public abstract class AbstractSubscriptionManagerTest {

    private final static String USER1 = "test";
    private final static String MAILBOX1 = "test1";
    private final static String MAILBOX2 = "test2";

    public abstract SubscriptionManager createSubscriptionManager();
    
    @Test
    public void testSubscriptionManager() throws Exception {
        SubscriptionManager manager = createSubscriptionManager();
        MailboxSession session = new MockMailboxSession(USER1);
        manager.startProcessingRequest(session);
        
        Assert.assertTrue(manager.subscriptions(session).isEmpty());
        
        manager.subscribe(session, MAILBOX1);
        Assert.assertEquals(MAILBOX1, manager.subscriptions(session).iterator().next());
        Assert.assertEquals(1, manager.subscriptions(session).size());
        
        
        manager.subscribe(session, MAILBOX1);
        Assert.assertEquals(MAILBOX1, manager.subscriptions(session).iterator().next());
        Assert.assertEquals(1, manager.subscriptions(session).size());
        
        manager.subscribe(session, MAILBOX2);
        Collection<String> col = manager.subscriptions(session);
      
        Assert.assertTrue(col.contains(MAILBOX2));
        Assert.assertTrue(col.contains(MAILBOX1));
        Assert.assertEquals(2, col.size());
        
        
        manager.unsubscribe(session, MAILBOX1);
        Assert.assertEquals(MAILBOX2, manager.subscriptions(session).iterator().next());
        Assert.assertEquals(1, manager.subscriptions(session).size());
        
        manager.unsubscribe(session, MAILBOX1);
        Assert.assertEquals(MAILBOX2, manager.subscriptions(session).iterator().next());
        Assert.assertEquals(1, manager.subscriptions(session).size());
        
        
        manager.endProcessingRequest(session);
    }
}
