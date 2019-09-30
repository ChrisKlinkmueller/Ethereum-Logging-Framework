package au.csiro.data61.aap.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Stream;

public class TreeNode<T> {
    private final T value;
    private final TreeNode<T>[] children;
    private final TreeNode<T> parent;
    
    public TreeNode(T value) {
        this(value, null, null);
    }

    public TreeNode(T value, TreeNode<T>[] children) {
        this(value, null, children);
    } 

    public TreeNode(T value, TreeNode<T> parent) {
        this(value, parent, null);
    }

    public TreeNode(T value, TreeNode<T> parent, TreeNode<T>[] children) {
        this.value = value;
        this.parent = parent;
        assert children == null ? true : Arrays.stream(children).allMatch(child -> child != null);
        this.children = children == null ? createEmptyChildrenArray() : Arrays.copyOf(children, children.length);
    }

    private TreeNode<T>[] createEmptyChildrenArray() {
        @SuppressWarnings("unchecked")
        TreeNode<T>[] emptyNodes = (TreeNode<T>[])Array.newInstance(TreeNode.class, 0);
        return emptyNodes;
    }

    public TreeNode<T> getParent() {
        return this.parent;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isLeave() {
        return this.children.length == 0;
    }

    public int childrenCount() {
        return this.children.length;
    }

    public TreeNode<T> getChild(int index) {
        assert 0 <= index && index < this.childrenCount();
        return this.children[index];
    }

    public Stream<TreeNode<T>> childStream() {
        return Arrays.stream(this.children);
    }

    public T getValue() {
        return this.value;
    }
}