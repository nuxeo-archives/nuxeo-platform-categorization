/*
 * (C) Copyright 2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TfIdfCategorizerTest {

    protected TfIdfCategorizer categorizer;

    protected static final String doc1 = "The cat sat on the mat.";

    protected static final String doc2 = "The cat does not like the dog.";

    protected static final String doc3 = "The dog likes the cat.";

    protected static final String doc4 = "The ouistiti is smiling at the mat.";

    protected static final String doc5 = "The chimp is smiling too.";

    protected static final String doc6 = "This is THE original sentence.";

    @Before
    public void setUp() {
        categorizer = new TfIdfCategorizer(65536);
        categorizer.getVectorizer().window(1);
        categorizer.update("pets", doc1);
        categorizer.update("pets", doc2);
        categorizer.update("pets", doc3);
        categorizer.update("monkeys", doc4);
        categorizer.update("monkeys", doc5);
        categorizer.update("original", doc6);
    }

    @Test
    public void testMissingTopics() {
        TfIdfCategorizer empty = new TfIdfCategorizer(65536);
        Map<String, Float> sims = empty.getSimilarities(Arrays.asList("this", "is", "a", "query"));
        assertEquals(0, sims.size());

        String query = "This is a query.";
        sims = empty.getSimilarities(query);
        assertEquals(0, sims.size());

        List<String> suggestions = categorizer.guessCategories(query, 3);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testEmptyQuery() {
        Map<String, Float> sims = categorizer.getSimilarities(new ArrayList<String>());
        assertEquals(0, sims.size());
    }

    @Test
    public void testUnseenTerms() {
        Map<String, Float> sims = categorizer.getSimilarities(Arrays.asList("bidule", "machin", "chouette"));
        assertEquals(0, sims.size());
    }

    @Test
    public void testCloseTopics() {
        String query = "The dog barks at the cat that sits on the mat.";
        Map<String, Float> sims = categorizer.getSimilarities(query);
        assertEquals(3, sims.size());
        assertEquals(0.60, sims.get("pets"), 0.01);
        assertEquals(0.15, sims.get("monkeys"), 0.01);
        assertEquals(0.00, sims.get("original"), 0.01);

        List<String> suggestions = categorizer.guessCategories(query, 3);
        assertEquals(1, suggestions.size());
        assertEquals("pets", suggestions.get(0));

        // results stay the same when switching in readonly mode:
        categorizer.disableUpdate();
        suggestions = categorizer.guessCategories(query, 3);
        assertEquals(1, suggestions.size());
        assertEquals("pets", suggestions.get(0));
    }

    @Test
    public void testPerfectMatch() {
        String allThePets = doc1 + " " + doc2 + " " + doc3;
        Map<String, Float> sims = categorizer.getSimilarities(allThePets);
        assertEquals(3, sims.size());
        assertEquals(1.00, sims.get("pets"), 0.01);
        assertEquals(0.05, sims.get("monkeys"), 0.01);
        assertEquals(0.00, sims.get("original"), 0.01);
    }

    @Test
    public void testRarityMatch() {
        Map<String, Float> sims = categorizer.getSimilarities("This is as original as a " + "very original sentence.");
        assertEquals(3, sims.size());
        assertEquals(0.00, sims.get("pets"), 0.01);
        assertEquals(0.00, sims.get("monkeys"), 0.01);
        assertEquals(0.94, sims.get("original"), 0.01);
    }

    @Test
    public void testSerialization() throws Exception {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        categorizer.saveToStream(byteOutStream);

        InputStream byteInStream = new ByteArrayInputStream(byteOutStream.toByteArray());
        TfIdfCategorizer copiedCategorizer = TfIdfCategorizer.load(byteInStream);
        Map<String, Float> sims = copiedCategorizer.getSimilarities("The dog barks"
                + " at the cat that sits on the mat.");
        assertEquals(3, sims.size());
        assertEquals(0.61, sims.get("pets"), 0.01);
        assertEquals(0.15, sims.get("monkeys"), 0.01);
        assertEquals(0.00, sims.get("original"), 0.01);
    }

}
