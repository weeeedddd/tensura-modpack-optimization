/*
 * OPTIONAL — nur noetig, wenn die drei Abyss-Biome ZUSAETZLICH in der
 * normalen Overworld auftauchen sollen. Fuer die eigene Dimension
 * "Shadow Abyss" wird DIESE Datei NICHT gebraucht (dort listet der
 * multi_noise biome_source die Biome direkt).
 *
 * Dies ist echter Java-Mod-Code (NeoForge 1.21.1 + TerraBlender). KubeJS
 * kann TerraBlender-Regionen nicht registrieren. Zum Einsatz muss dieser
 * Code in einen kleinen Companion-Mod kompiliert werden.
 *
 * TerraBlender-API-Namen koennen je nach TB-Version leicht abweichen —
 * gegen die installierte TerraBlender-Version pruefen.
 */
package net.tensura.abyss.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class ShadowAbyssRegion extends Region {

    // Biome-Keys der drei Abyss-Biome (Namespace = tensura_abyss)
    public static final ResourceKey<Biome> SLIME_ROT_MARSHES =
        ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("tensura_abyss", "slime_rot_marshes"));
    public static final ResourceKey<Biome> WHISPERING_VOID =
        ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("tensura_abyss", "whispering_void"));
    public static final ResourceKey<Biome> RUINED_SANCTUARY =
        ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("tensura_abyss", "ruined_sanctuary"));

    public ShadowAbyssRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(net.minecraft.core.Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // Beispielhafte Climate-Parameter (an den gewuenschten Anteil anpassen).
        this.addBiome(mapper,
            Climate.parameters(
                Climate.Parameter.span(-1.0F, 0.1F),   // temperature
                Climate.Parameter.span(-1.0F, 1.0F),   // humidity
                Climate.Parameter.span(-1.0F, 1.0F),   // continentalness
                Climate.Parameter.span(-1.0F, 1.0F),   // erosion
                Climate.Parameter.point(0.0F),         // depth
                Climate.Parameter.span(-1.0F, 1.0F),   // weirdness
                0.0F),                                  // offset
            SLIME_ROT_MARSHES);

        this.addBiome(mapper,
            Climate.parameters(
                Climate.Parameter.span(0.1F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.point(0.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                0.0F),
            WHISPERING_VOID);

        this.addBiome(mapper,
            Climate.parameters(
                Climate.Parameter.span(-0.05F, 0.05F),
                Climate.Parameter.span(-0.2F, 0.2F),
                Climate.Parameter.span(0.3F, 1.0F),
                Climate.Parameter.span(-1.0F, 0.0F),
                Climate.Parameter.point(0.0F),
                Climate.Parameter.span(-0.1F, 0.1F),
                0.0F),
            RUINED_SANCTUARY);
    }

    /*
     * Registrierung (z.B. in einem FMLCommonSetupEvent-Handler):
     *
     *   Regions.register(new ShadowAbyssRegion(
     *       ResourceLocation.fromNamespaceAndPath("tensura_abyss", "overworld_region"), 4));
     *
     * Weight 4 = moderater Anteil. Nur registrieren, wenn die Biome in der
     * Overworld erscheinen sollen.
     */
}
