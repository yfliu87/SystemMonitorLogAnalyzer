package com.SystemMonitor.Algorithm;

import java.util.Vector;

public class FPTree {

    public FPTree(String item) {
        this.item = item;
        next = null;
        children = new Vector<FPTree>();
        root = false;
    }

    boolean root;
    String item;
    Vector<FPTree> children;
    FPTree parent;
    FPTree next;
    int count;

    boolean isRoot(){
        return root;
    }

}