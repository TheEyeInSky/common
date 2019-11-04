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

import com.dongzy.common.common.collection.ArrayUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;

/**
 * <p>Assists in implementing {@link Object#equals(Object)} methods.</p>
 * <p>
 * <p> This class provides methods to build a good equals method for any
 * class. It follows rules laid out in
 * <a href="http://www.oracle.com/technetwork/java/effectivejava-136174.html">Effective Java</a>
 * , by Joshua Bloch. In particular the rule for comparing <code>doubles</code>,
 * <code>floats</code>, and arrays can be tricky. Also, making sure that
 * <code>equals()</code> and <code>hashCode()</code> are consistent can be
 * difficult.</p>
 * <p>
 * <p>Two Objects that compare as equals must generate the same hash code,
 * but two Objects with the same hash code do not have to be equal.</p>
 * <p>
 * <p>All relevant fields should be included in the calculation of equals.
 * Derived fields may be ignored. In particular, any field used in
 * generating a hash code must be used in the equals method, and vice
 * versa.</p>
 * <p>
 * <p>Typical use for the code is as follows:</p>
 * <pre>
 * public boolean equals(Object obj) {
 *   if (obj == null) { return false; }
 *   if (obj == this) { return true; }
 *   if (obj.getClass() != getClass()) {
 *     return false;
 *   }
 *   MyClass rhs = (MyClass) obj;
 *   return new EqualsBuilder()
 *                 .appendSuper(super.equals(obj))
 *                 .append(field1, rhs.field1)
 *                 .append(field2, rhs.field2)
 *                 .append(field3, rhs.field3)
 *                 .isEquals();
 *  }
 * </pre>
 * <p>
 * <p> Alternatively, there is a method that uses reflection to determine
 * the fields to test. Because these fields are usually private, the method,
 * <code>reflectionEquals</code>, uses <code>AccessibleObject.setAccessible</code> to
 * change the visibility of the fields. This will fail under a security
 * manager, unless the appropriate permissions are set up correctly. It is
 * also slower than testing explicitly.  Non-primitive fields are compared using
 * <code>equals()</code>.</p>
 * <p>
 * <p> A typical invocation for this method would look like:</p>
 * <pre>
 * public boolean equals(Object obj) {
 *   return EqualsBuilder.reflectionEquals(this, obj);
 * }
 * </pre>
 * <p>
 * used by the <code>reflectionEquals</code> methods.</p>
 *
 * @since 1.0
 */
public class EqualsBuilder implements Builder<Boolean> {

    /**
     * If the fields tested are equals.
     * The default value is <code>true</code>.
     */
    private boolean equals = true;
    private String[] excludeFields = null;

    /**
     * <p>Constructor for EqualsBuilder.</p>
     * <p>
     * <p>Starts off assuming that equals is <code>true</code>.</p>
     *
     * @see Object#equals(Object)
     */
    public EqualsBuilder() {
        // do nothing for now.
    }

    //-------------------------------------------------------------------------


    /**
     * Set field names to be excluded by reflection tests.
     *
     * @param excludeFields the fields to exclude
     * @return EqualsBuilder - used to chain calls.
     * @since 3.6
     */
    public EqualsBuilder setExcludeFields(final String... excludeFields) {
        this.excludeFields = excludeFields;
        return this;
    }

    /**
     * <p>This method uses reflection to determine if the two <code>Object</code>s
     * are equal.</p>
     * <p>
     * <p>It uses <code>AccessibleObject.setAccessible</code> to gain access to private
     * fields. This means that it will throw a security exception if run under
     * a security manager, if the permissions are not set up correctly. It is also
     * not as efficient as testing explicitly. Non-primitive fields are compared using
     * <code>equals()</code>.</p>
     * <p>
     * <p>Transient members will be not be tested, as they are likely derived
     * fields, and not part of the value of the Object.</p>
     * <p>
     * <p>Static fields will not be tested. Superclass fields will be included.</p>
     *
     * @param lhs           <code>this</code> object
     * @param rhs           the other object
     * @param excludeFields array of field names to exclude from testing
     * @return <code>true</code> if the two Objects have tested equals.
     */
    public static boolean reflectionEquals(final Object lhs, final Object rhs, final String... excludeFields) {
        if (lhs == rhs) {
            return true;
        }
        if (lhs == null || rhs == null) {
            return false;
        }
        return new EqualsBuilder().setExcludeFields(excludeFields).reflectionAppend(lhs, rhs).isEquals();
    }

