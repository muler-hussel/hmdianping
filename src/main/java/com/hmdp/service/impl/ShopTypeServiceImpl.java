package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //低一致性需求，内存淘汰即可。
    @Override
    public Result queryList() {
        //1.从redis查询数据
        String key = "CACHE_SHOPTYPE_KEY";
        String shopTypeListJson = stringRedisTemplate.opsForValue().get(key);
        //2.缓存中存在,直接返回数据
        if(StrUtil.isNotBlank(shopTypeListJson)){
            //json对象转换成list对象
            List<ShopType> list = JSONObject.parseArray(shopTypeListJson, ShopType.class);
            return Result.ok(list);
        }
        //3.缓存中不存在，在数据库中进行查询
        List<ShopType> list = this.query().orderByAsc("sort").list();
        String typeListJson = JSONUtil.toJsonStr(list);
        //4.不存在,返回错误信息
        if (typeListJson==null){
            return Result.fail("店铺列表不存在!");
        }
        //5.存在，将数据存到redis中
        stringRedisTemplate.opsForValue().set(key,typeListJson);
        return Result.ok(list);
    }
}
