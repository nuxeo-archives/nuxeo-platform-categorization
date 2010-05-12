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
package org.nuxeo.ecm.platform.categorization.service;

import java.io.IOException;


public interface CategorizerFactory {

    /**
     * Factory method to build a categorizer instance from a model file
     * typically holding data dependent parameters typically trained on a
     * statistically representative text corpus.
     *
     * @param modelFile the model file to load
     * @param readonly if true set the model in read only mode and disable the
     *            learning mode
     * @throws IOException
     */
    Categorizer loadInstance(String modelFile, boolean readonly)
            throws IOException;

}
