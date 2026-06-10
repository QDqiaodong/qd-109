package com.digital.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.digital.community.dto.AccessoryCardDTO;
import com.digital.community.dto.ImageGroupDTO;
import com.digital.community.dto.PostDTO;
import com.digital.community.entity.Post;
import com.digital.community.mapper.PostMapper;
import com.digital.community.vo.AccessoryCardVO;
import com.digital.community.vo.ImageGroupVO;
import com.digital.community.vo.PostVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final String LATEST_POSTS_KEY = "post:latest";
    private static final String HOT_POSTS_KEY = "post:hot";
    private static final long CACHE_EXPIRE = 30;

    @Resource
    private PostMapper postMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public Page<PostVO> page(Integer pageNum, Integer pageSize, Long categoryId, Integer type) {
        Page<PostVO> page = new Page<>(pageNum, pageSize);
        return postMapper.selectPostPage(page, categoryId, type, null);
    }

    public Page<PostVO> search(Integer pageNum, Integer pageSize, String keyword, Long categoryId, Integer type) {
        Page<PostVO> page = new Page<>(pageNum, pageSize);
        return postMapper.selectPostPage(page, categoryId, type, keyword);
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
        Page<PostVO> page = postMapper.selectPostPage(new Page<>(1, 10), null, null, null);
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
        Page<PostVO> page = new Page<>(1, 10);
        page.setOrders(List.of(OrderItem.desc("view_count")));
        Page<PostVO> result = postMapper.selectPostPage(page, null, null, null);
        List<PostVO> records = result.getRecords();
        redisTemplate.opsForValue().set(HOT_POSTS_KEY, records, CACHE_EXPIRE, TimeUnit.MINUTES);
        return records;
    }

    public PostVO detail(Long id) {
        Post post = postMapper.selectById(id);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            postMapper.updateById(post);
        }
        return postMapper.selectPostById(id);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, PostDTO dto) {
        Post post = new Post();
        post.setUserId(userId);
        post.setCategoryId(dto.getCategoryId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            post.setImages(String.join(",", dto.getImages()));
        }
        if (dto.getImageGroups() != null && !dto.getImageGroups().isEmpty()) {
            try {
                List<ImageGroupVO> groupVOs = dto.getImageGroups().stream()
                        .map(this::convertImageGroupToVO)
                        .collect(Collectors.toList());
                post.setImageGroups(objectMapper.writeValueAsString(groupVOs));
                List<String> allGroupImages = groupVOs.stream()
                        .filter(g -> g.getImages() != null)
                        .flatMap(g -> g.getImages().stream())
                        .collect(Collectors.toList());
                if (allGroupImages.size() > 0) {
                    post.setImages(String.join(",", allGroupImages));
                }
            } catch (Exception e) {
                post.setImageGroups(null);
            }
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
        post.setType(dto.getType() != null ? dto.getType() : 1);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(1);
        postMapper.insert(post);

        redisTemplate.delete(LATEST_POSTS_KEY);
        redisTemplate.delete(HOT_POSTS_KEY);

        return post.getId();
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

    public void incrementCommentCount(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
        }
    }
}
