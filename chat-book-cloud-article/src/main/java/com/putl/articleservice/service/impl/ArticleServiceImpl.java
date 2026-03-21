package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.controller.vo.ArticleCommandResult;
import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.enums.ArticleStatus;
import com.putl.articleservice.exception.ArticleNotFoundException;
import com.putl.articleservice.exception.BusinessException;
import com.putl.articleservice.mapper.ArticleInfoMapper;
import com.putl.articleservice.mapper.ArticleMapper;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.mapper.entity.ArticleInfoDO;
import com.putl.articleservice.service.ArticleService;
import com.putl.articleservice.service.TagService;
import com.putl.articleservice.utils.PageResult;
import com.putl.interactionservice.api.InteractionClient;
import com.putl.interactionservice.api.dto.UserFootListVO;
import com.putl.interactionservice.api.dto.UserFootVO;
import com.putl.userservice.api.UserClient;
import com.putl.userservice.api.dto.UserResult;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends BaseAbstractArticle implements ArticleService {
    private static final boolean REVIEW_REQUIRED = true;

    private final ArticleMapper articleMapper;
    private final ArticleInfoMapper articleInfoMapper;
    private final InteractionClient interactionClient;
    private final UserClient userClient;
    private final TagService tagService;

    @Override
    @Cacheable(value = "articleCache", key = "#articleId", unless = "#result == null")
    public ArticleVO getArticleInfo(Integer articleId) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO == null || articleDO.getStatus() == ArticleStatus.DELETED) {
            return null;
        }
        if (!canAccess(articleDO)) {
            throw new BusinessException(403, "文章暂不可见");
        }

        ArticleVO articleVO = BeanUtil.toBean(articleDO, ArticleVO.class);
        articleVO.setUpdatedAt(articleDO.getUpdateTime());

        ArticleInfoDO articleInfoDO = articleInfoMapper.selectOne(Wrappers.<ArticleInfoDO>lambdaQuery()
                .eq(ArticleInfoDO::getArticleId, articleId));
        if (articleInfoDO != null) {
            articleVO.setContent(articleInfoDO.getContent());
            if ((articleVO.getUserName() == null || articleVO.getUserName().isBlank()) && articleInfoDO.getUserName() != null) {
                articleVO.setUserName(articleInfoDO.getUserName());
            }
        }

        articleVO.setUserName(resolveUserName(articleDO.getUserId(), articleVO.getUserName()));
        // 回填文章标签
        articleVO.setTagIds(tagService.getArticleTagIds(articleId));
        fillInteractionState(articleId, articleVO);
        return articleVO;
    }

    @Override
    public ArticleVO getArticleDetail(Integer articleId) {
        return getArticleInfo(articleId);
    }

    @Override
    @Transactional
    public ArticleCommandResult saveDraft(ArticleVO articleVO) {
        return upsertArticle(articleVO, ArticleStatus.DRAFT);
    }

    @Override
    @Transactional
    public ArticleCommandResult publish(ArticleVO articleVO) {
        return upsertArticle(articleVO, REVIEW_REQUIRED ? ArticleStatus.PENDING_REVIEW : ArticleStatus.PUBLISHED);
    }

    @Override
    public PageResult<ArticleVO> queryPage(Integer pageNum, Integer pageSize) {
        Page<ArticleDO> page = new Page<>(pageNum, pageSize);
        Page<ArticleDO> articleDOPage = articleMapper.selectPage(page, Wrappers.<ArticleDO>lambdaQuery()
                .ne(ArticleDO::getStatus, ArticleStatus.DELETED));
        List<ArticleVO> articleVOS = BeanUtil.toBean(articleDOPage.getRecords(), ArticleVO.class);
        for (int i = 0; i < articleVOS.size(); i++) {
            articleVOS.get(i).setUpdatedAt(articleDOPage.getRecords().get(i).getUpdateTime());
        }
        return new PageResult<>(articleVOS, articleDOPage.getTotal());
    }

    @Override
    @Transactional
    public void addArticle(ArticleVO articleVO) {
        saveDraft(articleVO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "articleCache", key = "#articleVO.id")
    public void updateArticle(ArticleVO articleVO) {
        saveDraft(articleVO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "articleCache", key = "#articleId")
    public void deleteArticle(Integer articleId) {
        updateArticleStatus(articleId, ArticleStatus.DELETED);
    }

    @Override
    @Transactional
    @CacheEvict(value = "articleCache", allEntries = true)
    public void deleteArticleBatch(Integer[] articleIds) {
        for (Integer articleId : articleIds) {
            updateArticleStatus(articleId, ArticleStatus.DELETED);
        }
    }

    @Override
    @Transactional
    public void updateArticleStatus(Integer articleId, ArticleStatus status) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO == null) {
            throw new ArticleNotFoundException(articleId);
        }
        validateOwnership(articleDO);
        articleMapper.updateById(ArticleDO.builder()
                .id(articleId)
                .status(status)
                .build());
    }

    private ArticleCommandResult upsertArticle(ArticleVO command, ArticleStatus targetStatus) {
        Integer currentUserId = currentUserId();
        UserResult currentUser = queryCurrentUser(currentUserId);
        String usernameSnapshot = currentUser != null ? currentUser.getUsername() : command.getUserName();

        ArticleDO savedArticle;
        if (command.getId() == null) {
            savedArticle = createArticle(command, currentUserId, usernameSnapshot, targetStatus);
        } else {
            savedArticle = updateExistingArticle(command, currentUserId, usernameSnapshot, targetStatus);
        }

        return ArticleCommandResult.builder()
                .articleId(savedArticle.getId())
                .status(savedArticle.getStatus())
                .updatedAt(savedArticle.getUpdateTime())
                .build();
    }

    private ArticleDO createArticle(ArticleVO command, Integer currentUserId, String usernameSnapshot, ArticleStatus targetStatus) {
        ArticleDO articleDO = ArticleDO.builder()
                .userId(currentUserId)
                .userName(usernameSnapshot)
                .title(command.getTitle())
                .cover(command.getCover())
                .category(command.getCategory())
                .contentType(command.getContentType())
                .abstractText(command.getAbstractText())
                .status(targetStatus)
                .build();
        articleMapper.insert(articleDO);
        upsertArticleInfo(articleDO, command.getContent(), usernameSnapshot);
        // 保存文章标签
        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            tagService.setArticleTags(articleDO.getId(), command.getTagIds());
        }
        return articleMapper.selectById(articleDO.getId());
    }

    private ArticleDO updateExistingArticle(ArticleVO command, Integer currentUserId, String usernameSnapshot, ArticleStatus targetStatus) {
        ArticleDO existing = articleMapper.selectById(command.getId());
        if (existing == null || existing.getStatus() == ArticleStatus.DELETED) {
            throw new ArticleNotFoundException(command.getId());
        }
        validateOwnership(existing);
        validateOptimisticLock(existing, command);

        ArticleDO articleDO = ArticleDO.builder()
                .id(existing.getId())
                .userId(currentUserId)
                .userName(usernameSnapshot != null ? usernameSnapshot : existing.getUserName())
                .title(command.getTitle())
                .cover(command.getCover())
                .category(command.getCategory())
                .contentType(command.getContentType())
                .abstractText(command.getAbstractText())
                .status(targetStatus)
                .build();
        articleMapper.updateById(articleDO);
        upsertArticleInfo(articleMapper.selectById(existing.getId()), command.getContent(), articleDO.getUserName());
        // 更新文章标签
        tagService.setArticleTags(existing.getId(), command.getTagIds());
        return articleMapper.selectById(existing.getId());
    }

    private void upsertArticleInfo(ArticleDO articleDO, String content, String usernameSnapshot) {
        ArticleInfoDO articleInfoDO = articleInfoMapper.selectOne(Wrappers.<ArticleInfoDO>lambdaQuery()
                .eq(ArticleInfoDO::getArticleId, articleDO.getId()));
        if (articleInfoDO == null) {
            articleInfoMapper.insert(ArticleInfoDO.builder()
                    .articleId(articleDO.getId())
                    .userId(articleDO.getUserId())
                    .userName(usernameSnapshot)
                    .title(articleDO.getTitle())
                    .content(content == null ? "" : content)
                    .build());
            return;
        }

        articleInfoDO.setUserId(articleDO.getUserId());
        articleInfoDO.setUserName(usernameSnapshot);
        articleInfoDO.setTitle(articleDO.getTitle());
        if (content != null) {
            articleInfoDO.setContent(content);
        }
        articleInfoMapper.updateById(articleInfoDO);
    }

    private void fillInteractionState(Integer articleId, ArticleVO articleVO) {
        try {
            String currentUserIdStr = UserContext.getUserId();
            Integer currentUserId = currentUserIdStr != null ? Integer.valueOf(currentUserIdStr) : null;

            UserFootListVO stat = interactionClient.getUserFootList(articleId);
            if (stat != null) {
                articleVO.setViewCount(stat.getViewCount());
            } else {
                articleVO.setViewCount(0L);
            }

            if (currentUserId != null && currentUserId > 0) {
                UserFootVO userFoot = interactionClient.getUserFoot(articleId, currentUserId);
                if (userFoot != null) {
                    articleVO.setPraiseStat(userFoot.getPraiseStat() != null ? userFoot.getPraiseStat() : 0);
                    articleVO.setCollectStat(userFoot.getCollectStat() != null ? userFoot.getCollectStat() : 0);
                }
            } else {
                articleVO.setPraiseStat(0);
                articleVO.setCollectStat(0);
            }
        } catch (Exception e) {
            articleVO.setViewCount(0L);
            articleVO.setPraiseStat(0);
            articleVO.setCollectStat(0);
            log.error("获取互动信息失败: articleId={}", articleId, e);
        }
    }

    private String resolveUserName(Integer userId, String fallback) {
        if (userId == null) {
            return fallback;
        }
        try {
            CommonResult<UserResult> response = userClient.getUserById(userId);
            if (response != null && response.getData() != null && response.getData().getUsername() != null) {
                return response.getData().getUsername();
            }
        } catch (Exception e) {
            log.warn("查询作者实时信息失败，回退快照: userId={}", userId, e);
        }
        return fallback;
    }

    private UserResult queryCurrentUser(Integer currentUserId) {
        try {
            CommonResult<UserResult> response = userClient.getUserById(currentUserId);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.warn("查询当前用户失败，继续使用快照字段: userId={}", currentUserId, e);
            return null;
        }
    }

    private void validateOwnership(ArticleDO articleDO) {
        Integer currentUserId = currentUserId();
        if (!currentUserId.equals(articleDO.getUserId())) {
            throw new BusinessException(403, "无权修改他人文章");
        }
    }

    private boolean canAccess(ArticleDO articleDO) {
        if (articleDO.getStatus() == ArticleStatus.PUBLISHED) {
            return true;
        }
        String currentUserId = UserContext.getUserId();
        return currentUserId != null && Integer.valueOf(currentUserId).equals(articleDO.getUserId());
    }

    private void validateOptimisticLock(ArticleDO existing, ArticleVO command) {
        if (command.getUpdatedAt() == null || existing.getUpdateTime() == null) {
            return;
        }
        if (!existing.getUpdateTime().equals(command.getUpdatedAt())) {
            throw new BusinessException(409, "内容已被更新，请刷新后重试");
        }
    }

    private Integer currentUserId() {
        String userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalStateException("用户信息未找到，请重新登录");
        }
        return Integer.valueOf(userId);
    }
}
