/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and others.
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
