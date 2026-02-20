package com.example.circularlist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 循环列表核心实现
 * 使用BRPOPLPUSH实现循环遍历
 @author lk
 @create 2026/02/19-21:07
 */
@Slf4j
@Component
public class CircularList {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Value("${circular-list.list-prefix:circular:list:}")
    private String listPrefix;

    @Value("${circular-list.block-timeout:5}")
    private long blockTimeout;

    /**
     * 获取列表Key
     */
    private String getListKey(String listName) {
        return listPrefix + listName;
    }

    /**
     * 初始化循环列表
     *
     * @param listName 列表名称
     * @param items 初始元素
     * @return 初始化的元素数量
     * @param <T> 类型
     */
    public <T> long initList(String listName, List<T> items) {
        String key = getListKey(listName);

        try {
            //清空现有列表
            redisTemplate.delete(key);

            //添加元素
            for (T item : items) {
                String itemJson = mapper.writeValueAsString(item);
                redisTemplate.opsForList().rightPush(key, itemJson);
            }

            Long size = redisTemplate.opsForList().size(key);

            log.info("循环列表已初始化: listName={}, size={}", listName, size);

            return size != null ? size : 0;

        } catch (Exception e) {
            log.error("初始化循环列表失败: listName={}", listName, e);
            return 0;
        }
    }

    /**
     * 获取下一个元素(循环)
     * 使用BRPOPLPUSH实现循环： 从右边弹出，推入左边
     * @param listName 列表名称
     * @param clazz 元素类型
     * @return 下一个元素
     * @param <T> 类型
     */
    public <T> T getNext(String listName, Class<T> clazz) {
        String key = getListKey(listName);

        try {
            //BRPOPLPUSH: 从右边弹出，推入左边(同一个列表，实现循环)
            Object result = redisTemplate.opsForList().rightPopAndLeftPush(key, key, blockTimeout, TimeUnit.SECONDS);

            if (result == null) {
                log.debug("BRPOPLPUSH超时，列表为空: listName={}", listName);
                return null;
            }

            T item = mapper.readValue(result.toString(), clazz);

            log.debug("循环获取下一个元素: listName={}, item={}", listName, item);

            return item;

        } catch (Exception e) {
            log.error("循环获取元素失败: listName={}", listName, e);
            return null;
        }
    }

    /**
     * 非阻塞获取下一个元素
     * 使用RPOPLPUSH
     * @param listName 列表名称
     * @param clazz 元素类型
     * @return 下一个元素
     * @param <T> 类型
     */
    public <T> T getNextNonBlocking(String listName, Class<T> clazz) {
        String key = getListKey(listName);

        try {
            //RPOPLPUSH:非阻塞版本
            Object result = redisTemplate.opsForList().rightPopAndLeftPush(key, key);

            if (result == null) {
                log.debug("列表为空: listName={}", listName);
                return null;
            }

            T item = mapper.readValue(result.toString(), clazz);

            log.debug("非阻塞获取下一个元素: listName={}, item={}", listName, item);

            return item;

        } catch (Exception e) {
            log.error("非阻塞获取元素失败: listName={}", listName, e);
            return null;
        }

    }

    /**
     * 批量获取元素(循环多次)
     * @param listName 列表名称
     * @param count 获取数量
     * @param clazz 元素类型
     * @return 元素列表
     * @param <T> 类型
     */
    public <T> List<T> getNextBatch(String listName, int count, Class<T> clazz) {
        List<T> items = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            T item = getNextNonBlocking(listName, clazz);
            if (item != null) {
                items.add(item);
            } else {
                break;
            }
        }

        log.info("批量获取元素: listName={}, requested={}, actual={}", listName, count, items.size());

        return items;
    }

    /**
     * 添加元素到循环列表
     */
    public <T> boolean addItem(String listName, T item) {
        String key = getListKey(listName);

        try {
            String itemJson = mapper.writeValueAsString(item);
            Long result = redisTemplate.opsForList().rightPush(key, itemJson);

            log.info("添加元素到循环列表: listName={}, item={}", listName, item);

            return result != null && result > 0;
        } catch (Exception e) {
            log.error("添加元素失败: listName={}", listName, e);
            return false;
        }
    }

    /**
     * 移除指定元素
     */
    public <T> boolean removeItem(String listName, T item) {
        String key = getListKey(listName);

        try {
            String itemJson = mapper.writeValueAsString(item);
            Long removed = redisTemplate.opsForList().remove(key, 1, itemJson);

            log.info("从循环列表移除元素: listName={}, item={}, removed={}", listName, item, removed);

            return removed != null && removed > 0;

        } catch (Exception e) {
            log.error("移除元素失败: listName={}", listName, e);
            return false;
        }
    }

    /**
     * 获取列表大小
     */
    public long getSize(String listName) {
        String key = getListKey(listName);
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }

    /**
     * 查看列表所有元素(不改变列表)
     */
    public <T> List<T> viewAll(String listName, Class<T> clazz) {
        String key = getListKey(listName);
        List<T> items = new ArrayList<>();

        try {
            List<Object> rawItems = redisTemplate.opsForList().range(key, 0, -1);

            if (rawItems != null) {
                for (Object rawItem : rawItems) {
                    T item = mapper.readValue(rawItem.toString(), clazz);
                    items.add(item);
                }
            }

        } catch (Exception e) {
            log.error("查看列表失败: listName={}", listName, e);
        }
        return items;
    }

    /**
     * 清空列表
     */
    public boolean clear(String listName) {
        String key = getListKey(listName);
        Boolean result = redisTemplate.delete(key);

        log.info("清空循环列表: listName={}", listName);

        return result != null && result;
    }
}
