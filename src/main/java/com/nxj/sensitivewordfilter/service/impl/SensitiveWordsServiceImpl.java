package com.nxj.sensitivewordfilter.service.impl;

import com.nxj.sensitivewordfilter.mapper.SensitiveWordsMapper;
import com.nxj.sensitivewordfilter.model.ACAuto;
import com.nxj.sensitivewordfilter.model.SensitiveWordsResult;
import com.nxj.sensitivewordfilter.model.SensitiveWords;
import com.nxj.sensitivewordfilter.service.SensitiveWordsService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

@Service
public class SensitiveWordsServiceImpl implements SensitiveWordsService {

    @Resource
    private SensitiveWordsMapper sensitiveWordsMapper;
    private ACAuto comboAutomaton;
    private ACAuto singleAutomaton;
    private Set<String> comboWords;

    // 初始化服务，加载敏感词
    @PostConstruct
    public void init() {
        comboAutomaton = new ACAuto(); // 用于组合敏感词的自动机
        singleAutomaton = new ACAuto(); // 用于普通敏感词的自动机
        // 初始化组合敏感词自动机
        List<SensitiveWords> sensitiveWords = sensitiveWordsMapper.selectAll();
        comboWords = new HashSet<>();
        for(SensitiveWords words: sensitiveWords) {
            if(words.getType() == 0) {
                singleAutomaton.insert(words.getWord());
            }else {
                comboWords.add(words.getWord());
            }
        }

        comboAutomaton.initialize(comboWords);
        singleAutomaton.buildFailurePointers();
    }

    // 查找并返回文本中的敏感词，区分普通敏感词和组合敏感词
    public Collection<SensitiveWordsResult> findSensitiveWords(String text) {
        List<SensitiveWordsResult> comboFoundWords = comboAutomaton.search(text);
        List<SensitiveWordsResult> singleFoundWords = singleAutomaton.search(text);

        List<SensitiveWordsResult> allFoundWords = new ArrayList<>();
        allFoundWords.addAll(comboFoundWords);
        allFoundWords.addAll(singleFoundWords);
        for(SensitiveWordsResult sensitiveWordsResult: allFoundWords) {
            System.out.println(sensitiveWordsResult);
        }
        return allFoundWords;
    }

    // 向数据库添加新的敏感词，并更新 Trie
    @Override
    public void addSensitiveWord(SensitiveWords sensitiveWord) {
        sensitiveWordsMapper.insert(sensitiveWord);
        if(sensitiveWord.getType() == 0) {
            singleAutomaton.insert(sensitiveWord.getWord());
            singleAutomaton.buildFailurePointers();
        }else if(sensitiveWord.getType() == 1) {
            comboWords.add(sensitiveWord.getWord());
            comboAutomaton.initialize(comboWords);
        }
    }

}
