package com.dongzy.common.common.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 获取集合元素多有可能的排列可能
 *
 * @param <T> 元素的泛型类型
 * @author 勇
 */
public final class Permutations<T> extends LinkedList<LinkedList<T>> {

	private static final long serialVersionUID = 1L;

	private List<List<T>> _permutations;
	private int _length;
	private List<int[]> _indices;
	private int[] _value;
	private int _level = -1;

	/**
	 * 根据传入的参数构造函数
	 *
	 * @param items 需要进行组合运算的元素集合
	 */
	public Permutations(List<T> items) {
		this(items, items.size());
	}

	/**
	 * 根据传入的参数构造函数
	 *
	 * @param items  需要进行组合运算的元素集合
	 * @param length 每个结果中包含元素的个数
	 */
	public Permutations(List<T> items, int length) {
		_length = length;
		_permutations = new ArrayList<>();
		_indices = new ArrayList<>();
		buildIndices();
		for (List<T> oneCom : new Combinations<>(items, length).getEnumerator()) {
			for (List<T> entityList : getPermutations(oneCom)) {
				_permutations.add(entityList);
			}
		}
	}

	private void buildIndices() {
		_value = new int[_length];
		visit(0);
	}

	private void visit(int k) {
		_level += 1;
		_value[k] = _level;

		if (_level == _length) {
			_indices.add(_value);
			_value = Arrays.copyOf(_value, _length);
		} else {
			for (int i = 0; i < _length; i++) {
				if (_value[i] == 0) {
					visit(i);
				}
			}
		}

		_level -= 1;
		_value[k] = 0;
	}

	private List<List<T>> getPermutations(List<T> oneCom) {
		List<List<T>> t = new ArrayList<>();

		for (int[] idxs : _indices) {
			List<T> onePerm = new ArrayList<>();
			for (int i = 0; i < _length; i++) {
				onePerm.add(oneCom.get(idxs[i] - 1));
			}
			t.add(onePerm);
		}

		return t;
	}

	/**
	 * 返回排列结果的数量
	 *
	 * @return 结果数量
	 */
	public int getCount() {
		return _permutations.size();
	}

	/**
	 * 返回结果的集合对象
	 *
	 * @return 排列结果对象
	 */
	public List<List<T>> getEnumerator() {
		return _permutations;
	}
}
