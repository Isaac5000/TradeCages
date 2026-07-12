package com.cosmocraft.trading_cells.feature.milkcookie.application.usecase;

import com.cosmocraft.trading_cells.feature.milkcookie.application.port.input.CreateMilkCookieUseCase;
import com.cosmocraft.trading_cells.feature.milkcookie.domain.model.MilkCookieCreationRequest;

public final class CreateMilkCookieUseCaseImp implements CreateMilkCookieUseCase {
    @Override
    public boolean canCreate(MilkCookieCreationRequest request) {
        return request.holdingCookie() && request.targetIsAdultHorse();
    }
}
