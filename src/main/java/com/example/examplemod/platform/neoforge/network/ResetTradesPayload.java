package com.example.examplemod.platform.neoforge.network;

import com.example.examplemod.feature.tradecages.adapters.input.VillagerTradingCellBlockEntity;
import com.example.examplemod.platform.neoforge.bootstrap.TradingCells;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResetTradesPayload() implements CustomPacketPayload {
    public static final ResetTradesPayload INSTANCE = new ResetTradesPayload();
    public static final CustomPacketPayload.Type<ResetTradesPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TradingCells.MOD_ID, "reset_trades"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetTradesPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResetTradesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> VillagerTradingCellBlockEntity.handleResetTradesRequest(context.player()));
    }
}
