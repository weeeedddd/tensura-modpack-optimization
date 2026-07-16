package net.tensura.abyss.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tensura.abyss.commission.Commission;
import net.tensura.abyss.commission.CommissionManager;
import net.tensura.abyss.guild.*;
import net.tensura.abyss.network.ClientboundOpenGuildScreenPayload;

import java.util.UUID;

/**
 * Alle Shadow-Garden-Commands. Der komplette Baum ist per {@code requires(...)}
 * fuer Nicht-Tensura-Spieler UNSICHTBAR (kein Tab-Complete, kein Zugriff) —
 * damit ist das Gildensystem exklusiv fuer Possessed-oder-hoeher.
 */
public class ShadowCommands {

    @SubscribeEvent
    public void onRegister(RegisterCommandsEvent event) {
        // Gate: nur sichtbar/nutzbar mit Tensura-Rasse
        var gate = (java.util.function.Predicate<CommandSourceStack>) src ->
                src.getEntity() instanceof ServerPlayer sp && GuildManager.canUseGuildSystem(sp);

        LiteralArgumentBuilder<CommandSourceStack> shadow = Commands.literal("shadow")
                .requires(gate)
                // ── GUILD ──
                .then(Commands.literal("guild")
                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            String name = StringArgumentType.getString(ctx, "name");
                                            boolean ok = GuildManager.createGuild(p, name);
                                            if (!ok) p.sendSystemMessage(Component.literal(
                                                    "§cGilde konnte nicht erstellt werden (Name vergeben oder bereits in Gilde)."));
                                            return ok ? 1 : 0;
                                        })))
                        .then(Commands.literal("join")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            boolean ok = GuildManager.joinGuild(p, StringArgumentType.getString(ctx, "name"));
                                            if (!ok) p.sendSystemMessage(Component.literal("§cBeitritt fehlgeschlagen."));
                                            return ok ? 1 : 0;
                                        })))
                        .then(Commands.literal("leave")
                                .executes(ctx -> {
                                    GuildManager.leaveGuild(ctx.getSource().getPlayerOrException());
                                    return 1;
                                }))
                        .then(Commands.literal("info")
                                .executes(ctx -> guildInfo(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("open")
                                .executes(ctx -> openGui(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("accept")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    boolean ok = GuildInviteManager.accept(p);
                                    if (!ok) p.sendSystemMessage(Component.literal("§7Keine offene Einladung."));
                                    return ok ? 1 : 0;
                                })))
                // ── PARTY ──
                .then(Commands.literal("party")
                        .then(Commands.literal("create")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    PartyManager.create(p);
                                    p.sendSystemMessage(Component.literal("§dParty erstellt. Lade mit /shadow party invite <Spieler> ein."));
                                    return 1;
                                }))
                        .then(Commands.literal("invite")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(ctx -> partyInvite(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "player")))))
                        .then(Commands.literal("leave")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    PartyManager.leave(p);
                                    p.sendSystemMessage(Component.literal("§7Party verlassen."));
                                    return 1;
                                })))
                // ── COMMISSIONS ──
                .then(Commands.literal("commission")
                        .then(Commands.literal("list")
                                .executes(ctx -> commissionList(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("refill")
                                .requires(src -> src.hasPermission(2))
                                .executes(ctx -> {
                                    CommissionManager.refill(ctx.getSource().getLevel().getRandom(), 6);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal("§aCommission-Board aufgefuellt."), true);
                                    return 1;
                                })))
                // ── RADAR (Minimap-Fallback: listet Online-Gildenmitglieder) ──
                .then(Commands.literal("radar")
                        .executes(ctx -> radar(ctx.getSource().getPlayerOrException())));

        event.getDispatcher().register(shadow);

        // ── PARTY-CHAT: /p <message> ──
        event.getDispatcher().register(Commands.literal("p")
                .requires(gate)
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            PartyManager.chat(p.getServer(), p, StringArgumentType.getString(ctx, "message"));
                            return 1;
                        })));
    }

    /** Sendet eine Gilden-Momentaufnahme an den Client und oeffnet das GUI. */
    private int openGui(ServerPlayer p) {
        GuildSavedData data = GuildSavedData.get(p.getServer());
        Guild g = data.guildOf(p.getUUID());
        if (g == null) {
            p.sendSystemMessage(Component.literal("§7Du bist in keiner Gilde."));
            return 0;
        }
        GuildRank rank = g.members.get(p.getUUID());
        int number = g.memberNumbers.getOrDefault(p.getUUID(), 0);
        PacketDistributor.sendToPlayer(p, new ClientboundOpenGuildScreenPayload(
                g.name,
                rank == null ? "Member" : rank.display(),
                number,
                g.rank().name(),
                g.members.size(),
                g.memberLimit));
        return 1;
    }

    private int guildInfo(ServerPlayer p) {
        GuildSavedData data = GuildSavedData.get(p.getServer());
        Guild g = data.guildOf(p.getUUID());
        if (g == null) {
            p.sendSystemMessage(Component.literal("§7Du bist in keiner Gilde."));
            return 0;
        }
        p.sendSystemMessage(Component.literal("§b§lGilde: " + g.name));
        p.sendSystemMessage(Component.literal("§7Abenteurer-Rang: §6" + g.rank().name() +
                " §7(EXP " + g.guildExp + ")"));
        p.sendSystemMessage(Component.literal("§7Mitglieder: " + g.members.size() + "/" + g.memberLimit));
        Integer num = g.memberNumbers.get(p.getUUID());
        if (num != null) p.sendSystemMessage(Component.literal("§7Deine Nummer: §fMember #" + num));
        return 1;
    }

    private int partyInvite(CommandSourceStack src, String targetName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer leader = src.getPlayerOrException();
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            leader.sendSystemMessage(Component.literal("§cSpieler nicht gefunden/online."));
            return 0;
        }
        if (!GuildManager.canUseGuildSystem(target)) {
            leader.sendSystemMessage(Component.literal("§cDieser Spieler ist kein Shadow-Garden-Mitglied."));
            return 0;
        }
        Party party = PartyManager.partyOf(leader.getUUID());
        if (party == null) party = PartyManager.create(leader);
        boolean ok = PartyManager.join(target, party);
        if (ok) {
            leader.sendSystemMessage(Component.literal("§d" + target.getName().getString() + " ist der Party beigetreten."));
            target.sendSystemMessage(Component.literal("§dDu bist der Party von " + leader.getName().getString() + " beigetreten."));
        }
        return ok ? 1 : 0;
    }

    private int commissionList(ServerPlayer p) {
        CommissionManager.refill(p.serverLevel().getRandom(), 6);
        p.sendSystemMessage(Component.literal("§b§lShadow Garden — Commission Board:"));
        for (Commission c : CommissionManager.board()) {
            p.sendSystemMessage(Component.literal("§7- §f" + c.title +
                    " §8[" + c.type.name() + "] §a+" + c.rewardExp + " EXP" +
                    (c.partyOnly ? " §d(Party)" : "")));
        }
        return 1;
    }

    private int radar(ServerPlayer p) {
        MinecraftServer server = p.getServer();
        GuildSavedData data = GuildSavedData.get(server);
        Guild g = data.guildOf(p.getUUID());
        if (g == null) {
            p.sendSystemMessage(Component.literal("§7Keine Gilde — kein Radar."));
            return 0;
        }
        p.sendSystemMessage(Component.literal("§b§lShadow Radar — Gildenmitglieder online:"));
        for (UUID m : g.members.keySet()) {
            ServerPlayer mp = server.getPlayerList().getPlayer(m);
            if (mp != null && mp != p) {
                p.sendSystemMessage(Component.literal(String.format("§7- §f%s §8@ %d, %d, %d (%s)",
                        mp.getName().getString(), mp.getBlockX(), mp.getBlockY(), mp.getBlockZ(),
                        mp.level().dimension().location())));
            }
        }
        // Fuer echte Icons: siehe MinimapIntegration (Xaero/JourneyMap-Waypoint-API).
        return 1;
    }
}
