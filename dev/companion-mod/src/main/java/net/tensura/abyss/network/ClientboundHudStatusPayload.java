package net.tensura.abyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.tensura.abyss.TensuraAbyss;

/**
 * Server -> Client: lightweight status sync for the sidebar HUD.
 * Carries only what the client cannot read locally (guild membership);
 * race/magicules/EP come from ManasCore's client-synced storages and the
 * shadow rank from the vanilla team (both already on the client).
 *
 * Sent on login and whenever guild membership changes.
 */
public record ClientboundHudStatusPayload(String guildName) implements CustomPacketPayload {

    /** Marker for "not in a guild". */
    public static final String NO_GUILD = "";

    public static final Type<ClientboundHudStatusPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TensuraAbyss.MOD_ID, "hud_status"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHudStatusPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ClientboundHudStatusPayload::guildName,
                    ClientboundHudStatusPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
