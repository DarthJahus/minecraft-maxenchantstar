package net.jahus.maxenchant.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TooltipHandler {

    private static final String STAR = " \u2605"; // space + ⭐ (filled star)

    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
            if (enchantments.isEmpty()) {
                return;
            }

            Map<Identifier, Integer> levelsOnStack = new HashMap<>();
            Map<Identifier, RegistryEntry<Enchantment>> entryById = new HashMap<>();

            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> enchantmentEntry = entry.getKey();
                enchantmentEntry.getKey().ifPresent(key -> {
                    levelsOnStack.put(key.getValue(), entry.getIntValue());
                    entryById.put(key.getValue(), enchantmentEntry);
                });
            }

            if (levelsOnStack.isEmpty()) {
                return;
            }

            Set<Identifier> maxed = new HashSet<>();
            for (Map.Entry<Identifier, Integer> e : levelsOnStack.entrySet()) {
                Identifier id = e.getKey();
                int level = e.getValue();

                Integer serverMax = ClientNetworking.getServerMaxLevel(id);
                int maxLevel = serverMax != null ? serverMax : entryById.get(id).value().getMaxLevel();

                if (level >= maxLevel) {
                    maxed.add(id);
                }
            }

            for (int i = 0; i < lines.size(); i++) {
                Text line = lines.get(i);
                for (Identifier id : maxed) {
                    RegistryEntry<Enchantment> entry = entryById.get(id);
                    Text enchantName = Enchantment.getName(entry, levelsOnStack.get(id));
                    if (line.getString().equals(enchantName.getString())) {
                        lines.set(i, enchantName.copy().append(Text.literal(" \u2605").formatted(Formatting.YELLOW)));
                        break;
                    }
                }
            }
        });
    }
}
