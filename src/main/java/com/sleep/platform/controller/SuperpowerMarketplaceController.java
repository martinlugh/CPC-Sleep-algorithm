package com.sleep.platform.controller;

import com.sleep.platform.common.ApiResponse;
import com.sleep.platform.domain.request.SuperpowerInstallRequest;
import com.sleep.platform.domain.response.InstalledSuperpowerResponse;
import com.sleep.platform.domain.response.SuperpowerCatalogItemResponse;
import com.sleep.platform.service.SuperpowerMarketplaceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/superpowers")
public class SuperpowerMarketplaceController {

    private final SuperpowerMarketplaceService superpowerMarketplaceService;

    public SuperpowerMarketplaceController(SuperpowerMarketplaceService superpowerMarketplaceService) {
        this.superpowerMarketplaceService = superpowerMarketplaceService;
    }

    @GetMapping("/catalog")
    public ApiResponse<List<SuperpowerCatalogItemResponse>> listCatalog(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tier) {
        return ApiResponse.success(superpowerMarketplaceService.listCatalog(userId, category, tier));
    }

    @GetMapping("/catalog/{superpowerKey}")
    public ApiResponse<SuperpowerCatalogItemResponse> getCatalogEntry(
            @PathVariable String superpowerKey,
            @RequestParam(required = false) String userId) {
        return ApiResponse.success(superpowerMarketplaceService.getCatalogEntry(superpowerKey, userId));
    }

    @PostMapping("/install")
    public ApiResponse<InstalledSuperpowerResponse> install(@RequestBody SuperpowerInstallRequest request) {
        return ApiResponse.success("Superpower installed successfully",
                superpowerMarketplaceService.install(request));
    }

    @DeleteMapping("/uninstall")
    public ApiResponse<Void> uninstall(
            @RequestParam String userId,
            @RequestParam String superpowerKey) {
        superpowerMarketplaceService.uninstall(userId, superpowerKey);
        return ApiResponse.success("Superpower uninstalled successfully", null);
    }

    @GetMapping("/installed")
    public ApiResponse<List<InstalledSuperpowerResponse>> listInstalled(@RequestParam String userId) {
        return ApiResponse.success(superpowerMarketplaceService.listInstalled(userId));
    }
}
