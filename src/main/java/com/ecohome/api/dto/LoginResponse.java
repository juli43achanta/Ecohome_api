package com.ecohome.api.dto;

public record LoginResponse(
        String token,
        String nombre,
        String email,
        String rol,
        Integer userId
) {}
