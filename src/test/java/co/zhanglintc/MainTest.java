package co.zhanglintc;

import org.junit.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.args.ListPosition;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainTest {

    private static Jedis jedis;
    private static final String REDIS_HOST = "192.168.56.101";

    @BeforeClass
    public static void beforeClass() {
        jedis = new Jedis(REDIS_HOST, 6379);
    }

    @AfterClass
    public static void afterClass() {
        jedis.close();
    }

    @Before
    public void before() {
        jedis.flushDB();
    }

    @Test
    public void testString() throws InterruptedException {
        Set<String> keys = jedis.keys("*");
        Assert.assertEquals(0, keys.size());

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
        String kGetSetV = jedis.getSet(kGetSet, "1");
        Assert.assertEquals(null, kGetSetV);
        kGetSetV = jedis.get(kGetSet);
        Assert.assertEquals("1", kGetSetV);
    }

    @Test
    public void testList() {
        jedis.rpush("list", "1", "2", "3");
        long listLen = jedis.llen("list");
        Assert.assertEquals(3, listLen);

        jedis.rpush("list", "4", "5", "6");
        List<String> listAll = jedis.lrange("list", 0, -1);
        Assert.assertEquals(6, listAll.size());
        for (int i = 0; i < listAll.size(); i++) {
            Assert.assertEquals(String.valueOf(i + 1), listAll.get(i));
        }

        String byIndex = jedis.lindex("list", 0);
        Assert.assertEquals("1", byIndex);

        jedis.lpush("list", "0", "0", "0", "0", "0");
        Assert.assertEquals(11, jedis.llen("list"));
        jedis.lrem("list", 1, "0");
        Assert.assertEquals(10, jedis.llen("list"));
        jedis.lrem("list", 0, "0");
        Assert.assertEquals(6, jedis.llen("list"));

        jedis.ltrim("list", 0, 2);
        Assert.assertEquals(3, jedis.llen("list"));
        Assert.assertEquals("1", jedis.lindex("list", 0));
        Assert.assertEquals("3", jedis.lindex("list", -1));

        jedis.lset("list", 0, "0");
        Assert.assertEquals("0", jedis.lindex("list", 0));
        Assert.assertThrows(
                redis.clients.jedis.exceptions.JedisDataException.class,
                () -> jedis.lset("list-1", 0, "0")
        );

        jedis.linsert("list", ListPosition.BEFORE, "0", "999");
        Assert.assertEquals("999", jedis.lindex("list", 0));
    }

    @Test
    public void testSet() {
        jedis.sadd("set", "1", "2", "3");
        Assert.assertEquals(3, jedis.scard("set"));

        Set<String> setMembers = jedis.smembers("set");
        Assert.assertEquals("[1, 2, 3]", setMembers.toString());

        jedis.sadd("set", "hello");
        setMembers = jedis.smembers("set");
        Assert.assertTrue(setMembers.contains("hello"));
        jedis.srem("set", "hello");
        setMembers = jedis.smembers("set");
        Assert.assertFalse(setMembers.contains("hello"));

        List<String> randomMembers = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            List<String> list_len_2 = jedis.srandmember("set", 3);
            for (String s : list_len_2) {
                randomMembers.add(s);
            }
        }
        Assert.assertEquals(30, randomMembers.size());

        Assert.assertNotNull(jedis.spop("set"));
        Assert.assertNotNull(jedis.spop("set"));
        Assert.assertNotNull(jedis.spop("set"));
        Assert.assertNull(jedis.spop("set"));

        jedis.sadd("set1", "1", "2", "3");
        jedis.sadd("set2", "3", "4", "5");

        Assert.assertEquals("[1, 2]", jedis.sdiff("set1", "set2").toString());
        Assert.assertEquals("[4, 5]", jedis.sdiff("set2", "set1").toString());
        Assert.assertEquals("[3]", jedis.sinter("set1", "set2").toString());
        Assert.assertEquals("[3]", jedis.sinter("set2", "set1").toString());
        Assert.assertEquals("[1, 2, 3, 4, 5]", jedis.sunion("set1", "set2").toString());
        Assert.assertEquals("[1, 2, 3, 4, 5]", jedis.sunion("set2", "set1").toString());

        jedis.sadd("set-set", "1", "2", "3", "4", "5");
        jedis.sadd("set-set", "4", "5", "6", "7", "8");
        Assert.assertEquals("[1, 2, 3, 4, 5, 6, 7, 8]", jedis.sunion("set-set").toString());
    }
}
