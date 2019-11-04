package com.dongzy.common.data.tree;

import com.dongzy.common.common.collection.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 树状数据的辅助工具类
 */
@SuppressWarnings("unchecked")
public final class TreeUtils {

    /**
     * 将一个普通的平级列表转换为树状结构几何
     *
     * @param items 需要转换的平级列表
     * @param <T>   类型
     * @return 树的集合
     */
    public static <T extends ITreeModel> List<T> toTree(List<T> items) {

        if (CollectionUtils.isEmpty(items)) {
            return new ArrayList<>(0);
        }

        //检查数据
        //1.id不能为空，并且不能相同
        long count = items.stream().filter(p -> p.getId() != null).count();
        if (count != items.size()) {
            throw new IllegalArgumentException("存在id为空的数据项");
        }
        count = items.stream().map(ITreeModel::getId).distinct().count();
        if (count != items.size()) {
            throw new IllegalArgumentException("存在id重复的数据项");
        }

        //当没有移动任何项目时，数据就处理完毕了
        boolean isMove = true;
        while (isMove) {
            isMove = false;
            for (int i = 0; i < items.size(); i++) {
                T t1 = items.get(i);
                for (int j = i + 1; j < items.size(); j++) {
                    T t2 = items.get(j);
                    //如果找到了子节点，那么
                    if (Objects.equals(t1.getId(), t2.getParentId())) {
                        t1.getChildren().add(t2);
                        items.remove(j);
                        isMove = true;
                    } else if (Objects.equals(t1.getParentId(), t2.getId())) {
                        //如果找到了父节点，那么
                        t2.getChildren().add(t1);
                        items.remove(i);
                        i--;
                        isMove = true;
                        break;
                    }
                }
            }
        }

        return items;
    }

    /**
     * 将一个普通的平级列表转换为树状结构几何
     *
     * @param collection 需要转换的平级列表
     * @param <T>        类型
     * @return 树的集合
     */
    public static <T extends ITreeModel> Collection<T> toList(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return new ArrayList<>(0);
        }
        Collection<T> result = new ArrayList<>();

        for (T t : collection) {
            result.add(t);
            addSubToList(result, t);
        }

        return result;
    }

    private static <T extends ITreeModel, V> void addSubToList(Collection<T> result, ITreeModel<T, V> model) {
        for (T subModel : model.getChildren()) {
            result.add(subModel);
            addSubToList(result, subModel);
        }
    }
}
