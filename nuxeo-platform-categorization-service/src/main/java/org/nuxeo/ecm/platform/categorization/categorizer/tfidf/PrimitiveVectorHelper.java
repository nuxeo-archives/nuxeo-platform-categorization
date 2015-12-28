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

public class PrimitiveVectorHelper {

    public static void add(long[] target, long[] increment) {
        for (int i = 0; i < target.length; i++) {
            target[i] += increment[i];
        }
    }

    public static float dot(float[] v1, float[] v2) {
        float sum = 0;
        for (int i = 0; i < v1.length; i++) {
            sum += v1[i] * v2[i];
        }
        return sum;
    }

    public static float normOf(float[] vector) {
        float sum = 0f;
        for (int i = 0; i < vector.length; i++) {
            sum += vector[i] * vector[i];
        }
        return (float) Math.sqrt(sum);
    }

    public static long sum(long[] counts) {
        long sum = 0;
        for (int i = 0; i < counts.length; i++) {
            sum += counts[i];
        }
        return sum;
    }

}
