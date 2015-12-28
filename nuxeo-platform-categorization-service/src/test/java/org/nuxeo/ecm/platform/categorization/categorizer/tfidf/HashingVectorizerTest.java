/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
