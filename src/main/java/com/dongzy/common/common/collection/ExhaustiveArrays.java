package com.dongzy.common.common.collection;

import com.dongzy.common.common.Validate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 一个用于对数组进行穷举的算法
 */
public final class ExhaustiveArrays {

    /**
     * 开始穷举数据元素
     * 开始穷举数据元素,举例,比如输入的集合为:
     * a1  a2  a3
     * a4  a5
     * 那么输出值为:a1,a2,a3; a1,a5,a3; a4,a2,a3; a4,a5,a3;共计四组结果.
     *
     * @param arrays 需要穷举的二维集合
     * @param <T>    元素的泛型
     * @return 穷举后的一维数组
     */
    public static <T> List<List<T>> exhaustive(T[][] arrays) {

        //检查数据
        for (T[] array1 : arrays) {
            Validate.notNull(array1, "数组穷举对象的元素不能为空!");
        }

        //定义结果集对象
        List<List<T>> lists = new ArrayList<>();

        //将最后一列元素写入到初始结果集中
        T[] array = arrays[arrays.length - 1];
        for (T t : array) {
            List<T> initList = new ArrayList<>(1);
            initList.add(t);
            lists.add(initList);
        }

        //迭代添加元素到最终结果集合
        for (int i = arrays.length - 2; i >= 0; i--) {
            lists = exhaustive(arrays[i], lists);
        }

        return lists;
    }

    //不断复制并添加新元素到集合中
    private static <T> List<List<T>> exhaustive(T[] list, List<List<T>> lists) {

        //创建结果集
        List<List<T>> newLists = new ArrayList<>(list.length * lists.size());

        //遍历已有集合
        for (List<T> subList : lists) {
            //遍历新添加的元素
            for (T t : list) {
                //创建新的子集合对象
                List<T> newList = new ArrayList<>(subList.size() + 1);
                newList.add(t);                        //在最前面添加新元素
                newList.addAll(subList);            //添加上后续的元素
                newLists.add(newList);                //添加到结果集中
            }
        }
        return newLists;
    }
}
