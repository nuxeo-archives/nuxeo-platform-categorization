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
package org.nuxeo.ecm.platform.categorization.service;

import java.util.List;

/**
 * Common interface for document categorization based on text content.
 *
 * @author ogrisel
 */
public interface Categorizer {

    /**
     * Compute a list of suggested categories, sorted by decreasing confidence based on the textual content of the
     * document.
     *
     * @param textContent
     * @param maxSuggestions
     */
    List<String> guessCategories(String textContent, int maxSuggestions);

    /**
     * Compute a list of suggested categories, sorted by decreasing confidence based on the textual content of the
     * document.
     *
     * @param textContent
     * @param maxSuggestions
     * @param precisionThreshold or null to use the default threshold of the implementation.
     * @return
     */
    List<String> guessCategories(String textContent, int maxSuggestions, Double precisionThreshold);

}
