package com.example.circularlist.service;

import com.example.circularlist.model.AdItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 广告轮播服务
 * 使用循环列表实现广告轮流展示
 @author lk
 @create 2026/02/19-21:08
 */
@Slf4j
@Service
public class AdRotationService {

    @Autowired
    private CircularList circularList;

    private static final String AD_LIST = "ads";

    /**
     * 初始化广告列表
     */
    public void initAds(List<AdItem> ads) {
        circularList.initList(AD_LIST, ads);
        log.info("广告列表已初始化: count={}", ads.size());
    }

    /**
     * 获取下一个要展示的广告
     */
    public AdItem getNextAd() {
        AdItem ad = circularList.getNext(AD_LIST, AdItem.class);

        if (ad != null) {
            //增加展示次数
            ad.setImpressions(ad.getImpressions() + 1);

            log.info("展示广告: adId={}, title={}, impressions={}", ad.getAdId(), ad.getTitle(), ad.getImpressions());
        }

        return ad;
    }

    /**
     * 批量获取广告 (用于预加载)
     */
    public List<AdItem> getNextAds(int count) {
        return circularList.getNextBatch(AD_LIST, count, AdItem.class);
    }

    /**
     * 记录广告点击
     */
    public void recordClick(String adId) {
        List<AdItem> ads = circularList.viewAll(AD_LIST, AdItem.class);

        for (AdItem ad : ads) {
            if (ad.getAdId().equals(adId)) {
                ad.setClicks(ad.getClicks() + 1);
                log.info("广告点击: adId={}, clicks={}", adId, ad.getClicks());
                break;
            }
        }
    }

    /**
     * 添加广告
     */
    public boolean addAd(AdItem ad) {
        return circularList.addItem(AD_LIST, ad);
    }

    /**
     * 移除广告
     */
    public boolean removeAd(AdItem ad) {
        return circularList.removeItem(AD_LIST, ad);
    }

    /**
     * 查看所有广告
     */
    public List<AdItem> getAllAds() {
        return circularList.viewAll(AD_LIST, AdItem.class);
    }

    /**
     * 创建示例广告
     */
    public static List<AdItem> createSampleAds(int count) {
        List<AdItem> ads = new ArrayList<>();

        String[] categories = {"电子产品", "服装鞋帽", "食品饮料", "家居用品", "运动健身"};

        for (int i = 1; i <= count; i++) {
            AdItem ad = AdItem.builder()
                    .adId("ad-" + i)
                    .title(categories[i % categories.length] + "促销广告" + i)
                    .content("限时优惠，立减" + (i * 10) + "元！")
                    .imageUrl("https://example.com/ads/ad" + i + "jpg")
                    .clickUrl("https://example.com/products/" + i)
                    .impressions(0L)
                    .clicks(0L)
                    .build();

            ads.add(ad);
        }

        return ads;
    }
}
