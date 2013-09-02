package org.apache.james.mailbox.maildir.mail.model;

import java.io.IOException;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.maildir.MaildirFolder;
import org.apache.james.mailbox.model.MailboxACL;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.model.impl.SimpleMailbox;

public class MaildirMailbox<Id> extends SimpleMailbox<Id> {

    private MaildirFolder folder;
    private MailboxSession session;

    public MaildirMailbox(MailboxSession session, MailboxPath path, MaildirFolder folder) throws IOException {
        super(path, folder.getUidValidity());
        this.folder = folder;
        this.session = session;
    }

    @Override
    public MailboxACL getACL() {
        try {
            return folder.getACL(session);
        } catch (MailboxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setACL(MailboxACL acl) {
        try {
            folder.setACL(session, acl);
        } catch (MailboxException e) {
            throw new RuntimeException(e);
        }
    }

}
