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

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.nuxeo.ecm.platform.categorization.service.Categorizer;

public class SubjectsCategorizerTest {

    @Test
    public void testPretrainedTopicsModel() throws IOException, ClassNotFoundException {
        TfIdfCategorizerFactory factory = new TfIdfCategorizerFactory();
        Categorizer categorizer = factory.loadInstance("models/topics-51-tfidf-65536-model.gz", true);

        String query = "The Sydney Opera House was conceived and" + " largely built by Jørn Utzon.";
        List<String> suggestions = categorizer.guessCategories(query, 3, 5.0);
        assertEquals(1, suggestions.size());
        assertEquals("architecture", suggestions.get(0));

        query = "The database vendor Oracle bought the software editor" + " and hardware manufacturer Sun in 2009.";
        suggestions = categorizer.guessCategories(query, 3, 2.0);
        assertEquals(3, suggestions.size());
        assertEquals("information_technology", suggestions.get(0));
        assertEquals("electronic", suggestions.get(1));
        assertEquals("astronomy", suggestions.get(2)); // bad!

        query = "The Terracotta Army are the Terra Cotta Warriors" + " and Horses of Qin Shi Huang";
        suggestions = categorizer.guessCategories(query, 3, 4.0);
        assertEquals(1, suggestions.size());
        assertEquals("history", suggestions.get(0));

        query = "The financial crisis of 2007–2010 has been called by leading"
                + " economists the worst financial crisis since the" + " Great Depression of the 1930s.";
        suggestions = categorizer.guessCategories(query, 3, 2.0);
        assertEquals(1, suggestions.size());
        assertEquals("economy", suggestions.get(0));
    }
}
