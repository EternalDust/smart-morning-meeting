package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bi_dim_dict")
public class BiDimDict {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictCode;
    private String dictName;
    private String dictType;
    private Integer sort;
    private Integer status;
    private String createTime;
    private String updateTime;
    private String profile;
}
