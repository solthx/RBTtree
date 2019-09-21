/**
 * Java 语言: 二叉查找树
 *
 * @author solthx
 * @date 2019/09/18
 */
public class RBT<T extends Comparable<T>> {
    // 红黑树节点定义
    private RBTNode<T> mRoot; // 根节点
    static final boolean RED = true;
    static final boolean BLACK = false;

    public class RBTNode<T> {
        RBTNode<T> left, right, parent;
        boolean color; // false为黑色，true为红色
        T key;

        // 构造函数
        public RBTNode(T key, boolean color, RBTNode<T> parent, RBTNode<T> left, RBTNode<T> right) {
            this.key = key;
            this.color = color;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }

        public RBTNode(T key, boolean color) {
            this.key = key;
            this.color = color;
            this.parent = null;
            this.left = null;
            this.right = null;
        }
    }

    // ================插入和删除操作================
    /*
     * 对红黑树的节点(x)进行左旋转
     *
     * 左旋示意图(对节点x进行左旋)： px px / / x y / \ --(左旋)-. / \ # lx y x ry / \ / \ ly ry lx
     * ly
     *
     *
     */
    private void leftRotate(RBTNode<T> x) {
        // 友情提示, 写这种程序，最好画个图.
        RBTNode<T> y = x.right;
        if (y == null)
            return;
        if (x.parent == null)
            mRoot = y; // 如果X是根节点，旋转后会导致根节点的变换
        else {
            // 不是根节点，就更新parent节点
            if (x == x.parent.left)
                x.parent.left = y; // X处于左子树
            else
                x.parent.right = y; // X处于右子树
        }
        // 更新诀窍在于, 用完一个，更新一个
        x.right = y.left;
        y.left = x;
        y.parent = x.parent;
        x.parent = y;
        if (x.right != null)
            x.right.parent = x;
    }

    private void rightRotate(RBTNode<T> y) {
        // 上面的镜像操作，不多解释
        RBTNode<T> x = y.left;
        if (x == null)
            return;
        if (y.parent == null)
            mRoot = x;
        else {
            if (y == y.parent.left)
                y.parent.left = x;
            else
                y.parent.right = x;
        }
        y.left = x.right;
        x.right = y;
        x.parent = y.parent;
        y.parent = x;
        if (y.left != null)
            y.left.parent = y;
    }

    public boolean insert(T key) {
        RBTNode<T> node = new RBTNode<>(key, RED);
        if (node != null) {
            insert(node);
            return true;
        }
        return false;
    }

    private void insert(RBTNode<T> node) {
        int cmp = 0;
        RBTNode<T> y = null;
        RBTNode<T> x = mRoot;
        // y是插入位置的父节点(因为插入位置肯定是null嘛)
        // x负责找插入位置
        while (x != null) {
            y = x;
            cmp = node.key.compareTo(x.key);
            if (cmp < 0)
                x = x.left;
            else if (cmp > 0)
                x = x.right;
            else
                return;
        }
        // 更新插入新节点的父节点
        node.parent = y;
        if (y != null) {
            // 树不空
            if (cmp < 0)
                // 插在左边
                y.left = node;
            else
                y.right = node;
        } else // y 如果是null,说明插在根的位置，也就是树空
            mRoot = node;
        // 插入的节点一定为RED
        node.color = RED;
        // 插入修正
        insertFixUp(node);
    }

    private void insertFixUp(RBTNode<T> node) {
        RBTNode<T> x = node;
        RBTNode<T> uncle;
        RBTNode<T> father;
        // case 1 父节点是黑色，直接结束
        while (x != null && x.parent != null && x.parent.color == RED) {
            father = x.parent;
            if (x.parent == x.parent.parent.left) {
                // x.parent是左孩子
                uncle = father.parent.right;
                if (uncle != null && uncle.color == RED) { // case 2
                    father.color = BLACK;
                    uncle.color = BLACK;
                    father.parent.color = RED; // 为了保持黑高不变，上面的两个操作使得黑高增加了1，那么就要把一个黑色节点置红，使得黑高-1
                } else {
                    // case3
                    // uncle==null || uncle.color==BLACK
                    if (x == x.parent.right) {
                        // case 3-2
                        x = father;
                        leftRotate(father); // case 3-2 转变成了case 3-1
                    }
                    // case 3-1
                    father = x.parent;
                    father.color = BLACK;
                    father.parent.color = RED;
                    rightRotate(father.parent);
                }
            } else { // 上面的镜像
                uncle = father.parent.left;
                if (uncle != null && uncle.color == RED) {
                    uncle.color = BLACK;
                    father.color = BLACK;
                    father.parent.color = RED;
                } else {
                    if (x == x.parent.left) {
                        x = father;
                        rightRotate(father);
                    }
                    father = x.parent;
                    father.color = BLACK;
                    father.parent.color = RED;
                    leftRotate(father.parent);
                }
            }
            x = father.parent;
        }
        mRoot.color = BLACK;
    }

    private RBTNode<T> search(T key) {
        RBTNode<T> x = mRoot;
        int cmp = 0;
        while (x != null) {
            cmp = key.compareTo(x.key);
            if (cmp < 0)
                x = x.left;
            else if (cmp > 0)
                x = x.right;
            else
                break;
        }
        return x;
    }

