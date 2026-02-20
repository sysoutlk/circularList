package com.example.circularlist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 任务模型
 @author lk
 @create 2026/02/19-21:07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务数据
     */
    private Map<String, Object> data;

    /**
     * 分配到的节点
     */
    private String assignedNode;
}
