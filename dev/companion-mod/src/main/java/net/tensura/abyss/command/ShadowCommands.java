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
 * All Shadow Garden commands.
 *
 * DISCOVERABILITY FIX: the command tree used to be hidden behind a
 * {@code requires(...)} race gate — for players whose race the bridge could
 * not read, {@code /shadow} was invisible and unfindable. The tree is now
 * ALWAYS visible; membership is checked at execution time with a clear
 * English message instead.
 *
 * Entry points to the guild GUI:
 *   /shadowguild            (opens the GUI directly)
 *   /shadowguild open|menu  (same)
 *   /shadow guild open      (classic path)
 */
public class ShadowCommands {

    @SubscribeEvent
    public void onRegister(RegisterCommandsEvent event) {
        // ── Classic root: /shadow ──
        LiteralArgumentBuilder<CommandSourceStack> shadow = Commands.literal("shadow")
                .then(buildGuildTree("guild"))
                // ── PARTY ──
                .then(Commands.literal("party")
                        .then(Commands.literal("create")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    if (!requireMember(p)) return 0;
                                    PartyManager.create(p);
                                    p.sendSystemMessage(Component.literal("§dParty created. Invite with /shadow party invite <player>."));
                                    return 1;
                                }))
                        .then(Commands.literal("invite")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(ctx -> partyInvite(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "player")))))
                        .then(Commands.literal("leave")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    if (!requireMember(p)) return 0;
                                    PartyManager.leave(p);
                                    p.sendSystemMessage(Component.literal("§7You left the party."));
                                    return 1;
                                })))
                // ── COMMISSIONS ──
                .then(Commands.literal("commission")
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    if (!requireMember(p)) return 0;
                                    return commissionList(p);
                                }))
                        .then(Commands.literal("refill")
                                .requires(src -> src.hasPermission(2))
                                .executes(ctx -> {
                                    CommissionManager.refill(ctx.getSource().getLevel().getRandom(), 6);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal("§aCommission board refilled."), true);
                                    return 1;
                                })))
                // ── RADAR (minimap fallback: lists online guild members) ──
                .then(Commands.literal("radar")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!requireMember(p)) return 0;
                            return radar(p);
                        }));

        event.getDispatcher().register(shadow);

        // ── Dedicated, easily findable guild root: /shadowguild ──
        LiteralArgumentBuilder<CommandSourceStack> shadowGuild = buildGuildTree("shadowguild")
                // bare "/shadowguild" opens the GUI directly
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    if (!requireMember(p)) return 0;
                    return openGui(p);
                })
                // "/shadowguild menu" as a second obvious alias for "open"
                .then(Commands.literal("menu")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!requireMember(p)) return 0;
                            return openGui(p);
                        }));
        event.getDispatcher().register(shadowGuild);

        // ── PARTY CHAT: /p <message> ──
        event.getDispatcher().register(Commands.literal("p")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!requireMember(p)) return 0;
                            PartyManager.chat(p.getServer(), p, StringArgumentType.getString(ctx, "message"));
                            return 1;
                        })));
    }

    /**
     * Builds the guild subcommand tree (create/join/leave/info/open/accept).
     * Called once per root literal so both {@code /shadow guild ...} and
     * {@code /shadowguild ...} carry identical, independent nodes.
     */
    private LiteralArgumentBuilder<CommandSourceStack> buildGuildTree(String literal) {
        return Commands.literal(literal)
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    if (!requireMember(p)) return 0;
                                    String name = StringArgumentType.getString(ctx, "name");
                                    boolean ok = GuildManager.createGuild(p, name);
                                    if (!ok) p.sendSystemMessage(Component.literal(
                                            "§cCould not create the guild (name taken or you are already in a guild)."));
                                    return ok ? 1 : 0;
                                })))
                .then(Commands.literal("join")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    if (!requireMember(p)) return 0;
                                    boolean ok = GuildManager.joinGuild(p, StringArgumentType.getString(ctx, "name"));
                                    if (!ok) p.sendSystemMessage(Component.literal("§cCould not join that guild."));
                                    return ok ? 1 : 0;
                                })))
                .then(Commands.literal("leave")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!requireMember(p)) return 0;
                            GuildManager.leaveGuild(p);
                            return 1;
                        }))
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!requireMember(p)) return 0;
                            return guildInfo(p);
                        }))
                .then(Commands.literal("open")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!requireMember(p)) return 0;
                            return openGui(p);
                        }))
                .then(Commands.literal("accept")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            if (!requireMember(p)) return 0;
                            boolean ok = GuildInviteManager.accept(p);
                            if (!ok) p.sendSystemMessage(Component.literal("§7No pending invitation."));
                            return ok ? 1 : 0;
                        }));
    }

    /**
     * Execute-time membership gate with a clear, discoverable error message
     * (replaces the old invisible {@code requires(...)} predicate).
     */
    private static boolean requireMember(ServerPlayer p) {
        if (GuildManager.canUseGuildSystem(p)) return true;
        p.sendSystemMessage(Component.literal(
                "§8The Shadow Garden does not answer. §7You need an awakened Tensura race to use the guild system."));
        return false;
    }

    /** Sends a guild snapshot to the client and opens the GUI. */
    private int openGui(ServerPlayer p) {
        GuildSavedData data = GuildSavedData.get(p.getServer());
        Guild g = data.guildOf(p.getUUID());
        if (g == null) {
            p.sendSystemMessage(Component.literal("§7You are not in a guild yet. §8Create one with §f/shadowguild create <name>§8."));
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
            p.sendSystemMessage(Component.literal("§7You are not in a guild yet. §8Create one with §f/shadowguild create <name>§8."));
            return 0;
        }
        p.sendSystemMessage(Component.literal("§5§lGuild: " + g.name));
        p.sendSystemMessage(Component.literal("§7Adventurer rank: §6" + g.rank().name() +
                " §7(EXP " + g.guildExp + ")"));
        p.sendSystemMessage(Component.literal("§7Members: " + g.members.size() + "/" + g.memberLimit));
        Integer num = g.memberNumbers.get(p.getUUID());
        if (num != null) p.sendSystemMessage(Component.literal("§7Your number: §fMember #" + num));
        return 1;
    }

    private int partyInvite(CommandSourceStack src, String targetName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer leader = src.getPlayerOrException();
        if (!requireMember(leader)) return 0;
        ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            leader.sendSystemMessage(Component.literal("§cPlayer not found/online."));
            return 0;
        }
        if (!GuildManager.canUseGuildSystem(target)) {
            leader.sendSystemMessage(Component.literal("§cThat player is not a Shadow Garden member."));
            return 0;
        }
        Party party = PartyManager.partyOf(leader.getUUID());
        if (party == null) party = PartyManager.create(leader);
        boolean ok = PartyManager.join(target, party);
        if (ok) {
            leader.sendSystemMessage(Component.literal("§d" + target.getName().getString() + " joined the party."));
            target.sendSystemMessage(Component.literal("§dYou joined " + leader.getName().getString() + "'s party."));
        }
        return ok ? 1 : 0;
    }

    private int commissionList(ServerPlayer p) {
        CommissionManager.refill(p.serverLevel().getRandom(), 6);
        p.sendSystemMessage(Component.literal("§5§lShadow Garden — Commission Board:"));
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
            p.sendSystemMessage(Component.literal("§7No guild — no radar."));
            return 0;
        }
        p.sendSystemMessage(Component.literal("§5§lShadow Radar — guild members online:"));
        for (UUID m : g.members.keySet()) {
            ServerPlayer mp = server.getPlayerList().getPlayer(m);
            if (mp != null && mp != p) {
                p.sendSystemMessage(Component.literal(String.format("§7- §f%s §8@ %d, %d, %d (%s)",
                        mp.getName().getString(), mp.getBlockX(), mp.getBlockY(), mp.getBlockZ(),
                        mp.level().dimension().location())));
            }
        }
        // For real map icons: see MinimapIntegration (Xaero/JourneyMap waypoint API).
        return 1;
    }
}
