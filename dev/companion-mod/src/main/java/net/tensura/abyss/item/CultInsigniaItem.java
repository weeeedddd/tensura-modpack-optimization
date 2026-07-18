package net.tensura.abyss.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * CULT OF DIABLOS INSIGNIA — consuming it invokes a "Cult Challenge".
 *
 * Right-click (server-side):
 *   • consumes 1 insignia and starts a 60 s cooldown
 *   • brands the player with the cult's omen (Darkness + Unluck, 60 s)
 *   • summons a high-tier cult strike squad around the player:
 *       1x Evoker   "Cult High Priest"  (tags: cult_of_diablos + cult_knight —
 *                     counts as a CULT LEADER kill for the stage-3 race gate
 *                     and yields the elite insignia drop)
 *       2x Vindicator "Diablos Knight"   (tag: cult_of_diablos)
 *       3x Pillager  "Diablos Cultist"   (tag: cult_of_diablos)
 *   • the existing cult drop handler (shadow_garden.js EntityEvents.death)
 *     turns those tags into insignia / progression drops on kill.
 */
public class CultInsigniaItem extends AbyssLoreItem {

    private static final int COOLDOWN_TICKS = 1200; // 60 s
    private static final int OMEN_TICKS = 1200;     // 60 s debuff

    public CultInsigniaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        ServerLevel server = (ServerLevel) level;

        // ── The cult's omen: the challenger is marked ──
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, OMEN_TICKS, 0));
        player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, OMEN_TICKS, 0));

        // ── Summon the strike squad in a ring around the player ──
        RandomSource random = server.getRandom();
        summonCultist(server, player, EntityType.EVOKER, "Cult High Priest",
                ChatFormatting.DARK_PURPLE, random, true);
        for (int i = 0; i < 2; i++) {
            summonCultist(server, player, EntityType.VINDICATOR, "Diablos Knight",
                    ChatFormatting.DARK_RED, random, false);
        }
        for (int i = 0; i < 3; i++) {
            summonCultist(server, player, EntityType.PILLAGER, "Diablos Cultist",
                    ChatFormatting.DARK_PURPLE, random, false);
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 4.0F, 0.8F);

        player.displayClientMessage(Component.literal(
                "The Cult of Diablos answers your challenge...")
                .withStyle(ChatFormatting.DARK_PURPLE), true);
        player.sendSystemMessage(Component.literal(
                "§8── §5§lCult Challenge§r §8──"));
        player.sendSystemMessage(Component.literal(
                "§7A cult strike squad has been drawn to your position."));
        player.sendSystemMessage(Component.literal(
                "§7Slay the §5Cult High Priest§7 to claim elite spoils."));

        // ── Costs ──
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    private static void summonCultist(ServerLevel level, Player player, EntityType<? extends AbstractIllager> type,
                                      String name, ChatFormatting color, RandomSource random, boolean leader) {
        AbstractIllager mob = type.create(level);
        if (mob == null) return;
        double angle = random.nextDouble() * Math.PI * 2.0;
        double dist = 6.0 + random.nextDouble() * 6.0;
        BlockPos pos = BlockPos.containing(
                player.getX() + Math.cos(angle) * dist,
                player.getY(),
                player.getZ() + Math.sin(angle) * dist);
        pos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        mob.setCustomName(Component.literal(name).withStyle(color));
        mob.setPersistenceRequired();
        mob.addTag("cult_of_diablos");
        if (leader) mob.addTag("cult_knight"); // leader credit + elite drops
        mob.setTarget(player);
        level.addFreshEntity(mob);
    }
}
