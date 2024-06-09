package com.nxj.sensitivewordfilter.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 自动机节点
 */
public class ACNode {
    Map<Character, ACNode> children = new HashMap<>();
    boolean isEndOfWord;
    ACNode fail;
    String word;
}
