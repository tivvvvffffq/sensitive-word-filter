package com.nxj.sensitivewordfilter.model;

import java.util.*;

/**
 * 自实现AC自动机
 */
public class ACAuto {
    private ACNode root;
    private Map<String, List<String>> comboWordToParts; // 组合敏感词到其组成部分的映射

    public ACAuto() {
        root = new ACNode(); // 初始化根节点
        comboWordToParts = new HashMap<>();
    }

    // 向自动机中插入一个单词
    public void insert(String word) {
        ACNode current = root;
        for (char l : word.toCharArray()) {
            current = current.children.computeIfAbsent(l, c -> new ACNode());
        }
        current.isEndOfWord = true;
        current.word = word;
    }

    // 构建失败指针
    public void buildFailurePointers() {
        Queue<ACNode> queue = new LinkedList<>();
        root.fail = null;
        queue.add(root);

        while (!queue.isEmpty()) {
            ACNode parent = queue.remove();

            for (Map.Entry<Character, ACNode> entry : parent.children.entrySet()) {
                ACNode child = entry.getValue();
                child.fail = root;
                ACNode fail = parent.fail;

                while (fail != null) {
                    ACNode failChild = fail.children.get(entry.getKey());
                    if (failChild != null) {
                        child.fail = failChild;
                        break;
                    }
                    fail = fail.fail;
                }

                queue.add(child);
            }
        }
    }

    // 初始化方法，用于处理组合敏感词和构建自动机
    public void initialize(Set<String> comboWords) {
        for (String comboWord : comboWords) {
            String[] parts = comboWord.split(",");
            comboWordToParts.put(comboWord, Arrays.asList(parts));
            for (String part : parts) {
                insert(part);
            }
        }
        buildFailurePointers();
    }

    // 搜索敏感词
    public List<SensitiveWordsResult> search(String text) {
        List<SensitiveWordsResult> results = new LinkedList<>(); // 存储匹配结果的列表
        Map<String, Boolean> partsMatched = new HashMap<>(); // 用于记录组合敏感词各部分的匹配状态
        ACNode current = root; // 从根节点开始搜索

        for (int i = 0; i < text.length(); i++) {
            char l = text.charAt(i); // 获取当前字符

            // 当当前节点没有子节点包含当前字符时，沿失败指针向上移动
            while (current != null && !current.children.containsKey(l)) {
                current = current.fail; // 沿失败指针移动
            }
            // 如果没有找到匹配的节点，重置为根节点并继续下一个字符的匹配
            if (current == null) {
                current = root;
                continue;
            }

            // 移动到下一个匹配的节点
            current = current.children.get(l);

            // 检查当前节点及其所有失败指针链上的节点，看是否有匹配的敏感词
            for (ACNode temp = current; temp != null; temp = temp.fail) {
                // 如果找到一个敏感词的结尾
                if (temp.isEndOfWord) {
                    boolean isPartOfCombo = false; // 标记当前词是否为组合敏感词的一部分

                    // 遍历所有组合敏感词，检查当前词是否为其一部分
                    for (String comboWord : comboWordToParts.keySet()) {
                        if (comboWordToParts.get(comboWord).contains(temp.word)) {
                            isPartOfCombo = true; // 标记为组合敏感词的一部分
                            partsMatched.put(temp.word, true); // 更新匹配状态

                            // 检查是否整个组合敏感词都匹配了
                            if (isComboWordMatched(comboWord, partsMatched)) {
                                results.add(new SensitiveWordsResult(comboWord, 1)); // 添加组合敏感词结果
                                resetComboWordParts(comboWord, partsMatched); // 重置匹配状态
                                break; // 退出循环，因为已匹配完整的组合敏感词
                            }
                        }
                    }

                    // 如果当前词不是任何组合敏感词的一部分，则作为单独敏感词添加
                    if (!isPartOfCombo) {
                        results.add(new SensitiveWordsResult(temp.word, 0)); // 添加单独敏感词结果
                    }
                }
            }
        }

        return results; // 返回匹配结果列表
    }


    // 检查组合敏感词是否全部匹配
    private boolean isComboWordMatched(String comboWord, Map<String, Boolean> partsMatched) {
        for (String part : comboWordToParts.get(comboWord)) {
            if (!partsMatched.getOrDefault(part, false)) {
                return false;
            }
        }
        return true;
    }

    // 重置组合敏感词的匹配状态
    private void resetComboWordParts(String comboWord, Map<String, Boolean> partsMatched) {
        for (String part : comboWordToParts.get(comboWord)) {
            partsMatched.put(part, false);
        }
    }

}


