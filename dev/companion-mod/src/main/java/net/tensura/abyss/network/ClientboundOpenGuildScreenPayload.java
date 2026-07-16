package net.tensura.abyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.tensura.abyss.TensuraAbyss;

/**
 * Server -> Client: oeffnet das Gilden-Hauptmenue mit einer Momentaufnahme der
 * Gildendaten (damit der Client nichts direkt aus der SavedData lesen muss).
 */
public record ClientboundOpenGuildScreenPayload(
        String guildName,
        String memberRank,
        int memberNumber,
        String adventurerRank,
        int memberCount,
        int memberLimit
) implements CustomPacketPayload {

    public static final Type<ClientboundOpenGuildScreenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, "open_guild_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenGuildScreenPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ClientboundOpenGuildScreenPayload::guildName,
                    ByteBufCodecs.STRING_UTF8, ClientboundOpenGuildScreenPayload::memberRank,
                    ByteBufCodecs.VAR_INT,     ClientboundOpenGuildScreenPayload::memberNumber,
                    ByteBufCodecs.STRING_UTF8, ClientboundOpenGuildScreenPayload::adventurerRank,
                    ByteBufCodecs.VAR_INT,     ClientboundOpenGuildScreenPayload::memberCount,
                    ByteBufCodecs.VAR_INT,     ClientboundOpenGuildScreenPayload::memberLimit,
                    ClientboundOpenGuildScreenPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
