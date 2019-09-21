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

下图x为插入节点，parent为插入节点的父节点，parent的父节点为爷爷节点， parent的兄弟节点为叔叔节点

#### Case 1 插入节点的父节点是黑色的
这种情况是我们最喜欢的了，因为什么什么事都不用做，如果父节点是黑色，插入一个红色节点什么性质也不会破坏

#### Case 2 插入节点的父节点是红色的，并且 ( 叔叔节点存在 且 叔叔节点为红色 ）
这种情况会变成下面这张图: 
![insertCase2_1](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5case2.png)

上图画的只是一个树的局部(左下角)

这个时候，只要把parent和uncle给染黑，

那么当前就局部成立了， 但是，因为染黑节点，导致了局部黑高+1， 局部黑高+1， 就会破坏“每个节点到任意一个叶子节点的黑高相同” 这一性质， 因此我们要给局部的黑高-1

即把爷爷节点染红， 因为爷爷节点在这个情况下一定是黑的。

为什么呢？ 因为根据性质 "红色节点的孩子节点一定是黑节点" 可以知道，如果爷爷节点是红色的，那么父节点和叔节点一定是黑色的，这与case2不符合，因此爷爷节点一定是黑色的。

把黑色的染红，就使得黑高-1. 因此局部黑高不变，并且满足了局部的所有性质

同时，因为爷爷节点被染红，所以可能会导致“上面的局部”性质被破坏，因此需要向上传递继续fixUp

当前节点x变成爷爷节点，然后继续判断.

因此，对于case2应进行的操作就是:
1. 把叔节点和父节点染黑
2. 把爷爷节点染红, 变成了下图
3. 向上传递，把爷爷节点当作当前节点，然后继续向上fixUp
![insertCase2_2](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5Case2_2.png)

#### Case 3 插入节点的父节点是红色的，并且 （ 叔叔节点不存在 或 叔叔节点为黑色 ）
##### Case 3-1 叔叔节点不存在的情况(下图1) 和 ( 叔叔节点为黑色 且 插入的节点和父亲节点和爷爷节点在一条线上(下图2) )  

![insertCase3_2](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5Case3_2.png)

![insertCase3_1](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5Case3_1.png)

上面的两种情况是可以直接进行旋转的，

转换的原则和上面还是一样：
    在不破坏局部黑高的情况，使得两个红色节点不相连.

这里的解决方法就是:

    把父亲节点染黑和爷爷节点染红，然后对爷爷节点进行右旋

    之所以这么做，是因为要让局部根(就是爷爷节点)的颜色不变，然后把左子树多余的点移到右边,而为了保证黑高不变，必须移过去的是红色节点。
    
    基于上面的两个考虑，就通过把父节点染黑(作为局部根, 这里的爷爷节点一定是黑色的，原因上面讲过了), 然后把爷爷节点染红(作为移到右子树的点)，然后对爷爷节点进行右旋转.

因此，对于case 3_1的解决办法就是：

1. 把父节点染黑，把爷爷节点染红
2. 对爷爷节点做右旋操作.

##### Case 3-2 叔叔节点不存在的情况(下图1) 和 ( 叔叔节点为黑色 且 插入的节点和父亲节点和爷爷节点呈Z字形(下图2) )  

![insertCase3_3](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5Case3_3.png)

