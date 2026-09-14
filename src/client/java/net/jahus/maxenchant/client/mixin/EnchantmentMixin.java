package net.jahus.maxenchant.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.jahus.maxenchant.client.ClientNetworking;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Replaces the text-matching approach previously in TooltipHandler.java:
 * instead of comparing rendered tooltip lines against a re-derived name to
 * guess which enchantment they belong to, this hooks the exact point where
 * an enchantment's display name is built and appends the star there.
 *
 * Confirmed empirically (not guessed) via a temporary logging probe that
 * ItemEnchantmentsComponent#appendTooltip calls this method internally when
 * building tooltip lines. Because the hook is on getName itself rather than
 * on appendTooltip, the star also appears anywhere else this name is
 * rendered (enchanting table, anvil, etc.) - confirmed in the same probe
 * logs and intentional per user decision, not a side effect to work around.
 *
 * Uses MixinExtras' @WrapMethod rather than a plain @Inject(at = "RETURN"):
 * the nbteditor mod (present on the user's main instance) also mixes into
 * this exact method with @Inject(at = "HEAD", cancellable = true) and calls
 * setReturnValue() unconditionally to show its own "level/max" display.
 * That cancels execution before any @Inject(at = "RETURN") in another mod
 * ever runs - confirmed by cloning nbteditor's source
 * (mixin/toggled/EnchantmentMixin.java) rather than assumed. @WrapMethod
 * wraps the whole method regardless of what other mixins do inside it, so
 * original.call(...) below returns whatever the method would have produced
 * either way (vanilla text, or nbteditor's "level/max" text if nbteditor is
 * present) - this mod's star is appended on top of that, without needing to
 * know or duplicate either implementation's formatting logic.
 *
 * Target: Enchantment.getName(RegistryEntry<Enchantment>, int), static,
 * confirmed present under yarn 1.21.4+build.8 (TooltipHandler.java already
 * called this method statically and compiled against it).
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {

    private static final String STAR = " \u2605"; // space + ★ (filled star)

    @WrapMethod(
            method = "getName(Lnet/minecraft/registry/entry/RegistryEntry;I)Lnet/minecraft/text/Text;"
    )
    private static Text maxenchant$appendStarIfMaxed(
            RegistryEntry<Enchantment> entry,
            int level,
            Operation<Text> original
    ) {
        Text name = original.call(entry, level);

        if (!maxenchant$isMaxed(entry, level)) {
            return name;
        }
        return name.copy().append(Text.literal(STAR).formatted(Formatting.YELLOW));
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
