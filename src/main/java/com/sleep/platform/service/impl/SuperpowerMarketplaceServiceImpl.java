package com.sleep.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.sleep.platform.common.BizException;
import com.sleep.platform.domain.entity.SuperpowerCatalogEntity;
import com.sleep.platform.domain.entity.UserInstalledSuperpowerEntity;
import com.sleep.platform.domain.request.SuperpowerInstallRequest;
import com.sleep.platform.domain.response.InstalledSuperpowerResponse;
import com.sleep.platform.domain.response.SuperpowerCatalogItemResponse;
import com.sleep.platform.mapper.SuperpowerCatalogMapper;
import com.sleep.platform.mapper.UserInstalledSuperpowerMapper;
import com.sleep.platform.service.SuperpowerMarketplaceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SuperpowerMarketplaceServiceImpl implements SuperpowerMarketplaceService {

    private final SuperpowerCatalogMapper superpowerCatalogMapper;
    private final UserInstalledSuperpowerMapper userInstalledSuperpowerMapper;

    public SuperpowerMarketplaceServiceImpl(SuperpowerCatalogMapper superpowerCatalogMapper,
                                            UserInstalledSuperpowerMapper userInstalledSuperpowerMapper) {
        this.superpowerCatalogMapper = superpowerCatalogMapper;
        this.userInstalledSuperpowerMapper = userInstalledSuperpowerMapper;
    }

    @Override
    public List<SuperpowerCatalogItemResponse> listCatalog(String userId, String category, String tier) {
        LambdaQueryWrapper<SuperpowerCatalogEntity> query = new LambdaQueryWrapper<SuperpowerCatalogEntity>()
                .eq(SuperpowerCatalogEntity::getEnabled, true)
                .eq(StringUtils.hasText(category), SuperpowerCatalogEntity::getCategory, category)
                .eq(StringUtils.hasText(tier), SuperpowerCatalogEntity::getTier, tier)
                .orderByAsc(SuperpowerCatalogEntity::getCategory, SuperpowerCatalogEntity::getName);

        List<SuperpowerCatalogEntity> catalog = superpowerCatalogMapper.selectList(query);

        Set<String> installedKeys = listInstalledKeys(userId).stream().collect(Collectors.toSet());

        return catalog.stream()
                .map(e -> toCatalogItemResponse(e, installedKeys.contains(e.getSuperpowerKey())))
                .collect(Collectors.toList());
    }

    @Override
    public SuperpowerCatalogItemResponse getCatalogEntry(String superpowerKey, String userId) {
        SuperpowerCatalogEntity entity = superpowerCatalogMapper.selectOne(
                new LambdaQueryWrapper<SuperpowerCatalogEntity>()
                        .eq(SuperpowerCatalogEntity::getSuperpowerKey, superpowerKey)
                        .eq(SuperpowerCatalogEntity::getEnabled, true));
        if (entity == null) {
            throw new BizException(404, "Superpower not found: " + superpowerKey);
        }
        boolean installed = isInstalled(userId, superpowerKey);
        return toCatalogItemResponse(entity, installed);
    }

    @Override
    public InstalledSuperpowerResponse install(SuperpowerInstallRequest request) {
        if (!StringUtils.hasText(request.getUserId())) {
            throw new BizException(400, "userId is required");
        }
        if (!StringUtils.hasText(request.getSuperpowerKey())) {
            throw new BizException(400, "superpowerKey is required");
        }

        SuperpowerCatalogEntity catalog = superpowerCatalogMapper.selectOne(
                new LambdaQueryWrapper<SuperpowerCatalogEntity>()
                        .eq(SuperpowerCatalogEntity::getSuperpowerKey, request.getSuperpowerKey())
                        .eq(SuperpowerCatalogEntity::getEnabled, true));
        if (catalog == null) {
            throw new BizException(404, "Superpower not found: " + request.getSuperpowerKey());
        }

        UserInstalledSuperpowerEntity existing = findInstalled(request.getUserId(), request.getSuperpowerKey());
        if (existing != null) {
            existing.setConfigOverridesJson(request.getConfigOverridesJson());
            existing.setUpdatedAt(LocalDateTime.now());
            userInstalledSuperpowerMapper.updateById(existing);
            return toInstalledResponse(existing, catalog);
        }

        UserInstalledSuperpowerEntity entity = new UserInstalledSuperpowerEntity();
        entity.setId(IdWorker.getId());
        entity.setUserId(request.getUserId());
        entity.setSuperpowerKey(request.getSuperpowerKey());
        entity.setConfigOverridesJson(request.getConfigOverridesJson());
        entity.setInstalledAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        userInstalledSuperpowerMapper.insert(entity);
        return toInstalledResponse(entity, catalog);
    }

    @Override
    public void uninstall(String userId, String superpowerKey) {
        int deleted = userInstalledSuperpowerMapper.delete(
                new LambdaQueryWrapper<UserInstalledSuperpowerEntity>()
                        .eq(UserInstalledSuperpowerEntity::getUserId, userId)
                        .eq(UserInstalledSuperpowerEntity::getSuperpowerKey, superpowerKey));
        if (deleted == 0) {
            throw new BizException(404, "Superpower '" + superpowerKey + "' is not installed for user " + userId);
        }
    }

    @Override
    public List<InstalledSuperpowerResponse> listInstalled(String userId) {
        List<UserInstalledSuperpowerEntity> installed = userInstalledSuperpowerMapper.selectList(
                new LambdaQueryWrapper<UserInstalledSuperpowerEntity>()
                        .eq(UserInstalledSuperpowerEntity::getUserId, userId)
                        .orderByDesc(UserInstalledSuperpowerEntity::getInstalledAt));

        if (installed.isEmpty()) {
            return List.of();
        }

        Set<String> keys = installed.stream()
                .map(UserInstalledSuperpowerEntity::getSuperpowerKey)
                .collect(Collectors.toSet());

        Map<String, SuperpowerCatalogEntity> catalogMap = superpowerCatalogMapper.selectList(
                new LambdaQueryWrapper<SuperpowerCatalogEntity>()
                        .in(SuperpowerCatalogEntity::getSuperpowerKey, keys))
                .stream()
                .collect(Collectors.toMap(SuperpowerCatalogEntity::getSuperpowerKey, e -> e));

        return installed.stream()
                .map(e -> toInstalledResponse(e, catalogMap.get(e.getSuperpowerKey())))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listInstalledKeys(String userId) {
        if (!StringUtils.hasText(userId)) {
            return List.of();
        }
        return userInstalledSuperpowerMapper.selectList(
                new LambdaQueryWrapper<UserInstalledSuperpowerEntity>()
                        .eq(UserInstalledSuperpowerEntity::getUserId, userId)
                        .select(UserInstalledSuperpowerEntity::getSuperpowerKey))
                .stream()
                .map(UserInstalledSuperpowerEntity::getSuperpowerKey)
                .collect(Collectors.toList());
    }

    private boolean isInstalled(String userId, String superpowerKey) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        return findInstalled(userId, superpowerKey) != null;
    }

    private UserInstalledSuperpowerEntity findInstalled(String userId, String superpowerKey) {
        return userInstalledSuperpowerMapper.selectOne(
                new LambdaQueryWrapper<UserInstalledSuperpowerEntity>()
                        .eq(UserInstalledSuperpowerEntity::getUserId, userId)
                        .eq(UserInstalledSuperpowerEntity::getSuperpowerKey, superpowerKey));
    }

    private SuperpowerCatalogItemResponse toCatalogItemResponse(SuperpowerCatalogEntity entity, boolean installed) {
        SuperpowerCatalogItemResponse r = new SuperpowerCatalogItemResponse();
        r.setSuperpowerKey(entity.getSuperpowerKey());
        r.setName(entity.getName());
        r.setDescription(entity.getDescription());
        r.setCategory(entity.getCategory());
        r.setTier(entity.getTier());
        r.setVersion(entity.getVersion());
        r.setInstalled(installed);
        return r;
    }

    private InstalledSuperpowerResponse toInstalledResponse(UserInstalledSuperpowerEntity entity,
                                                             SuperpowerCatalogEntity catalog) {
        InstalledSuperpowerResponse r = new InstalledSuperpowerResponse();
        r.setSuperpowerKey(entity.getSuperpowerKey());
        r.setConfigOverridesJson(entity.getConfigOverridesJson());
        r.setInstalledAt(entity.getInstalledAt());
        if (catalog != null) {
            r.setName(catalog.getName());
            r.setCategory(catalog.getCategory());
            r.setTier(catalog.getTier());
        }
        return r;
    }
}
