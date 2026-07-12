package com.cosmocraft.trading_cells.platform.neoforge.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class TradingCellPayloadRegistration {
    private TradingCellPayloadRegistration() {
    }

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ResetTradesPayload.TYPE, ResetTradesPayload.STREAM_CODEC, ResetTradesPayload::handle);
    }
}
