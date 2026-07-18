package net.tensura.abyss.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.tensura.abyss.bridge.TensuraBridge;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * SIGNATURE RECORD — a progression recorder.
 *
 * Right-click "signs" the record: it captures the player's current identity —
 * active race, magicules, existence points, dimension and date — into the
 * item's custom data. Each new signing overwrites the previous snapshot, so a
 * record doubles as a portable progress archive ("how far had I come when I
 * signed this?"). The tooltip renders the stored snapshot.
 *
 * Guild membership data written by {@code GuildManager.giveSignatureRecord}
 * (guild / member_number / rank) is preserved and shown alongside.
 */
public class SignatureRecordItem extends Item {

    public SignatureRecordItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        // ── Capture the snapshot ──
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();

        tag.putString("signed_by", player.getGameProfile().getName());
        ResourceLocation race = TensuraBridge.getRaceId(player);
        tag.putString("race", race != null ? race.toString() : "unawakened");
        tag.putLong("magicules", Math.round(TensuraBridge.getMagicules(player)));
        tag.putLong("ep", Math.round(TensuraBridge.getMaxEP(player)));
        tag.putString("dimension", player.level().dimension().location().toString());
        tag.putString("signed_on", LocalDate.now().toString());

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        level.playSound(null, player.blockPosition(),
                SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.displayClientMessage(Component.literal(
                "Signature recorded — your legend is written in shadow.")
                .withStyle(ChatFormatting.DARK_PURPLE), true);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data != null ? data.copyTag() : null;

        if (tag == null || tag.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.tensura_abyss.signature_record.blank")
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("Right-click to sign your current legend.")
                    .withStyle(ChatFormatting.GRAY));
            super.appendHoverText(stack, context, tooltip, flag);
            return;
        }

        NumberFormat fmt = NumberFormat.getIntegerInstance(Locale.US);

        // ── Guild block (written by GuildManager, if any) ──
        if (tag.contains("guild")) {
            tooltip.add(Component.literal("Guild: " + tag.getString("guild"))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (tag.contains("member_number")) {
            tooltip.add(Component.literal("Member #" + tag.getInt("member_number"))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (tag.contains("rank")) {
            tooltip.add(Component.literal("Adventurer Rank: " + tag.getString("rank"))
                    .withStyle(ChatFormatting.GOLD));
        }

        // ── Signed snapshot ──
        if (tag.contains("signed_by")) {
            tooltip.add(Component.literal("§8── §5Signed Legend §8──"));
            tooltip.add(Component.literal("Bearer: " + tag.getString("signed_by"))
                    .withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.literal("Race: " + prettyRace(tag.getString("race")))
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.literal("Magicules: " + fmt.format(tag.getLong("magicules")))
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Existence Points: " + fmt.format(tag.getLong("ep")))
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Signed " + tag.getString("signed_on")
                    + " · " + tag.getString("dimension"))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    /** "tensura_abyss:eminence_of_the_abyss" -> "Eminence Of The Abyss" */
    private static String prettyRace(String raceId) {
        int colon = raceId.indexOf(':');
        String path = colon >= 0 ? raceId.substring(colon + 1) : raceId;
        String[] words = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }
}
