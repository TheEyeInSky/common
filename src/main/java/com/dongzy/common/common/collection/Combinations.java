package com.dongzy.common.common.collection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 获取集合元素多有可能的组合可能
 *
 * @param <T> 元素的泛型类型
 * @author 勇
 */
public final class Combinations<T> extends LinkedList<LinkedList<T>> {

	private List<List<T>> combinations;
	private List<T> items;
	private int length;
	private int[] endIndices;

	/**
	 * 根据传入的参数构造函数
	 *
	 * @param items 需要进行组合运算的元素集合
	 */
	public Combinations(List<T> items) {
		this(items, items.size());
	}

	/**
	 * 根据传入的参数构造函数
	 *
	 * @param items  需要进行组合运算的元素集合
	 * @param length 组合结果中包含元素的个数，如数字为3 ，那么每种组合的结果中包含三个不同的对象
	 */
	public Combinations(List<T> items, int length) {
		this.items = items;
		this.length = length;
		combinations = new ArrayList<>();
		endIndices = new int[length];
		int j = length - 1;
		for (int i = items.size() - 1; i > items.size() - 1 - length; i--) {
			endIndices[j] = i;
			j--;
		}
		computeCombination();
	}

	/**
	 * 组合算法
	 */
	private void computeCombination() {
		int[] indices = new int[length];
		for (int i = 0; i < length; i++) {
			indices[i] = i;
		}

		do {
			List<T> oneCom = new ArrayList<>();
			for (int k = 0; k < length; k++) {
				oneCom.add(items.get(indices[k]));
			}
			combinations.add(oneCom);
		} while (getNext(indices));
	}

	/**
	 * 获取下一个对象
	 */
	private boolean getNext(int[] indices) {
		boolean hasMore = true;

		for (int j = endIndices.length - 1; j > -1; j--) {
			if (indices[j] < endIndices[j]) {
				indices[j]++;
				for (int k = 1; j + k < endIndices.length; k++) {
					indices[j + k] = indices[j] + k;
				}
				break;
			} else if (j == 0) {
				hasMore = false;
			}
		}
		return hasMore;
	}

	/**
	 * 返回结果集合的数量
	 *
	 * @return 结果集合的数量
	 */
	public int getCount() {
		return combinations.size();
	}

	/**
	 * 返回结果的集合对象
	 *
	 * @return 组合的结果对象
	 */
	public List<List<T>> getEnumerator() {
		return combinations;
	}
}
