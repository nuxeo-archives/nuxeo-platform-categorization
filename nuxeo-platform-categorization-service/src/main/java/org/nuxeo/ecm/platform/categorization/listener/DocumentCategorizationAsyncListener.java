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
package org.nuxeo.ecm.platform.categorization.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.common.collections.ScopedMap;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.categorization.service.DocumentCategorizationService;
import org.nuxeo.runtime.api.Framework;

/**
 * Base default implementation of an asynchronous event listener that runs the document categorization service.
 *
 * @author ogrisel@nuxeo.com
 */
public class DocumentCategorizationAsyncListener implements PostCommitEventListener {

    protected static final Log log = LogFactory.getLog(DocumentCategorizationAsyncListener.class);

    protected static final String ALLREADY_CATEGORIZED_FLAG = DocumentCategorizationAsyncListener.class.getName();

    // to be overridden in derived classes
    protected Set<String> eventNames = new HashSet<String>(Arrays.asList(DocumentEventTypes.DOCUMENT_CREATED,
            DocumentEventTypes.DOCUMENT_UPDATED));

    protected DocumentCategorizationService service;

    public void handleEvent(EventBundle events) {
        // collect ids of documents to analyze while filtering duplicated doc
        // ids
        Set<DocumentModel> collectedDocuments = new LinkedHashSet<DocumentModel>(events.size());
        for (Event event : events) {
            if (!eventNames.contains(event.getName())) {
                continue;
            }
            EventContext ctx = event.getContext();
            if (ctx.hasProperty(ALLREADY_CATEGORIZED_FLAG)) {
                // avoid infinite loops with event listeners triggering them
                // selves on the same documents
                continue;
            }
            if (ctx instanceof DocumentEventContext) {
                DocumentEventContext docCtx = (DocumentEventContext) ctx;
                DocumentModel doc = docCtx.getSourceDocument();
                if (doc != null) {
                    ScopedMap contextData = doc.getContextData();
                    contextData.putScopedValue(ScopeType.REQUEST, ALLREADY_CATEGORIZED_FLAG, Boolean.TRUE);
                    collectedDocuments.add(doc);
                }
            }
        }
        if (!collectedDocuments.isEmpty()) {
            // assume all document stem from the same repo, with the
            // save session
            CoreSession session = collectedDocuments.iterator().next().getCoreSession();
            DocumentCategorizationService categorizationService = Framework.getService(
                    DocumentCategorizationService.class);
            List<DocumentModel> documents = categorizationService.updateCategories(
                    new ArrayList<DocumentModel>(collectedDocuments));
            if (!documents.isEmpty()) {
                session.saveDocuments(documents.toArray(new DocumentModel[documents.size()]));
                session.save();
            }
        }
    }

}
