package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import io.lettuce.core.ScriptOutputType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
@RunWith(SpringRunner.class)
public class HmDianPingApplicationTests {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    IShopService shopService;

    @Test
    public void testNowSecond(){
        LocalDateTime time = LocalDateTime.of(2023,1 , 1,0,0,0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println(second);
    }

    @Test
    public void testRedisson() throws Exception{
    //获取锁（可重入）,指定锁的名称
        RLock lock = redissonClient.getLock("anylock");
        //尝试获取锁，参数分别是：获取锁的最大等待时间（期间会重试）,锁自动释放时间，时间单位
        boolean isLock = lock.tryLock(1,10, TimeUnit.SECONDS);
        //判断是否获取锁成功
        if(isLock){
            try {
                System.out.println("执行业务");
            }
            finally {
                //释放锁
                lock.unlock();
            }
        }
    }
    /**
     * UV统计，测试百万数据的统计
     */
    @Test
    public void testHyperLogLog(){
        //准备数组，装用户数据
        String[] users = new String[1000];
        int index = 0;
        for(int i = 1; i <= 1000000; i++){
            //赋值
            users[index++] = "user" + i;
            //每1000条发送一次
            if(i % 1000 == 0){
                index = 0;
                stringRedisTemplate.opsForHyperLogLog().add("hull1", users);
            }
        }
        //统计数量
        Long size = stringRedisTemplate.opsForHyperLogLog().size("hull1");
        System.out.println("size= " + size);
    }

    @Test
    public void loadShopData() {
        //查询店铺信息
        List<Shop> list = shopService.list();
        //店铺分组，按typeid分
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        //分批完成存储redis
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            //获取类型id
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY + typeId;
            //获取同类型店铺集合
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            //写入redis geoadd key 经度 纬度 member
            for (Shop shop : value) {
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),
                        new Point(shop.getX(), shop.getY())));
                //stringRedisTemplate.opsForGeo().add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString());
            }
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }
}
