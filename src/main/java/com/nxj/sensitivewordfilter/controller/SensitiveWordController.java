package com.nxj.sensitivewordfilter.controller;

import com.nxj.sensitivewordfilter.service.SensitiveWordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ahocorasick.trie.Emit;
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
public class SensitiveWordController {

    @Resource
    private SensitiveWordService sensitiveWordService;

    @PostMapping("/match")
    public ResponseEntity<?> matchSensitiveWords(@RequestBody String text) {
        Collection<Emit> tokens = sensitiveWordService.findSensitiveWords(text);
        return ResponseEntity.ok(tokens);
    }
}
