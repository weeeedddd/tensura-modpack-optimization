package net.tensura.abyss.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tensura.abyss.TensuraAbyss;

/**
 * Bloecke der Companion-Mod. Aktuell nur der Abyss-Portal-Rahmen (craftbar,
 * per Rechtsklick mit Dunklem Aether oeffnet er die Shadow-Abyss-Dimension —
 * Logik in overrides/kubejs/server_scripts/shadow_abyss_portal.js).
 */
public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TensuraAbyss.MOD_ID);

    public static final DeferredBlock<Block> ABYSS_PORTAL_FRAME = BLOCKS.registerSimpleBlock(
            "abyss_portal_frame",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(25.0F, 1200.0F)          // Haerte / Explosions-Widerstand
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 6)            // schwaches Glimmen (~0.4)
    );
}
