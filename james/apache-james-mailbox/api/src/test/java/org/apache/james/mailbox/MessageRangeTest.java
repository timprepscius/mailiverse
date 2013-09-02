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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

import org.apache.james.mailbox.model.MessageRange;
import org.junit.Test;

public class MessageRangeTest {

    @Test
    public void testToRanges() {
        List<MessageRange> ranges = MessageRange.toRanges(Arrays.asList(1L,2L,3L,5L,6L,9L));
        assertEquals(3, ranges.size());
        checkRange(1, 3, ranges.get(0));
        checkRange(5, 6, ranges.get(1));
        checkRange(9, 9, ranges.get(2));

    }
    
    @Test
    public void testOneUidToRange() {
        List<MessageRange> ranges = MessageRange.toRanges(Arrays.asList(1L));
        assertEquals(1, ranges.size());
        checkRange(1, 1, ranges.get(0));
    }
    
    // Test for MAILBOX-56
    @Test
    public void testTwoSeqUidToRange() {
        List<MessageRange> ranges = MessageRange.toRanges(Arrays.asList(1L,2L));
        assertEquals(1, ranges.size());
        checkRange(1, 2, ranges.get(0));

    }
    
    
    private void checkRange(long from, long to, MessageRange range) {
        assertEquals(from, range.getUidFrom());
        assertEquals(to, range.getUidTo());
    }
    
    @Test
    public void testSplitOne() {
        MessageRange one = MessageRange.one(1);
        List<MessageRange> ranges = one.split(2);
        assertEquals(1, ranges.size());
        checkRange(1, 1, ranges.get(0));
        assertEquals(MessageRange.Type.ONE, ranges.get(0).getType());
    }
    
    @Test
    public void testSplitFrom() {
        MessageRange from = MessageRange.from(1);
        List<MessageRange> ranges = from.split(2);
        assertEquals(1, ranges.size());
        checkRange(1, MessageRange.NOT_A_UID, ranges.get(0));
        assertEquals(MessageRange.Type.FROM, ranges.get(0).getType());
    }
    
    @Test
    public void testSplitRange() {
        MessageRange range = MessageRange.range(1,10);
        List<MessageRange> ranges = range.split(3);
        assertEquals(4, ranges.size());
        checkRange(1, 3, ranges.get(0));
        assertEquals(MessageRange.Type.RANGE, ranges.get(0).getType());
        checkRange(4, 6, ranges.get(1));
        assertEquals(MessageRange.Type.RANGE, ranges.get(1).getType());
        checkRange(7, 9, ranges.get(2));
        assertEquals(MessageRange.Type.RANGE, ranges.get(2).getType());
        checkRange(10, 10, ranges.get(3));
        assertEquals(MessageRange.Type.ONE, ranges.get(3).getType());
    }
}
