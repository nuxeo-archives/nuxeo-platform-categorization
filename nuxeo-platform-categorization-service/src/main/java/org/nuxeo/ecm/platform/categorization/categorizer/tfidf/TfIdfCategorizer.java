/*
 * (C) Copyright 2009-2016 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.ecm.platform.categorization.categorizer.tfidf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.nuxeo.ecm.platform.categorization.service.Categorizer;

/**
 * Maintains a map of TF counts vectors in memory (just for a few reference documents or topics) along with the common
 * IDF estimate of all previously seen text content.
 * <p>
 * See: http://en.wikipedia.org/wiki/Tfidf
 * <p>
 * Classification is then achieved using the cosine similarity between the TF-IDF of the document to classify and the
 * registered topics.
 */
public class TfIdfCategorizer extends PrimitiveVectorHelper implements Categorizer, Serializable {

    private static final long serialVersionUID = 1L;

    public static final Log log = LogFactory.getLog(TfIdfCategorizer.class);

    protected final Set<String> topicNames = new TreeSet<>();

    protected final Map<String, Object> topicTermCount = new ConcurrentHashMap<>();

    protected final Map<String, Object> cachedTopicTfIdf = new ConcurrentHashMap<>();

    protected final Map<String, Float> cachedTopicTfIdfNorm = new ConcurrentHashMap<>();

    protected long[] allTermCounts;

    protected final int dim;

    protected float[] cachedIdf;

    protected long totalTermCount = 0;

    protected final HashingVectorizer vectorizer;

    protected transient Analyzer analyzer;

    protected Double ratioOverMedian = 3.0;

    protected boolean updateDisabled = false;

    public TfIdfCategorizer() {
        this(524288); // 2 ** 19
    }

    public TfIdfCategorizer(int dim) {
        this.dim = dim;
        allTermCounts = new long[dim];
        vectorizer = new HashingVectorizer().dimension(dim);
    }

    public HashingVectorizer getVectorizer() {
        return vectorizer;
    }

    public Analyzer getAnalyzer() {
        if (analyzer == null) {
            // TODO: make it possible to configure the stop words
            analyzer = new StandardAnalyzer();
        }
        return analyzer;
    }

    /**
     * Precompute all the TF-IDF vectors and unload the original count vectors to spare some memory. Updates won't be
     * possible any more.
     */
    public synchronized void disableUpdate() {
        updateDisabled = true;
        // compute all the frequencies
        getIdf();
        for (String topicName : topicNames) {
            tfidf(topicName);
            tfidfNorm(topicName);
        }
        // upload the count vectors
        topicTermCount.clear();
        allTermCounts = null;
    }

    /**
     * Update the model to take into account the statistical properties of a document that is known to be relevant to
     * the given topic. Warning: this method is not thread safe: it should not be used concurrently with @see
     * #getSimilarities(List)
     *
     * @param topicName the name of the document topic or category
     * @param terms the list of document tokens (use a lucene analyzer to extract theme for instance)
     */
    public void update(String topicName, List<String> terms) {
        if (updateDisabled) {
            throw new IllegalStateException("updates are no longer authorized once #disableUpdate has been called");
        }
        long[] counts = vectorizer.count(terms);
        totalTermCount += sum(counts);
        long[] topicCounts = (long[]) topicTermCount.get(topicName);
        if (topicCounts == null) {
            topicCounts = new long[dim];
            topicTermCount.put(topicName, topicCounts);
            topicNames.add(topicName);
        }
        add(topicCounts, counts);
        add(allTermCounts, counts);
        invalidateCache(topicName);
    }

    /**
     * Update the model to take into account the statistical properties of a document that is known to be relevant to
     * the given topic. Warning: this method is not thread safe: it should not be used concurrently with @see
     * #getSimilarities(List)
     *
     * @param topicName the name of the document topic or category
     * @param textContent textual content to be tokenized and analyzed
     */
    public void update(String topicName, String textContent) {
        update(topicName, tokenize(textContent));
    }

    protected void invalidateCache(String topicName) {
        cachedTopicTfIdf.remove(topicName);
        cachedTopicTfIdfNorm.remove(topicName);
        cachedIdf = null;
    }

    protected void invalidateCache() {
        for (String topicName : topicNames) {
            invalidateCache(topicName);
        }
    }

