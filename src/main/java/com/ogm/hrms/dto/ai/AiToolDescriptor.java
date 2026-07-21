package com.ogm.hrms.dto.ai;

/** API view describing an available AI tool and the authority required to use it. */
public record AiToolDescriptor(String name, String description, String requiredAuthority) {
}
