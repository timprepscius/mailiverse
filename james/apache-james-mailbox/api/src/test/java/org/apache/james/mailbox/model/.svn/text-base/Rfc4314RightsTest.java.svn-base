/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.james.mailbox.model;

import static org.junit.Assert.assertEquals;

import org.apache.james.mailbox.exception.UnsupportedRightException;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLRight;
import org.apache.james.mailbox.model.SimpleMailboxACL;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLRights;
import org.apache.james.mailbox.model.SimpleMailboxACL.Rfc4314Rights;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Peter Palaga
 */
public class Rfc4314RightsTest {
    
    private Rfc4314Rights aeik;
    private MailboxACLRights full;
    private Rfc4314Rights lprs;
    private MailboxACLRights none;
    private Rfc4314Rights twx;
    
    @Before
    public void setUp() throws Exception {
        aeik = new SimpleMailboxACL.Rfc4314Rights("aeik");
        lprs = new SimpleMailboxACL.Rfc4314Rights("lprs");
        twx = new SimpleMailboxACL.Rfc4314Rights("twx");
        full = SimpleMailboxACL.FULL_RIGHTS;
        none = SimpleMailboxACL.NO_RIGHTS;
    }
    @Test
    public void testExceptFull() throws UnsupportedRightException {
        assertEquals(none, full.except(aeik).except(lprs).except(twx));
    }
    
    @Test
    public void testExceptNonExistent() throws UnsupportedRightException {
        assertEquals(aeik, aeik.except(lprs));
    }

    @Test
    public void testExceptUnsupportedFlag() {
        try {
            String unsupportedFlag = "z";
            new SimpleMailboxACL.Rfc4314Rights(unsupportedFlag );
            Assert.fail(UnsupportedRightException.class.getName() +" expected for unsupported right flag '"+ unsupportedFlag +"'.");
        } catch (UnsupportedRightException e) {
            /* OK */
        }
    }
    
    @Test
    public void testExceptZero() throws UnsupportedRightException {
        assertEquals(aeik, aeik.except(none));
    }
    
    @Test
    public void testIterable() {
        testIterable(full);
        testIterable(none);
        testIterable(aeik);
        testIterable(lprs);
        testIterable(twx);
    }
    
    private static void testIterable(MailboxACLRights rights) {
        String stringRights = rights.serialize();
        int i = 0;
        for (MailboxACLRight r : rights) {
            assertEquals(stringRights.charAt(i++), r.getValue());
        }
        assertEquals(stringRights.length(), i);

    }

    @Test
    public void testParse() throws UnsupportedRightException {
        assertEquals(aeik.getValue(), Rfc4314Rights.a_Administer_MASK | Rfc4314Rights.e_PerformExpunge_MASK | Rfc4314Rights.i_Insert_MASK | Rfc4314Rights.k_CreateMailbox_MASK);
        assertEquals(lprs.getValue(), Rfc4314Rights.l_Lookup_MASK | Rfc4314Rights.p_Post_MASK | Rfc4314Rights.s_WriteSeenFlag_MASK | Rfc4314Rights.r_Read_MASK);
        assertEquals(twx.getValue(), Rfc4314Rights.t_DeleteMessages_MASK | Rfc4314Rights.w_Write_MASK | Rfc4314Rights.x_DeleteMailbox_MASK);
    }

    @Test
    public void testSerialize() throws UnsupportedRightException {
        assertEquals("aeik", aeik.serialize());
        assertEquals("lprs", lprs.serialize());
        assertEquals("twx", twx.serialize());
        assertEquals("aeiklprstwx", full.serialize());
        assertEquals("", none.serialize());
    }
    
    @Test
    public void testUnionFull() throws UnsupportedRightException {
        assertEquals(full, aeik.union(lprs).union(twx));
    }
    @Test
    public void testUnionZero() throws UnsupportedRightException {
        assertEquals(lprs, lprs.union(none));
    }


}
