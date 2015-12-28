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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PropertyException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.utils.BlobsExtractor;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.model.Extension;

public class DocumentCategorizationServiceImpl extends DefaultComponent implements DocumentCategorizationService {

    public static final String CATEGORIZERS_XP_NAME = "categorizers";

    public static final String ANY2TEXT = "any2text";

    private static final Log log = LogFactory.getLog(DocumentCategorizationServiceImpl.class);

    protected Map<String, CategorizerDescriptor> mergedCategorizers;

    protected final List<CategorizerDescriptor> registeredCategorizers = new ArrayList<CategorizerDescriptor>();

    protected final BlobsExtractor extractor = new BlobsExtractor();

    protected ConversionService conversionService;

    @Override
    public void registerExtension(Extension extension) {
        if (extension.getExtensionPoint().equals(CATEGORIZERS_XP_NAME)) {
            Object[] contribs = extension.getContributions();
            for (Object contrib : contribs) {
                if (contrib instanceof CategorizerDescriptor) {
                    registerCategorizerDescriptor((CategorizerDescriptor) contrib, extension);
                }
            }
        }
    }

    @Override
    public void unregisterExtension(Extension extension) {
        if (extension.getExtensionPoint().equals(CATEGORIZERS_XP_NAME)) {
            Object[] contribs = extension.getContributions();
            for (Object contrib : contribs) {
                if (contrib instanceof CategorizerDescriptor) {
                    unregisterCategorizerDescriptor((CategorizerDescriptor) contrib, extension);
                }
            }
        }
    }

    protected void registerCategorizerDescriptor(CategorizerDescriptor descriptor, Extension extension) {

        descriptor.initializeInContext(extension.getContext());

        // register and invalidFate merged Categorizers
        registeredCategorizers.add(descriptor);
        mergedCategorizers = null;
    }

    protected synchronized void unregisterCategorizerDescriptor(CategorizerDescriptor descriptor, Extension extension) {

        int index = registeredCategorizers.lastIndexOf(descriptor);
        if (index != -1) {
            registeredCategorizers.remove(index);
            mergedCategorizers = null;
        } else {
            log.warn(String.format("no registered Categorizer under name '%s'", descriptor.getName()));
        }
    }

    protected Map<String, CategorizerDescriptor> getMergedDescriptors() {
        if (mergedCategorizers == null) {
            synchronized (this) {
                if (mergedCategorizers == null) {
                    mergedCategorizers = new LinkedHashMap<String, CategorizerDescriptor>();
                    for (CategorizerDescriptor descriptor : registeredCategorizers) {
                        String name = descriptor.getName();
                        if (descriptor.isEnabled()) {
                            CategorizerDescriptor previousDescriptor = mergedCategorizers.get(name);
                            CategorizerDescriptor mergedDescriptor = new CategorizerDescriptor();
                            mergedDescriptor.merge(previousDescriptor);
                            mergedDescriptor.merge(descriptor);
                            mergedCategorizers.put(name, mergedDescriptor);
                        } else {
                            mergedCategorizers.remove(name);
                        }
                    }
                }
            }
        }
        return mergedCategorizers;
    }

    public List<DocumentModel> updateCategories(CoreSession session, List<DocumentRef> docRefs) {
        DocumentModelList documents = session.getDocuments(docRefs.toArray(new DocumentRef[docRefs.size()]));
        return updateCategories(documents);
    }

    public List<DocumentModel> updateCategories(List<DocumentModel> documents) {

        Set<DocumentModel> impactedDocs = new LinkedHashSet<DocumentModel>();

        for (DocumentModel doc : documents) {
            List<CategorizerDescriptor> categorizersToApply = new LinkedList<CategorizerDescriptor>();
            for (CategorizerDescriptor categorizer : getMergedDescriptors().values()) {
                if (categorizer.shouldProcess(doc)) {
                    categorizersToApply.add(categorizer);
                }
            }
            if (!categorizersToApply.isEmpty()) {
                // avoid extracting the fulltext content if no categorizer to
                // apply
                String textContent = extractTextContent(doc);
                for (CategorizerDescriptor categorizer : categorizersToApply) {
                    if (textContent.length() > categorizer.getMinTextLength()) {
                        categorizer.processDocument(doc, textContent);
                    }
                }
                impactedDocs.add(doc);
            }
        }
        return new ArrayList<DocumentModel>(impactedDocs);
    }

    public String extractTextContent(DocumentModel doc) {
        List<String> strings = new LinkedList<String>();

        // text properties
        strings.add(doc.getTitle());
        String description = doc.getProperty("dc:description").getValue(String.class);
        if (description != null) {
            strings.add(description);
        }
        // TODO: extract / factorize / reuse the SQL storage full-text indexing
        // text extraction code

        List<Blob> blobs = extractor.getBlobs(doc);
        try {
            String noteContent = (String) doc.getPropertyValue("note:note");
            Blob noteBlob = Blobs.createBlob(noteContent, "text/html");
            blobs.add(noteBlob);
        } catch (PropertyException pe) {
            // not a note, ignore
        }

        // binary properties
        ConversionService conversionService = getConversionService();
        for (Blob blob : blobs) {
            try {
                SimpleBlobHolder bh = new SimpleBlobHolder(blob);
                BlobHolder result = conversionService.convert(ANY2TEXT, bh, null);
                if (result == null) {
                    continue;
                }
                blob = result.getBlob();
                if (blob == null) {
                    continue;
                }
                String string = new String(blob.getByteArray(), "UTF-8");
                // strip '\0 chars from text
                if (string.indexOf('\0') >= 0) {
                    string = string.replace("\0", " ");
                }
                strings.add(string);
            } catch (ConversionException | IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return StringUtils.join(strings, "\n");
    }

    protected ConversionService getConversionService() {
        if (conversionService == null) {
            conversionService = Framework.getService(ConversionService.class);
        }
        return conversionService;
    }
}
