package net.tensura.abyss.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tensura.abyss.guild.GuildManager;
import net.tensura.abyss.market.MarketManager;

/**
 * Server-seitige Verarbeitung der Schwarzmarkt-Aktionen. Common-Code.
 * Der Markt ist Teil des geheimen Gildensystems -> nur mit Tensura-Rasse.
 */
public final class MarketActionHandler {
    private MarketActionHandler() {}

    public static void handle(ServerboundMarketActionPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!GuildManager.canUseGuildSystem(player)) return;

            int action = payload.action();
            boolean open = false;

            if (action == ServerboundMarketActionPacket.OPEN) {
                open = true;
            } else if (action == ServerboundMarketActionPacket.CONVERT) {
                MarketManager.convertProducts(player);
            } else if (action >= ServerboundMarketActionPacket.BUY_BASE) {
                MarketManager.buy(player, action - ServerboundMarketActionPacket.BUY_BASE);
            }

            long coins = MarketManager.getCoins(player);
            PacketDistributor.sendToPlayer(player,
                    new ClientboundMarketSyncPayload((int) Math.min(Integer.MAX_VALUE, coins), open));
        });
    }
}
