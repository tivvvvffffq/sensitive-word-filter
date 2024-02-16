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
    private Map<String, Set<String>> partToOriginalWordsMap; // 存储敏感词各部分到原始组合敏感词的映射
    private Map<String, Set<String>> originalWordToPartsMap; // 存储原始组合敏感词到其组成部分的映射
    private Set<String> normalSensitiveWords; // 存储普通敏感词
    private Set<String> combinedSensitiveWords; // 存储原本的组合敏感词

    // 初始化服务，加载敏感词并构建 Trie
    @PostConstruct
    public void init() {
        // 初始化存储普通敏感词和组合敏感词的集合
        normalSensitiveWords = new HashSet<>();
        combinedSensitiveWords = new HashSet<>();
        // 初始化存储组合敏感词各部分与完整词的映射关系的哈希表
        partToOriginalWordsMap = new HashMap<>();
        originalWordToPartsMap = new HashMap<>();

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
                combinedSensitiveWords.add(word.getWord());
                // 分割组合敏感词为单独的部分
                String[] parts = word.getWord().split(",");
                for (String part : parts) {
                    // 将每个部分添加到 Trie 构建器中
                    part = part.trim();
                    builder.addKeyword(part);
                    partToOriginalWordsMap.computeIfAbsent(part, k -> new HashSet<>()).add(word.getWord());
                    originalWordToPartsMap.computeIfAbsent(word.getWord(), k -> new HashSet<>()).add(part);
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
        // 用于临时存储和检查组合敏感词的部分是否全部出现
        Map<String, Set<String>> tempOriginalWordToPartsMap = new HashMap<>();
        for (Emit emit : emits) {
            String foundWord = emit.getKeyword();
            if (normalSensitiveWords.contains(foundWord)) {
                results.add(new SensitiveWordsResult(foundWord, 0));
            } else if (partToOriginalWordsMap.containsKey(foundWord.trim())) {
                foundWord = foundWord.trim();
                Set<String> originSet = partToOriginalWordsMap.get(foundWord);
                for (String originalWord : originSet) {
                    // 深拷贝部分，确保不直接修改原始的映射
                    tempOriginalWordToPartsMap.computeIfAbsent(originalWord, k -> new HashSet<>(originalWordToPartsMap.get(k))).remove(foundWord);
                    // 检查是否所有部分都已经匹配
                    if (tempOriginalWordToPartsMap.get(originalWord).isEmpty()) {
                        results.add(new SensitiveWordsResult(originalWord, 1));
                        // 移除已经匹配的组合敏感词，防止重复添加
                        tempOriginalWordToPartsMap.remove(originalWord);
                    }
                }
            }
        }
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
            combinedSensitiveWords.add(sensitiveWord.getWord());
            for (String part : parts) {
                part = part.trim();
                // 更新 part 到原始组合词的映射
                partToOriginalWordsMap.computeIfAbsent(part, k -> new HashSet<>()).add(sensitiveWord.getWord());
                // 更新原始组合词到其部分的映射
                originalWordToPartsMap.computeIfAbsent(sensitiveWord.getWord(), k -> new HashSet<>()).add(part);
            }
        }
        rebuildTrie();
    }

    // 从数据库中删除敏感词，并更新 Trie
    @Override
    public void deleteSensitiveWord(SensitiveWords sensitiveWord) {
        sensitiveWordsMapper.delete(sensitiveWord.getId());
        if (sensitiveWord.getType() == 0) {
            // 从普通敏感词集合中移除
            normalSensitiveWords.remove(sensitiveWord.getWord());
        } else if (sensitiveWord.getType() == 1) {
            // 从组合敏感词集合中移除
            combinedSensitiveWords.remove(sensitiveWord.getWord());
            String[] parts = sensitiveWord.getWord().split(",");
            for (String part : parts) {
                part = part.trim();
                // 从部分到原始组合词的映射中移除相关条目
                Set<String> originalWords = partToOriginalWordsMap.get(part);
                if (originalWords != null) {
                    originalWords.remove(sensitiveWord.getWord());
                    // 如果某个部分不再映射到任何组合敏感词，可以考虑从映射中完全移除这个部分
                    if (originalWords.isEmpty()) {
                        partToOriginalWordsMap.remove(part);
                    }
                }
            }
            // 从原始组合词到其部分的映射中移除条目
            originalWordToPartsMap.remove(sensitiveWord.getWord());
        }
        rebuildTrie();
    }
}
