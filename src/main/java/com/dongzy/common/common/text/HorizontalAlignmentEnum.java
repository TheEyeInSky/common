package com.dongzy.common.common.text;

/**
 * 水平对齐方式枚举
 */
public enum HorizontalAlignmentEnum {

    NORMAL(0),      //默认对齐方式
    LEFT(1),        //左对齐
    CENTER(2),      //居中对齐
    RIGHT(3);       //右对齐

    private int value;

    HorizontalAlignmentEnum(int value) {
        this.value = value;
    }

    /**
     * 根据传入的数值，转换为相应枚举值
     *
     * @param i 数值
     * @return 枚举值
     */
    public static HorizontalAlignmentEnum getByValue(int i) {
        switch (i) {
            case 1:
                return LEFT;
            case 2:
                return CENTER;
            case 3:
                return RIGHT;
            default:
                return NORMAL;
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
