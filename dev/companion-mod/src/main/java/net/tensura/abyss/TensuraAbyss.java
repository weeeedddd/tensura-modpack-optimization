package net.tensura.abyss;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.tensura.abyss.command.ShadowCommands;
import net.tensura.abyss.guild.GuildEventHandler;
import net.tensura.abyss.registry.AbyssRaces;
import net.tensura.abyss.registry.ModArmorMaterials;
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
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // Custom-Rassen nativ in Tensuras Race-Registry anmelden (Weg B)
        AbyssRaces.register(modEventBus);
        LOGGER.info("[Tensura Abyss] {} Custom-Rassen zur Registrierung angemeldet.",
                AbyssRaces.registeredCount());

        // Server-/Game-Events (Gilden-Login-Tracking, Commands)
        NeoForge.EVENT_BUS.register(new GuildEventHandler());
        NeoForge.EVENT_BUS.register(new ShadowCommands());

        LOGGER.info("[Tensura Abyss] Companion mod initialised.");
    }
}
