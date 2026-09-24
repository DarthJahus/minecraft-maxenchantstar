package net.jahus.maxenchant.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jahus.maxenchant.network.EffectiveMaxLevelsPayload;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClientNetworking {

    private static Map<Identifier, Integer> serverMaxLevels = null;

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(EffectiveMaxLevelsPayload.ID, (payload, context) -> {
            if (payload.protocolVersion() != EffectiveMaxLevelsPayload.EXPECTED_PROTOCOL_VERSION) {
                MaxEnchantStarClient.LOGGER.warn(
                        "Received nomaxenchant:effective_max_levels with unexpected protocol version {} (expected {}); ignoring",
                        payload.protocolVersion(), EffectiveMaxLevelsPayload.EXPECTED_PROTOCOL_VERSION);
                serverMaxLevels = null;
                return;
            }
            serverMaxLevels = Collections.unmodifiableMap(new HashMap<>(payload.maxLevels()));
        });
    }

    public static Integer getServerMaxLevel(Identifier enchantmentId) {
        return serverMaxLevels != null ? serverMaxLevels.get(enchantmentId) : null;
    }

    public static boolean hasServerData() {
        return serverMaxLevels != null;
    }

    public static void reset() {
        serverMaxLevels = null;
    }
}
