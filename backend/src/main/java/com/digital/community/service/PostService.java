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
import com.digital.community.util.TransactionContentValidator;
import com.digital.community.vo.AccessoryCardVO;
import com.digital.community.vo.CollocationSchemeVO;
import com.digital.community.vo.FaultThemeSuggestionVO;
import com.digital.community.vo.FaultThemeVO;
import com.digital.community.vo.ImageGroupVO;
import com.digital.community.vo.ModelFaultStatsVO;
import com.digital.community.vo.PostVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
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

    private static final String COLLOCATION_SCHEME_KEY_PREFIX = "post:collocation:";
    private static final long COLLOCATION_CACHE_EXPIRE_MINUTES = 60;

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
            Set<String> allKeys = redisTemplate.keys(LATEST_PAGE_KEY_PREFIX + "*");
            if (allKeys != null && !allKeys.isEmpty()) {
                redisTemplate.delete(allKeys);
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
        TransactionContentValidator.validate(dto.getTitle());
        TransactionContentValidator.validate(dto.getContent());

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
        invalidateCollocationCache();

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
        vo.setCategory(dto.getCategory());
        vo.setRole(dto.getRole());
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

    private static final Map<String, FaultThemeDefinition> FAULT_THEME_DEFINITIONS = Map.ofEntries(
            Map.entry("disconnection", new FaultThemeDefinition("disconnection", "掉连/断连问题", "📡",
                    List.of("断连", "掉连", "掉线", "断开", "连接中断", "经常断", "频繁断", "时断时续", "不稳定", "连不上", "连接失败", "自动断开"))),
            Map.entry("noise", new FaultThemeDefinition("noise", "底噪/杂音问题", "🔊",
                    List.of("底噪", "杂音", "滋滋", "电流声", "噪声", "噪音", "爆破音", "破音", "卡顿声", "断断续续的声音", "沙沙声"))),
            Map.entry("compatibility", new FaultThemeDefinition("compatibility", "兼容异常", "⚠️",
                    List.of("不兼容", "兼容", "识别不了", "无法识别", "读不出来", "检测不到", "不支持", "不匹配", "适配", "驱动", "兼容性", "认不出"))),
            Map.entry("power", new FaultThemeDefinition("power", "供电不足/充电异常", "🔋",
                    List.of("供电不足", "充电慢", "充不进", "无法充电", "不充电", "掉电", "续航差", "电量", "充电异常", "断电", "发热", "发烫", "功率"))),
            Map.entry("sound", new FaultThemeDefinition("sound", "音质/无声问题", "🎵",
                    List.of("没声音", "无声", "没声", "音质差", "声音小", "声音异常", "单声道", "一边没声", "左右声道", "音质", "音量", "麦克风", "麦没声"))),
            Map.entry("lag", new FaultThemeDefinition("lag", "延迟/卡顿问题", "⏱️",
                    List.of("延迟", "卡顿", "慢", "反应慢", "不同步", "音画不同步", "滞后", "卡", "延迟高", "响应慢"))),
            Map.entry("bluetooth", new FaultThemeDefinition("bluetooth", "蓝牙连接问题", "📶",
                    List.of("蓝牙", "配对", "搜不到", "搜索不到", "蓝牙连", "配对失败", "无法配对", "蓝牙搜"))),
            Map.entry("keyboard", new FaultThemeDefinition("keyboard", "按键/输入问题", "⌨️",
                    List.of("按键", "失灵", "连击", "双击", "按不了", "没反应", "键位", "卡键", "臭轴", "键盘", "摇杆", "漂移"))),
            Map.entry("display", new FaultThemeDefinition("display", "显示/画面问题", "🖥️",
                    List.of("黑屏", "花屏", "闪屏", "显示异常", "画面", "屏幕", "闪烁", "显示不出来", "无显示", "模糊"))),
            Map.entry("transfer", new FaultThemeDefinition("transfer", "传输/读写问题", "💾",
                    List.of("传输慢", "读写慢", "速度慢", "拷贝慢", "写入慢", "读取慢", "传文件", "传输失败", "丢数据", "丢包")))
    );

    private static final Map<String, String> THEME_TROUBLESHOOTING_TIPS = Map.ofEntries(
            Map.entry("disconnection", "排查建议：1. 尝试更换数据线/接口；2. 检查设备蓝牙/Wi-Fi模块是否正常；3. 关闭附近干扰源（微波炉、无线设备）；4. 更新设备固件和驱动"),
            Map.entry("noise", "排查建议：1. 更换高品质屏蔽线；2. 检查接地是否良好；3. 避免与大功率设备共用插座；4. 调整音频采样率和缓冲设置"),
            Map.entry("compatibility", "排查建议：1. 到官网下载最新驱动；2. 查阅兼容性列表确认是否支持当前系统；3. 尝试更换USB端口（USB 2.0/3.0切换）；4. 更新系统至最新版本"),
            Map.entry("power", "排查建议：1. 更换原装充电器和数据线；2. 清洁充电口灰尘；3. 查看是否开启了省电模式；4. 确认充电协议是否匹配"),
            Map.entry("sound", "排查建议：1. 检查音量设置和音频输出设备；2. 重新插拔音频线；3. 更新声卡驱动；4. 在其他设备上测试确认是否为硬件问题"),
            Map.entry("lag", "排查建议：1. 关闭后台占用带宽的程序；2. 检查CPU占用率；3. 将接收设备靠近发射器；4. 尝试切换USB接口到主板直连口"),
            Map.entry("bluetooth", "排查建议：1. 删除旧配对后重新配对；2. 更新蓝牙驱动；3. 检查蓝牙设备电量；4. 避免2.4G Wi-Fi与蓝牙同频干扰"),
            Map.entry("keyboard", "排查建议：1. 清洁按键下灰尘杂物；2. 更换数据线或无线接收器；3. 使用按键检测软件测试；4. 如在保修期内可申请售后"),
            Map.entry("display", "排查建议：1. 更换视频线；2. 更新显卡驱动；3. 检查显示器输入源设置；4. 降低分辨率/刷新率测试"),
            Map.entry("transfer", "排查建议：1. 确认接口版本（USB 3.0/3.1等）；2. 检查磁盘健康状态；3. 更换高质量数据线；4. 避免同时进行多个大文件传输")
    );

    private static class FaultThemeDefinition {
        final String key;
        final String name;
        final String icon;
        final List<String> keywords;

        FaultThemeDefinition(String key, String name, String icon, List<String> keywords) {
            this.key = key;
            this.name = name;
            this.icon = icon;
            this.keywords = keywords;
        }
    }

    public FaultThemeSuggestionVO getFaultSuggestions(String modelKeyword, Long categoryId) {
        FaultThemeSuggestionVO vo = new FaultThemeSuggestionVO();
        vo.setInputModel(modelKeyword);

        List<PostVO> posts;
        if (modelKeyword != null && !modelKeyword.isBlank()) {
            posts = postMapper.selectHelpPostsByModelKeyword(modelKeyword, categoryId);
        } else {
            posts = postMapper.selectAllHelpPosts(categoryId);
        }

        List<ModelFaultStatsVO> matchedModels = aggregateModelFaultStats(posts);

        if (modelKeyword != null && !modelKeyword.isBlank()) {
            String lowerKeyword = modelKeyword.toLowerCase();
            matchedModels = matchedModels.stream()
                    .filter(m -> m.getModel().toLowerCase().contains(lowerKeyword))
                    .collect(Collectors.toList());
        }

        matchedModels = matchedModels.stream()
                .sorted((a, b) -> b.getTotalHelpPosts() - a.getTotalHelpPosts())
                .limit(10)
                .collect(Collectors.toList());

        vo.setMatchedModels(matchedModels);

        Map<String, Integer> themeGlobalCounts = new java.util.HashMap<>();
        Map<String, List<String>> themeSymptomsMap = new java.util.HashMap<>();
        int totalMatchedPosts = 0;

        for (PostVO post : posts) {
            Map<String, String> faultInfo = post.getFaultInfo();
            if (faultInfo == null || faultInfo.isEmpty()) continue;

            String deviceModel = faultInfo.getOrDefault("deviceModel", "");
            String accessoryModel = faultInfo.getOrDefault("accessoryModel", "");
            if (modelKeyword != null && !modelKeyword.isBlank()) {
                String lower = modelKeyword.toLowerCase();
                boolean match = deviceModel.toLowerCase().contains(lower)
                        || accessoryModel.toLowerCase().contains(lower)
                        || (post.getTitle() != null && post.getTitle().toLowerCase().contains(lower))
                        || (post.getContent() != null && post.getContent().toLowerCase().contains(lower));
                if (!match) continue;
            }

            String symptoms = faultInfo.getOrDefault("symptoms", "") + " " +
                    (post.getTitle() != null ? post.getTitle() : "") + " " +
                    (post.getContent() != null ? post.getContent() : "");
            symptoms = symptoms.toLowerCase();

            Set<String> matchedThemes = new java.util.HashSet<>();
            for (Map.Entry<String, FaultThemeDefinition> entry : FAULT_THEME_DEFINITIONS.entrySet()) {
                for (String kw : entry.getValue().keywords) {
                    if (symptoms.contains(kw.toLowerCase())) {
                        matchedThemes.add(entry.getKey());
                        break;
                    }
                }
            }

            if (!matchedThemes.isEmpty()) {
                totalMatchedPosts++;
                for (String theme : matchedThemes) {
                    themeGlobalCounts.merge(theme, 1, Integer::sum);
                    String symptomText = faultInfo.getOrDefault("symptoms", "");
                    if (!symptomText.isBlank()) {
                        themeSymptomsMap.computeIfAbsent(theme, k -> new java.util.ArrayList<>())
                                .add(symptomText.length() > 80 ? symptomText.substring(0, 80) + "..." : symptomText);
                    }
                }
            }
        }

        final int finalTotal = totalMatchedPosts;
        List<FaultThemeVO> commonThemes = themeGlobalCounts.entrySet().stream()
                .map(e -> {
                    FaultThemeVO themeVO = new FaultThemeVO();
                    FaultThemeDefinition def = FAULT_THEME_DEFINITIONS.get(e.getKey());
                    themeVO.setThemeKey(def.key);
                    themeVO.setThemeName(def.name);
                    themeVO.setThemeIcon(def.icon);
                    themeVO.setCount(e.getValue());
                    themeVO.setPercentage(finalTotal > 0 ? Math.round(e.getValue() * 1000.0 / finalTotal) / 10.0 : 0.0);
                    List<String> samples = themeSymptomsMap.get(e.getKey());
                    if (samples != null && !samples.isEmpty()) {
                        themeVO.setSampleSymptoms(samples.get(0));
                    }
                    return themeVO;
                })
                .sorted((a, b) -> b.getCount() - a.getCount())
                .limit(8)
                .collect(Collectors.toList());
        vo.setCommonThemes(commonThemes);

        StringBuilder tips = new StringBuilder();
        if (commonThemes.size() > 0) {
            tips.append("基于历史求助数据分析，该型号常见问题及建议：\n");
            for (int i = 0; i < Math.min(commonThemes.size(), 3); i++) {
                FaultThemeVO theme = commonThemes.get(i);
                String tip = THEME_TROUBLESHOOTING_TIPS.get(theme.getThemeKey());
                if (tip != null) {
                    tips.append(i + 1).append(". ").append(theme.getThemeIcon()).append(" ")
                            .append(theme.getThemeName()).append("（占比").append(theme.getPercentage())
                            .append("%）：\n   ").append(tip).append("\n");
                }
            }
        } else {
            tips.append("暂未找到该型号的历史故障数据，建议详细描述您的问题，社区会尽力帮您解决。");
        }
        vo.setTroubleshootingTips(tips.toString());

        return vo;
    }

    public List<ModelFaultStatsVO> getHotFaultModels(Long categoryId, Integer limit) {
        List<PostVO> posts = postMapper.selectAllHelpPosts(categoryId);
        List<ModelFaultStatsVO> models = aggregateModelFaultStats(posts);
        return models.stream()
                .sorted((a, b) -> b.getTotalHelpPosts() - a.getTotalHelpPosts())
                .limit(limit != null ? limit : 20)
                .collect(Collectors.toList());
    }

    private List<ModelFaultStatsVO> aggregateModelFaultStats(List<PostVO> posts) {
        Map<String, ModelAccumulator> modelMap = new java.util.LinkedHashMap<>();

        for (PostVO post : posts) {
            Map<String, String> faultInfo = post.getFaultInfo();
            if (faultInfo == null || faultInfo.isEmpty()) continue;

            String deviceModel = (faultInfo.getOrDefault("deviceModel", "")).trim();
            String accessoryModel = (faultInfo.getOrDefault("accessoryModel", "")).trim();
            String symptoms = faultInfo.getOrDefault("symptoms", "") + " " +
                    (post.getTitle() != null ? post.getTitle() : "") + " " +
                    (post.getContent() != null ? post.getContent() : "");
            symptoms = symptoms.toLowerCase();

            if (!deviceModel.isBlank()) {
                accumulateModel(modelMap, deviceModel, "deviceModel", post, symptoms);
            }
            if (!accessoryModel.isBlank()) {
                accumulateModel(modelMap, accessoryModel, "accessoryModel", post, symptoms);
            }
        }

        return modelMap.entrySet().stream()
                .map(e -> {
                    ModelFaultStatsVO stats = new ModelFaultStatsVO();
                    ModelAccumulator acc = e.getValue();
                    stats.setModel(e.getKey());
                    stats.setModelType(acc.modelType);
                    stats.setCategoryId(acc.categoryId);
                    stats.setCategoryName(acc.categoryName);
                    stats.setTotalHelpPosts(acc.totalHelpPosts);
                    stats.setTotalComments(acc.totalComments);
                    stats.setTotalViews(acc.totalViews);

                    int themeTotal = acc.themeCounts.values().stream().mapToInt(Integer::intValue).sum();
                    List<FaultThemeVO> topThemes = acc.themeCounts.entrySet().stream()
                            .map(te -> {
                                FaultThemeVO themeVO = new FaultThemeVO();
                                FaultThemeDefinition def = FAULT_THEME_DEFINITIONS.get(te.getKey());
                                if (def == null) return null;
                                themeVO.setThemeKey(def.key);
                                themeVO.setThemeName(def.name);
                                themeVO.setThemeIcon(def.icon);
                                themeVO.setCount(te.getValue());
                                themeVO.setPercentage(themeTotal > 0 ? Math.round(te.getValue() * 1000.0 / themeTotal) / 10.0 : 0.0);
                                List<String> samples = acc.themeSymptoms.get(te.getKey());
                                if (samples != null && !samples.isEmpty()) {
                                    themeVO.setSampleSymptoms(samples.get(0));
                                }
                                return themeVO;
                            })
                            .filter(t -> t != null)
                            .sorted((a, b) -> b.getCount() - a.getCount())
                            .limit(5)
                            .collect(Collectors.toList());
                    stats.setTopThemes(topThemes);

                    stats.setRelatedPosts(acc.relatedPosts.stream()
                            .sorted((a, b) -> b.getCommentCount() - a.getCommentCount())
                            .limit(5)
                            .collect(Collectors.toList()));

                    return stats;
                })
                .collect(Collectors.toList());
    }

    private void accumulateModel(Map<String, ModelAccumulator> modelMap, String model,
                                  String modelType, PostVO post, String symptoms) {
        String normalizedModel = normalizeModelName(model);
        if (normalizedModel.isBlank()) return;

        ModelAccumulator acc = modelMap.computeIfAbsent(normalizedModel, k -> new ModelAccumulator());
        if (acc.modelType == null) {
            acc.modelType = modelType;
            acc.categoryId = post.getCategoryId();
            acc.categoryName = post.getCategoryName();
        }
        acc.totalHelpPosts++;
        acc.totalComments += post.getCommentCount() != null ? post.getCommentCount() : 0;
        acc.totalViews += post.getViewCount() != null ? post.getViewCount() : 0;
        acc.relatedPosts.add(post);

        Set<String> matchedThemes = new java.util.HashSet<>();
        for (Map.Entry<String, FaultThemeDefinition> entry : FAULT_THEME_DEFINITIONS.entrySet()) {
            for (String kw : entry.getValue().keywords) {
                if (symptoms.contains(kw.toLowerCase())) {
                    matchedThemes.add(entry.getKey());
                    break;
                }
            }
        }
        for (String theme : matchedThemes) {
            acc.themeCounts.merge(theme, 1, Integer::sum);
            String sample = post.getFaultInfo().getOrDefault("symptoms", "");
            if (!sample.isBlank()) {
                acc.themeSymptoms.computeIfAbsent(theme, k -> new java.util.ArrayList<>())
                        .add(sample.length() > 80 ? sample.substring(0, 80) + "..." : sample);
            }
        }
    }

    private String normalizeModelName(String model) {
        if (model == null) return "";
        String s = model.trim();
        s = s.replaceAll("\\s+", " ");
        s = s.replaceAll("[\\(\\)（）【】\\[\\]]", "");
        return s.length() > 50 ? s.substring(0, 50) : s;
    }

    @SuppressWarnings("unchecked")
    public List<CollocationSchemeVO> getCollocationSchemes(Long categoryId, Integer minItems, Integer maxItems, Integer limit) {
        String cacheKey = buildCollocationCacheKey(categoryId, minItems, maxItems, limit);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return (List<CollocationSchemeVO>) cached;
            } catch (Exception e) {
                redisTemplate.delete(cacheKey);
            }
        }

        List<PostVO> posts = postMapper.selectAllExperiencePosts(categoryId);
        List<CollocationSchemeVO> schemes = aggregateCollocationSchemes(posts, minItems, maxItems, limit);

        try {
            redisTemplate.opsForValue().set(cacheKey, schemes, COLLOCATION_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }

        return schemes;
    }

    private String buildCollocationCacheKey(Long categoryId, Integer minItems, Integer maxItems, Integer limit) {
        StringBuilder sb = new StringBuilder(COLLOCATION_SCHEME_KEY_PREFIX);
        if (categoryId != null) {
            sb.append("c:").append(categoryId).append(":");
        }
        sb.append("min:").append(minItems != null ? minItems : 2).append(":");
        sb.append("max:").append(maxItems != null ? maxItems : 5).append(":");
        sb.append("limit:").append(limit != null ? limit : 20);
        return sb.toString();
    }

    public void invalidateCollocationCache() {
        try {
            Set<String> keys = redisTemplate.keys(COLLOCATION_SCHEME_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ignored) {
        }
    }

    private List<CollocationSchemeVO> aggregateCollocationSchemes(List<PostVO> posts, Integer minItems, Integer maxItems, Integer limit) {
        int min = minItems != null ? minItems : 2;
        int max = maxItems != null ? maxItems : 5;
        int maxLimit = limit != null ? limit : 20;

        Map<String, CollocationAccumulator> schemeMap = new java.util.LinkedHashMap<>();
        int totalPostsWithAccessories = 0;

        for (PostVO post : posts) {
            List<AccessoryCardVO> cards = post.getAccessoryCards();
            if (cards == null || cards.isEmpty()) continue;

            List<String> models = cards.stream()
                    .map(AccessoryCardVO::getModel)
                    .filter(m -> m != null && !m.isBlank())
                    .map(this::normalizeModelName)
                    .filter(m -> !m.isBlank())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            if (models.size() < min) continue;
            totalPostsWithAccessories++;

            List<List<String>> subsets = generateSubsets(models, min, Math.min(max, models.size()));

            for (List<String> subset : subsets) {
                String key = generateSchemeKey(subset);
                CollocationAccumulator acc = schemeMap.computeIfAbsent(key, k -> {
                    CollocationAccumulator a = new CollocationAccumulator();
                    a.accessoryModels = new ArrayList<>(subset);
                    a.itemCount = subset.size();
                    a.categoryId = post.getCategoryId();
                    a.categoryName = post.getCategoryName();
                    return a;
                });
                acc.postCount++;
                acc.totalViews += post.getViewCount() != null ? post.getViewCount() : 0;
                acc.totalComments += post.getCommentCount() != null ? post.getCommentCount() : 0;
                if (acc.relatedPosts.size() < 5) {
                    acc.relatedPosts.add(post);
                }
            }
        }

        final int total = totalPostsWithAccessories;
        List<CollocationSchemeVO> result = schemeMap.entrySet().stream()
                .map(e -> {
                    CollocationSchemeVO vo = new CollocationSchemeVO();
                    CollocationAccumulator acc = e.getValue();
                    vo.setSchemeKey(e.getKey());
                    vo.setAccessoryModels(acc.accessoryModels);
                    vo.setItemCount(acc.itemCount);
                    vo.setPostCount(acc.postCount);
                    vo.setCategoryId(acc.categoryId);
                    vo.setCategoryName(acc.categoryName);
                    vo.setPercentage(total > 0 ? Math.round(acc.postCount * 1000.0 / total) / 10.0 : 0.0);
                    vo.setRelatedPosts(acc.relatedPosts);
                    vo.setTotalViews(acc.totalViews);
                    vo.setTotalComments(acc.totalComments);
                    return vo;
                })
                .sorted((a, b) -> {
                    int cmp = b.getPostCount() - a.getPostCount();
                    if (cmp != 0) return cmp;
                    return b.getTotalViews() - a.getTotalViews();
                })
                .limit(maxLimit)
                .collect(Collectors.toList());

        return result;
    }

    private List<List<String>> generateSubsets(List<String> items, int minSize, int maxSize) {
        List<List<String>> result = new java.util.ArrayList<>();
        int n = items.size();
        for (int size = minSize; size <= maxSize; size++) {
            generateSubsetsHelper(items, 0, size, new java.util.ArrayList<>(), result);
        }
        return result;
    }

    private void generateSubsetsHelper(List<String> items, int start, int size, List<String> current, List<List<String>> result) {
        if (current.size() == size) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < items.size(); i++) {
            current.add(items.get(i));
            generateSubsetsHelper(items, i + 1, size, current, result);
            current.remove(current.size() - 1);
        }
    }

    private String generateSchemeKey(List<String> models) {
        return String.join(" | ", models);
    }

    private static class CollocationAccumulator {
        List<String> accessoryModels;
        int itemCount;
        int postCount;
        Long categoryId;
        String categoryName;
        int totalViews;
        int totalComments;
        final List<PostVO> relatedPosts = new java.util.ArrayList<>();
    }

    private static class ModelAccumulator {
        String modelType;
        Long categoryId;
        String categoryName;
        int totalHelpPosts;
        int totalComments;
        int totalViews;
        final Map<String, Integer> themeCounts = new java.util.HashMap<>();
        final Map<String, List<String>> themeSymptoms = new java.util.HashMap<>();
        final List<PostVO> relatedPosts = new java.util.ArrayList<>();
    }
}
