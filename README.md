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

## 三、插入操作(Insert)
红黑树的插入操作分为两个部分，即: 插入和修正（插入之后可能会破坏红黑树的上面的几个性质，所以要进行修正(fix-up)）

### 1. 节点的插入
这一部分和BST的节点插入几乎一摸一样，就不多说了.
不同的地方是：
 - 更新parent
 - 新插入的节点置为红色
 - 进行fix-up操作
```java
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
```

### 2. 插入修正操作(insertFixUp)
插入之后，因为节点是红色的，不会影响黑高，所以只会破坏"如果一个节点是红色的，那么这个节点的左右子节点一定是黑色的" 这一性质.  
为了方便说明，这里引入"叔叔节点"这一概念，叔叔节点顾名思义，就是父节点的兄弟节点.
下面的case主要就是根据父节点和叔叔节点的情况，来对爷爷节点进行旋转.

#### Case 1 插入节点的父节点是黑色的
这种情况是我们最喜欢的了，因为什么什么事都不用做，如果父节点是黑色，插入一个红色节点什么性质也不会破坏

#### Case 2 插入节点的父节点是红色的，并且 ( 叔叔节点存在 且 叔叔节点为红色 ）
这种情况会变成下面这张图: 
![insertCase2_1](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5case2.png)

上图画的只是一个树的局部(左下角)

这个时候，只要把parent和uncle给染黑，

那么当前就局部成立了， 但是，因为染黑节点，导致了局部黑高+1， 局部黑高+1， 就会破坏“每个节点到任意一个叶子节点的黑高相同” 这一性质， 因此我们要给局部的黑高-1

即把爷爷节点染红， 因为爷爷节点在这个情况下一定是黑的。

为什么呢？ 因为根据性质"红色节点的孩子节点一定是黑节点"可以知道，如果爷爷节点是红色的，那么父节点和叔节点一定是黑色的，这与case2不符合，因此爷爷节点一定是黑色的。

把黑色的染红，就使得黑高-1. 因此局部黑高不变，并且满足了局部的所有性质

同时，因为爷爷节点被染红，所以可能会导致“上面的局部”性质被破坏，因此需要向上传递继续fixUp

当前节点x变成爷爷节点，然后继续判断.

因此，对于case2应进行的操作就是:
1. 把叔节点和父节点染黑
2. 把爷爷节点染红, 变成了下图
3. 向上传递，把爷爷节点当作当前节点，然后继续向上fixUp
![insertCase2_2](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5Case2_2.png)

### Case 3 插入节点的父节点是红色的，并且 （ 叔叔节点不存在 或 叔叔节点为黑色 ）
Case 3-1 叔叔节点不存在的情况 和 
![insertCase3_2](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5Case3_2.png)

![insertCase3_1](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5Case3_1.png)
