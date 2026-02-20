package com.example.circularlist.service;

import com.example.circularlist.model.Task;
import com.example.circularlist.model.WorkerNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡服务
 * 使用循环列表实现Round-Robin负载均衡
 @author lk
 @create 2026/02/19-21:08
 */
@Slf4j
@Service
public class LoadBalancerService {

    @Autowired
    private CircularList circularList;

    private static final String WORKER_LIST = "workers";

    /**
     * 初始化工作节点池
     */
    public void initWorkers(List<WorkerNode> workers) {
        circularList.initList(WORKER_LIST, workers);
        log.info("工作节点池已初始化: count={}", workers.size());
    }

    /**
     * 获取下一个工作节点
     */
    public WorkerNode getNextWorker() {
        WorkerNode worker = circularList.getNext(WORKER_LIST, WorkerNode.class);

        if (worker != null) {
            //更新最后使用时间
            worker.setLastUsedTime(LocalDateTime.now());
            log.info("分配工作节点: nodeId={}, nodeName={}", worker.getNodeId(), worker.getNodeName());
        }

        return worker;
    }

    /**
     * 分配任务到工作节点
     */
    public Task assignTask(Task task) {
        WorkerNode worker = getNextWorker();

        if (worker == null) {
            log.error("没有可用的工作节点");
            return null;
        }

        task.setAssignedNode(worker.getNodeId());

        log.info("任务已分配: taskId={}, assignedNode={}", task.getTaskId(), task.getAssignedNode());

        return task;
    }

    /**
     * 批量分配任务
     */
    public List<Task> assignTasks(List<Task> tasks) {
        List<Task> assignedTasks = new ArrayList<>();

        for (Task task : tasks) {
            Task assigned = assignTask(task);
            if (assigned != null) {
                assignedTasks.add(assigned);
            }
        }

        log.info("批量任务分配完成: total={}, assigned={}", tasks.size(), assignedTasks.size());

        return assignedTasks;
    }

    /**
     * 添加工作节点
     */
    public boolean addWorker(WorkerNode worker) {
        return circularList.addItem(WORKER_LIST, worker);
    }

    /**
     * 移除工作节点
     */
    public boolean removeWorker(WorkerNode worker) {
        return circularList.removeItem(WORKER_LIST, worker);
    }

    /**
     * 查看所有工作节点
     */
    public List<WorkerNode> getAllWorkers() {
        return circularList.viewAll(WORKER_LIST, WorkerNode.class);
    }

    /**
     * 获取工作节点数量
     */
    public long getWorkerCount() {
        return circularList.getSize(WORKER_LIST);
    }

    /**
     * 创建示例工作节点
     */
    public static List<WorkerNode> createSampleWorkers(int count) {
        List<WorkerNode> workers = new ArrayList<>();

        for (int i = 0; i <= count; i++) {
            WorkerNode worker = WorkerNode.builder()
                    .nodeId("node-" + i)
                    .nodeName("Wokder " + i)
                    .host("192.168.1." + (100 + i))
                    .port(8080 + i)
                    .status("online")
                    .currentLoad(0)
                    .maxLoad(100)
                    .build();

            workers.add(worker);
        }

        return workers;
    }

    /**
     * 创建示例任务
     */
    public static List<Task> createSampleTasks(int count) {
        List<Task> tasks = new ArrayList<>();

        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i <= count; i++) {
            data.clear();
            data.put("input", "data-" + i);
            data.put("priority", i % 3);
            Task task = Task.builder()
                    .taskId("task-" + i)
                    .taskType("compute")
                    .data(data)
                    .build();

            tasks.add(task);
        }
        return tasks;
    }
}
