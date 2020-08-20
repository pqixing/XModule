package com.pqixing.leetcode.link;

public class LinkSortNode {

    public static void main(String[] args) {

        int[] source = new int[]{1, 4, 3, 3, 5, 2};
        Node a = null, temp = null;
        for (int s : source) {
            Node node = new Node();
            node.value = s;
            if (temp != null) temp.next = node;
            temp = node;
            if (a == null) a = node;
        }
        System.out.println(sort(a));
    }

    /**
     * 排序链表。 a是一个奇数升序，偶数降序的正整数链表， 排序后返回升序链表。etc: [1,4,3,3,5,2] -> [1,2,3,3,4,5]
     *
     * @param a a是一个奇数升序，偶数降序的正整数链表
     * @return 排序后返回升序链表
     */
    public static Node sort(Node a) {
        if (a.next == null) return a;

        Node odd = a, thrid = a, even = null;
        boolean isOdd = true;//当前位置是奇数
        while (a != null) {
            Node temp = a.next;
            if (isOdd) {
                thrid.next = a;
                thrid = a;
            } else {
                a.next = even;
                even = a;
            }
            isOdd = !isOdd;
            a = temp;
        }
        thrid.next = null;

        Node result = null, third = null;
        while (odd != null || even != null) {
            if (odd != null && (even == null || odd.value <= even.value)) {
                if (third != null) third.next = odd;
                third = odd;
                odd = odd.next;
            } else {
                if (third != null) third.next = even;
                third = even;
                even = even.next;
            }
            if (result == null) result = third;

        }
        return result;
    }

}

