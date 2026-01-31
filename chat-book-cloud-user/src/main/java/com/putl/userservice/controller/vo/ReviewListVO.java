package com.putl.userservice.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewListVO {
    //评论id
    private Integer id;
    //文章id
    private Integer articleId;
    //评论内容
    private String content;
    //父评论id
    private Integer parentId;
    //用户名
    private String username;
    //用户头像
    private String headerImg;
    //评论时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    //子评论
    private List<ReviewListVO> children;
}
