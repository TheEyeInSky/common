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
import com.dongzy.common.common.text.StringBuilderExt;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.collection.ArrayUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * <p>Assists in implementing {@link Object#toString()} methods.</p>
 * <p>
 * <p>This class enables a good and consistent <code>toString()</code> to be built for any
 * class or object. This class aims to simplify the process by:</p>
 * <ul>
 * <li>allowing field names</li>
 * <li>handling all types consistently</li>
 * <li>handling nulls consistently</li>
 * <li>outputting arrays and multi-dimensional arrays</li>
 * <li>enabling the detail level to be controlled for Objects and Collections</li>
 * <li>handling class hierarchies</li>
 * </ul>
 * <p>
 * <p>To use this class write code as follows:</p>
 * <p>
 * <pre>
 * public class Person {
 *   String name;
 *   int age;
 *   boolean smoker;
 *
 *   ...
 *
 *   public String toString() {
 *     return new ToStringBuilder(this).
 *       append("name", name).
 *       append("age", age).
 *       append("smoker", smoker).
 *       toString();
 *   }
 * }
 * </pre>
 * <p>This will produce a toString of the format:
 * <code>Person@7f54[name=Stephen,age=29,smoker=false]</code></p>
 * <p>Alternatively, there is a method that uses reflection to determine
 * the fields to test. Because these fields are usually private, the method,
 * <code>reflectionToString</code>, uses <code>AccessibleObject.setAccessible</code> to
 * change the visibility of the fields. This will fail under a security manager,
 * unless the appropriate permissions are set up correctly. It is also
 * slower than testing explicitly.</p>
 * <p>
 * <p>A typical invocation for this method would look like:</p>
 * <p>
 * <pre>
 * public String toString() {
 *   return ToStringBuilder.reflectionToString(this);
 * }
 * </pre>
 * <p>
 * <p>You can also use the builder to debug 3rd party objects:</p>
 * <p>
 * <pre>
 * System.out.println("An object: " + ToStringBuilder.reflectionToString(anObject));
 * </pre>
 * <p>
 * <p>The exact format of the <code>toString</code> is determined by
 *
 * @since 1.0
 */
public class ToStringBuilder implements Builder<Integer> {

    private final StringBuilderExt stringBuilder = new StringBuilderExt(100);
    private String result;

    /**
     * <p>
     * Uses two hard coded choices for the constants needed to build a <code>hashCode</code>.
     * </p>
     */
    public ToStringBuilder() {
    }

    /**
     * <p>Uses <code>ReflectionToStringBuilder</code> to generate a
     * <code>toString</code> for the specified object.</p>
     *
     * @param object        the Object to create a <code>toString</code> for
     * @param excludeFields array of field names to exclude from use in calculation of hash code
     * @return the String result
     * @throws IllegalArgumentException if the object is <code>null</code>
     */
    public static String reflectionToString(final Object object, final String... excludeFields) {
        return reflectionToString(object, false, excludeFields);
    }

    /**
     * <p>Uses <code>ReflectionToStringBuilder</code> to generate a
     * <code>toString</code> for the specified object.</p>
     *
     * @param object        the Object to create a <code>toString</code> for
     * @param excludeFields array of field names to exclude from use in calculation of hash code
     * @param fullDetail    <code>true</code> for detail, <code>false</code>
     * @return the String result
     * @throws IllegalArgumentException if the object is <code>null</code>
     */
    public static String reflectionToString(final Object object, final boolean fullDetail, final String... excludeFields) {
        Validate.notNull(object);
        final ToStringBuilder builder = new ToStringBuilder();
        Class<?> clazz = object.getClass();
        reflectionAppend(object, clazz, builder, fullDetail, excludeFields);
        while (clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            reflectionAppend(object, clazz, builder, fullDetail, excludeFields);
        }
        return builder.toString();
    }

    /**
     * <p>
     * Appends the fields and values defined by the given object of the given <code>Class</code>.
     * </p>
     *
     * @param object        the object to append details of
     * @param clazz         the class to append details of
     * @param builder       the builder to append to
     * @param fullDetail    <code>true</code> for detail, <code>false</code>
     * @param excludeFields Collection of String field names to exclude from use in calculation of hash code
     */
    private static void reflectionAppend(final Object object, final Class<?> clazz, final ToStringBuilder builder,
                                         final boolean fullDetail, final String[] excludeFields) {

        final Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        for (final Field field : fields) {
            if (!ArrayUtils.contains(excludeFields, field.getName())
                    && !field.getName().contains("$")
                    && !Modifier.isTransient(field.getModifiers())
                    && !Modifier.isStatic(field.getModifiers())) {
                try {
                    final Object fieldValue = field.get(object);
                    builder.append(field.getName(), fieldValue, fullDetail);
                } catch (final IllegalAccessException e) {
                    // this can't happen. Would get a Security exception instead
                    // throw a runtime exception in case the impossible happens.
                    throw new InternalError("Unexpected IllegalAccessException");
                }
            }
        }
    }

    // -------------------------------------------------------------------------

    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>
     * value.</p>
     *
     * @param obj the value to add to the <code>toString</code>
     * @return this
     */
    public ToStringBuilder append(final Object obj) {
        stringBuilder.append("\"");
        stringBuilder.append(obj);
        stringBuilder.append("\", ");
        return this;
    }

    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>
     * value.</p>
     *
     * @param fieldName the field name
     * @param obj       the value to add to the <code>toString</code>
     * @return this
     */
    public ToStringBuilder append(final String fieldName, final Object obj, final boolean fullDetail) {
        if (fullDetail) {
            stringBuilder.append("{");
            stringBuilder.appendFormat("\"{0}\":\"{1}\"", fieldName, obj);
            stringBuilder.append("}, ");
        } else {
            append(obj);
        }
        return this;
    }

    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>
     * array.</p>
     *
     * @param array the array to add to the <code>toString</code>
     * @param <T>   type
     * @return this
     */
    public <T> ToStringBuilder append(final T[] array) {
        Validate.notEmpty(array);
        stringBuilder.append("[");
        for (T obj : array) {
            stringBuilder.append("\"");
            stringBuilder.append(obj);
            stringBuilder.append("\", ");
        }
        stringBuilder.setLength(stringBuilder.length() - 2);
        stringBuilder.append("], ");
        return this;
    }

    /**
     * <p>Append to the <code>toString</code> an <code>Object</code>
     * array.</p>
     * <p>
     * <p>A boolean parameter controls the level of detail to show.
     * Setting <code>true</code> will output the array in full. Setting
     * <code>false</code> will output a summary, typically the size of
     * the array.</p>
     *
     * @param fieldName  the field name
     * @param array      the array to add to the <code>toString</code>
     * @param fullDetail <code>true</code> for detail, <code>false</code> for summary info
     * @param <T>        type
     * @return this
     */
    public <T> ToStringBuilder append(final String fieldName, final T[] array, final boolean fullDetail) {
        if (fullDetail) {
            stringBuilder.append("{");
            stringBuilder.appendFormat("\"{0}\":", fieldName);
            append(array);
            stringBuilder.append("}, ");
        } else {
            append(array);
        }
        return this;
    }

    /**
     * Returns the computed <code>hashCode</code>.
     *
     * @return <code>hashCode</code> based on the fields appended
     * @since 3.0
     */
    @Override
    public Integer build() {
        return hashCode();
    }

    /**
     * <p>
     * The computed <code>hashCode</code> from toHashCode() is returned due to the likelihood
     * of bugs in mis-calling toHashCode() and the unlikeliness of it mattering what the hashCode for
     * ToStringBuilder itself is.</p>
     *
     * @return <code>hashCode</code> based on the fields appended
     * @since 2.5
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        if (result == null) {
            if (stringBuilder.length() > 2) {
                stringBuilder.setLength(stringBuilder.length() - 2);
            }
            result = stringBuilder.toString();
        }
        return result;
    }

}