    public void remove(T key) {
        RBTNode<T> node = search(key);
        if (node != null)
            remove(node);
    }

    // 先正常删除，然后再fixup
    private void remove(RBTNode<T> x) {
        boolean originalColor = x.color; // 记录即将被删除的节点颜色
        RBTNode<T> replace;
        if (x.left == null && x.right == null) {
            // 直接删掉
            replace = null;
            if (x.parent == null)
                mRoot = null;
            else if (x.parent.left == x)
                x.parent.left = null;
            else {
                x.parent.right = null;

            }
        } else if (x.left == null) {
            // 右孩子上位
            replace = x.right;
            x.right.parent = x.parent;
            if (x.parent == null)
                mRoot = x.right;
            else if (x.parent.left == x)
                x.parent.left = x.right;
            else
                x.parent.right = x.right;
        } else if (x.right == null) {
            // 左孩子上位
            replace = x.right;
            x.left.parent = x.parent;
            if (x.parent == null)
                mRoot = x.left;
            else if (x.parent.left == x)
                x.parent.left = x.left;
            else
                x.parent.right = x.left;
        } else {
            replace = x.right;
            while (replace.left != null)
                replace = replace.left;
            originalColor = replace.color;
            // 用replace来替换x
            // 然后再删除replace
            remove(replace);
            x.key = replace.key;
            x.color = replace.color;
            replace = x;
        }
        if (originalColor == BLACK && mRoot != null)
            removeFixUp(replace, x.parent);
    }

    private void removeFixUp(RBTNode<T> x, RBTNode<T> p) {
        if (x == null)
            return;
        RBTNode<T> father, sib;
        while (x.parent != null && x.color == BLACK) {
            father = x.parent;
            if (father.left == x) {
                // x是左孩子
                sib = father.right; // 兄弟节点
                if (sib.color == RED) {
                    // case 1
                    father.color = RED;
                    sib.color = BLACK;
                    leftRotate(father);
                    sib = father.right;
                    // case 1 转换成case 2,3,4
                }
                // 进入case 2,3,4
                if (sib.left.color == BLACK && sib.right.color == BLACK) {
                    sib.color = RED; // case 2
                    x = sib.parent; // case 2
                } else if (sib.right.color == BLACK) {
                    // case3 转换成 case 4
                    // 把靠外子节点变为红色
                    sib.left.color = BLACK;
                    sib.color = RED;
                    rightRotate(sib);
                    sib = x.parent.right;
                    // case 3 转变为case 4
                }
                // case 4
                sib.color = sib.parent.color;
                sib.parent.color = BLACK;
                sib.right.color = BLACK;
                leftRotate(sib.parent);
                x = mRoot;
            } else {
                // x是右孩子
                sib = father.left;
                if (sib.color == RED) {
                    // case 1
                    sib.color = BLACK;
                    father.color = RED;
                    rightRotate(father);
                    sib = x.parent.left;
                    // case 1变为case 2
                }
                if (sib.left.color == BLACK && sib.right.color == BLACK) {
                    // case 2
                    sib.color = RED;
                    x = sib.parent;
                    // case 2 跳出 或转 case 1,3,4
                }
                if (sib.left.color == BLACK) {
                    // case 3
                    sib.right.color = BLACK;
                    sib.color = RED;
                    leftRotate(sib);
                    sib = x.parent.left;
                } // case 3 转成 case 4

                // case 4
                sib.color = x.parent.color;
                sib.left.color = BLACK;
                sib.parent.color = BLACK;
                rightRotate(sib.parent);
                x = mRoot;
            }
        }
        if (x != null)
            x.color = BLACK;
    }
    // ================插入和删除操作 end================

    // ================用于Test的工具方法================
    public void preOrder() {
        preOrder(mRoot);
        System.out.println();
    }

    private void preOrder(RBTNode<T> r) {
        if (r == null)
            return;
        System.out.print(r.key + " ");
        preOrder(r.left);
        preOrder(r.right);
    }

    public void inOrder() {
        inOrder(mRoot);
        System.err.println();
    }

    private void inOrder(RBTNode<T> r) {
        if (r == null)
            return;
        inOrder(r.left);
        System.out.print(r.key + " ");
        inOrder(r.right);
    }

    public void postOrder() {
        postOrder(mRoot);
        System.err.println();
    }

    private void postOrder(RBTNode<T> r) {
        if (r == null)
            return;
        postOrder(r.left);
        postOrder(r.right);
        System.out.print(r.key + " ");
    }

    /*
     * 打印"红黑树"
     *
     * key -- 节点的键值 direction -- 0，表示该节点是根节点; -1，表示该节点是它的父结点的左孩子; 1，表示该节点是它的父结点的右孩子。
     */
    private void print(RBTNode<T> tree, T key, int direction) {

        if (tree != null) {

            if (direction == 0) // tree是根节点
                System.out.printf("%2d(B) is root\n", tree.key);
            else // tree是分支节点
                System.out.printf("%2d(%s) is %2d's %6s child\n", tree.key, tree.color ? "R" : "B", key,
                        direction == 1 ? "right" : "left");

            print(tree.left, tree.key, -1);
            print(tree.right, tree.key, 1);
        }
    }

    public void print() {
        if (mRoot != null)
            print(mRoot, mRoot.key, 0);
    }
}