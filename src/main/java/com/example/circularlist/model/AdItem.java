package com.example.circularlist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 广告项模型
 @author lk
 @create 2026/02/19-21:07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdItem {

    /**
     * 广告ID
     */
    private String adId;

    /**
     * 广告标题
     */
    private String title;

    /**
     * 广告内容
     */
    private String content;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 点击链接
     */
    private String clickUrl;

    /**
     * 展示次数
     */
    private Long impressions;

    /**
     * 点击次数
     */
    private Long clicks;
}
