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

package org.apache.james.mailbox.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.util.SharedByteArrayInputStream;

import junit.framework.Assert;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MessageMetaData;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.model.MessageResult;
import org.apache.james.mailbox.model.MessageResult.FetchGroup;
import org.apache.james.mailbox.model.UpdatedFlags;
import org.apache.james.mailbox.store.mail.MessageMapper;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.Message;
import org.apache.james.mailbox.store.mail.model.impl.PropertyBuilder;
import org.apache.james.mailbox.store.mail.model.impl.SimpleMessage;
import org.junit.Test;

public class StoreMessageResultIteratorTest {

    @Test
    public void testBatching() {
        MessageRange range = MessageRange.range(1, 10);
        int batchSize = 3;
        StoreMessageResultIterator<Long> it = new StoreMessageResultIterator<Long>(new MessageMapper<Long>() {

            @Override
            public void endRequest() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T execute(Transaction<T> transaction) throws MailboxException {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<Message<Long>> findInMailbox(Mailbox<Long> mailbox, MessageRange set,
                    org.apache.james.mailbox.store.mail.MessageMapper.FetchType type, int limit)
                    throws MailboxException {
                long start = set.getUidFrom();
                long end = set.getUidTo();
                long calcEnd = start + limit;
                if (calcEnd > end) {
                    calcEnd = end;
                }

                List<Message<Long>> messages = new ArrayList<Message<Long>>();
                long i = start;
                while (i < calcEnd) {
                    long uid = i;
                    SimpleMessage<Long> m = new SimpleMessage<Long>(null, 0, 0, new SharedByteArrayInputStream(
                            "".getBytes()), new Flags(), new PropertyBuilder(), 1L);
                    m.setUid(uid);
                    messages.add(m);
                    i++;
                }
                return messages.iterator();
            }

            @Override
            public Map<Long, MessageMetaData> expungeMarkedForDeletionInMailbox(Mailbox<Long> mailbox, MessageRange set)
                    throws MailboxException {
                throw new UnsupportedOperationException();

            }

            @Override
            public long countMessagesInMailbox(Mailbox<Long> mailbox) throws MailboxException {
                throw new UnsupportedOperationException();

            }

            @Override
            public long countUnseenMessagesInMailbox(Mailbox<Long> mailbox) throws MailboxException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete(Mailbox<Long> mailbox, Message<Long> message) throws MailboxException {
                throw new UnsupportedOperationException();
            }

            @Override
            public Long findFirstUnseenMessageUid(Mailbox<Long> mailbox) throws MailboxException {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<Long> findRecentMessageUidsInMailbox(Mailbox<Long> mailbox) throws MailboxException {
                throw new UnsupportedOperationException();

            }

            @Override
            public MessageMetaData add(Mailbox<Long> mailbox, Message<Long> message) throws MailboxException {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<UpdatedFlags> updateFlags(Mailbox<Long> mailbox, Flags flags, boolean value,
                    boolean replace, MessageRange set) throws MailboxException {
                throw new UnsupportedOperationException();
            }

            @Override
            public MessageMetaData copy(Mailbox<Long> mailbox, Message<Long> original) throws MailboxException {
                throw new UnsupportedOperationException();

            }

            @Override
            public long getLastUid(Mailbox<Long> mailbox) throws MailboxException {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getHighestModSeq(Mailbox<Long> mailbox) throws MailboxException {
                throw new UnsupportedOperationException();
            }

            @Override
            public MessageMetaData move(Mailbox<Long> mailbox, Message<Long> original) throws MailboxException {
                throw new UnsupportedOperationException();

            }

        }, null, range, batchSize, new FetchGroup() {

            @Override
            public Set<PartContentDescriptor> getPartContentDescriptors() {
                return null;
            }

            @Override
            public int content() {
                return FetchGroup.MINIMAL;
            }
        });

        long i = 1;
        while (it.hasNext()) {
            MessageResult r = it.next();
            Assert.assertEquals(i++, r.getUid());
        }
        Assert.assertEquals(10, i);

    }

}
