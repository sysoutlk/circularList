package com.example.circularlist.controller;

import com.example.circularlist.model.AdItem;
import com.example.circularlist.model.Task;
import com.example.circularlist.model.WorkerNode;
import com.example.circularlist.service.AdRotationService;
import com.example.circularlist.service.CircularList;
import com.example.circularlist.service.LoadBalancerService;
import com.example.circularlist.service.ResourcePoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 循环列表Controller
 @author lk
 @create 2026/02/19-21:08
 */
@Slf4j
@RestController
@RequestMapping("/api/circular")
public class CircularController {

    @Autowired
    private CircularList circularList;

    @Autowired
    private AdRotationService adRotationService;

    @Autowired
    private LoadBalancerService loadBalancerService;

    @Autowired
    private ResourcePoolService resourcePoolService;

    //负载均衡相关API

    /**
     * 初始化工作节点
     * @param count
     * @return
     */
    @PostMapping("/workers/init")
    public ResponseEntity<Map<String, Object>> initWorkers(@RequestParam(defaultValue = "5") int count) {
        List<WorkerNode> workers = loadBalancerService.createSampleWorkers(count);
        loadBalancerService.initWorkers(workers);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "工作节点已初始化");
        result.put("workerCount", count);
        result.put("workers", workers);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取下一个工作节点
     * @return
     */
    @GetMapping("/workers/next")
    public ResponseEntity<WorkerNode> getNextWorker() {
        WorkerNode worker = loadBalancerService.getNextWorker();
        return ResponseEntity.ok(worker);
    }

    /**
     * 分配任务
     * @param count
     * @return
     */
    @PostMapping("/tasks/assign")
    public ResponseEntity<Map<String, Object>> assignTask(@RequestParam(defaultValue = "10") int count) {
        List<Task> tasks = loadBalancerService.createSampleTasks(count);
        List<Task> assignedTasks = loadBalancerService.assignTasks(tasks);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "任务已分配");
        result.put("totalTasks", count);
        result.put("assignedTasks", assignedTasks.size());
        result.put("tasks", assignedTasks);
        return ResponseEntity.ok(result);
    }

    /**
     * 查看所有工作节点
     * @return
     */
    @GetMapping("/workers/all")
    public ResponseEntity<List<WorkerNode>> getAllWorkers() {
        List<WorkerNode> workers = loadBalancerService.getAllWorkers();
        return ResponseEntity.ok(workers);
    }


    //广告轮播相关API

    /**
     * 初始化广告列表
     * @param count
     * @return
     */
    @PostMapping("/ads/init")
    public ResponseEntity<Map<String, Object>> initAds(@RequestParam(defaultValue = "5") int count) {
        List<AdItem> ads = adRotationService.createSampleAds(count);
        adRotationService.initAds(ads);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "广告列表已初始化");
        result.put("adCount", count);
        result.put("ads", ads);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取下一个广告
     * @return
     */
    @GetMapping("/ads/next")
    public ResponseEntity<AdItem> getNextId() {
        AdItem ad = adRotationService.getNextAd();
        return ResponseEntity.ok(ad);
    }

    /**
     * 批量获取广告
     * @return
     */
    @GetMapping("/ads/batch")
    public ResponseEntity<List<AdItem>> getNextAds(@RequestParam(defaultValue = "5") int count) {
        List<AdItem> ads = adRotationService.getNextAds(count);
        return ResponseEntity.ok(ads);
    }

    /**
     * 记录点击广告
     * @param adId
     * @return
     */
    @PostMapping("/ads/click")
    public ResponseEntity<Map<String, Object>> recordClick(@RequestParam String adId) {
        adRotationService.recordClick(adId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "点击已记录");
        result.put("adId", adId);
        return ResponseEntity.ok(result);
    }

    /**
     * 查看所有广告
     * @return
     */
    @GetMapping("/ads/all")
    public ResponseEntity<List<AdItem>> getAllAds() {
        List<AdItem> ads = adRotationService.getAllAds();
        return ResponseEntity.ok(ads);
    }


    //资源池相关API

    /**
     * 初始化资源池
     * @param poolName
     * @param size
     * @param resourceType
     * @return
     */
    @PostMapping("/pool/init")
    public ResponseEntity<Map<String, Object>> initResourcePool(@RequestParam String poolName, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "connection") String resourceType) {
        resourcePoolService.initResourcePool(poolName, size, resourceType);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "资源池已初始化");
        result.put("poolName", poolName);
        result.put("size", size);
        result.put("resourceType", resourceType);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取资源
     * @param poolName
     * @return
     */
    @GetMapping("/pool/acquire")
    public ResponseEntity<ResourcePoolService.Resource> acquireResource(@RequestParam String poolName) {
        ResourcePoolService.Resource resource = resourcePoolService.acquireResource(poolName);
        return ResponseEntity.ok(resource);
    }

    /**
     * 查看资源池状态
     * @param poolName
     * @return
     */
    @GetMapping("/pool/status")
    public ResponseEntity<Map<String, Object>> getPoolStatus(@RequestParam String poolName) {
        List<ResourcePoolService.Resource> resources = resourcePoolService.getPoolStatus(poolName);
        long available = resources.stream()
                .filter(r -> "available".equals(r.getStatus()))
                .count();

        Map<String, Object> status = new HashMap<>();
        status.put("poolName", poolName);
        status.put("totalSize", resources.size());
        status.put("available", available);
        status.put("inUse", resources.size() - available);
        status.put("resources", resources);

        return ResponseEntity.ok(status);
    }


    //通用循环列表API

    /**
     * 查看列表大小
     * @param listName
     * @return
     */
    @GetMapping("/list/size")
    public ResponseEntity<Map<String, Object>> getListSize(@RequestParam String listName) {
        long size = circularList.getSize(listName);

        Map<String, Object> result = new HashMap<>();
        result.put("listName", listName);
        result.put("size", size);
        return ResponseEntity.ok(result);
    }

    /**
     * 清空列表
     * @param listName
     * @return
     */
    @DeleteMapping("/list/clear")
    public ResponseEntity<Map<String, Object>> clearList(@RequestParam String listName) {
        boolean success = circularList.clear(listName);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("listName", listName);
        result.put("message", success ? "列表已清空" : "清空失败");
        return ResponseEntity.ok(result);
    }
}
