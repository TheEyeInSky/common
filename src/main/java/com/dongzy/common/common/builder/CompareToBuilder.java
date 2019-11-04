/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dongzy.common.common.builder;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.collection.ArrayUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Assists in implementing {@link Comparable#compareTo(Object)} methods.
 * <p>
 * <p>It is consistent with <code>equals(Object)</code> and
 * <code>hashcode()</code> built with {@link EqualsBuilder} and
 * {@link HashCodeBuilder}.</p>
 * <p>
 * <p>Two Objects that compare equal using <code>equals(Object)</code> should normally
 * also compare equal using <code>compareTo(Object)</code>.</p>
 * <p>
 * <p>All relevant fields should be included in the calculation of the
 * comparison. Derived fields may be ignored. The same fields, in the same
 * order, should be used in both <code>compareTo(Object)</code> and
 * <code>equals(Object)</code>.</p>
 * <p>
 * <p>To use this class write code as follows:</p>
 * <p>
 * <pre>
 * public class MyClass {
 *   String field1;
 *   int field2;
 *   boolean field3;
 *
 *   ...
 *
 *   public int compareTo(Object o) {
 *     MyClass myClass = (MyClass) o;
 *     return new CompareToBuilder()
 *       .appendSuper(super.compareTo(o)
 *       .append(this.field1, myClass.field1)
 *       .append(this.field2, myClass.field2)
 *       .append(this.field3, myClass.field3)
 *       .toComparison();
 *   }
 * }
 * </pre>
 * <p>
 * <p>Values are compared in the order they are appended to the builder. If any comparison returns
 * a non-zero result, then that value will be the result returned by {@code toComparison()} and all
 * subsequent comparisons are skipped.</p>
 * <p>
 * reflection to determine the fields to append. Because fields can be private,
 * <code>reflectionCompare</code> uses {@link AccessibleObject#setAccessible(boolean)} to
 * bypass normal access control checks. This will fail under a security manager,
 * unless the appropriate permissions are set up correctly. It is also
 * slower than appending explicitly.</p>
 * <p>
 * <p>A typical implementation of <code>compareTo(Object)</code> using
 * <code>reflectionCompare</code> looks like:</p>
 * <p>
 * <pre>
 * public int compareTo(Object o) {
 *   return CompareToBuilder.reflectionCompare(this, o);
 * }
 * </pre>
 * <p>
 * <p>The reflective methods compare object fields in the order returned by
 * {@link Class#getDeclaredFields()}. The fields of the class are compared first, followed by those
 * of its parent classes (in order from the bottom to the top of the class hierarchy).</p>
 *
 * @see Comparable
 * @see Object#equals(Object)
 * @see Object#hashCode()
 * @see EqualsBuilder
 * @see HashCodeBuilder
 * @since 1.0
 */
public class CompareToBuilder implements Builder<Integer> {

    /**
     * Current state of the comparison as appended fields are checked.
     */
    private int comparison;

    /**
     * <p>Constructor for CompareToBuilder.</p>
     * <p>
     * <p>Starts off assuming that the objects are equal. Multiple calls are
     * then made to the various append methods, followed by a call to
     * {@link #toComparison} to get the result.</p>
     */
    public CompareToBuilder() {
        super();
        comparison = 0;
    }

    //-----------------------------------------------------------------------

    /**
     * <p>Compares two <code>Object</code>s via reflection.</p>
     * <p>
     * <p>Fields can be private, thus <code>AccessibleObject.setAccessible</code>
     * is used to bypass normal access control checks. This will fail under a
     * security manager unless the appropriate permissions are set.</p>
     * <p>
     * <ul>
     * <li>Static fields will not be compared</li>
     * <li>If <code>compareTransients</code> is <code>true</code>,
     * compares transient members.  Otherwise ignores them, as they
     * are likely derived fields.</li>
     * <li>Superclass fields will be compared</li>
     * </ul>
     * <p>
     * <p>If both <code>lhs</code> and <code>rhs</code> are <code>null</code>,
     * they are considered equal.</p>
     *
     * @param lhs           left-hand object
     * @param rhs           right-hand object
     * @param excludeFields array of fields to exclude
     * @return a negative integer, zero, or a positive integer as <code>lhs</code>
     * is less than, equal to, or greater than <code>rhs</code>
     * @throws NullPointerException if either <code>lhs</code> or <code>rhs</code>
     *                              (but not both) is <code>null</code>
     * @throws ClassCastException   if <code>rhs</code> is not assignment-compatible
     *                              with <code>lhs</code>
     * @since 2.2
     */
    public static int reflectionCompare(final Object lhs, final Object rhs, final String... excludeFields) {
        if (lhs == rhs) {
            return 0;
        }
        Validate.isTrue(lhs != null && rhs != null);
        Class<?> lhsClazz = lhs.getClass();
        if (!lhsClazz.isInstance(rhs)) {
            throw new ClassCastException();
        }
        final CompareToBuilder compareToBuilder = new CompareToBuilder();
        reflectionAppend(lhs, rhs, lhsClazz, compareToBuilder, excludeFields);
        while (lhsClazz.getSuperclass() != null) {
            lhsClazz = lhsClazz.getSuperclass();
            reflectionAppend(lhs, rhs, lhsClazz, compareToBuilder, excludeFields);
        }
        return compareToBuilder.toComparison();
    }

    /**
     * <p>Appends to <code>builder</code> the comparison of <code>lhs</code>
     * to <code>rhs</code> using the fields defined in <code>clazz</code>.</p>
     *
     * @param lhs           left-hand object
     * @param rhs           right-hand object
     * @param clazz         <code>Class</code> that defines fields to be compared
     * @param builder       <code>CompareToBuilder</code> to append to
     * @param excludeFields fields to exclude
     */
    private static void reflectionAppend(final Object lhs, final Object rhs, final Class<?> clazz,
                                         final CompareToBuilder builder,
                                         final String[] excludeFields) {

        final Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        for (int i = 0; i < fields.length && builder.comparison == 0; i++) {
            final Field f = fields[i];
            if (!ArrayUtils.contains(excludeFields, f.getName())
                    && !f.getName().contains("$")
                    && !Modifier.isTransient(f.getModifiers())
                    && !Modifier.isStatic(f.getModifiers())) {
                try {
                    builder.append(f.get(lhs), f.get(rhs));
                } catch (final IllegalAccessException e) {
                    // This can't happen. Would get a Security exception instead.
                    // Throw a runtime exception in case the impossible happens.
                    throw new InternalError("Unexpected IllegalAccessException");
                }
            }
        }
    }

    //-----------------------------------------------------------------------

    /**
     * <p>Appends to the <code>builder</code> the comparison of
     * two <code>Object</code>s.</p>
     * <p>
     * <ol>
     * <li>Check if <code>lhs == rhs</code></li>
     * <li>Check if either <code>lhs</code> or <code>rhs</code> is <code>null</code>,
     * a <code>null</code> object is less than a non-<code>null</code> object</li>
     * <li>Check the object contents</li>
     * </ol>
     * <p>
     * <p><code>lhs</code> must either be an array or implement {@link Comparable}.</p>
     *
     * @param lhs left-hand object
     * @param rhs right-hand object
     * @return this - used to chain append calls
     * @throws ClassCastException if <code>rhs</code> is not assignment-compatible
     *                            with <code>lhs</code>
     */
    public CompareToBuilder append(final Object lhs, final Object rhs) {
        return append(lhs, rhs, null);
    }

    /**
     * <p>Appends to the <code>builder</code> the comparison of
     * two <code>Object</code>s.</p>
     * <p>
     * <ol>
     * <li>Check if <code>lhs == rhs</code></li>
     * <li>Check if either <code>lhs</code> or <code>rhs</code> is <code>null</code>,
     * a <code>null</code> object is less than a non-<code>null</code> object</li>
     * <li>Check the object contents</li>
     * </ol>
     * <p>
     * <p>If <code>lhs</code> is an array, array comparison methods will be used.
     * Otherwise <code>comparator</code> will be used to compare the objects.
     * If <code>comparator</code> is <code>null</code>, <code>lhs</code> must
     * implement {@link Comparable} instead.</p>
     *
     * @param lhs        left-hand object
     * @param rhs        right-hand object
     * @param comparator <code>Comparator</code> used to compare the objects,
     *                   <code>null</code> means treat lhs as <code>Comparable</code>
     * @return this - used to chain append calls
     * @throws ClassCastException if <code>rhs</code> is not assignment-compatible
     *                            with <code>lhs</code>
     * @since 2.0
     */
    public CompareToBuilder append(final Object lhs, final Object rhs, final Comparator<?> comparator) {
        if (comparison != 0) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null) {
            comparison = -1;
            return this;
        }
        if (rhs == null) {
            comparison = +1;
            return this;
        }

        if (lhs.getClass().isArray()) {
            // factor out array case in order to keep method small enough to be inlined
            append((Object[]) lhs, (Object[]) rhs, comparator);
        } else if (lhs instanceof Iterable) {
            append((Collection<?>) lhs, (Collection<?>) rhs, comparator);
        } else {
            // the simple case, not an array, just test the element
            if (comparator == null) {
                @SuppressWarnings("unchecked") // assume this can be done; if not throw CCE as per Javadoc
                final Comparable<Object> comparable = (Comparable<Object>) lhs;
                comparison = comparable.compareTo(rhs);
            } else {
                @SuppressWarnings("unchecked") // assume this can be done; if not throw CCE as per Javadoc
                final Comparator<Object> comparator2 = (Comparator<Object>) comparator;
                comparison = comparator2.compare(lhs, rhs);
            }
        }
        return this;
    }

    /**
     * <p>Appends to the <code>builder</code> the deep comparison of
     * two <code>Object</code> arrays.</p>
     * <p>
     * <ol>
     * <li>Check if arrays are the same using <code>==</code></li>
     * <li>Check if for <code>null</code>, <code>null</code> is less than non-<code>null</code></li>
     * <li>Check array length, a short length array is less than a long length array</li>
     * <li>Check array contents element by element using {@link #append(Object, Object, Comparator)}</li>
     * </ol>
     * <p>
     * <p>This method will also will be called for the top level of multi-dimensional,
     * ragged, and multi-typed arrays.</p>
     *
     * @param lhs        left-hand array
     * @param rhs        right-hand array
     * @param comparator <code>Comparator</code> to use to compare the array elements,
     *                   <code>null</code> means to treat <code>lhs</code> elements as <code>Comparable</code>.
     * @return this - used to chain append calls
     * @throws ClassCastException if <code>rhs</code> is not assignment-compatible
     *                            with <code>lhs</code>
     * @since 2.0
     */
    public CompareToBuilder append(final Object[] lhs, final Object[] rhs, final Comparator<?> comparator) {
        if (comparison != 0) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null) {
            comparison = -1;
            return this;
        }
        if (rhs == null) {
            comparison = +1;
            return this;
        }
        if (lhs.length != rhs.length) {
            comparison = lhs.length < rhs.length ? -1 : +1;
            return this;
        }
        for (int i = 0; i < lhs.length && comparison == 0; i++) {
            append(lhs[i], rhs[i], comparator);
        }
        return this;
    }

    /**
     * <p>Appends to the <code>builder</code> the deep comparison of
     * two <code>Object</code> collection.</p>
     * <p>
     * <ol>
     * <li>Check if collection are the same using <code>==</code></li>
     * <li>Check if for <code>null</code>, <code>null</code> is less than non-<code>null</code></li>
     * <li>Check collection length, a short length collection is less than a long length collection</li>
     * <li>Check collection contents element by element using {@link #append(Object, Object, Comparator)}</li>
     * </ol>
     * <p>
     * <p>This method will also will be called for the top level of multi-dimensional,
     * ragged, and multi-typed collection.</p>
     *
     * @param lhs        left-hand collection
     * @param rhs        right-hand collection
     * @param comparator <code>Comparator</code> to use to compare the collection elements,
     *                   <code>null</code> means to treat <code>lhs</code> elements as <code>Comparable</code>.
     * @return this - used to chain append calls
     * @throws ClassCastException if <code>rhs</code> is not assignment-compatible
     *                            with <code>lhs</code>
     * @since 2.0
     */
    public CompareToBuilder append(final Collection<?> lhs, final Collection<?> rhs, final Comparator<?> comparator) {
        if (comparison != 0) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null) {
            comparison = -1;
            return this;
        }
        if (rhs == null) {
            comparison = +1;
            return this;
        }
        if (lhs.size() != rhs.size()) {
            comparison = lhs.size() < rhs.size() ? -1 : +1;
            return this;
        }

        Iterator<?> lhsIterable = lhs.iterator();
        Iterator<?> rhsIterable = rhs.iterator();
        while (lhsIterable.hasNext() && comparison == 0) {
            append(lhsIterable.next(), rhsIterable.next(), comparator);
        }
        return this;
    }

    //-------------------------------------------------------------------------

    /**
     * Appends to the <code>builder</code> the comparison of
     * two <code>long</code>s.
     *
     * @param lhs left-hand value
     * @param rhs right-hand value
     * @return this - used to chain append calls
     */
    public CompareToBuilder append(final long lhs, final long rhs) {
        if (comparison != 0) {
            return this;
        }
        comparison = lhs < rhs ? -1 : lhs > rhs ? 1 : 0;
        return this;
    }

    /**
     * Appends to the <code>builder</code> the comparison of
     * two <code>int</code>s.
     *
     * @param lhs left-hand value
     * @param rhs right-hand value
     * @return this - used to chain append calls
     */
    public CompareToBuilder append(final int lhs, final int rhs) {
        if (comparison != 0) {
            return this;
        }
        comparison = lhs < rhs ? -1 : lhs > rhs ? 1 : 0;
        return this;
    }

    /**
     * Appends to the <code>builder</code> the comparison of
     * two <code>short</code>s.
     *
     * @param lhs left-hand value
     * @param rhs right-hand value
     * @return this - used to chain append calls
     */
    public CompareToBuilder append(final short lhs, final short rhs) {
        if (comparison != 0) {
            return this;
        }
        comparison = lhs < rhs ? -1 : lhs > rhs ? 1 : 0;
        return this;
    }

    /**
     * Appends to the <code>builder</code> the comparison of
     * two <code>char</code>s.
     *
     * @param lhs left-hand value
     * @param rhs right-hand value
     * @return this - used to chain append calls
     */
    public CompareToBuilder append(final char lhs, final char rhs) {
        if (comparison != 0) {
            return this;
        }
        comparison = lhs < rhs ? -1 : lhs > rhs ? 1 : 0;
        return this;
    }

    /**
     * Appends to the <code>builder</code> the comparison of
     * two <code>byte</code>s.
     *
     * @param lhs left-hand value
     * @param rhs right-hand value
     * @return this - used to chain append calls
     */
    public CompareToBuilder append(final byte lhs, final byte rhs) {
        if (comparison != 0) {
            return this;
        }
        comparison = lhs < rhs ? -1 : lhs > rhs ? 1 : 0;
        return this;
    }

    /**
     * <p>Appends to the <code>builder</code> the comparison of
     * two <code>double</code>s.</p>
     * <p>
     * <p>This handles NaNs, Infinities, and <code>-0.0</code>.</p>
     * <p>
     * <p>It is compatible with the hash code generated by
     * <code>HashCodeBuilder</code>.</p>
     *
     * @param lhs left-hand value
     * @param rhs right-hand value
     * @return this - used to chain append calls
     */
    public CompareToBuilder append(final double lhs, final double rhs) {
        if (comparison != 0) {
            return this;
        }
        comparison = Double.compare(lhs, rhs);
        return this;
    }

    /**
     * <p>Appends to the <code>builder</code> the comparison of
     * two <code>float</code>s.</p>
     * <p>
     * <p>This handles NaNs, Infinities, and <code>-0.0</code>.</p>
     * <p>
     * <p>It is compatible with the hash code generated by
     * <code>HashCodeBuilder</code>.</p>
     *
     * @param lhs left-hand value
     * @param rhs right-hand value
     * @return this - used to chain append calls
     */
    public CompareToBuilder append(final float lhs, final float rhs) {
        if (comparison != 0) {
            return this;
        }
        comparison = Float.compare(lhs, rhs);
        return this;
    }

    /**
     * Appends to the <code>builder</code> the comparison of
     * two <code>booleans</code>s.
     *
     * @param lhs left-hand value
     * @param rhs right-hand value
     * @return this - used to chain append calls
     */
    public CompareToBuilder append(final boolean lhs, final boolean rhs) {
        if (comparison != 0) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (!lhs) {
            comparison = -1;
        } else {
            comparison = +1;
        }
        return this;
    }

    //-----------------------------------------------------------------------

    /**
     * Returns a negative integer, a positive integer, or zero as
     * the <code>builder</code> has judged the "left-hand" side
     * as less than, greater than, or equal to the "right-hand"
     * side.
     *
     * @return final comparison result
     * @see #build()
     */
    public int toComparison() {
        return comparison;
    }

    /**
     * Returns a negative Integer, a positive Integer, or zero as
     * the <code>builder</code> has judged the "left-hand" side
     * as less than, greater than, or equal to the "right-hand"
     * side.
     *
     * @return final comparison result as an Integer
     * @see #toComparison()
     * @since 3.0
     */
    @Override
    public Integer build() {
        return toComparison();
    }
}

