package net.tensura.abyss.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tensura.abyss.client.screen.GuildMainScreen;
import net.tensura.abyss.network.ClientboundOpenGuildScreenPayload;

/**
 * Client-seitige Payload-Verarbeitung. Wird NUR auf dem Client geladen
 * (siehe Dist-Guard in NetworkHandler).
 */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handleOpenGuild(ClientboundOpenGuildScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                Minecraft.getInstance().setScreen(new GuildMainScreen(payload)));
    }
}
