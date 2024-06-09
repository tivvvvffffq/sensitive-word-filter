package com.nxj.sensitivewordfilter.model;

import java.util.HashMap;
import java.util.Map;

public class ACNode {
    Map<Character, ACNode> children = new HashMap<>();
    boolean isEndOfWord;
    ACNode fail;
    String word;
}
