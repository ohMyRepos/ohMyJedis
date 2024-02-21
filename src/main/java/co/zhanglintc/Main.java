package co.zhanglintc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;

public class Main {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);
        JedisPooled jedis = new JedisPooled("192.168.33.10",6379);
        jedis.flushDB();
        jedis.close();
    }
}