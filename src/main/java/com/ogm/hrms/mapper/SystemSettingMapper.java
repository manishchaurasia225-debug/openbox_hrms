package com.ogm.hrms.mapper;

import com.ogm.hrms.dto.org.SystemSettingResponse;
import com.ogm.hrms.entity.SystemSetting;
import org.springframework.stereotype.Component;

/** Maps {@link SystemSetting} entities to {@link SystemSettingResponse} DTOs. */
@Component
public class SystemSettingMapper {

    public SystemSettingResponse toResponse(SystemSetting setting) {
        return new SystemSettingResponse(
                setting.getId(),
                setting.getSettingKey(),
                setting.getSettingValue(),
                setting.getCategory(),
                setting.getDescription(),
                setting.isEditable(),
                setting.getUpdatedAt());
    }
}
