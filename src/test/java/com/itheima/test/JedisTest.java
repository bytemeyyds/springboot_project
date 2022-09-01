package com.itheima.test;


import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

/**
 * 使用Jedis操作Redis
 */
public class JedisTest {
    @Test
    public void testRedis(){
        //1  获取连接
        Jedis jedis = new Jedis("localhost", 6379);
        //2  执行具体的操作
        jedis.set("username","xiaoming");
        System.out.println(jedis.get("username"));

        jedis.del("username");

        jedis.hset("myhash","addr","beijing");
        System.out.println(jedis.hget("myhash", "addr"));

        System.out.println(jedis.keys("*"));

        //3  关闭连接
        jedis.close();
    }
}
