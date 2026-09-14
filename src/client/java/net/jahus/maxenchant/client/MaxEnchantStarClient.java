package net.jahus.maxenchant.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.jahus.maxenchant.network.EffectiveMaxLevelsPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * maxenchant is a client-only mod: it has no common ModInitializer and no
 * server-side code. It reads nomaxenchant's public network channel
 * (nomaxenchant:effective_max_levels) if present, with a fallback to the
 * local vanilla registry's Enchantment#getMaxLevel() otherwise. This means
 * maxenchant works standalone (star reflects vanilla caps only) even
 * without nomaxenchant installed anywhere.
 */
public class MaxEnchantStarClient implements ClientModInitializer {

    public static final String MOD_ID = "maxenchant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(EffectiveMaxLevelsPayload.ID, EffectiveMaxLevelsPayload.CODEC);

        ClientNetworking.init();
        TooltipHandler.init();

        // Clear cached server data on disconnect so a later server (with
        // different or no datapack caps) isn't judged against stale values.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientNetworking.reset());
    }
}
