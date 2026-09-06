package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.DiscussionMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DiscussionMessageMapper extends BaseMapper<DiscussionMessage> {
}
