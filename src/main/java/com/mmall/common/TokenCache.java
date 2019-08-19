package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TokenCache {
    private static Logger logger= LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";
    //本地缓存    LUR算法  初始化最大值10000  如果超过10000就是用LRU算法进行清除  有效期12个小时
    private static LoadingCache<String,String> localCache= CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现，当调用get取值的时候，如果key没有对应的值，就调用这个方法进行加载
                @Override
                public String load(String key) throws Exception {
                    return "null";
                }
            });
    public static void setKey(String key,String value){
        localCache.put(key,value);
    }
    public static String getKey(String key){
        String value = null;
        try {
            value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("localCache get error!!",e);
        }
        return null;
    }
}