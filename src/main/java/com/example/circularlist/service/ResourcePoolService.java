package com.example.circularlist.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源池服务
 * 使用循环列表管理连接池、线程池等资源
 @author lk
 @create 2026/02/19-21:08
 */
@Slf4j
@Service
public class ResourcePoolService {

    @Autowired
    private CircularList circularList;

    /**
     * 资源对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Resource {
        private String resourceId;
        private String resourceType;
        //available, in-use
        private String status;
        private LocalDateTime lastUsedTime;
        private Long usageCount;
    }

    /**
     * 初始化资源池
     */
    public void initResourcePool(String poolName, int poolSize, String resourceType) {
        List<Resource> resources = new ArrayList<>();

        for (int i = 1; i <= poolSize; i++) {
            Resource resource = Resource.builder()
                    .resourceId(resourceType + "-" + i)
                    .resourceType(resourceType)
                    .status("available")
                    .lastUsedTime(LocalDateTime.now())
                    .usageCount(0L)
                    .build();

            resources.add(resource);
        }

        circularList.initList(poolName, resources);
        log.info("资源池已初始化: poolName={}, size={}, type={}", poolName, poolSize, resourceType);
    }

    /**
     * 获取资源(循环分配)
     */
    public Resource acquireResource(String poolName) {
        Resource resource = circularList.getNext(poolName, Resource.class);

        if (resource != null) {
            resource.setStatus("in-use");
            resource.setLastUsedTime(LocalDateTime.now());
            resource.setUsageCount(resource.getUsageCount() + 1);

            log.info("获取资源: poolName={}, resourceId={}, usageCount={}", poolName, resource.getResourceId(), resource.getUsageCount());
        }

        return resource;
    }

    /**
     * 归还资源
     */
    public void releaseResource(Resource resource) {
        resource.setStatus("available");
        log.info("归还资源: resourceId={}", resource.getResourceId());
    }

    /**
     * 查看资源池状态
     */
    public List<Resource> getPoolStatus(String poolName) {
        return circularList.viewAll(poolName, Resource.class);
    }
}
