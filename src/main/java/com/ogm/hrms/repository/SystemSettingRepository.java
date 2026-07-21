package com.ogm.hrms.repository;

import com.ogm.hrms.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    Optional<SystemSetting> findBySettingKey(String settingKey);

    boolean existsBySettingKey(String settingKey);

    List<SystemSetting> findByDeletedFalseOrderBySettingKeyAsc();

    List<SystemSetting> findByCategoryIgnoreCaseAndDeletedFalseOrderBySettingKeyAsc(String category);
}
