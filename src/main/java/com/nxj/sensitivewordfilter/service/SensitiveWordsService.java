package com.nxj.sensitivewordfilter.service;

import com.nxj.sensitivewordfilter.model.SensitiveWordsResult;
import com.nxj.sensitivewordfilter.model.SensitiveWords;

import java.util.Collection;

public interface SensitiveWordsService {

    Collection<SensitiveWordsResult> findSensitiveWords(String text);
    void addSensitiveWord(SensitiveWords sensitiveWords);

}
