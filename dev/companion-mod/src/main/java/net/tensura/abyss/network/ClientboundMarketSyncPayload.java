package net.tensura.abyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.tensura.abyss.TensuraAbyss;

/**
 * Server -> Client: aktueller Muenz-Stand.
 * open = true -> Schwarzmarkt-GUI oeffnen; false -> nur den offenen Screen aktualisieren.
 */
public record ClientboundMarketSyncPayload(int coins, boolean open) implements CustomPacketPayload {

    public static final Type<ClientboundMarketSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, "market_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMarketSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ClientboundMarketSyncPayload::coins,
                    ByteBufCodecs.BOOL, ClientboundMarketSyncPayload::open,
                    ClientboundMarketSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