![insertCase3_4](https://github.com/solthx/RBTtree/blob/master/pic/%E6%8F%92%E5%85%A5Case3_4.png)

这两种情况直接对父节点进行左旋，就变成了Case3-1的情况.


Case3的代码:
```java
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
```


## 四、删除操作(Remove)
红黑树的删除操作要比插入操作复杂一些.  不过和插入操作类似，也是被分为两个部分, 即: 删除和修正(删除之后可能会破坏红黑树的上面的几个性质，所以要进行修正(fix-up))

### 1. 节点的删除(remove)
这一部分和bst的删除思想是一样, 但还是有几个地方不同:
1. 需要记录被删除节点的颜色
2. 需要更新parent节点(画图防漏)
3. 必要的话，记得更新根节点
4. 多了一个fix-up操作

```java
private void remove(RBTNode<T> x) {
    boolean originalColor = x.color; // 记录即将被删除的节点颜色
    // x表示被删除的那个节点，replace表示"删除了x之后，在x的那个位置上的那个节点"
    RBTNode<T> replace; 
    if (x.left == null && x.right == null) {
        // 左右孩子都不存在，直接删除该节点
        replace = null;
        if (x.parent == null)
            mRoot = null;
        else if (x.parent.left == x)
            x.parent.left = null;
        else 
            x.parent.right = null;
    } else if (x.left == null) {
        // 左孩子不存在，删除该节点，就把右孩子拉上来
        replace = x.right;
        x.right.parent = x.parent;
        if (x.parent == null)
            mRoot = x.right;
        else if (x.parent.left == x)
            x.parent.left = x.right;
        else
            x.parent.right = x.right;
    } else if (x.right == null) {
        // 右孩子不存在，删除该节点，就把左孩子上位
        replace = x.right;
        x.left.parent = x.parent;
        if (x.parent == null)
            mRoot = x.left;
        else if (x.parent.left == x)
            x.parent.left = x.left;
        else
            x.parent.right = x.left;
    } else {
        // 左右孩子都存在，去右子树把最小节点赋值给当前节点，然后删除右子树的那个最小节点
        // 思想和bst的删除一样
        replace = x.right;
        while (replace.left != null)
            replace = replace.left;
        originalColor = replace.color;
        // 用replace来替换x
        // 然后再删除replace
        remove(replace);
        // 把右子树的最小节点赋值给x
        x.key = replace.key;
        x.color = replace.color;
        // 因此，x就变成了替换之后的节点
        // 更新replace
        replace = x;  
    }
    // 如果被删除的那个节点是黑色的, 那么黑高被改变，就需要进行fix-Up
    if (originalColor == BLACK && mRoot != null)
        removeFixUp(replace, x.parent);
}
```


### 2. 删除修正操作(removeFixUp)
只有当被删除的节点是黑色节点的时候，才会进行删除修正的操作，因为黑色节点的删除导致了黑高的改变，破坏了性质.

因为不管是因为删除了一个节点还是因为什么样的原因，需要修正的情况就是因为一边的黑高比另一边少1！ 假设一边黑高是n，另一边是n-1, 我们的目标就是通过旋转和染色，使得两边的黑高都是n，
而不是两边都变成n-1。 (不过，通过先把两边黑高变成n-1然后向上或向下传递，也是一种操作，后面会讲)

因为删除修正的情况，主要是围绕着以下几个节点：

1. 删除节点的位置(被删除的是x，删除了x之后，x节点被replace节点所替换，所以这里实际是replace)
2. 删除节点的兄弟节点sib
3. 兄弟节点的左右子节点
如下图所示, 因为只是为了说明节点关系，因此并没有进行颜色上的表示:
![removeCase0_1](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp0_1.png)

再次强调，下面的所有case都是在删除掉的节点是黑色的情况下发生的，也就是说此时，高度已经不平衡了。

然后下面的case以在左子树来举例子，思想理解了以后，右子树的情况就是左子树的镜像情况.

#### case 1 兄弟节点sib是红色的情况
下图为case 1的情况，旁边数字表示黑高，可以很显然的看到左子树的黑高比右子树要少1
![removeCase1_1](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp1_1.png)

对于这种情况，解决方法是把case 1的情况转换成case 2,3,4的情况，也就是兄弟节点sib是黑色的情况. 那么该怎么转换呢?

因为sib的颜色是红色的，所以sib的左孩子节点一定是黑色的或不存在的, 
也就是说，如果通过旋转，能够使得sib的左孩子节点变成当前replace节点的兄弟节点的话，那么就可以把case 1的情况转换到case 2,3,4了，

这一想法可以通过旋转来实现. 也就是通过对parent节点进行左旋，旋转之后，当前的sib就变成了parent的parent, 当前sib的左孩子就变成了parent的右孩子，replace依然是parent的左孩子，
这时，就变成了如下这张图.
![removeCase1_2](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp1_2.png)

但因为旋转的关系，黑高变的很乱了，因为我们要把这种不平衡的关系向下转移，所以尽量不要影响上层，因此尽量保证上层的右子树黑高依然是n+1, 左子树的黑高依然是n，然后向下传递，

所以我们只要把原parent染红，原sib染黑，就可以了, 

因此，现在只要解决掉原parent的位置的不平衡情况就可以了.

此时replace的兄弟节点一定是黑色的或者是不存在的，也就是case 2 3 4的情况了！ 如下图所示
![removeCase1_3](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp1_3.png)

转换后的样子我们可以看到，上层的不平衡依然是 左黑高为n, 右黑高为n+1 (和一开始一样),
左子树的不平衡为 左黑高n-1，右黑高为n， 因此，我们只要解决左子树黑高少1的问题，上层的问题也解决了， 同时还保证了replace的兄弟节点为黑色(或不存在), 成功的把case1 转换成了case 2,3,4

小结以下case 1 转换成case2,3,4 的解决方法:
1. 把parent染红，把sib染黑
2. 对parent进行左旋

#### case 2 兄弟节点sib为黑色， 兄弟节点sib的左右孩子节点均为黑色
这种情况如下图所示 
![removeCase2_1](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp2_1.png)


根据上图，我们可以看到，因为sib节点是黑色的，所以如果replace节点也是黑色的话，那么父亲节点有可能是红色的，所以这里讨论以下:
##### case 2-1 父亲节点为红色
这种情况最好办了，只要把父亲节点染黑，sib节点染红，就万事大吉了。 
因为这样做了之后，从 左黑高n-1，右黑高n的不平衡变成了, 左黑高n，右黑高n的平衡。如下图所示
![removeCase2_2](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp2_2.png)

##### case 2-2 父亲节点为黑色
这种情况，依然是把sib节点染红， 染红之后，左右子树的黑高就相同的，parent节点就平衡了，
但是，我们的目标是把 两边黑高都变成n， 这里虽然平衡了，但两边黑高是n-1，因此在局部上的黑高还是少1， 所以要向上继续调整，即把父节点当作replace，继续来做调整, 如下图所示.
![removeCase2_3](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp2_3.png)

因此，对于case 2的情况我们要做的事:

1. 把兄弟节点染红
2. 如果父节点是红的，那就把父节点染黑然后结束； 如果父节点是黑的，那就把父节点当作replace节点，继续调整，转换成case 3或case 4



#### 新的概念引入
为了方便对case 3的讲解，这里引入一个概念，就叫“共线”好了,
假设，如果 （ 节点b是节点a的右孩子，节点c是节点b的右孩子） 或者 （节点b是节点a的左孩子，节点c是节点b的左孩子） 那么就称 “节点a,b,c共线” , 也就是说，这三点在一条斜线上.

现在引入一个节点Y， 节点Y满足:
1. 是sib的孩子节点
2. 节点Y，节点sib，节点parent 三点共线

#### case 4 兄弟节点sib为黑色 且 在sib的孩子节点中，节点Y为红色.

令和parent，sib共线的那个 sib的孩子节点为 节点Y

这里为啥把case 3给跳了？？？ 

因为case 3的最终目标是转换成case 4的情况， 所以先来了解一下case 4

case 4的情况如下图所示:
![removeCase4_1](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp4_1.png)

如果把目标是**把左右黑高都变成n**， 

抽象一点来说，就是**从parent的右子树里，拿出一个红色节点，染黑，丢给parent的左子树**.

这个一过程，可以通过 旋转+染色的传递 来完成。

现在聪明的你应该明白了，为什么要求节点Y必须要是红色的 了把。

这里的核心思想就是:

**要用这个共线的孩子节点 来替代sib, 然后sib再去替代parent，然后把parent给染黑，给送到左子树里。** (ps: 这里的替换只是在某一位置上的颜色的替换，不是值的替换)

在节点替换的过程中，颜色上的变换，可以通过染色里来完成，位置上的变换可以通过旋转来完成.


因此，在这一case中， 需要做的事就是：

1. 把 节点Y 染成 节点sib的颜色(就是黑色)
2. 把 节点sib 染成 节点parent的颜色
3. 把节点parent的颜色 染成黑色 
4. 对parent节点进行左旋

如此一下就完成了上面提到的那个 “抽象的操作”， 变成了下面这张图.
![removeCase4_2](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp4_2.png)

#### case 3 兄弟节点sib为黑色 且 节点Y为黑色 , 节点Y的兄弟节点为红色

这种情况其实就是在不是case 2的情况下，节点Y不是红色的情况.

因此，我们要想办法把这个情况转成case 4，也就是把节点Y变红。

为了更好的理解，还是放一张case 3的图把。
![removeCase3_1](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp3_1.png)

其实这里的思想 也是替换的思想， 节点Y的兄弟节点不是红色的么？ 那就用它来替换掉节点Y好了。

而且，把一个红色节点从左子树拿走，也不会改变黑高。

所以，这里的操作就是：
1. 把节点Y的兄弟节点染成sib的颜色(就是黑色)
2. 把sib的颜色染成节点Y的颜色 (就是红色)
3. 对sib节点进行右旋操作

这样一来，就实现了上面的想法，变成了下面这张图，即case 4的情况.
![removeCase3_2](https://github.com/solthx/RBTtree/blob/master/pic/removeFixUp3_2.png)

最后放上删除修正操作的代码部分:
```java
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
```

下面这张图，是删除修正操作的case之间的转换
![case_trans](https://github.com/solthx/RBTtree/blob/master/pic/case_trans.png)