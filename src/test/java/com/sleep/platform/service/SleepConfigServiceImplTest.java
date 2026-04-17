package com.sleep.platform.service;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.entity.SleepAlgorithmConfigEntity;
import com.sleep.platform.domain.request.SleepConfigUpdateRequest;
import com.sleep.platform.domain.response.SleepConfigResponse;
import com.sleep.platform.mapper.SleepAlgorithmConfigMapper;
import com.sleep.platform.service.impl.SleepConfigServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SleepConfigServiceImplTest {

    @Test
    void shouldReadAndUpdateConfig() {
        SleepAnalysisProperties properties = new SleepAnalysisProperties();
        properties.setCurrentModelVersion("V1");
        properties.setCurrentRuleVersion("R1");
        properties.setMinValidRriCount(100);
        SleepAlgorithmConfigMapper mapper = Mockito.mock(SleepAlgorithmConfigMapper.class);
        Mockito.when(mapper.selectOne(Mockito.any())).thenReturn(null);

        SleepConfigServiceImpl service = new SleepConfigServiceImpl(properties, mapper);
        SleepConfigResponse current = service.getCurrentConfig();
        Assertions.assertEquals("V1", current.getCurrentModelVersion());

        SleepConfigUpdateRequest request = new SleepConfigUpdateRequest();
        request.setConfigKey("currentModelVersion");
        request.setConfigValue("V2");
        request.setConfigDesc("更新模型版本");
        request.setEnabled(true);
        SleepConfigResponse updated = service.updateConfig(request);
        Assertions.assertEquals("V2", updated.getCurrentModelVersion());
        Mockito.verify(mapper, Mockito.times(1)).insert(Mockito.any(SleepAlgorithmConfigEntity.class));
    }
}
