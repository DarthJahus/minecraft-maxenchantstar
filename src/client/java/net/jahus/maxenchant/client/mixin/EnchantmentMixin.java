package net.jahus.maxenchant.client.mixin;

import net.jahus.maxenchant.client.ClientNetworking;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the text-matching approach previously in TooltipHandler.java:
 * instead of comparing rendered tooltip lines against a re-derived name to
 * guess which enchantment they belong to, this hooks the exact point where
 * an enchantment's display name is built and appends the star there.
 *
 * Confirmed empirically (not guessed) via a temporary logging probe that
 * ItemEnchantmentsComponent#appendTooltip calls this method internally when
 * building tooltip lines - see project history for the probe and its log
 * output. Because the hook is on getName itself rather than on appendTooltip,
 * the star also appears anywhere else this name is rendered (enchanting
 * table, anvil, etc.) - confirmed in the same probe logs and intentional
 * per user decision, not a side effect to work around.
 *
 * Target: Enchantment.getName(RegistryEntry<Enchantment>, int), static,
 * confirmed present under yarn 1.21.4+build.8 (TooltipHandler.java already
 * called this method statically and compiled against it).
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {

    private static final String STAR = " \u2605"; // space + ★ (filled star)

    @Inject(
            method = "getName(Lnet/minecraft/registry/entry/RegistryEntry;I)Lnet/minecraft/text/Text;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void maxenchant$appendStarIfMaxed(
            RegistryEntry<Enchantment> entry,
            int level,
            CallbackInfoReturnable<Text> cir
    ) {
        if (!maxenchant$isMaxed(entry, level)) {
            return;
        }
        Text original = cir.getReturnValue();
        cir.setReturnValue(original.copy().append(Text.literal(STAR).formatted(Formatting.YELLOW)));
    }

    /**
     * Same effective-max-level logic previously duplicated in
     * TooltipHandler.java: prefer the server-provided value
     * (nomaxenchant's effective_max_levels, when connected to a server
     * running it), fall back to the local vanilla registry otherwise.
     */
    private static boolean maxenchant$isMaxed(RegistryEntry<Enchantment> entry, int level) {
        return entry.getKey()
                .map(key -> {
                    Integer serverMax = ClientNetworking.getServerMaxLevel(key.getValue());
                    int maxLevel = serverMax != null ? serverMax : entry.value().getMaxLevel();
                    return level >= maxLevel;
                })
                .orElse(false);
    }
}
