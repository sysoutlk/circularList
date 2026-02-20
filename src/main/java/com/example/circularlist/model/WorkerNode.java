package com.example.circularlist.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作节点模型
 @author lk
 @create 2026/02/19-21:07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerNode {

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 节点状态(online/offline)
     */
    private String status;

    /**
     * 当前负载(任务数)
     */
    private Integer currentLoad;

    /**
     * 最大负载
     */
    private Integer maxLoad;

    /**
     * 最后使用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUsedTime;
}
