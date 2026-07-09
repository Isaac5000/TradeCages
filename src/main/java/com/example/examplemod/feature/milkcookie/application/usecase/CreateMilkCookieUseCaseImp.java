package com.example.examplemod.feature.milkcookie.application.usecase;

import com.example.examplemod.feature.milkcookie.application.port.input.CreateMilkCookieUseCase;
import com.example.examplemod.feature.milkcookie.domain.model.MilkCookieCreationRequest;

public final class CreateMilkCookieUseCaseImp implements CreateMilkCookieUseCase {
    @Override
    public boolean canCreate(MilkCookieCreationRequest request) {
        return request.holdingCookie() && request.targetIsAdultHorse();
    }
}
