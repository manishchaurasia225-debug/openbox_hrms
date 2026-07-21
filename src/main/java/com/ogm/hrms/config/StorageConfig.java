package com.ogm.hrms.config;

import com.ogm.hrms.storage.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Registers {@link StorageProperties} for the document storage backend. */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {
}
