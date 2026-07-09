package com.example.examplemod.feature.milkcookie.application.port.input;

import com.example.examplemod.feature.milkcookie.domain.model.MilkCookieCreationRequest;

public interface CreateMilkCookieUseCase {
    boolean canCreate(MilkCookieCreationRequest request);
}
