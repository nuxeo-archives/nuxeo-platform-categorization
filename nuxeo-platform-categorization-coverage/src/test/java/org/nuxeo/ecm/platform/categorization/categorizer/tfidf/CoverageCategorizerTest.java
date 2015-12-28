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

public class CoverageCategorizerTest {

    @Test
    public void testPretrainedCountryModel() throws IOException, ClassNotFoundException {
        TfIdfCategorizerFactory factory = new TfIdfCategorizerFactory();
        Categorizer categorizer = factory.loadInstance("models/countries-30-tfidf-65536-model.gz", true);

        String query = "Berlin is a Bundesland";
        List<String> suggestions = categorizer.guessCategories(query, 3, 5.0);
        assertEquals(suggestions.size(), 2);
        assertEquals("germany", suggestions.get(0));
        assertEquals("russia", suggestions.get(1));

        query = "Paris sera toujours Paris.";
        suggestions = categorizer.guessCategories(query, 3, 5.0);
        assertEquals(1, suggestions.size());
        assertEquals("france", suggestions.get(0));

        query = "The Terracotta Army are the Terra Cotta Warriors" + " and Horses of Qin Shi Huang";
        suggestions = categorizer.guessCategories(query, 3, 3.0);
        assertEquals(1, suggestions.size());
        assertEquals("china", suggestions.get(0));
    }

}
