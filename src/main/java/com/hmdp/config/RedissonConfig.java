package com.hmdp.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        //配置
        Config config = new Config();
        //添加单点地址。集群地址用useClusterServers()
        config.useSingleServer().setAddress("redis://localhost:6379");
        //创建RedissonClient对象
        return Redisson.create(config);
    }
    //三个独立锁，解决主从一致问题
    public RedissonClient redissonClient2(){
        //配置
        Config config = new Config();
        //添加单点地址。集群地址用useClusterServers()
        config.useSingleServer().setAddress("redis://localhost:6380");
        //创建RedissonClient对象
        return Redisson.create(config);
    }
    public RedissonClient redissonClient3(){
        //配置
        Config config = new Config();
        //添加单点地址。集群地址用useClusterServers()
        config.useSingleServer().setAddress("redis://localhost:6381");
        //创建RedissonClient对象
        return Redisson.create(config);
    }
}
