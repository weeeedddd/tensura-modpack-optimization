package net.tensura.abyss.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tensura.abyss.guild.*;

/**
 * Server-seitige Verarbeitung von {@link ServerboundGuildInvitePacket}.
 * Reines Common-Code (keine Client-Imports) -> auf dem Dedicated Server sicher.
 */
public final class ServerGuildInviteHandler {
    private ServerGuildInviteHandler() {}

    public static void handle(ServerboundGuildInvitePacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) return;

            // Nur mit Tensura-Rasse (geheimes System) und in einer Gilde.
            if (!GuildManager.canUseGuildSystem(sender)) return;
            GuildSavedData data = GuildSavedData.get(sender.getServer());
            Guild g = data.guildOf(sender.getUUID());
            if (g == null) {
                sender.sendSystemMessage(Component.literal("§cYou are not in a guild."));
                return;
            }
            // Nur Leader/Vize duerfen einladen.
            GuildRank rank = g.members.get(sender.getUUID());
            if (rank == GuildRank.MEMBER) {
                sender.sendSystemMessage(Component.literal("§cOnly the leader/vice can invite."));
                return;
            }

            String targetName = payload.targetName().trim();
            ServerPlayer target = sender.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sender.sendSystemMessage(Component.literal("§cPlayer not online: " + targetName));
                return;
            }
            if (!GuildManager.canUseGuildSystem(target)) {
                sender.sendSystemMessage(Component.literal("§cThat player is not a Shadow Garden member."));
                return;
            }

            String note = payload.note() == null ? "" : payload.note().trim();
            GuildInviteManager.invite(target.getUUID(), g.name, note, sender.getName().getString());

            sender.sendSystemMessage(Component.literal("§dInvitation sent to " + targetName + "."));
            target.sendSystemMessage(Component.literal("§5§l» Shadow Garden «")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            target.sendSystemMessage(Component.literal("§7" + sender.getName().getString() +
                    " invites you to the guild \"" + g.name + "\"."));
            if (!note.isEmpty()) {
                target.sendSystemMessage(Component.literal("§d“" + note + "”"));
            }
            target.sendSystemMessage(Component.literal("§7Accept with §f/shadowguild accept"));
        });
    }
}
