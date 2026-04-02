package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.controller.vo.ArticleCommandResult;
import com.putl.articleservice.controller.vo.ArticleReviewResultVO;
import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.enums.ArticleReviewAction;
import com.putl.articleservice.enums.ArticleStatus;
import com.putl.articleservice.exception.ArticleNotFoundException;
import com.putl.articleservice.exception.BusinessException;
import com.putl.articleservice.mapper.AdminOperationLogMapper;
import com.putl.articleservice.mapper.ArticleInfoMapper;
import com.putl.articleservice.mapper.ArticleMapper;
import com.putl.articleservice.mapper.ArticleReviewLogMapper;
import com.putl.articleservice.mapper.entity.AdminOperationLogDO;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.mapper.entity.ArticleInfoDO;
import com.putl.articleservice.mapper.entity.ArticleReviewLogDO;
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
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends BaseAbstractArticle implements ArticleService {
    private static final boolean REVIEW_REQUIRED = true;
    private static final Set<String> VALID_ARTICLE_TYPES = Set.of("ORIGINAL", "REPRINT", "TRANSLATION");
    private static final Set<String> VALID_CREATION_STATEMENTS = Set.of("PERSONAL_VIEW", "NETWORK_SOURCE", "AI_ASSISTED");

    private final ArticleMapper articleMapper;
    private final ArticleInfoMapper articleInfoMapper;
    private final ArticleReviewLogMapper articleReviewLogMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
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
        articleVO.setCreationStatements(parseCreationStatements(articleDO.getCreationStatement()));

        ArticleInfoDO articleInfoDO = articleInfoMapper.selectOne(Wrappers.<ArticleInfoDO>lambdaQuery()
                .eq(ArticleInfoDO::getArticleId, articleId));
        if (articleInfoDO != null) {
            articleVO.setContent(articleInfoDO.getContent());
            if ((articleVO.getUserName() == null || articleVO.getUserName().isBlank()) && articleInfoDO.getUserName() != null) {
                articleVO.setUserName(articleInfoDO.getUserName());
            }
        }

        UserResult author = queryUser(articleDO.getUserId(), "查询作者实时信息失败，回退快照");
        if (author != null) {
            if (author.getUsername() != null && !author.getUsername().isBlank()) {
                articleVO.setUserName(author.getUsername());
            }
            articleVO.setAuthorAvatar(author.getPhoto());
        }

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
            articleVOS.get(i).setCreationStatements(parseCreationStatements(articleDOPage.getRecords().get(i).getCreationStatement()));
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

    @Override
    @Transactional
    @CacheEvict(value = "articleCache", key = "#articleId")
    public ArticleReviewResultVO approveArticle(Integer articleId) {
        return reviewArticleInternal(articleId, ArticleReviewAction.APPROVE, null, null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "articleCache", key = "#articleId")
    public ArticleReviewResultVO rejectArticle(Integer articleId, String reason) {
        return reviewArticleInternal(articleId, ArticleReviewAction.REJECT, reason, null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "articleCache", allEntries = true)
    public List<ArticleReviewResultVO> batchReviewArticles(List<Integer> articleIds, ArticleReviewAction action, String reason) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new BusinessException(400, "待审核文章列表不能为空");
        }
        if (action == null) {
            throw new BusinessException(400, "审核动作不能为空");
        }

        Set<Integer> deduplicatedIds = new LinkedHashSet<>(articleIds);
        String batchId = deduplicatedIds.size() > 1 ? UUID.randomUUID().toString() : null;
        List<ArticleReviewResultVO> results = new ArrayList<>(deduplicatedIds.size());

        for (Integer articleId : deduplicatedIds) {
            results.add(reviewArticleInternal(articleId, action, reason, batchId));
        }

        return results;
    }

    private ArticleCommandResult upsertArticle(ArticleVO command, ArticleStatus targetStatus) {
        Integer currentUserId = currentUserId();
        UserResult currentUser = queryCurrentUser(currentUserId);
        String usernameSnapshot = currentUser != null ? currentUser.getUsername() : command.getUserName();
        validateArticleCommand(command, targetStatus);

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
                .category(normalizeCategory(command.getCategory()))
                .contentType(command.getContentType())
                .abstractText(command.getAbstractText())
                .articleType(normalizeArticleType(command.getArticleType()))
                .creationStatement(joinCreationStatements(command.getCreationStatements()))
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
                .category(normalizeCategory(command.getCategory()))
                .contentType(command.getContentType())
                .abstractText(command.getAbstractText())
                .articleType(normalizeArticleType(command.getArticleType()))
                .creationStatement(joinCreationStatements(command.getCreationStatements()))
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

    private void validateArticleCommand(ArticleVO command, ArticleStatus targetStatus) {
        if (targetStatus == ArticleStatus.DRAFT) {
            return;
        }
        if (command == null) {
            throw new BusinessException(400, "文章内容不能为空");
        }
        if (command.getTagIds() == null || command.getTagIds().isEmpty()) {
            throw new BusinessException(400, "文章标签不能为空");
        }

        String articleType = normalizeArticleType(command.getArticleType());
        if (!StringUtils.hasText(articleType)) {
            throw new BusinessException(400, "文章类型不能为空");
        }
        if (!VALID_ARTICLE_TYPES.contains(articleType)) {
            throw new BusinessException(400, "文章类型不合法");
        }

        List<String> creationStatements = parseCreationStatements(joinCreationStatements(command.getCreationStatements()));
        if (creationStatements.contains("NETWORK_SOURCE")
                && "ORIGINAL".equals(articleType)
                && creationStatements.size() == 1) {
            throw new BusinessException(400, "原创文章不能仅声明网络来源");
        }
        if (!"ORIGINAL".equals(articleType) && !creationStatements.contains("NETWORK_SOURCE")) {
            throw new BusinessException(400, "转载或翻译文章必须声明网络来源");
        }
    }

    private Integer normalizeCategory(Integer category) {
        return category == null ? 4 : category;
    }

    private String normalizeArticleType(String articleType) {
        return articleType == null ? "" : articleType.trim().toUpperCase();
    }

    private String joinCreationStatements(List<String> creationStatements) {
        if (creationStatements == null || creationStatements.isEmpty()) {
            return "";
        }
        return creationStatements.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(VALID_CREATION_STATEMENTS::contains)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private List<String> parseCreationStatements(String creationStatement) {
        if (!StringUtils.hasText(creationStatement)) {
            return new ArrayList<>();
        }
        return Arrays.stream(creationStatement.split(","))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(VALID_CREATION_STATEMENTS::contains)
                .distinct()
                .toList();
    }

    private UserResult queryUser(Integer userId, String failLogMessage) {
        if (userId == null) {
            return null;
        }
        try {
            CommonResult<UserResult> response = userClient.getUserById(userId);
            if (response != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("{}: userId={}", failLogMessage, userId, e);
        }
        return null;
    }

    private UserResult queryCurrentUser(Integer currentUserId) {
        return queryUser(currentUserId, "查询当前用户失败，继续使用快照字段");
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

    private ArticleReviewResultVO reviewArticleInternal(Integer articleId, ArticleReviewAction action, String reason, String batchId) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO == null || articleDO.getStatus() == ArticleStatus.DELETED) {
            throw new ArticleNotFoundException(articleId);
        }
        if (articleDO.getStatus() != ArticleStatus.PENDING_REVIEW) {
            throw new BusinessException(409, "文章当前不在待审核状态，无法重复审核");
        }

        String normalizedReason = normalizeReviewReason(action, reason);
        Integer reviewerId = currentUserId();
        String reviewerName = UserContext.getUsername() != null && !UserContext.getUsername().isBlank()
                ? UserContext.getUsername()
                : "admin-" + reviewerId;
        LocalDateTime reviewedAt = LocalDateTime.now();
        ArticleStatus targetStatus = action == ArticleReviewAction.APPROVE ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT;

        articleMapper.updateById(ArticleDO.builder()
                .id(articleId)
                .status(targetStatus)
                .updateTime(reviewedAt)
                .build());

        articleReviewLogMapper.insert(ArticleReviewLogDO.builder()
                .articleId(articleId)
                .reviewerId(reviewerId)
                .reviewerName(reviewerName)
                .reviewAction(action.name())
                .reviewReason(normalizedReason)
                .batchId(batchId)
                .createTime(reviewedAt)
                .build());

        log.info("管理员完成文章审核: articleId={}, action={}, reviewerId={}, batchId={}",
                articleId, action.name(), reviewerId, batchId);

        return ArticleReviewResultVO.builder()
                .articleId(articleId)
                .status(targetStatus)
                .reviewAction(action)
                .reviewReason(normalizedReason)
                .reviewerId(reviewerId)
                .reviewerName(reviewerName)
                .reviewedAt(reviewedAt)
                .batchId(batchId)
                .build();
    }

    private String normalizeReviewReason(ArticleReviewAction action, String reason) {
        String normalized = reason == null ? "" : reason.trim();
        if (action == ArticleReviewAction.REJECT && normalized.isEmpty()) {
            throw new BusinessException(400, "驳回时必须填写原因");
        }
        return normalized;
    }

    // ==================== 后台内容治理动作 ====================

    @Override
    @Transactional
    public void publishArticle(Integer articleId) {
        ArticleDO article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ArticleNotFoundException(articleId);
        }
        article.setStatus(ArticleStatus.PUBLISHED);
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);
        insertOpLog("ARTICLE_PUBLISH", "ARTICLE", articleId, null);
    }

    @Override
    @Transactional
    public void unpublishArticle(Integer articleId) {
        ArticleDO article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ArticleNotFoundException(articleId);
        }
        article.setStatus(ArticleStatus.DRAFT);
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);
        insertOpLog("ARTICLE_UNPUBLISH", "ARTICLE", articleId, null);
    }

    @Override
    @Transactional
    public void adminDeleteArticle(Integer articleId) {
        ArticleDO article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ArticleNotFoundException(articleId);
        }
        article.setStatus(ArticleStatus.DELETED);
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);
        insertOpLog("ARTICLE_DELETE", "ARTICLE", articleId, null);
    }

    @Override
    @Transactional
    public void restoreArticle(Integer articleId) {
        ArticleDO article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ArticleNotFoundException(articleId);
        }
        article.setStatus(ArticleStatus.DRAFT);
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);
        insertOpLog("ARTICLE_RESTORE", "ARTICLE", articleId, null);
    }

    @Override
    public void batchPublish(List<Integer> articleIds) {
        articleIds.forEach(this::publishArticle);
    }

    @Override
    public void batchUnpublish(List<Integer> articleIds) {
        articleIds.forEach(this::unpublishArticle);
    }

    @Override
    public void batchDelete(List<Integer> articleIds) {
        articleIds.forEach(this::adminDeleteArticle);
    }

    private void insertOpLog(String action, String targetType, Integer targetId, Map<String, Object> detail) {
        Integer operatorId = null;
        try {
            operatorId = currentUserId();
        } catch (Exception e) {
            log.warn("insertOpLog: 无法获取操作人ID", e);
        }
        String operatorName = UserContext.getUsername();
        AdminOperationLogDO logDO = AdminOperationLogDO.builder()
                .operatorId(operatorId)
                .operatorName(operatorName)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detail != null ? JsonUtil.toJsonString(detail) : null)
                .ip(UserContext.getClientIp())
                .createTime(LocalDateTime.now())
                .build();
        try {
            adminOperationLogMapper.insert(logDO);
        } catch (Exception e) {
            log.error("记录管理员操作日志失败: action={}, targetId={}", action, targetId, e);
        }
    }

    @Override
    public Long count() {
        return articleMapper.selectCount(null);
    }
}
