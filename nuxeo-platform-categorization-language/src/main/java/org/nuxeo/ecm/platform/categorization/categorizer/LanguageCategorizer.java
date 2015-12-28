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
