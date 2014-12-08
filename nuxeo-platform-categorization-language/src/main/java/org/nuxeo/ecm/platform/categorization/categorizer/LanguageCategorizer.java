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
package org.nuxeo.ecm.platform.categorization.categorizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knallgrau.utils.textcat.TextCategorizer;
import org.nuxeo.ecm.platform.categorization.service.Categorizer;

/**
 * Sample language guesser that straightforwardly use the pre-built models of the TextCat library.
 */
public class LanguageCategorizer implements Categorizer {

    protected final TextCategorizer languageGuesser;

    protected final static Map<String, String> languageNameToISO639Code = new HashMap<String, String>();

    public LanguageCategorizer(String modelFile) {
        if (modelFile != null) {
            languageGuesser = new TextCategorizer(modelFile);
        } else {
            languageGuesser = new TextCategorizer();
        }
    }

    public List<String> guessCategories(String textContent, int maxSuggestions) {
        // only return one, whatever max suggestion is
        return Arrays.asList(languageGuesser.categorize(textContent));
    }

    public List<String> guessCategories(String textContent, int maxSuggestions, Double precisionTreshold) {
        // languageGuesser does not support setting a custom threshold
        return guessCategories(textContent, maxSuggestions);
    }

}
