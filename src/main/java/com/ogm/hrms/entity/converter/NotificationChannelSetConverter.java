package com.ogm.hrms.entity.converter;

import com.ogm.hrms.enums.NotificationChannel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persists an ordered {@link NotificationChannel} set as a comma-separated column (e.g.
 * {@code "IN_APP,EMAIL"}), keeping channel configuration in a single column while remaining a typed
 * {@code Set} in the domain. Unknown tokens are ignored defensively so a stale row never breaks load.
 */
@Converter
public class NotificationChannelSetConverter implements AttributeConverter<Set<NotificationChannel>, String> {

    @Override
    public String convertToDatabaseColumn(Set<NotificationChannel> channels) {
        if (channels == null || channels.isEmpty()) {
            return "";
        }
        return channels.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    @Override
    public Set<NotificationChannel> convertToEntityAttribute(String dbData) {
        Set<NotificationChannel> result = new LinkedHashSet<>();
        if (dbData == null || dbData.isBlank()) {
            return result;
        }
        Arrays.stream(dbData.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .forEach(token -> {
                    try {
                        result.add(NotificationChannel.valueOf(token));
                    } catch (IllegalArgumentException ignored) {
                        // Skip tokens no longer backed by the enum rather than failing the load.
                    }
                });
        return result;
    }
}
