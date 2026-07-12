package com.cosmocraft.trading_cells.feature.milkcookie.application.port.input;

import com.cosmocraft.trading_cells.feature.milkcookie.domain.model.MilkCookieCreationRequest;

public interface CreateMilkCookieUseCase {
    boolean canCreate(MilkCookieCreationRequest request);
}