    /**
     * <p>Tests if two <code>objects</code> by using reflection.</p>
     * <p>
     * <p>It uses <code>AccessibleObject.setAccessible</code> to gain access to private
     * fields. This means that it will throw a security exception if run under
     * a security manager, if the permissions are not set up correctly. It is also
     * not as efficient as testing explicitly. Non-primitive fields are compared using
     * <code>equals()</code>.</p>
     * <p>
     * <p>If the testTransients field is set to <code>true</code>, transient
     * members will be tested, otherwise they are ignored, as they are likely
     * derived fields, and not part of the value of the <code>Object</code>.</p>
     * <p>
     * <p>Static fields will not be included. Superclass fields will be appended
     * up to and including the specified superclass in field <code>reflectUpToClass</code>.
     * A null superclass is treated as java.lang.Object.</p>
     * <p>
     * <p>Field names listed in field <code>excludeFields</code> will be ignored.</p>
     *
     * @param lhs the left hand object
     * @param rhs the left hand object
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder reflectionAppend(final Object lhs, final Object rhs) {
        if (!equals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            equals = false;
            return this;
        }

        // Find the leaf class since there may be transients in the leaf
        // class or in classes between the leaf and root.
        // If we are not testing transients or a subclass has no ivars,
        // then a subclass can test equals to a superclass.
        final Class<?> lhsClass = lhs.getClass();
        final Class<?> rhsClass = rhs.getClass();
        Class<?> testClass;
        if (lhsClass.isInstance(rhs)) {
            testClass = lhsClass;
            if (!rhsClass.isInstance(lhs)) {
                // rhsClass is a subclass of lhsClass
                testClass = rhsClass;
            }
        } else if (rhsClass.isInstance(lhs)) {
            testClass = rhsClass;
            if (!lhsClass.isInstance(rhs)) {
                // lhsClass is a subclass of rhsClass
                testClass = lhsClass;
            }
        } else {
            // The two classes are not related.
            equals = false;
            return this;
        }

        try {
            if (testClass.isArray()) {
                append(lhs, rhs);
            } else {
                reflectionAppend(lhs, rhs, testClass);
                while (testClass.getSuperclass() != null) {
                    testClass = testClass.getSuperclass();
                    reflectionAppend(lhs, rhs, testClass);
                }
            }
        } catch (final IllegalArgumentException e) {
            // In this case, we tried to test a subclass vs. a superclass and
            // the subclass has ivars or the ivars are transient and
            // we are testing transients.
            // If a subclass has ivars that we are trying to test them, we get an
            // exception and we know that the objects are not equal.
            equals = false;
            return this;
        }
        return this;
    }

    /**
     * <p>Appends the fields and values defined by the given object of the
     * given Class.</p>
     *
     * @param lhs   the left hand object
     * @param rhs   the right hand object
     * @param clazz the class to append details of
     */
    private void reflectionAppend(final Object lhs, final Object rhs, final Class<?> clazz) {

        final Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        for (int i = 0; i < fields.length && equals; i++) {
            final Field f = fields[i];
            if (!ArrayUtils.contains(excludeFields, f.getName())
                    && !f.getName().contains("$")
                    && !Modifier.isTransient(f.getModifiers())
                    && !Modifier.isStatic(f.getModifiers())) {
                try {
                    append(f.get(lhs), f.get(rhs));
                } catch (final IllegalAccessException e) {
                    //this can't happen. Would get a Security exception instead
                    //throw a runtime exception in case the impossible happens.
                    throw new InternalError("Unexpected IllegalAccessException");
                }
            }
        }

    }

    //-------------------------------------------------------------------------

    /**
     * <p>Adds the result of <code>super.equals()</code> to this builder.</p>
     *
     * @param superEquals the result of calling <code>super.equals()</code>
     * @return EqualsBuilder - used to chain calls.
     * @since 2.0
     */
    public EqualsBuilder appendSuper(final boolean superEquals) {
        if (!equals) {
            return this;
        }
        equals = superEquals;
        return this;
    }

    //-------------------------------------------------------------------------

