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
package org.apache.james.mailbox.store.mail.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.mail.Flags;
import javax.mail.util.SharedByteArrayInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class SimpleMessageTest {
    private static final String MESSAGE_CONTENT = "Simple message content without special characters";
    private static final String MESSAGE_CONTENT_SPECIAL_CHAR = "Simple message content with special characters: \"'(§è!çà$*`";
    private static final SimpleMessage<Long> MESSAGE = buildMessage(MESSAGE_CONTENT);
    private static final SimpleMessage<Long> MESSAGE_SPECIAL_CHAR = buildMessage(MESSAGE_CONTENT_SPECIAL_CHAR);

    @Test
    public void testSize() {
        assertEquals(MESSAGE_CONTENT.length(), MESSAGE.getFullContentOctets());
    }

    @Test
    public void testInputStreamSize() throws IOException {
        InputStream is = MESSAGE.getFullContent();
        int b = 0;
        int byteCount = 0;
        while ((b = is.read()) != -1) {
            byteCount++;
        }
        assertEquals(MESSAGE_CONTENT.length(), byteCount);
    }

    @Test
    public void testInputStreamSizeSpecialCharacters() throws IOException {
        InputStream is = MESSAGE_SPECIAL_CHAR.getFullContent();
        int b = 0;
        int byteCount = 0;
        while ((b = is.read()) != -1) {
            byteCount++;
        }
        assertFalse(MESSAGE_CONTENT_SPECIAL_CHAR.length() == byteCount);
    }

    @Test
    public void testFullContent() throws IOException {
        assertEquals(MESSAGE_CONTENT,
                new String(IOUtils.toByteArray(MESSAGE.getFullContent())));
        assertEquals(MESSAGE_CONTENT_SPECIAL_CHAR,
                new String(IOUtils.toByteArray(MESSAGE_SPECIAL_CHAR.getFullContent())));
    }

    private static SimpleMessage<Long> buildMessage(String content) {
        return new SimpleMessage<Long>(Calendar.getInstance().getTime(),
                content.length(), 0, new SharedByteArrayInputStream(
                        content.getBytes()), new Flags(),
                new PropertyBuilder(), 1L);
    }

}
