package com.nxj.sensitivewordfilter.model;

/**
 * 查询结果
 */
public class SensitiveWordsResult {
    private String word;
    private int type; // 0: 普通敏感词, 1: 组合敏感词

    public SensitiveWordsResult(String word, int type) {
        this.word = word;
        this.type = type;
    }

    @Override
    public String toString() {
        return "SensitiveWordsResult{" +
                "word='" + word + '\'' +
                ", type=" + type +
                '}';
    }

    public String getWord() {
            return word;
        }

    public void setWord(String word) {
            this.word = word;
        }

    public int getType() {
            return type;
        }

    public void setType(int type) {
        this.type = type;
    }
}