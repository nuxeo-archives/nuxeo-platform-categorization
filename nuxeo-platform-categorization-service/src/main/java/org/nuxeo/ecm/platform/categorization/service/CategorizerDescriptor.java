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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.PropertyException;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.runtime.model.RuntimeContext;

@XObject("categorizer")
public class CategorizerDescriptor {

    public static final Log log = LogFactory.getLog(CategorizerDescriptor.class);

    protected static int DEFAULT_MAX_SUGGESTIONS = 3;

    protected static int DEFAULT_MIN_TEXT_LENGTH = 50;

    protected RuntimeContext runtimeContext;

    @XNode("@name")
    protected String name;

    @XNode("@property")
    protected String propertyXPath;

    @XNode("@factory")
    protected String className;

    @XNode("@model")
    protected String modelFile;

    @XNode("@enabled")
    protected boolean enabled = true;

    @XNode("@maxSuggestions")
    protected int maxSuggestions = DEFAULT_MAX_SUGGESTIONS;

    @XNode("@minTextLength")
    protected int minTextLength = DEFAULT_MIN_TEXT_LENGTH;

    @XNode("@precisionThreshold")
    protected Double precisionThreshold;

    @XNodeList(value = "skip/facet@name", type = ArrayList.class, componentType = String.class)
    public List<String> skipFacets = new ArrayList<String>();

    @XNodeMap(value = "mapping/outcome", key = "@name", type = HashMap.class, componentType = String.class)
    Map<String, String> mapping = new HashMap<String, String>();

    protected Categorizer categorizer;

    protected CategorizerFactory factory;

    public String getName() {
        return name;
    }

    public void initializeInContext(RuntimeContext context) {
        if (className != null) {
            try {
                factory = (CategorizerFactory) context.loadClass(className).newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        // if className is null, this descriptor is probably an override
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void processDocument(DocumentModel doc, String textContent) throws PropertyException {
        if (categorizer == null) {
            // lazy loading of the model in memory
            categorizer = factory.loadInstance(modelFile, true);
        }

        List<String> suggestedCategories = categorizer.guessCategories(textContent, maxSuggestions, precisionThreshold);
        log.debug(String.format("Sugestions for document '%s' and property '%s'"
                + " with textcontent of length %d: [%s]", doc.getTitle(), propertyXPath, textContent.length(),
                StringUtils.join(suggestedCategories, ", ")));

        List<String> propertyValues = new ArrayList<String>(maxSuggestions);
        if (!mapping.isEmpty()) {
            for (String suggestion : suggestedCategories) {
                String property = mapping.get(suggestion);
                if (property != null) {
                    propertyValues.add(property);
                }
            }
        } else {
            propertyValues.addAll(suggestedCategories);
        }

        if (propertyValues.isEmpty()) {
            return;
        } else if (propertyValues.size() > maxSuggestions) {
            propertyValues = propertyValues.subList(0, maxSuggestions);
        }
        Property property = doc.getProperty(propertyXPath);
        if (property.isList()) {
            doc.setPropertyValue(propertyXPath, (Serializable) propertyValues);
        } else {
            doc.setPropertyValue(propertyXPath, propertyValues.get(0));
        }
    }

    @SuppressWarnings("unchecked")
    public boolean shouldProcess(DocumentModel doc) {
        if (skipFacets != null) {
            for (String facetToSkip : skipFacets) {
                if (doc.hasFacet(facetToSkip)) {
                    return false;
                }
            }
        }
        // TODO make it possible to delegate the work to the categorizer impl
        try {
            Property property = doc.getProperty(propertyXPath);
            if (property.getValue() == null) {
                return true;
            }
            if (property.isList()) {
                List<String> values = property.getValue(List.class);
                if (values.isEmpty()) {
                    return true;
                }
            } else if (property.isComplex()) {
                // TODO: use a dedicated exception class instead
                throw new NuxeoException(propertyXPath
                        + " is a complex type field and hence is not suitable for text based categorization");
            } else if (property.getValue().toString().trim().length() == 0) {
                return true;
            }
        } catch (PropertyException e) {
            // document has not such property
            return false;
        }
        // do not categorize document that already have a non-empty target
        // property
        return false;
    }

    public int getMinTextLength() {
        return minTextLength;
    }

    public void setMinTextLength(int minTextLength) {
        this.minTextLength = minTextLength;
    }

    /**
     * Chainable update the parameters of the current descriptor with the non-null parameters of the other descriptor.
     */
    public CategorizerDescriptor merge(CategorizerDescriptor other) {
        if (other != null) {
            if (other.propertyXPath != null) {
                propertyXPath = other.propertyXPath;
            }
            if (other.className != null) {
                className = other.className;
            }
            if (other.categorizer != null) {
                categorizer = other.categorizer;
            }
            if (other.factory != null) {
                factory = other.factory;
            }
            if (other.maxSuggestions != DEFAULT_MAX_SUGGESTIONS) {
                maxSuggestions = other.maxSuggestions;
            }
            if (other.minTextLength != DEFAULT_MIN_TEXT_LENGTH) {
                minTextLength = other.minTextLength;
            }
            if (other.precisionThreshold != null) {
                precisionThreshold = other.precisionThreshold;
            }
            if (other.modelFile != null) {
                modelFile = other.modelFile;
            }
            if (!other.mapping.isEmpty()) {
                mapping = other.mapping;
            }
            if (!other.skipFacets.isEmpty()) {
                skipFacets = other.skipFacets;
            }
        }
        return this;
    }
}
