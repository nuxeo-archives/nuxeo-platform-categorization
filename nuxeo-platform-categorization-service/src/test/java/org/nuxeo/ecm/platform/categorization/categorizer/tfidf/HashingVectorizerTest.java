/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Olivier Grisel
 */

package org.nuxeo.ecm.platform.categorization.categorizer.tfidf;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HashingVectorizerTest {

    protected HashingVectorizer hv_5_0_1;

    protected HashingVectorizer hv_5_1_2;

    @Before
    public void setUp() {
        hv_5_0_1 = new HashingVectorizer().dimension(5).window(0).probes(1);
        hv_5_1_2 = new HashingVectorizer().dimension(5).window(1).probes(2);
    }

    @Test
    public void testSimpleHashing() {
        long[] counts = hv_5_0_1.count(Arrays.asList("term1"));
        assertTrue(Arrays.equals(new long[] { 0, 1, 0, 0, 0 }, counts));

        counts = hv_5_0_1.count(Arrays.asList("term1", "term2"));
        assertTrue(Arrays.equals(new long[] { 1, 1, 0, 0, 0 }, counts));

        // collision!
        counts = hv_5_0_1.count(Arrays.asList("term1", "term2", "term3"));
        assertTrue(Arrays.equals(new long[] { 2, 1, 0, 0, 0 }, counts));
    }

    @Test
    public void testWindowAndProbeHashing() {
        long[] counts = hv_5_1_2.count(Arrays.asList("term1"));
        assertTrue(Arrays.equals(new long[] { 0, 1, 1, 0, 0 }, counts));

        counts = hv_5_1_2.count(Arrays.asList("term1", "term2"));
        // 6 counts: 2 probes * (2 unigrams + 1 bigram)
        assertTrue(Arrays.equals(new long[] { 1, 3, 2, 0, 0 }, counts));
    }

}
