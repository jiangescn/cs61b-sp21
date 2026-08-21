package game2048;

import java.util.Formatter;
import java.util.Observable;


/** 一局 2048 游戏的状态。
 *  @author Jianges
 */
public class Model extends Observable {
    /** 棋盘当前的内容。 */
    private Board board;
    /** 当前分数。 */
    private int score;
    /** 迄今为止的最高分，在游戏结束时更新。 */
    private int maxScore;
    /** 当且仅当游戏已经结束时为 true。 */
    private boolean gameOver;

    /* 坐标系：棋盘的第 C 列、第 R 行（第 0 行、第 0 列位于棋盘左下角）
     * 对应 board.tile(c, r)。请注意！它的工作方式类似 (x, y) 坐标。
     */

    /** 方块的最大数值。 */
    public static final int MAX_PIECE = 2048;

    /** 在边长为 SIZE、没有方块的棋盘上创建一局新的 2048 游戏，初始分数为 0。 */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** 创建一局新的 2048 游戏，其中 RAWVALUES 保存各方块的数值（空位置为 0）。
     * RAWVALUES 按 (row, col) 索引，(0, 0) 对应左下角。用于测试。 */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** 返回当前位置 (COL, ROW) 上的 Tile，其中 0 <= ROW < size()，
     * 0 <= COL < size()。如果该位置没有方块，则返回 null。
     * 此方法用于测试，之后应弃用并删除。
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** 返回棋盘一条边上的格子数。
     * 此方法用于测试，之后应弃用并删除。 */
    public int size() {
        return board.size();
    }

    /** 当且仅当游戏结束时返回 true（已经无路可走，或者棋盘上存在数值为
     * 2048 的方块）。 */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** 返回当前分数。 */
    public int score() {
        return score;
    }

    /** 返回当前最高游戏分数（在游戏结束时更新）。 */
    public int maxScore() {
        return maxScore;
    }

    /** 清空棋盘并重置分数。 */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** 将 TILE 添加到棋盘上；该位置当前必须没有其他 Tile。 */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** 将棋盘向 SIDE 方向倾斜。当且仅当棋盘发生变化时返回 true。
     *
     * 1. 如果两个 Tile 对象在移动方向上相邻且数值相同，它们会合并成一个
     *    数值为原来两倍的 Tile，并将这个新数值加到实例变量 score 中。
     * 2. 一次倾斜中，由合并产生的方块不会再次参与合并。因此每次移动时，
     *    每个方块最多只会参与一次合并（也可能一次都不参与）。
     * 3. 如果移动方向上三个相邻方块的数值相同，则位于移动方向前方的两个
     *    方块合并，后方的方块不合并。
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO：修改 this.board（也可能需要修改 this.score），以反映棋盘向
        // Side SIDE 方向倾斜后的结果。如果棋盘发生变化，将局部变量 changed
        // 设为 true。

        this.board.setViewingPerspective(side);

        for (int i = board.size() - 2; i >= 0; i--)
        {
            for (int j = 0; j < board.size(); j++)
            {
                int ii = i;
                Tile curr = board.tile(j, ii);

                if (curr == null)
                {
                    continue;
                }

                while (ii < board.size() - 1 && board.tile(j, ii + 1) == null)
                {
                    board.move(j, ii + 1, curr);
                    ii++;
                    curr = board.tile(j, ii);
                    changed = true;
                }
            }
        }

        for (int i = board.size() - 2; i >= 0; i--)
        {
            for (int j = 0; j < board.size(); j++)
            {
                Tile current = board.tile(j, i);
                Tile next = board.tile(j, i + 1);

                if (current != null && next != null && current.value() == next.value())
                {
                    board.move(j, i + 1, current);
                    score += this.board.tile(j, i + 1).value();
                    changed = true;
                }
            }
        }

        for (int i = board.size() - 2; i >= 0; i--)
        {
            for (int j = 0; j < board.size(); j++)
            {
                int ii = i;
                Tile curr = board.tile(j, ii);

                if (curr == null)
                {
                    continue;
                }

                while (ii < board.size() - 1 && board.tile(j, ii + 1) == null)
                {
                    board.move(j, ii + 1, curr);
                    ii++;
                    curr = board.tile(j, ii);
                    changed = true;
                }
            }
        }

        this.board.setViewingPerspective(Side.NORTH);

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** 检查游戏是否结束，并相应地设置变量 gameOver。
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** 判断游戏是否结束。 */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** 如果 Board 上至少有一个空格子，则返回 true。
     * 空格子以 null 存储。
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO：完成此函数。
        for (int i = 0; i < b.size(); i++)
        {
            for (int j = 0; j < b.size(); j++)
            {
                if(b.tile(i, j) == null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 如果存在数值等于最大有效值的方块，则返回 true。
     * 最大有效值由 MAX_PIECE 给出。注意，给定 Tile 对象 t，
     * 可以通过 t.value() 获取它的数值。
     */
    public static boolean maxTileExists(Board b) {
        // TODO：完成此函数。
        for (int i = 0; i < b.size(); i++)
        {
            for (int j = 0; j < b.size(); j++)
            {
                if(b.tile(i, j) != null && b.tile(i, j).value() == MAX_PIECE)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 如果棋盘上存在任何合法移动，则返回 true。
     * 以下两种情况表示存在合法移动：
     * 1. 棋盘上至少有一个空格子。
     * 2. 存在两个数值相同的相邻方块。
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO：完成此函数。
        if(emptySpaceExists(b))
        {
            return true;
        }

        for (int i = 1; i < b.size(); i++)
        {
            for (int j = 0; j < b.size(); j++)
            {
                if(b.tile(i, j).value() == b.tile(i - 1, j).value())
                {
                    return true;
                }
            }
        }
        for (int i = 0; i < b.size(); i++)
        {
            for (int j = 1; j < b.size(); j++)
            {
                if(b.tile(i, j).value() == b.tile(i, j - 1).value())
                {
                    return true;
                }
            }
        }


        return false;
    }


    @Override
     /** 以字符串形式返回模型，用于调试。 */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** 返回两个模型是否相等。 */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** 返回 Model 字符串的哈希码。 */
    public int hashCode() {
        return toString().hashCode();
    }
}
