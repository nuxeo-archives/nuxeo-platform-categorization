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

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;

/**
 * Service to automatically suggests values for metadata category fields from
 * the text content of a document.
 *
 * @author ogrisel
 */
public interface DocumentCategorizationService {

    /**
     * Run all registered document categorizers on the given documents fetched
     * using the provided session.
     *
     * Note: the service does not save the documents. This responsibility is
     * left to the caller.
     *
     * @param session the Core session used to fetch the document
     * @param docRefs the list of documents to process
     * @return the updated documents (unchanged documents are filtered out)
     * @throws Exception if there is a problem while extracting the text content
     */
    List<DocumentModel> updateCategories(CoreSession session,
            List<DocumentRef> docRefs) throws ClientException, Exception;

    /**
     * Run all registered document categorizers on the given documents.
     *
     * Note: the service does not save the documents. This responsibility is
     * left to the caller.
     *
     * @param documents the list of documents to process
     * @return the updated documents (unchanged documents are filtered out)
     * @throws Exception if there is a problem while extracting the text content
     */
    List<DocumentModel> updateCategories(List<DocumentModel> documents)
            throws ClientException, Exception;

}
