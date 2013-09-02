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

package org.apache.james.mailbox.inmemory.mail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.Flags;
import javax.mail.Flags.Flag;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MessageMetaData;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.store.SimpleMessageMetaData;
import org.apache.james.mailbox.store.mail.AbstractMessageMapper;
import org.apache.james.mailbox.store.mail.ModSeqProvider;
import org.apache.james.mailbox.store.mail.UidProvider;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.Message;
import org.apache.james.mailbox.store.mail.model.impl.SimpleMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mail.server.handler.UserInformation;
import mail.server.handler.UserInformationFactory;
import core.util.Streams;

public class InMemoryMessageMapper extends AbstractMessageMapper<Long>
{
    private static final int INITIAL_SIZE = 8;
    Logger log = LoggerFactory.getLogger(InMemoryMessageMapper.class);

    public InMemoryMessageMapper(MailboxSession session, UidProvider<Long> uidProvider,
            ModSeqProvider<Long> modSeqProvider) {
        super(session, uidProvider, modSeqProvider);
    }

    private Map<Long, Message<Long>> getMembershipByUidForMailbox(Mailbox<Long> mailbox) {
        return new ConcurrentHashMap<Long, Message<Long>>(INITIAL_SIZE);
    }

    /**
     * @see org.apache.james.mailbox.store.mail.MessageMapper#countMessagesInMailbox(org.apache.james.mailbox.store.mail.model.Mailbox)
     */
    public long countMessagesInMailbox(Mailbox<Long> mailbox) throws MailboxException {
        throw new MailboxException("not supported");
    }

    /**
     * @see org.apache.james.mailbox.store.mail.MessageMapper#countUnseenMessagesInMailbox(org.apache.james.mailbox.store.mail.model.Mailbox)
     */
    public long countUnseenMessagesInMailbox(Mailbox<Long> mailbox) throws MailboxException {
        throw new MailboxException("not supported");
    }

    /**
     * @see org.apache.james.mailbox.store.mail.MessageMapper#delete(org.apache.james.mailbox.store.mail.model.Mailbox,
     *      org.apache.james.mailbox.store.mail.model.Message)
     */
    public void delete(Mailbox<Long> mailbox, Message<Long> message) throws MailboxException {
        throw new MailboxException("not supported");
    }

    /**
     * @see org.apache.james.mailbox.store.mail.MessageMapper#findInMailbox(org.apache.james.mailbox.store.mail.model.Mailbox,
     *      org.apache.james.mailbox.model.MessageRange,
     *      org.apache.james.mailbox.store.mail.MessageMapper.FetchType, int)
     */
    public Iterator<Message<Long>> findInMailbox(Mailbox<Long> mailbox, MessageRange set, FetchType ftype, int max) 
            throws MailboxException
    {
        throw new MailboxException("not supported");
    }

    /**
     * @see org.apache.james.mailbox.store.mail.MessageMapper#findRecentMessageUidsInMailbox(org.apache.james.mailbox.store.mail.model.Mailbox)
     */
    public List<Long> findRecentMessageUidsInMailbox(Mailbox<Long> mailbox) throws MailboxException {
        throw new MailboxException("not supported");
    }

    /**
     * @see org.apache.james.mailbox.store.mail.MessageMapper#findFirstUnseenMessageUid(org.apache.james.mailbox.store.mail.model.Mailbox)
     */
    public Long findFirstUnseenMessageUid(Mailbox<Long> mailbox) throws MailboxException {
        throw new MailboxException("not supported");
    }

    public void deleteAll() {
    }

    /**
     * Do nothing
     */
    public void endRequest() {
        // Do nothing
    }

     /**
     * (non-Javadoc)
     * 
     * @see org.apache.james.mailbox.store.mail.MessageMapper#move(org.apache.james.mailbox.store.mail.model.Mailbox,
     *      org.apache.james.mailbox.store.mail.model.Message)
     */
    @Override
    public MessageMetaData move(Mailbox<Long> mailbox, Message<Long> original) throws MailboxException {
        throw new UnsupportedOperationException("Not implemented - see https://issues.apache.org/jira/browse/IMAP-370");
    }

    /**
     * @see org.apache.james.mailbox.store.mail.AbstractMessageMapper#copy(org.apache.james.mailbox.store.mail.model.Mailbox,
     *      long, long, org.apache.james.mailbox.store.mail.model.Message)
     */
    protected MessageMetaData copy(Mailbox<Long> mailbox, long uid, long modSeq, Message<Long> original) 
        throws MailboxException
    {
        throw new MailboxException("not supported");
    }

    /**
     * @see org.apache.james.mailbox.store.mail.AbstractMessageMapper#save(org.apache.james.mailbox.store.mail.model.Mailbox,
     *      org.apache.james.mailbox.store.mail.model.Message)
     */
    protected MessageMetaData save(Mailbox<Long> mailbox, Message<Long> message) throws MailboxException {
//		if (new Random().nextInt() % 3 == 0)
//			throw new MailboxException("Artificially simulating save failure");
			
        SimpleMessage<Long> copy = new SimpleMessage<Long>(mailbox, message);

        copy.setUid(message.getUid());
        copy.setModSeq(message.getModSeq());
        getMembershipByUidForMailbox(mailbox).put(message.getUid(), copy);

        log.info("save message begin");

        try
        {
            UserInformation user = 
                UserInformationFactory.getInstance().getUserInformation(mailbox.getUser());

            if (mailbox.getName().equalsIgnoreCase("INBOX"))
                user.handleIn(copy.getFullContent());
            else 
                if (mailbox.getName().equalsIgnoreCase("SENT"))
            user.handleOut(copy.getFullContent());
        }
        catch (Throwable e)
        {
            try
            {
                UserInformationFactory.getInstance().recordFailure(
                    mailbox.getUser(), e);
            }
            catch (Exception ex)
            {
                // total failure to log the exception
                ex.printStackTrace();
                e.printStackTrace();
                log.info("user information factory failure." + ex.toString()
                    + " originating from " + e.toString());
            }

            throw new MailboxException("Failed to deliver message");
        }
        finally
        {
            log.info("save message end");
        }

        return new SimpleMessageMetaData(message);
    }

    /**
     * Do nothing
     */
    protected void begin() throws MailboxException {
    }

    /**
     * Do nothing
     */
    protected void commit() throws MailboxException {
    }

    /**
     * Do nothing
     */
    protected void rollback() throws MailboxException {
    }

    @Override
    public Map<Long, MessageMetaData> expungeMarkedForDeletionInMailbox(
			final Mailbox<Long> mailbox, MessageRange set)
			throws MailboxException {
        throw new MailboxException("not supported");
    }
}
