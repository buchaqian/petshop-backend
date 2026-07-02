package com.petshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.entity.Commission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommissionMapper extends BaseMapper<Commission> {
}
