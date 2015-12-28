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

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;

/**
 * Service to automatically suggests values for metadata category fields from the text content of a document.
 *
 * @author ogrisel
 */
public interface DocumentCategorizationService {

    /**
     * Run all registered document categorizers on the given documents fetched using the provided session. Note: the
     * service does not save the documents. This responsibility is left to the caller.
     *
     * @param session the Core session used to fetch the document
     * @param docRefs the list of documents to process
     * @return the updated documents (unchanged documents are filtered out)
     */
    List<DocumentModel> updateCategories(CoreSession session, List<DocumentRef> docRefs);

    /**
     * Run all registered document categorizers on the given documents. Note: the service does not save the documents.
     * This responsibility is left to the caller.
     *
     * @param documents the list of documents to process
     * @return the updated documents (unchanged documents are filtered out)
     */
    List<DocumentModel> updateCategories(List<DocumentModel> documents);

}
