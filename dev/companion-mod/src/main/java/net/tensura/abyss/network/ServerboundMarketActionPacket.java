package net.tensura.abyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.tensura.abyss.TensuraAbyss;

/**
 * Client -> Server: eine Schwarzmarkt-Aktion.
 *   action 0  = Markt oeffnen (Server antwortet mit Sync+open)
 *   action 1  = Erzeugnisse -> Muenzen umwandeln
 *   action >=10 = Kauf von Angebot (action - 10)
 */
public record ServerboundMarketActionPacket(int action) implements CustomPacketPayload {

    public static final int OPEN = 0;
    public static final int CONVERT = 1;
    public static final int BUY_BASE = 10;

    public static final Type<ServerboundMarketActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, "market_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMarketActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerboundMarketActionPacket::action,
                    ServerboundMarketActionPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
