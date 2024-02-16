package com.nxj.sensitivewordfilter.controller;

import com.nxj.sensitivewordfilter.model.SensitiveWordsResult;
import com.nxj.sensitivewordfilter.service.SensitiveWordsService;
import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;

@Api
@RestController
@RequestMapping("/api/sensitive")
public class SensitiveWordsController {

    @Resource
    private SensitiveWordsService sensitiveWordsService;

    @PostMapping("/match")
    public ResponseEntity<?> matchSensitiveWords(@RequestBody String text) {
        Collection<SensitiveWordsResult> tokens = sensitiveWordsService.findSensitiveWords(text);
        return ResponseEntity.ok(tokens);
    }
}