    /**
     * For each registered topic, compute the cosine similarity of the TFIDF vector of the topic and the one of the
     * document given by a list of tokens.
     *
     * @param terms a tokenized document.
     * @return a map of topic names to float values from 0 to 1 sorted by reverse value.
     */
    public Map<String, Float> getSimilarities(List<String> terms) {
        SortedMap<String, Float> similarities = new TreeMap<>();

        float[] tfidf1 = getTfIdf(vectorizer.count(terms));
        float norm1 = normOf(tfidf1);
        if (norm1 == 0) {
            return similarities;
        }

        for (String topicName : topicNames) {
            float[] tfidf2 = tfidf(topicName);
            float norm2 = tfidfNorm(topicName);
            if (norm2 == 0) {
                continue;
            }
            similarities.put(topicName, dot(tfidf1, tfidf2) / (norm1 * norm2));
        }
        return sortByDecreasingValue(similarities);
    }

    /**
     * For each registered topic, compute the cosine similarity of the TFIDF vector of the topic and the one of the
     * document.
     *
     * @param allThePets the document to be tokenized and analyzed
     * @return a map of topic names to float values from 0 to 1 sorted by reverse value.
     */
    public Map<String, Float> getSimilarities(String allThePets) {
        return getSimilarities(tokenize(allThePets));
    }

    protected float tfidfNorm(String topicName) {
        Float norm = cachedTopicTfIdfNorm.get(topicName);
        if (norm == null) {
            norm = normOf(tfidf(topicName));
            cachedTopicTfIdfNorm.put(topicName, norm);
        }
        return norm.floatValue();
    }

    protected float[] tfidf(String topicName) {
        float[] tfidf = (float[]) cachedTopicTfIdf.get(topicName);
        if (tfidf == null) {
            tfidf = getTfIdf((long[]) topicTermCount.get(topicName));
            cachedTopicTfIdf.put(topicName, tfidf);
        }
        return tfidf;
    }

    protected float[] getTfIdf(long[] counts) {
        float[] idf = getIdf();
        float[] tfidf = new float[counts.length];
        long sum = sum(counts);
        if (sum == 0) {
            return tfidf;
        }
        for (int i = 0; i < counts.length; i++) {
            tfidf[i] = ((float) counts[i]) / sum * idf[i];
        }
        return tfidf;
    }

    protected float[] getIdf() {
        if (cachedIdf == null) {
            float[] idf = new float[allTermCounts.length];
            for (int i = 0; i < allTermCounts.length; i++) {
                if (allTermCounts[i] == 0) {
                    idf[i] = 0;
                } else {
                    idf[i] = (float) Math.log1p(((float) totalTermCount) / allTermCounts[i]);
                }
            }
            // atomic update to ensure thread-safeness
            cachedIdf = idf;
        }
        return cachedIdf;
    }

    public int getDimension() {
        return dim;
    }

    /**
     * Utility method to initialize the parameters from a set of UTF-8 encoded text files with names used as topic
     * names.
     * <p>
     * The content of the file to assumed to be lines of terms separated by whitespaces without punctuation.
     */
    public void learnFiles(File folder) throws IOException {
        if (!folder.isDirectory()) {
            throw new IOException(String.format("%s is not a folder", folder.getAbsolutePath()));
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            String topicName = file.getName();
            if (topicName.contains(".")) {
                topicName = topicName.substring(0, topicName.indexOf('.'));
            }
            log.info(String.format("About to analyze file %s", file.getAbsolutePath()));
            try (FileInputStream is = new FileInputStream(file);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
                String line = reader.readLine();
                int i = 0;
                while (line != null) {
                    update(topicName, line);
                    line = reader.readLine();
                    i++;
                    if (i % 10000 == 0) {
                        log.info(String.format("Analyzed %d lines from '%s'", i, file.getAbsolutePath()));
                    }
                }
            }
        }
    }

