package com.dongzy.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dongzy
 * @Desc
 * @date 2019/9/16.
 */

public class CompareNumericUtil {
    /**
     * 区间比较返回下标
     * @param toBeCompared
     * @param minCon
     * @param maxCon
     * @param values
     * @return index
     */
    public static Integer compareNumeric(BigDecimal toBeCompared, Boolean minCon, Boolean maxCon, BigDecimal... values) {
        int length = values.length;
        //升序排列
        valuesSort(values, 0, length - 1);
        Integer index = compareNumerics(toBeCompared, values, minCon, maxCon);
        return index;
    }

    /**
     * 默认右包含
     *
     * @param toBeCompared
     * @param vals
     * @param minCon
     * @param maxCon
     * @return
     */
    private static Integer compareNumerics(BigDecimal toBeCompared, BigDecimal[] vals, Boolean minCon, Boolean maxCon) {
        BigDecimal MIN = null;
        BigDecimal MAX = null;
        if (minCon == null) {
            minCon = false;
        }
        if (maxCon == null) {
            maxCon = true;
        }
        Map<Integer, List<BigDecimal>> valGroup = new HashMap<>(16);
        //把规则分组，两两相邻分组
        for (int i = 0; i <= vals.length; i++) {
            List<BigDecimal> listGroup = new ArrayList<>();
            if (i == 0) {
                listGroup.add(MIN);
                listGroup.add(vals[i]);
            } else if (i == vals.length) {
                listGroup.add(vals[i - 1]);
                listGroup.add(MAX);
            } else {
                listGroup.add(vals[i - 1]);
                listGroup.add(vals[i]);
            }

            valGroup.put(i, listGroup);
        }

        for (Map.Entry<Integer, List<BigDecimal>> listEntry : valGroup.entrySet()) {
            Integer key = listEntry.getKey();
            List<BigDecimal> group = listEntry.getValue();
            if (group.get(0) == null && group.get(1) != null) {
                if (maxCon) {
                    if (toBeCompared.compareTo(group.get(1)) <= 0) {
                        return key;
                    }
                } else {
                    if (toBeCompared.compareTo(group.get(1)) < 0) {
                        return key;
                    }
                }
                continue;
            } else if (group.get(0) != null && group.get(1) != null) {
                if (maxCon && minCon) {
                    if (toBeCompared.compareTo(group.get(0)) >= 0 && toBeCompared.compareTo(group.get(1)) <= 0) {
                        return key;
                    }
                } else if (maxCon && !minCon) {
                    if (toBeCompared.compareTo(group.get(0)) > 0 && toBeCompared.compareTo(group.get(1)) <= 0) {
                        return key;
                    }
                } else if (!maxCon && minCon) {
                    if (toBeCompared.compareTo(group.get(0)) >= 0 && toBeCompared.compareTo(group.get(1)) < 0) {
                        return key;
                    }
                } else {
                    if (toBeCompared.compareTo(group.get(0)) > 0 && toBeCompared.compareTo(group.get(1)) < 0) {
                        return key;
                    }
                }
                continue;
            } else {
                if (minCon) {
                    if (toBeCompared.compareTo(group.get(0)) >= 0) {
                        return key;
                    }
                } else {
                    if (toBeCompared.compareTo(group.get(0)) > 0) {
                        return key;
                    }
                }
                continue;
            }
        }
        return null;
    }

    /**
     * 快速排序
     *
     * @param values
     * @return
     */
    private static void valuesSort(BigDecimal[] values, int low, int high) {
        int i = low;
        int j = high;
        if (low > high) {
            return;
        }
        BigDecimal t;
        BigDecimal temp = values[low];
        //遍历
        while (i < j) {
            //先看右边，依次往左递减
            while (temp.compareTo(values[j]) <= 0 && i < j) {
                j--;
            }
            //再看左边，依次往右递增
            while (temp.compareTo(values[i]) >= 0 && i < j) {
                i++;
            }
            //如果满足条件则交换
            if (i < j) {
                t = values[j];
                values[j] = values[i];
                values[i] = t;
            }
        }
        //最后将基准为与i和j相等位置的数字交换
        values[low] = values[i];
        values[i] = temp;
        //递归调用左半数组
        valuesSort(values, low, j - 1);
        //递归调用右半数组
        valuesSort(values, j + 1, high);
    }
}
