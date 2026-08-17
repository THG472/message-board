package com.messageboard.vo;

import lombok.Data;

/**
 * 热榜排名
 */
@Data
public class HotRankVO {

    private Integer rank;
    private Long id;
    private String title;
    private String category;
    private String userName;
    private String orgName;
    private Integer likeCount;
    private Integer commentCount;
}
