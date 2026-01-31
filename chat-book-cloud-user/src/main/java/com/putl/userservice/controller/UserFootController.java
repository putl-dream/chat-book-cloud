package com.putl.userservice.controller;


import com.putl.userservice.client.result.ArticleListVO;
import com.putl.userservice.common.ReqInfoContext;
import com.putl.userservice.controller.vo.UserFootListVO;
import com.putl.userservice.controller.vo.UserFootVO;
import com.putl.userservice.service.UserFootService;
import com.putl.userservice.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户足迹接口")
@RestController
@RequestMapping("/user/foot")
@RequiredArgsConstructor
public class UserFootController {
    private final UserFootService userFootService;

    @Operation(summary = "添加浏览记录")
    @GetMapping("/browse")
    public boolean addBrowse(@RequestParam Integer articleId, @RequestParam Integer userId){
        return userFootService.addBrowse(articleId, userId);
    }

    @Operation(summary = "更新点赞记录")
    @GetMapping("/praise")
    public Result<Integer> updatePraise(@RequestParam Integer articleId){
        int praise = userFootService.updatePraise(articleId, ReqInfoContext.getReqInfo().getUserId());
        return Result.success(praise);
    }

    @Operation(summary = "更新收藏记录")
    @GetMapping("/collection")
    public Result<Integer> updateCollection(@RequestParam Integer articleId){
        int collection = userFootService.updateCollection(articleId, ReqInfoContext.getReqInfo().getUserId());
        return Result.success(collection);
    }

    @Operation(summary = "更新评论记录")
    @GetMapping("/comment")
    public Result<Integer> updateComment(@RequestParam Integer articleId){
        int comment = userFootService.updateComment(articleId, ReqInfoContext.getReqInfo().getUserId());
        return Result.success(comment);
    }

    @Operation(summary = "获取用户足迹信息")
    @GetMapping("/getUserFoot")
    public UserFootVO getUserFoot(@RequestParam Integer articleId, @RequestParam Integer userId){
        return userFootService.getUserFoot(articleId, userId);
    }

    @Operation(summary = "获取文章列表数据")
    @GetMapping("/getUserFootList")
    public UserFootListVO getUserFootList(@RequestParam Integer articleId){
        return userFootService.getUserFootList(articleId);
    }

    @Operation(summary = "获取用户浏览记录")
    @GetMapping("/getHistory")
    public Result<List<ArticleListVO>> getHistory(@RequestParam Integer page, @RequestParam Integer size){
        List<ArticleListVO> history = userFootService.getHistory(ReqInfoContext.getReqInfo().getUserId(), page, size);
        return Result.success(history);
    }

    @Operation(summary = "获取消息")
    @GetMapping("/getMessage")
    public Result<List<ArticleListVO>> getMessage(){
        List<ArticleListVO> message = userFootService.getMessage(ReqInfoContext.getReqInfo().getUserId());
        return Result.success(message);
    }
}
