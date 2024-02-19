package co.zhanglintc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        JedisPooled jedis = new JedisPooled("192.168.33.10",6379);
        String k = "k";
        for (int i = 0; i < 10; i++) {
            jedis.lpush(k, String.valueOf(i));
        }
        for (int i = 0; i < 30; i++) {
            jedis.rpoplpush(k, "v");
            Thread.sleep(1000);
        }
        jedis.flushDB();
        jedis.close();
    }
}