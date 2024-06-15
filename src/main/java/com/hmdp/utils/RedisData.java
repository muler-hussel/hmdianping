package com.hmdp.utils;

import lombok.Data;

import java.time.LocalDateTime;

//设置逻辑过期时间。为了避免直接更改shop/shopdto。data即需要存储的数据
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
