/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.platform.categorization.categorizer.tfidf;

import java.io.IOException;

import org.nuxeo.ecm.platform.categorization.service.Categorizer;
import org.nuxeo.ecm.platform.categorization.service.CategorizerFactory;

public class TfIdfCategorizerFactory implements CategorizerFactory {

    public Categorizer loadInstance(String modelFile, boolean readonly) {
        try {
            TfIdfCategorizer categorizer = TfIdfCategorizer.load(modelFile);
            if (readonly) {
                categorizer.disableUpdate();
            }
            return categorizer;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
