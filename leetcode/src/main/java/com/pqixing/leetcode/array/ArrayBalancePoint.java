package com.pqixing.leetcode.array;

public class ArrayBalancePoint {
    public static void main(String[] args) {
        System.out.println("--------------start : testGetBalancePoint--------------");
        System.out.println(getBalancePoint(new int[]{1, 3, 1, 4}));
        System.out.println(getBalancePoint(new int[]{1, 0, 1, 4}));
        System.out.println(getBalancePoint(new int[]{1, -3, 1, 4, -6}));
        System.out.println("--------------end : testGetBalancePoint--------------");
    }

    /**
     * 获取平衡点   平衡点定义： 该位置左边的值的和等于右边值的和。 etc: [1,3,4,2,2] -> 2 ; [-1,4,5,-6] -> 1
     *
     * @param array 原数组
     * @return 返回平衡点坐标，如果不存在，返回-1
     */
    public static int getBalancePoint(int[] array) {

        if (array.length == 0) return -1;

        int count = 0;

        for (int j : array) count += j;

        int leftCount = 0;
        int rightCount = count;

        for (int i = 0; i < array.length; i++) {

            if (i != 0) leftCount += array[i - 1];
            rightCount -= array[i];
            if (leftCount == rightCount) return i;
        }

        return -1;
    }
}
