package com.digital.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digital.community.context.UserContext;
import com.digital.community.dto.AccessoryCardDTO;
import com.digital.community.dto.ImageGroupDTO;
import com.digital.community.dto.PostDTO;
import com.digital.community.entity.Category;
import com.digital.community.entity.Post;
import com.digital.community.mapper.CategoryMapper;
import com.digital.community.mapper.PostMapper;
import com.digital.community.vo.AccessoryCardVO;
import com.digital.community.vo.ImageGroupVO;
import com.digital.community.vo.PostVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final String LATEST_POSTS_KEY = "post:latest";
    private static final String HOT_POSTS_KEY = "post:hot";
    private static final String POST_VIEW_KEY_PREFIX = "post:view:";
    private static final String LATEST_PAGE_KEY_PREFIX = "post:page:latest:";
    private static final long CACHE_EXPIRE = 30;
    private static final long VIEW_COUNT_EXPIRE_MINUTES = 30;

    private static final long PAGE_1_EXPIRE_MINUTES = 2;
    private static final long PAGE_2_EXPIRE_MINUTES = 5;
    private static final long PAGE_3_EXPIRE_MINUTES = 10;
    private static final long PAGE_N_EXPIRE_MINUTES = 20;
    private static final long PAGE_MAX_EXPIRE_MINUTES = 60;

    private static final Map<String, String> FIELD_LABELS = Map.of(
            "deviceModel", "设备型号",
            "accessoryModel", "配件型号",
            "connectionType", "连接方式",
            "platform", "使用平台",
            "environment", "读写环境",
            "symptoms", "出现症状",
            "triedActions", "已尝试动作"
    );

    @Resource
    private PostMapper postMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    public Page<PostVO> page(Integer pageNum, Integer pageSize, Long categoryId, Integer type, String sort) {
        boolean isLatestSort = sort == null || "latest".equals(sort);

        if (isLatestSort) {
            String cacheKey = buildLatestPageCacheKey(pageNum, pageSize, categoryId, type);
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                try {
                    return (Page<PostVO>) cached;
                } catch (Exception e) {
                    redisTemplate.delete(cacheKey);
                }
            }
        }

        Page<PostVO> page = new Page<>(pageNum, pageSize);
        Page<PostVO> result = postMapper.selectPostPage(page, categoryId, type, null, sort);

        if (isLatestSort) {
            String cacheKey = buildLatestPageCacheKey(pageNum, pageSize, categoryId, type);
            long expireMinutes = calculatePageExpire(pageNum);
            try {
                Page<PostVO> cacheCopy = new Page<>(pageNum, pageSize);
                cacheCopy.setRecords(result.getRecords());
                cacheCopy.setTotal(result.getTotal());
                redisTemplate.opsForValue().set(cacheKey, cacheCopy, expireMinutes, TimeUnit.MINUTES);
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    private String buildLatestPageCacheKey(Integer pageNum, Integer pageSize, Long categoryId, Integer type) {
        StringBuilder sb = new StringBuilder(LATEST_PAGE_KEY_PREFIX);
        sb.append("p:").append(pageNum).append(":").append(pageSize);
        if (categoryId != null) {
            sb.append(":c:").append(categoryId);
        }
        if (type != null) {
            sb.append(":t:").append(type);
        }
        return sb.toString();
    }

    private long calculatePageExpire(Integer pageNum) {
        if (pageNum <= 1) {
            return PAGE_1_EXPIRE_MINUTES;
        } else if (pageNum == 2) {
            return PAGE_2_EXPIRE_MINUTES;
        } else if (pageNum == 3) {
            return PAGE_3_EXPIRE_MINUTES;
        } else {
            long extra = (long) (pageNum - 3) * 5L;
            long expire = PAGE_N_EXPIRE_MINUTES + extra;
            return Math.min(expire, PAGE_MAX_EXPIRE_MINUTES);
        }
    }

    private void invalidateLatestPageCache() {
        try {
            Set<String> headKeys = redisTemplate.keys(LATEST_PAGE_KEY_PREFIX + "p:1:*");
            if (headKeys != null && !headKeys.isEmpty()) {
                redisTemplate.delete(headKeys);
            }
        } catch (Exception ignored) {
        }
    }

    public Page<PostVO> search(Integer pageNum, Integer pageSize, String keyword, Long categoryId, Integer type) {
        Page<PostVO> page = new Page<>(pageNum, pageSize);
        return postMapper.selectPostPage(page, categoryId, type, keyword, "latest");
    }

    public List<PostVO> searchSuggestions(String keyword) {
        return postMapper.selectPostSuggestions(keyword);
    }

    @SuppressWarnings("unchecked")
    public List<PostVO> latestPosts() {
        List<PostVO> cached = (List<PostVO>) redisTemplate.opsForValue().get(LATEST_POSTS_KEY);
        if (cached != null) {
            return cached;
        }
        Page<PostVO> page = postMapper.selectPostPage(new Page<>(1, 10), null, null, null, "latest");
        List<PostVO> records = page.getRecords();
        redisTemplate.opsForValue().set(LATEST_POSTS_KEY, records, CACHE_EXPIRE, TimeUnit.MINUTES);
        return records;
    }

    @SuppressWarnings("unchecked")
    public List<PostVO> hotPosts() {
        List<PostVO> cached = (List<PostVO>) redisTemplate.opsForValue().get(HOT_POSTS_KEY);
        if (cached != null) {
            return cached;
        }
        Page<PostVO> page = postMapper.selectPostPage(new Page<>(1, 10), null, null, null, "hot");
        List<PostVO> records = page.getRecords();
        redisTemplate.opsForValue().set(HOT_POSTS_KEY, records, CACHE_EXPIRE, TimeUnit.MINUTES);
        return records;
    }

    public PostVO detail(Long id) {
        Post post = postMapper.selectById(id);
        if (post != null && shouldCountView(id)) {
            postMapper.incrementViewCount(id);
        }
        return postMapper.selectPostById(id);
    }

    private boolean shouldCountView(Long postId) {
        String viewerIdentifier = getViewerIdentifier();
        String key = POST_VIEW_KEY_PREFIX + postId + ":" + viewerIdentifier;
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(key, "1", VIEW_COUNT_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(absent);
    }

    private String getViewerIdentifier() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            return "user:" + userId;
        }
        String ip = getClientIp();
        return "ip:" + ip;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return "unknown";
            }
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip == null ? "unknown" : ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, PostDTO dto) {
        if (dto.getType() != null && dto.getType() == 2) {
            validateHelpPostFields(dto);
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setCategoryId(dto.getCategoryId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        if (dto.getImageGroups() != null && !dto.getImageGroups().isEmpty()) {
            try {
                List<ImageGroupVO> groupVOs = dto.getImageGroups().stream()
                        .map(this::convertImageGroupToVO)
                        .collect(Collectors.toList());
                post.setImageGroups(objectMapper.writeValueAsString(groupVOs));
                if (dto.getImages() == null || dto.getImages().isEmpty()) {
                    List<String> allGroupImages = groupVOs.stream()
                            .filter(g -> g.getImages() != null)
                            .flatMap(g -> g.getImages().stream())
                            .collect(Collectors.toList());
                    if (!allGroupImages.isEmpty()) {
                        post.setImages(String.join(",", allGroupImages));
                    }
                }
            } catch (Exception e) {
                post.setImageGroups(null);
            }
        }
        if ((post.getImages() == null || post.getImages().isEmpty())
                && dto.getImages() != null && !dto.getImages().isEmpty()) {
            post.setImages(String.join(",", dto.getImages()));
        }
        if (dto.getAccessoryCards() != null && !dto.getAccessoryCards().isEmpty()) {
            try {
                List<AccessoryCardVO> cardVOs = dto.getAccessoryCards().stream()
                        .map(this::convertToVO)
                        .collect(Collectors.toList());
                post.setAccessoryCards(objectMapper.writeValueAsString(cardVOs));
            } catch (Exception e) {
                post.setAccessoryCards(null);
            }
        }
        if (dto.getFaultInfo() != null && !dto.getFaultInfo().isEmpty()) {
            try {
                post.setFaultInfo(objectMapper.writeValueAsString(dto.getFaultInfo()));
            } catch (Exception e) {
                post.setFaultInfo(null);
            }
        }
        post.setType(dto.getType() != null ? dto.getType() : 1);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(1);
        postMapper.insert(post);

        redisTemplate.delete(LATEST_POSTS_KEY);
        redisTemplate.delete(HOT_POSTS_KEY);
        invalidateLatestPageCache();

        return post.getId();
    }

    private void validateHelpPostFields(PostDTO dto) {
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("求助帖请选择所属分类");
        }
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new IllegalArgumentException("所选分类不存在");
        }
        String requiredFieldsJson = category.getRequiredFields();
        if (requiredFieldsJson == null || requiredFieldsJson.isBlank()) {
            return;
        }
        List<String> requiredFields;
        try {
            requiredFields = objectMapper.readValue(requiredFieldsJson, List.class);
        } catch (Exception e) {
            return;
        }
        Map<String, String> faultInfo = dto.getFaultInfo();
        List<String> missingFields = requiredFields.stream()
                .filter(field -> {
                    if (faultInfo == null) return true;
                    String value = faultInfo.get(field);
                    return value == null || value.isBlank();
                })
                .toList();
        if (!missingFields.isEmpty()) {
            String labels = missingFields.stream()
                    .map(f -> FIELD_LABELS.getOrDefault(f, f))
                    .collect(Collectors.joining("、"));
            throw new IllegalArgumentException("「" + category.getName() + "」分类的求助帖需补充：" + labels);
        }
    }

    private AccessoryCardVO convertToVO(AccessoryCardDTO dto) {
        AccessoryCardVO vo = new AccessoryCardVO();
        vo.setModel(dto.getModel());
        vo.setInterfaceType(dto.getInterfaceType());
        vo.setCompatibleDevices(dto.getCompatibleDevices());
        vo.setUsageScenarios(dto.getUsageScenarios());
        vo.setPros(dto.getPros());
        vo.setCons(dto.getCons());
        return vo;
    }

    private ImageGroupVO convertImageGroupToVO(ImageGroupDTO dto) {
        ImageGroupVO vo = new ImageGroupVO();
        vo.setKey(dto.getKey());
        vo.setLabel(dto.getLabel());
        vo.setImages(dto.getImages());
        vo.setSort(dto.getSort());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void incrementCommentCount(Long postId) {
        postMapper.incrementCommentCount(postId);
        recalibrateCommentCount(postId);
    }

    private void recalibrateCommentCount(Long postId) {
        try {
            Post post = postMapper.selectById(postId);
            if (post == null) {
                return;
            }
            int actualCount = postMapper.countCommentsByPostId(postId);
            if (!post.getCommentCount().equals(actualCount)) {
                post.setCommentCount(actualCount);
                postMapper.updateById(post);
            }
        } catch (Exception e) {
            // ignore calibration error
        }
    }
}
