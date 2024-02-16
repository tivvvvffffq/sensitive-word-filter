package com.nxj.sensitivewordfilter.service.impl;

import com.nxj.sensitivewordfilter.mapper.SensitiveWordsMapper;
import com.nxj.sensitivewordfilter.model.SensitiveWordsResult;
import com.nxj.sensitivewordfilter.model.SensitiveWords;
import com.nxj.sensitivewordfilter.service.SensitiveWordsService;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

@Service
public class SensitiveWordsServiceImpl implements SensitiveWordsService {

    @Resource
    private SensitiveWordsMapper sensitiveWordsMapper;

    private Trie trie;
    private Set<String> normalSensitiveWords; // 存储普通敏感词
    private Set<String> combinedSensitiveWords; // 存储组合敏感词
    private Map<String, String> combinedWordParts; // 存储组合敏感词的各部分

    // 初始化服务，加载敏感词并构建 Trie
    @PostConstruct
    public void init() {
        // 初始化存储普通敏感词和组合敏感词的集合
        normalSensitiveWords = new HashSet<>();
        combinedSensitiveWords = new HashSet<>();
        // 初始化存储组合敏感词各部分与完整词的映射关系的哈希表
        combinedWordParts = new HashMap<>();
        // 从数据库中获取所有敏感词
        List<SensitiveWords> sensitiveWords = sensitiveWordsMapper.selectAll();

        // 创建 Trie 结构的构建器
        Trie.TrieBuilder builder = Trie.builder();
        for (SensitiveWords word : sensitiveWords) {
            if (word.getType() == 0) { // 处理普通敏感词
                // 将普通敏感词添加到 Trie 构建器和普通敏感词集合中
                builder.addKeyword(word.getWord());
                normalSensitiveWords.add(word.getWord());
            } else if (word.getType() == 1) { // 处理组合敏感词
                // 分割组合敏感词为单独的部分
                String[] parts = word.getWord().split(",");
                for (String part : parts) {
                    // 将每个部分添加到 Trie 构建器中
                    builder.addKeyword(part.trim());
                    // 记录组合敏感词及其组成部分
                    combinedSensitiveWords.add(word.getWord());
                    combinedWordParts.put(part.trim(), word.getWord());
                }
            }
        }
        // 构建 Trie 结构
        this.trie = builder.build();
    }

    // 重新构建 Trie
    private void rebuildTrie() {
        Trie.TrieBuilder builder = Trie.builder();
        for (String word : normalSensitiveWords) {
            builder.addKeyword(word);
        }
        for (String word : combinedSensitiveWords) {
            builder.addKeyword(word);
        }
        this.trie = builder.build();
    }

    // 查找并返回文本中的敏感词，区分普通敏感词和组合敏感词
    public Collection<SensitiveWordsResult> findSensitiveWords(String text) {
        Collection<Emit> emits = trie.parseText(text);
        List<SensitiveWordsResult> results = new ArrayList<>();
        Set<String> addedCombinedWords = new HashSet<>(); // 用于存储已添加的组合敏感词

        // 遍历所有找到的敏感词
        for (Emit emit : emits) {
            // 获取匹配到的敏感词文本
            String foundWord = emit.getKeyword();

            // 如果找到的词是组合敏感词的一部分
            if (combinedWordParts.containsKey(foundWord)) {
                // 获取组合敏感词的原始形式
                String originalCombinedWord = combinedWordParts.get(foundWord);
                // 检查是否已经添加了这个组合敏感词
                if (!addedCombinedWords.contains(originalCombinedWord)) {
                    // 添加到结果中，并标记为已添加
                    results.add(new SensitiveWordsResult(originalCombinedWord, 1));
                    addedCombinedWords.add(originalCombinedWord);
                }
            } else {
                // 如果不是组合敏感词的一部分，类型设置为 0（普通敏感词）
                results.add(new SensitiveWordsResult(foundWord, 0));
            }
        }

        // 返回检测到的所有敏感词及其类型
        return results;
    }

    // 向数据库添加新的敏感词，并更新 Trie
    @Override
    public void addSensitiveWord(SensitiveWords sensitiveWord) {
        sensitiveWordsMapper.insert(sensitiveWord);
        if (sensitiveWord.getType() == 0) {
            normalSensitiveWords.add(sensitiveWord.getWord());
        } else if (sensitiveWord.getType() == 1) {
            String[] parts = sensitiveWord.getWord().split(",");
            for (String part : parts) {
                combinedSensitiveWords.add(sensitiveWord.getWord());
                combinedWordParts.put(part.trim(), sensitiveWord.getWord());
            }
        }
        rebuildTrie();
    }

    // 从数据库中删除敏感词，并更新 Trie
    @Override
    public void deleteSensitiveWord(SensitiveWords sensitiveWord) {
        sensitiveWordsMapper.delete(sensitiveWord.getId());
        if (sensitiveWord.getType() == 0) {
            normalSensitiveWords.remove(sensitiveWord.getWord());
        } else if (sensitiveWord.getType() == 1) {
            String[] parts = sensitiveWord.getWord().split(",");
            for (String part : parts) {
                combinedSensitiveWords.remove(sensitiveWord.getWord());
                combinedWordParts.remove(part.trim());
            }
        }
        rebuildTrie();
    }
}
