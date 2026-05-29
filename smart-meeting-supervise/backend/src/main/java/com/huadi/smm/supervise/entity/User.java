package com.huadi.smm.supervise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sm_supervise_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String account;

    private String password;

    private String name;

    private Integer role;

    private String dept;

    private String phone;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
