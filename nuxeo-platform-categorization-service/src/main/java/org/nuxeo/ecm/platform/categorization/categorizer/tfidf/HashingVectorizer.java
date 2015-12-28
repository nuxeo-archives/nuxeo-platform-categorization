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
package org.nuxeo.ecm.platform.categorization.categorizer.tfidf;

import java.io.Serializable;
import java.util.List;

/**
 * Hashed vector representation of the token unigrams and bigrams of a document provided as a sequence of tokens.
 * <p>
 * We use a hash representations to be able to maintain low memory requirements by avoiding to store an explicit map
 * from string bigrams to feature vector index in memory.
 * <p>
 * http://hunch.net/~jl/projects/hash_reps/index.html http://en.wikipedia.org/wiki/Bloom_filter#Counting_filters
 *
 * @author ogrisel
 */
public class HashingVectorizer implements Serializable {

    private static final long serialVersionUID = 1L;

    protected int dim = 524288; // 2 ** 19

    protected int probes = 2;

    protected int window = 0;

    /**
     * Chain configuration of the number of buckets, which is also the number of the vectors dimensions, small values
     * mean high probabilities of collisions.
     */
    public HashingVectorizer dimension(int dim) {
        this.dim = dim;
        return this;
    }

    /**
     * Chain configuration of the number of terms to hash together: window = 1 means unigrams and bigrams, window = 3
     * would add bigrams of distance 2, and so on.
     */
    public HashingVectorizer window(int window) {
        this.window = window;
        return this;
    }

    /**
     * Chain configuration of the number of probes, i.e. number of distinct hash functions to use for each ngram count.
     */
    public HashingVectorizer probes(int probes) {
        this.probes = probes;
        return this;
    }

    // TODO: implement a sparse equivalent using an long[] for the indices of
    // non-zero values and a second count[] for the counts them-selves
    public long[] count(List<String> tokens) {
        long[] counts = new long[dim];
        addCounts(tokens, counts);
        return counts;
    }

    public void addCounts(List<String> tokens, long[] counts) {
        int n = 0;
        for (String token : tokens) {
            for (int probe = 0; probe < probes; probe++) {
                counts[hash(token, probe)]++;
            }
            if (window > 0) {
                for (int j = Math.max(0, n - window); j < n; j++) {
                    for (int probe = 0; probe < probes; probe++) {
                        counts[hash(token, tokens.get(j), probe)]++;
                    }
                }
            }
            n++;
        }
    }

    protected int hash(String token, int probe) {
        return hash(token, null, probe);
    }

    protected int hash(String token, String prevToken, int probe) {
        int h = (token + " " + prevToken + " " + probe).hashCode() % dim;
        if (h < 0) {
            h += dim;
        }
        return h;
    }

}
