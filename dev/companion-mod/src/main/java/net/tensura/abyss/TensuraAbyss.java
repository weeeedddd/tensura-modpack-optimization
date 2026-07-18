package net.tensura.abyss;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.tensura.abyss.command.ShadowCommands;
import net.tensura.abyss.event.ShadowGuildPermissionHandler;
import net.tensura.abyss.guild.GuildEventHandler;
import net.tensura.abyss.registry.AbyssRaces;
import net.tensura.abyss.registry.ModArmorMaterials;
import net.tensura.abyss.registry.ModBlocks;
import net.tensura.abyss.registry.ModCreativeTabs;
import net.tensura.abyss.registry.ModItems;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Companion-Mod fuer das Modpack "Tensura Abyss".
 *
 * Uebernimmt die "harten" Registrierungen (Items, Slime-Suit-Ruestung mit
 * echtem Armor-Material) und stellt das Shadow-Garden-Gilden-/Commission-
 * Backend bereit. Der Zugriff auf Tensura-Spielerdaten laeuft ueber
 * {@link net.tensura.abyss.bridge.TensuraBridge} (Reflection).
 */
@Mod(TensuraAbyss.MOD_ID)
public class TensuraAbyss {
    public static final String MOD_ID = "tensura_abyss";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TensuraAbyss(IEventBus modEventBus) {
        // Registry-Objekte am Mod-Event-Bus anmelden
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);      // vor den Items (BlockItem braucht den Block)
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // Custom-Rassen in die ManasCore/Tensura-Race-Registry eintragen (Weg B),
        // via Architectury-DeferredRegister.
        AbyssRaces.register();

        // Server-/Game-Events (Gilden-Login-Tracking, Commands)
        NeoForge.EVENT_BUS.register(new GuildEventHandler());
        NeoForge.EVENT_BUS.register(new ShadowCommands());

        // Architectury-Event: Rassenwechsel -> Gilden-Berechtigung (LuckPerms)
        ShadowGuildPermissionHandler.init();

        // Architectury-Event: Shadow-Pfad betreten -> Slime Sword vergeben
        net.tensura.abyss.event.ShadowGearHandler.init();

        LOGGER.info("[Tensura Abyss] Companion mod initialised.");
    }
}
