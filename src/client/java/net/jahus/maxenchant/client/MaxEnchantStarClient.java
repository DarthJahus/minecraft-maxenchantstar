package net.jahus.maxenchant.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.jahus.maxenchant.network.EffectiveMaxLevelsPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-only: reads nomaxenchant:effective_max_levels if present, otherwise
 * falls back to Enchantment#getMaxLevel() from the local registry.
 */
public class MaxEnchantStarClient implements ClientModInitializer {

    public static final String MOD_ID = "maxenchant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.clientboundPlay().register(EffectiveMaxLevelsPayload.ID, EffectiveMaxLevelsPayload.CODEC);

        ClientNetworking.init();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientNetworking.reset());
    }
}