    /**
     * Save the model to a compressed binary format on the filesystem.
     *
     * @param file where to write the model
     */
    public void saveToFile(File file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            saveToStream(out);
        }
    }

    /**
     * Save a compressed binary representation of the trained model.
     *
     * @param out the output stream to write to
     */
    public void saveToStream(OutputStream out) throws IOException {
        if (updateDisabled) {
            throw new IllegalStateException("model in disabled update mode cannot be saved");
        }
        invalidateCache();
        GZIPOutputStream gzOut = new GZIPOutputStream(out);
        ObjectOutputStream objOut = new ObjectOutputStream(gzOut);
        objOut.writeObject(this);
        gzOut.finish();
    }

    /**
     * Load a TfIdfCategorizer instance from it's compressed binary representation.
     *
     * @param in the input stream to read from
     * @return a new instance with parameters coming from the saved version
     */
    public static TfIdfCategorizer load(InputStream in) throws IOException, ClassNotFoundException {
        GZIPInputStream gzIn = new GZIPInputStream(in);
        ObjectInputStream objIn = new ObjectInputStream(gzIn);
        TfIdfCategorizer cat = (TfIdfCategorizer) objIn.readObject();
        log.info(String.format("Sucessfully loaded model with %d topics, dimension %d and density %f",
                cat.getTopicNames().size(), cat.getDimension(), cat.getDensity()));
        return cat;
    }

    public double getDensity() {
        long sum = 0;
        for (Object singleTopicTermCount : topicTermCount.values()) {
            for (long c : (long[]) singleTopicTermCount) {
                sum += c != 0L ? 1 : 0;
            }
        }
        for (long c : allTermCounts) {
            sum += c != 0 ? 1 : 0;
        }
        return ((double) sum) / ((topicNames.size() + 1) * getDimension());
    }

    public Set<String> getTopicNames() {
        return topicNames;
    }

    /**
     * Load a TfIdfCategorizer instance from it's compressed binary representation from a named resource in the
     * classloading path of the current thread.
     *
     * @param modelPath the path of the file model in the classloading path
     * @return a new instance with parameters coming from the saved version
     */
    public static TfIdfCategorizer load(String modelPath) throws IOException, ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return load(loader.getResourceAsStream(modelPath));
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Train a model:\n" + "First argument is the model filename (e.g. my-model.gz)\n"
                    + "Second argument is the path to a folder with UTF-8 text files\n"
                    + "Third optional argument is the dimension of the model");
            System.exit(0);
        }
        File modelFile = new File(args[0]);
        TfIdfCategorizer categorizer;
        if (modelFile.exists()) {
            if (log.isInfoEnabled()) {
                log.info("Loading model from: " + modelFile.getAbsolutePath());
            }
            try (FileInputStream is = new FileInputStream(modelFile)) {
                categorizer = load(is);
            }
        } else {
            if (args.length == 3) {
                categorizer = new TfIdfCategorizer(Integer.valueOf(args[2]));
            } else {
                categorizer = new TfIdfCategorizer();
            }
            if (log.isInfoEnabled()) {
                log.info("Initializing new model with dimension: " + categorizer.getDimension());
            }
        }
        categorizer.learnFiles(new File(args[1]));
        if (log.isInfoEnabled()) {
            log.info("Saving trained model to: " + modelFile.getAbsolutePath());
        }
        categorizer.saveToFile(modelFile);
    }

    public List<String> guessCategories(String textContent, int maxSuggestions) {
        return guessCategories(textContent, maxSuggestions, null);
    }

    public List<String> guessCategories(String textContent, int maxSuggestions, Double precisionThreshold) {
        precisionThreshold = precisionThreshold == null ? ratioOverMedian : precisionThreshold;
        Map<String, Float> sims = getSimilarities(tokenize(textContent));
        Float median = findMedian(sims);
        List<String> suggested = new ArrayList<>();
        for (Map.Entry<String, Float> sim : sims.entrySet()) {
            double ratio = median != 0 ? sim.getValue() / median : 100;
            if (suggested.size() >= maxSuggestions || ratio < precisionThreshold) {
                break;
            }
            suggested.add(sim.getKey());
        }
        return suggested;
    }

    public List<String> tokenize(String textContent) {
        try {
            List<String> terms = new ArrayList<>();
            TokenStream tokenStream = getAnalyzer().tokenStream(null, textContent);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                terms.add(charTermAttribute.toString());
            }
            tokenStream.end();
            tokenStream.close();
            return terms;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Map<String, Float> sortByDecreasingValue(Map<String, Float> map) {
        List<Map.Entry<String, Float>> list = new LinkedList<>(map.entrySet());
        list.sort((e1, e2) -> -e1.getValue().compareTo(e2.getValue()));
        Map<String, Float> result = new LinkedHashMap<>();
        for (Map.Entry<String, Float> e : list) {
            result.put(e.getKey(), e.getValue());
        }
        return result;
    }

    public static Float findMedian(Map<String, Float> sortedMap) {
        int remaining = sortedMap.size() / 2;
        Float median = 0.0f;
        for (Float value : sortedMap.values()) {
            median = value;
            if (remaining-- <= 0) {
                break;
            }
        }
        return median;
    }

}
