package net.tensura.abyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.tensura.abyss.TensuraAbyss;

/**
 * Client -> Server: eine Gilden-Einladung (Zielspieler + geheime Notiz),
 * gesendet vom {@code GuildInviteScreen} beim Klick auf "Senden".
 */
public record ServerboundGuildInvitePacket(String targetName, String note)
        implements CustomPacketPayload {

    public static final Type<ServerboundGuildInvitePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, "guild_invite"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundGuildInvitePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ServerboundGuildInvitePacket::targetName,
                    ByteBufCodecs.STRING_UTF8, ServerboundGuildInvitePacket::note,
                    ServerboundGuildInvitePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
