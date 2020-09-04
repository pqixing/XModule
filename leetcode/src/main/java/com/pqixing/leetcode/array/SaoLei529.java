package com.pqixing.leetcode.array;

/**
 * 让我们一起来玩扫雷游戏！
 * <p>
 * 给定一个代表游戏板的二维字符矩阵。 'M' 代表一个未挖出的地雷，'E' 代表一个未挖出的空方块，'B' 代表没有相邻（上，下，左，右，和所有4个对角线）地雷的已挖出的空白方块，数字（'1' 到 '8'）表示有多少地雷与这块已挖出的方块相邻，'X' 则表示一个已挖出的地雷。
 * <p>
 * 现在给出在所有未挖出的方块中（'M'或者'E'）的下一个点击位置（行和列索引），根据以下规则，返回相应位置被点击后对应的面板：
 * <p>
 * 如果一个地雷（'M'）被挖出，游戏就结束了- 把它改为 'X'。
 * 如果一个没有相邻地雷的空方块（'E'）被挖出，修改它为（'B'），并且所有和其相邻的未挖出方块都应该被递归地揭露。
 * 如果一个至少与一个地雷相邻的空方块（'E'）被挖出，修改它为数字（'1'到'8'），表示相邻地雷的数量。
 * 如果在此次点击中，若无更多方块可被揭露，则返回面板。
 * <p>
 * 示例 1：
 * <p>
 * 输入:
 * <p>
 * [['E', 'E', 'E', 'E', 'E'],
 * ['E', 'E', 'M', 'E', 'E'],
 * ['E', 'E', 'E', 'E', 'E'],
 * ['E', 'E', 'E', 'E', 'E']]
 * <p>
 * Click : [3,0]
 * <p>
 * 输出:
 * <p>
 * [['B', '1', 'E', '1', 'B'],
 * ['B', '1', 'M', '1', 'B'],
 * ['B', '1', '1', '1', 'B'],
 * ['B', 'B', 'B', 'B', 'B']]
 * <p>
 * <p>
 * 输入:
 * <p>
 * [['B', '1', 'E', '1', 'B'],
 * ['B', '1', 'M', '1', 'B'],
 * ['B', '1', '1', '1', 'B'],
 * ['B', 'B', 'B', 'B', 'B']]
 * <p>
 * Click : [1,2]
 * <p>
 * 输出:
 * <p>
 * [['B', '1', 'E', '1', 'B'],
 * ['B', '1', 'X', '1', 'B'],
 * ['B', '1', '1', '1', 'B'],
 * ['B', 'B', 'B', 'B', 'B']]
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/minesweeper
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 */
public class SaoLei529 {


    private static char[] lei = new char[]{'B', '1', '2', '3', '4', '5', '6', '7', '8'};

    public static char[][] updateBoard(char[][] board, int[] click) {
        int x = click[0];
        int y = click[1];
        if (board[x][y] == 'M') board[x][y] = 'X';
        else if (board[x][y] == 'E') update(board, x, y);
        return board;
    }

    private static int[] xl = new int[]{-1, 0, 1};

    private static void update(char[][] board, int x, int y) {
        if (board[x][y] != 'E') return;
        int count = 0;//查找周围的雷的数量

        int[][] run =  new int[9][2];
        for (int i : xl) {
            int x1 = x + i;
            if (x1 >= 0 && x1 < board.length) for (int j : xl) {
                int y1 = y + j;
                if (y1 >= 0 && y1 < board[0].length) {
                    if (board[x1][y1] == 'M') count++;
                }
            }
        }
        board[x][y] = lei[count];
        if (count == 0) for (int i : xl) {
            int x1 = x + i;
            if (x1 >= 0 && x1 < board.length) for (int j : xl) {
                int y1 = y + j;
                if (y1 >= 0 && y1 < board[0].length) {
                    if (board[x][y] == 'E') update(board, x1, y1);
                }
            }
        }
    }
}




