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
