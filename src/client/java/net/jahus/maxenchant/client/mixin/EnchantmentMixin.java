package net.jahus.maxenchant.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.jahus.maxenchant.client.ClientNetworking;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Appends ★ when the displayed level is at the effective max.
 * Prefers nomaxenchant server data; falls back to local getMaxLevel().
 *
 * Mojang 26.x: Enchantment.getFullname(Holder&lt;Enchantment&gt;, int) → Component
 * (Yarn was getName(RegistryEntry, int) → Text)
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {

    private static final String STAR = " \u2605";

    @WrapMethod(
            method = "getFullname(Lnet/minecraft/core/Holder;I)Lnet/minecraft/network/chat/Component;"
    )
    private static Component maxenchant$appendStarIfMaxed(
            Holder<Enchantment> entry,
            int level,
            Operation<Component> original
    ) {
        Component name = original.call(entry, level);

        if (!maxenchant$isMaxed(entry, level)) {
            return name;
        }
        return name.copy().append(Component.literal(STAR).withStyle(ChatFormatting.YELLOW));
    }

    private static boolean maxenchant$isMaxed(Holder<Enchantment> entry, int level) {
        return entry.unwrapKey()
                .map(key -> {
                    Integer serverMax = ClientNetworking.getServerMaxLevel(key.identifier());
                    int maxLevel = serverMax != null ? serverMax : entry.value().getMaxLevel();
                    return level >= maxLevel;
                })
                .orElse(false);
    }
}
