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
package org.nuxeo.ecm.platform.categorization.service;

import java.util.List;

/**
 * Common interface for document categorization based on text content.
 *
 * @author ogrisel
 */
public interface Categorizer {

    /**
     * Compute a list of suggested categories, sorted by decreasing confidence
     * based on the textual content of the document.
     *
     * @param textContent
     * @param maxSuggestions
     */
    List<String> guessCategories(String textContent, int maxSuggestions);

    /**
     * Compute a list of suggested categories, sorted by decreasing confidence
     * based on the textual content of the document.
     *
     * @param textContent
     * @param maxSuggestions
     * @param precisionThreshold or null to use the default threshold of the
     *            implementation.
     * @return
     */
    List<String> guessCategories(String textContent, int maxSuggestions,
            Double precisionThreshold);

}
