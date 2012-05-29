/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

import java.io.IOException;

import org.nuxeo.ecm.platform.categorization.service.CategorizerFactory;

public class LanguageCategorizerFactory implements CategorizerFactory {

    public LanguageCategorizer loadInstance(String modelFile, boolean readonly)
            throws IOException {
        return new LanguageCategorizer(modelFile);
    }

}
