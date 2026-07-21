package com.ogm.hrms.service;

import com.ogm.hrms.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;

/**
 * Decides whether a request originates from the office network, using the configurable
 * {@code attendance.office-ip-allowlist} setting (comma-separated exact IPs and/or IPv4 CIDR ranges).
 * This is the Wi-Fi/IP basis for office attendance — there is no GPS/geolocation (project-rules.md).
 */
@Service
public class OfficeNetworkService {

    private static final String ALLOWLIST_KEY = "attendance.office-ip-allowlist";

    private final SystemSettingRepository systemSettingRepository;

    public OfficeNetworkService(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    public boolean isOfficeIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String allowlist = systemSettingRepository.findBySettingKey(ALLOWLIST_KEY)
                .map(s -> s.getSettingValue())
                .orElse("");
        if (allowlist == null || allowlist.isBlank()) {
            return false;
        }
        for (String raw : allowlist.split(",")) {
            String entry = raw.trim();
            if (entry.isEmpty()) {
                continue;
            }
            if (entry.contains("/")) {
                if (matchesCidr(ip, entry)) {
                    return true;
                }
            } else if (entry.equals(ip)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            int prefix = Integer.parseInt(parts[1]);
            long ipLong = ipv4ToLong(ip);
            long netLong = ipv4ToLong(parts[0]);
            if (ipLong < 0 || netLong < 0 || prefix < 0 || prefix > 32) {
                return false;
            }
            long mask = prefix == 0 ? 0L : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
            return (ipLong & mask) == (netLong & mask);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private long ipv4ToLong(String ip) {
        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            return -1;
        }
        long result = 0;
        for (String octet : octets) {
            int value = Integer.parseInt(octet);
            if (value < 0 || value > 255) {
                return -1;
            }
            result = (result << 8) | value;
        }
        return result;
    }
}
