package com.dongzy.common.common.collection;

import com.dongzy.common.common.text.StringUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>用于对集合对象进行操作的辅助类，主要包括以下几类：
 * 1、取并集、取交集、取补集、元素去重
 * 2、数组和集合对象相互转换
 * 3、获取集合中的第一个元素、最后一个元素
 * <p>
 * 集合中的对象必须正确的实现了equals方法，才能确保最终的结果符合期望
 * 将集合转换为数组对象时，如果集合对象为空，那么返回的数组对象的值为null，
 * 调用程序注意判断null状态，避免触发{@code NullPointerException}异常
 *
 * @author zouyong
 * @since JDK1.8
 */
public final class CollectionUtils {

    /**
     * 取并集，如果集合中存在相同的元素，保留两个集合中数量较多元素，
     * <p>比如第一个集合包含2个a，第二个集合包含3个a，那么合并后的集合中包含3个a</p>
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <T>   泛型集合对象
     * @return 并集集合
     */
    public static <T> Collection<T> union(Collection<T> coll1, Collection<T> coll2) {
        ArrayList<T> list = new ArrayList<>();
        Map<T, Integer> mapa = getCardinalityMap(coll1);
        Map<T, Integer> mapb = getCardinalityMap(coll2);
        Set<T> elts = new HashSet<>(coll1);
        elts.addAll(coll2);
        for (T obj : elts) {
            for (int i = 0, m = Math.max(getFreq(obj, mapa), getFreq(obj, mapb)); i < m; i++) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 取交集，如果集合中存在相同的元素，保留两个集合中数量较小的元素，
     * <p>比如第一个集合包含2个a，第二个集合包含3个a，那么交集合中包含2个a</p>
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <T>   泛型集合对象
     * @return 交集集合
     */
    public static <T> Collection<T> intersection(Collection<T> coll1, Collection<T> coll2) {
        ArrayList<T> list = new ArrayList<>();
        Map<T, Integer> mapa = getCardinalityMap(coll1);
        Map<T, Integer> mapb = getCardinalityMap(coll2);
        Set<T> elts = new HashSet<>(coll1);
        elts.addAll(coll2);
        for (T obj : elts) {
            for (int i = 0, m = Math.min(getFreq(obj, mapa), getFreq(obj, mapb)); i < m; i++) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 析取结果，体现出两个集合中不相同的元素
     * <p>比如第一个集合包含2个a，第二个集合包含3个a，那么结果中包含1个a</p>
     * <p>比如第一个集合包含5个a，第二个集合包含1个a，那么结果中包含4个a</p>
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <T>   泛型集合对象
     * @return 补集集合
     */
    public static <T> Collection<T> disjunction(Collection<T> coll1, Collection<T> coll2) {
        ArrayList<T> list = new ArrayList<>();
        Map<T, Integer> mapa = getCardinalityMap(coll1);
        Map<T, Integer> mapb = getCardinalityMap(coll2);
        Set<T> elts = new HashSet<>(coll1);
        elts.addAll(coll2);
        for (T obj : elts) {
            for (int i = 0, m = ((Math.max(getFreq(obj, mapa), getFreq(obj, mapb))) - (Math.min(getFreq(obj, mapa), getFreq(obj, mapb)))); i < m; i++) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 返回集合1减去集合2的结果，属于绝对补集的概念，要求集合2中的元素完全存在于集合1中
     * <p>比如第一个集合包含2个a，第二个集合包含1个a，那么补集中包含1个a</p>
     * <p>比如第一个集合包含5个a，第二个集合包含1个a，那么补集中包含4个a</p>
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <T>   泛型集合对象
     * @return 差集集合
     */
    public static <T> Collection<T> subtract(Collection<T> coll1, Collection<T> coll2) {
        ArrayList<T> list = new ArrayList<>(coll1);
        list.removeAll(coll2);
        return list;
    }

    /**
     * 判断集合1中是否包含集合2中的任意元素
     * <p>
     * In other words, this method returns <code>true</code> iff the
     * {@link #intersection} of <i>coll1</i> and <i>coll2</i> is not empty.
     *
     * @param coll1 the first collection, must not be null
     * @param coll2 the first collection, must not be null
     * @return <code>true</code> iff the intersection of the collections is non-empty
     * @see #intersection
     * @since 2.1
     */
    public static <T> boolean containsAny(final Collection<T> coll1, final Collection<T> coll2) {
        if (coll1.size() < coll2.size()) {
            for (T aColl1 : coll1) {
                if (coll2.contains(aColl1)) {
                    return true;
                }
            }
        } else {
            for (T aColl2 : coll2) {
                if (coll1.contains(aColl2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 返回集合1是否为集合2的子集
     *
     * @param coll1 the first (sub?) collection, must not be null
     * @param coll2 the second (super?) collection, must not be null
     * @return <code>true</code> iff <i>a</i> is a sub-collection of <i>b</i>
     */
    public static <T> boolean isSubCollection(final Collection<T> coll1, final Collection<T> coll2) {
        Map<T, Integer> mapa = getCardinalityMap(coll1);
        Map<T, Integer> mapb = getCardinalityMap(coll2);
        for (T obj : coll1) {
            if (getFreq(obj, mapa) > getFreq(obj, mapb)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断两个集合是否完全相等
     * <p>
     * That is, iff the cardinality of <i>e</i> in <i>a</i> is
     * equal to the cardinality of <i>e</i> in <i>b</i>,
     * for each element <i>e</i> in <i>a</i> or <i>b</i>.
     *
     * @param coll1 the first collection, must not be null
     * @param coll2 the second collection, must not be null
     * @return <code>true</code> iff the collections contain the same elements with the same cardinalities.
     */
    public static <T> boolean isEqualCollection(final Collection<T> coll1, final Collection<T> coll2) {
        if (coll1.size() != coll2.size()) {
            return false;
        } else {
            Map<T, Integer> mapa = getCardinalityMap(coll1);
            Map<T, Integer> mapb = getCardinalityMap(coll2);
            if (mapa.size() != mapb.size()) {
                return false;
            } else {
                for (T obj : mapa.keySet()) {
                    if (getFreq(obj, mapa) != getFreq(obj, mapb)) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    /**
     * 元素去重
     *
     * @param collection 集合
     * @param <T>        泛型集合对象
     * @return 去重后的集合
     */
    public static <T> Collection<T> distinct(Collection<T> collection) {
        return collection.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 将集合对象转换为数组，如果集合为空，将会返回null
     *
     * @param collection 对象集合
     * @param <T>        泛型集合对象
     * @return 对象数组
     */
    public static <T> T[] toArray(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        Class<?> cl = getFirst(collection).getClass();
        @SuppressWarnings("unchecked")
        T[] ts = (T[]) Array.newInstance(cl, collection.size());
        return collection.toArray(ts);
    }

    /**
     * 将集合对象转换为数组，如果集合为空，将会返回长度为0的数组。
     *
     * @param collection 对象集合
     * @param cl         需要转换的类的对象类型
     * @param <T>        泛型集合对象
     * @return 对象数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> collection, Class<T> cl) {
        if (isEmpty(collection)) {
            return (T[]) Array.newInstance(cl, 0);
        } else {
            T[] ts = (T[]) Array.newInstance(cl, collection.size());
            return collection.toArray(ts);
        }
    }

    /**
     * 获取集合中的第一个元素
     *
     * @param collection 需要查询的集合对象
     * @param <T>        泛型的类型
     * @return 第一个对象
     */
    public static <T> T getFirst(Collection<T> collection) {
        if (collection != null) {
            Iterator<T> iterable = collection.iterator();
            if (iterable.hasNext()) {
                return iterable.next();
            }
        }
        return null;
    }

    /**
     * 判断集合是否为空
     *
     * @param array 需要判断的集合
     * @param <T>   类型
     * @return 是否为空
     */
    public static <T> boolean isEmpty(Collection<T> array) {
        return array == null || array.isEmpty();
    }

    /**
     * 判断集合是否为空
     *
     * @param map 需要判断的集合
     * @return 是否为空
     */
    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断集合中的所有元素是否为空值，包括null和空白字符串
     *
     * @param array 需要判断的集合
     * @param <T>   类型
     * @return 是否全部为空
     */
    public static <T> boolean isBlank(Collection<T> array) {
        if (isEmpty(array)) return true;

        for (T t : array) {
            if (t != null && StringUtils.notBlank(t.toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算元素出现的次数
     *
     * @param obj  the object to find the cardinality of
     * @param coll the collection to search
     * @return the the number of occurrences of obj in coll
     */
    public static <T> int cardinality(T obj, final Collection<T> coll) {
        if (coll instanceof Set) {
            return (coll.contains(obj) ? 1 : 0);
        }
        int count = 0;
        if (obj == null) {
            for (T aColl : coll) {
                if (aColl == null) {
                    count++;
                }
            }
        } else {
            for (T aColl : coll) {
                if (obj.equals(aColl)) {
                    count++;
                }
            }
        }
        return count;
    }


    /**
     * Returns a {@link Map} mapping each unique element in the given
     * {@link Collection} to an {@link Integer} representing the number
     * of occurrences of that element in the {@link Collection}.
     * <p>
     * Only those elements present in the collection will appear as
     * keys in the map.
     *
     * @param coll the collection to get the cardinality map for, must not be null
     * @return the populated cardinality map
     */
    public static <T> Map<T, Integer> getCardinalityMap(final Collection<T> coll) {
        Map<T, Integer> count = new HashMap<>();
        for (T obj : coll) {
            Integer c = count.get(obj);
            count.put(obj, (c == null) ? 1 : c + 1);
        }
        return count;
    }

    private static <T> int getFreq(final T obj, final Map<T, Integer> freqMap) {
        Integer count = freqMap.get(obj);
        return (count != null) ? count : 0;
    }
}
