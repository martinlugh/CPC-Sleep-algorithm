package com.sleep.platform.service;

import com.sleep.platform.domain.request.SuperpowerInstallRequest;
import com.sleep.platform.domain.response.InstalledSuperpowerResponse;
import com.sleep.platform.domain.response.SuperpowerCatalogItemResponse;

import java.util.List;

public interface SuperpowerMarketplaceService {

    List<SuperpowerCatalogItemResponse> listCatalog(String userId, String category, String tier);

    SuperpowerCatalogItemResponse getCatalogEntry(String superpowerKey, String userId);

    InstalledSuperpowerResponse install(SuperpowerInstallRequest request);

    void uninstall(String userId, String superpowerKey);

    List<InstalledSuperpowerResponse> listInstalled(String userId);

    List<String> listInstalledKeys(String userId);
}
