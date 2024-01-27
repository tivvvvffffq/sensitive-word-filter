package com.nxj.sensitivewordfilter.service;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class SensitiveWordService {
    private Trie trie;

    public SensitiveWordService() {
        //初始化Trie树，从数据库或文件中加载敏感词
        this.trie = Trie.builder()
                .addKeywords("赌场", "赌博", "敏感词")
                .build();
    }

    public Collection<Emit> findSensitiveWords(String text) {
        return trie.parseText(text);
    }
}
