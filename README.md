# RBTtree
记录一下这两天对红黑树的学习和理解，使用的教材是算法导论，实现的语言是Java

## 一、RBTree的特点
### 1. 红黑树的五个性质:
- 每个节点要么是红色，要么是黑色
- 根节点一定是黑色的
- 如果一个节点是红色的，那么这个节点的左右子节点一定是黑色的(但如果当前节点是黑色的，那么左右子节点红黑都可以)
- 对每个节点，从该节点到任意一个叶子节点的路径上，遇到的黑色节点数一定都是相同的.
- 每次新插入的节点为红色.为了方便后面的说明，这里假定，节点A到任意一个叶子节点的路径上，遇到的黑色节点的数量为黑高.

上面的性质3, 4, 5很重要， 红黑树之所以难，就是在于在插入和删除之后，对性质3,4,5的维护上, 下面一个一个来讲!

### 2. 红黑树的节点保存的内容:
- 节点颜色(color)
- 左右子节点(left, right)
- 父亲节点 (parent)
- 需要保存的信息 (key)

## 二、旋转操作(Rotate)
红黑树也是平衡树的一种，自然少不了旋转。 这里的旋转操作和AVL树里的旋转操作几乎一样，只不过需要多处理一个parent的信息。  

实现时需要注意的坑：
    1. 如果是在根处旋转，记得更新根
    2. 记得更新parent，这里建议画个图，可以不漏.

```java
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
```
