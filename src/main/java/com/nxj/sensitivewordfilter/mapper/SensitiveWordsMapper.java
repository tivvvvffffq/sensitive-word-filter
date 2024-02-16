package com.nxj.sensitivewordfilter.mapper;

import com.nxj.sensitivewordfilter.model.SensitiveWords;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SensitiveWordsMapper {
     int insert(SensitiveWords sensitiveWords);

     int delete(Integer id);

     SensitiveWords selectById(Integer id);

     List<SensitiveWords> selectAll();
}