    /**
     * <p>Test if two <code>Object</code>s are equal using either
     * #{@link #reflectionAppend(Object, Object)}, if object are non
     * primitives (or wrapper of primitives) or if field <code>testRecursive</code>
     * is set to <code>false</code>. Otherwise, using their
     * <code>equals</code> method.</p>
     *
     * @param lhs the left hand object
     * @param rhs the right hand object
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final Object lhs, final Object rhs) {
        if (!equals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }

        if (lhs.getClass().isArray()) {
            append((Object[]) lhs, (Object[]) rhs);
        } else if (lhs instanceof Iterable) {
            append((Collection<?>) lhs, (Collection<?>) rhs);
        } else {
            equals = lhs.equals(rhs);
        }
        return this;
    }

    /**
     * <p>
     * Test if two <code>long</code> s are equal.
     * </p>
     *
     * @param lhs the left hand <code>long</code>
     * @param rhs the right hand <code>long</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final long lhs, final long rhs) {
        if (!equals) {
            return this;
        }
        equals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>int</code>s are equal.</p>
     *
     * @param lhs the left hand <code>int</code>
     * @param rhs the right hand <code>int</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final int lhs, final int rhs) {
        if (!equals) {
            return this;
        }
        equals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>short</code>s are equal.</p>
     *
     * @param lhs the left hand <code>short</code>
     * @param rhs the right hand <code>short</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final short lhs, final short rhs) {
        if (!equals) {
            return this;
        }
        equals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>char</code>s are equal.</p>
     *
     * @param lhs the left hand <code>char</code>
     * @param rhs the right hand <code>char</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final char lhs, final char rhs) {
        if (!equals) {
            return this;
        }
        equals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>byte</code>s are equal.</p>
     *
     * @param lhs the left hand <code>byte</code>
     * @param rhs the right hand <code>byte</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final byte lhs, final byte rhs) {
        if (!equals) {
            return this;
        }
        equals = lhs == rhs;
        return this;
    }

    /**
     * <p>Test if two <code>double</code>s are equal by testing that the
     * pattern of bits returned by <code>doubleToLong</code> are equal.</p>
     * <p>
     * <p>This handles NaNs, Infinities, and <code>-0.0</code>.</p>
     * <p>
     * <p>It is compatible with the hash code generated by
     * <code>HashCodeBuilder</code>.</p>
     *
     * @param lhs the left hand <code>double</code>
     * @param rhs the right hand <code>double</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final double lhs, final double rhs) {
        if (!equals) {
            return this;
        }
        return append(Double.doubleToLongBits(lhs), Double.doubleToLongBits(rhs));
    }

    /**
     * <p>Test if two <code>float</code>s are equal byt testing that the
     * pattern of bits returned by doubleToLong are equal.</p>
     * <p>
     * <p>This handles NaNs, Infinities, and <code>-0.0</code>.</p>
     * <p>
     * <p>It is compatible with the hash code generated by
     * <code>HashCodeBuilder</code>.</p>
     *
     * @param lhs the left hand <code>float</code>
     * @param rhs the right hand <code>float</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final float lhs, final float rhs) {
        if (!equals) {
            return this;
        }
        return append(Float.floatToIntBits(lhs), Float.floatToIntBits(rhs));
    }

    /**
     * <p>Test if two <code>booleans</code>s are equal.</p>
     *
     * @param lhs the left hand <code>boolean</code>
     * @param rhs the right hand <code>boolean</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final boolean lhs, final boolean rhs) {
        if (!equals) {
            return this;
        }
        equals = lhs == rhs;
        return this;
    }

    /**
     * <p>Performs a deep comparison of two <code>Object</code> arrays.</p>
     * <p>
     * <p>This also will be called for the top level of
     * multi-dimensional, ragged, and multi-typed arrays.</p>
     * <p>
     * <p>Note that this method does not compare the type of the arrays; it only
     * compares the contents.</p>
     *
     * @param lhs the left hand <code>Object[]</code>
     * @param rhs the right hand <code>Object[]</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final Object[] lhs, final Object[] rhs) {
        if (!equals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
        }
        for (int i = 0; i < lhs.length && equals; ++i) {
            append(lhs[i], rhs[i]);
        }
        return this;
    }

    /**
     * <p>Performs a deep comparison of two <code>Object</code> collection.</p>
     * <p>
     * <p>This also will be called for the top level of
     * multi-dimensional, ragged, and multi-typed collection.</p>
     * <p>
     * <p>Note that this method does not compare the type of the collection; it only
     * compares the contents.</p>
     *
     * @param lhs the left hand <code>Collection</code>
     * @param rhs the right hand <code>Collection</code>
     * @return EqualsBuilder - used to chain calls.
     */
    public EqualsBuilder append(final Collection<?> lhs, final Collection<?> rhs) {
        if (!equals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            this.setEquals(false);
            return this;
        }
        if (lhs.size() != rhs.size()) {
            this.setEquals(false);
            return this;
        }

        Iterator<?> lhsIterable = lhs.iterator();
        Iterator<?> rhsIterable = rhs.iterator();
        while (lhsIterable.hasNext() && equals) {
            append(lhsIterable.next(), rhsIterable.next());
        }
        return this;
    }

    /**
     * <p>Returns <code>true</code> if the fields that have been checked
     * are all equal.</p>
     *
     * @return boolean
     */
    public boolean isEquals() {
        return this.equals;
    }

    /**
     * Sets the <code>isEquals</code> value.
     *
     * @param equals The value to set.
     * @since 2.1
     */
    protected void setEquals(final boolean equals) {
        this.equals = equals;
    }

    /**
     * <p>Returns <code>true</code> if the fields that have been checked
     * are all equal.</p>
     *
     * @return <code>true</code> if all of the fields that have been checked
     * are equal, <code>false</code> otherwise.
     * @since 3.0
     */
    @Override
    public Boolean build() {
        return isEquals();
    }

    /**
     * Reset the EqualsBuilder so you can use the same object again
     *
     * @since 2.5
     */
    public void reset() {
        this.equals = true;
    }
}
