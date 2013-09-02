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
package org.apache.james.mailbox.jpa;

import java.util.Date;

import javax.mail.Flags;
import javax.mail.internet.SharedInputStream;

import org.apache.james.mailbox.MailboxPathLocker;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.jpa.mail.model.JPAMailbox;
import org.apache.james.mailbox.jpa.mail.model.openjpa.JPAMessage;
import org.apache.james.mailbox.store.MailboxEventDispatcher;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.StoreMessageManager;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.Message;
import org.apache.james.mailbox.store.mail.model.impl.PropertyBuilder;
import org.apache.james.mailbox.store.search.MessageSearchIndex;

/**
 * Abstract base class which should be used from JPA implementations.
 */
public class JPAMessageManager extends StoreMessageManager<Long> {
    
    public JPAMessageManager(MailboxSessionMapperFactory<Long> mapperFactory, final MessageSearchIndex<Long> index, final MailboxEventDispatcher<Long> dispatcher, final MailboxPathLocker locker, final Mailbox<Long> mailbox, MailboxACLResolver aclResolver, GroupMembershipResolver groupMembershipResolver) throws MailboxException {
        super(mapperFactory, index, dispatcher, locker, mailbox, aclResolver, groupMembershipResolver);     
    }
    
    @Override
    protected Message<Long> createMessage(Date internalDate, final int size, int bodyStartOctet, final SharedInputStream content, 
            final Flags flags, PropertyBuilder propertyBuilder) throws MailboxException{

        final Message<Long> message = new JPAMessage((JPAMailbox) getMailboxEntity(), internalDate, size, flags, content,  bodyStartOctet,  propertyBuilder);
        return message;
    }


    /**
     * Support user flags
     */
    @Override
    protected Flags getPermanentFlags(MailboxSession session) {
        Flags flags =  super.getPermanentFlags(session);
        flags.add(Flags.Flag.USER);
        return flags;
    }
    
}
