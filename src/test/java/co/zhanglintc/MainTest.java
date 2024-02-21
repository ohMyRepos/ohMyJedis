package co.zhanglintc;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.List;

public class MainTest {

    private static Jedis jedis;
    private static final String REDIS_HOST = "192.168.56.101";

    @BeforeClass
    public static void beforeClass() {
        jedis = new Jedis(REDIS_HOST, 6379);
        jedis.flushDB();
    }

    @AfterClass
    public static void afterClass() {
        jedis.close();
    }

    @Test
    public void testString() throws InterruptedException {
        String kInt = "kInt";
        jedis.set(kInt, "1");
        jedis.incr(kInt);
        Assert.assertEquals("2", jedis.get(kInt));
        jedis.decr(kInt);
        Assert.assertEquals("1", jedis.get(kInt));

        String kStr = "kStr";
        jedis.set(kStr, "string");
        Assert.assertThrows(
                redis.clients.jedis.exceptions.JedisDataException.class,
                () -> jedis.incr(kStr)
        );

        String kExist = "kExist";
        jedis.setex(kExist, 10, "1");
        Assert.assertEquals("1", jedis.get(kExist));
        jedis.setex(kExist, 1, "2");
        Assert.assertEquals("2", jedis.get(kExist));
        Thread.sleep(1111);
        Assert.assertEquals(null, jedis.get(kExist));

        String kNotExist = "kNotExist";
        long ok1 = jedis.setnx(kNotExist, "1");
        Assert.assertEquals(1, ok1);
        Assert.assertEquals("1", jedis.get(kNotExist));
        long ok2 = jedis.setnx(kNotExist, "2");
        Assert.assertEquals(0, ok2);
        Assert.assertEquals("1", jedis.get(kNotExist));
        jedis.expire(kNotExist, 1);
        Assert.assertEquals("1", jedis.get(kNotExist));
        Thread.sleep(1111);
        Assert.assertEquals(null, jedis.get(kNotExist));

        String[][] mSet = {
                {"k1", "k111"},
                {"k2", "k222"},
                {"k3", "k333"}
        };
        jedis.mset(
                mSet[0][0], mSet[0][1],
                mSet[1][0], mSet[1][1],
                mSet[2][0], mSet[2][1]
        );
        List<String> mGet = jedis.mget(mSet[0][0], mSet[1][0], mSet[2][0]);
        Assert.assertEquals(mSet.length, mGet.size());
        for (int i = 0; i < mSet.length; i++) {
            Assert.assertEquals(mSet[i][1], mGet.get(i));
        }

        String kGetSet = "kGetSet";
        String kGetSetV =jedis.getSet(kGetSet, "1");
        Assert.assertEquals(null, kGetSetV);
        kGetSetV = jedis.get(kGetSet);
        Assert.assertEquals("1", kGetSetV);
    }
}
