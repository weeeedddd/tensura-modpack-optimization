package net.tensura.abyss.client;

/**
 * Client-side cache for HUD data that arrives via
 * {@link net.tensura.abyss.network.ClientboundHudStatusPayload}.
 * Only ever touched on the client.
 */
public final class ClientHudState {
    private ClientHudState() {}

    private static volatile String guildName = "";

    public static String guildName() {
        return guildName;
    }

    public static void setGuildName(String name) {
        guildName = name == null ? "" : name;
    }
}
