package com.putl.interactionservice.controller;

import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.interactionservice.service.UserFootService;
import com.putl.interactionservice.vo.NotificationVO;
import com.putl.interactionservice.vo.UserFootListVO;
import com.putl.interactionservice.vo.UserFootVO;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "互动服务-足迹接口")
@RestController
@RequestMapping("/interaction/foot")
@RequiredArgsConstructor
@Slf4j
public class UserFootController {
    private final UserFootService userFootService;

    @Operation(summary = "添加浏览记录")
    @PostMapping("/browse")
    public boolean addBrowse(@RequestParam Integer articleId, @RequestParam Integer userId) {
        return userFootService.addBrowse(articleId, userId);
    }

    @Operation(summary = "更新点赞记录")
    @PostMapping("/praise")
    public CommonResult<Integer> updatePraise(@RequestParam Integer articleId) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        int praise = userFootService.updatePraise(articleId, Integer.parseInt(userId));
        return CommonResult.success(praise);
    }

    @Operation(summary = "更新收藏记录")
    @PostMapping("/collection")
    public CommonResult<Integer> updateCollection(@RequestParam Integer articleId) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        int collection = userFootService.updateCollection(articleId, Integer.parseInt(userId));
        return CommonResult.success(collection);
    }

    @Operation(summary = "更新评论记录")
    @PostMapping("/comment")
    public CommonResult<Integer> updateComment(@RequestParam Integer articleId) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        int comment = userFootService.updateComment(articleId, Integer.parseInt(userId));
        return CommonResult.success(comment);
    }

    @Operation(summary = "获取用户足迹信息")
    @GetMapping("/getUserFoot")
    public UserFootVO getUserFoot(@RequestParam Integer articleId, @RequestParam Integer userId) {
        return userFootService.getUserFoot(articleId, userId);
    }

    @Operation(summary = "获取文章列表数据")
    @GetMapping("/getUserFootList")
    public UserFootListVO getUserFootList(@RequestParam Integer articleId) {
        return userFootService.getUserFootList(articleId);
    }

    @Operation(summary = "获取用户浏览记录")
    @GetMapping("/getHistory")
    public CommonResult<List<ArticleListVO>> getHistory(@RequestParam("page") Integer page, @RequestParam("size") Integer size) {
        log.info("Entering getHistory interface: page={}, size={}", page, size);
        String userId = UserContext.getUserId();
        if (userId == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        List<ArticleListVO> history = userFootService.getHistory(Integer.parseInt(userId), page, size);
        return CommonResult.success(history);
    }

    @Operation(summary = "获取互动通知（点赞/收藏/评论/浏览）")
    @GetMapping("/getNotifications")
    public CommonResult<List<NotificationVO>> getNotifications() {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        List<NotificationVO> notifications = userFootService.getNotifications(Integer.parseInt(userId));
        return CommonResult.success(notifications);
    }
}
