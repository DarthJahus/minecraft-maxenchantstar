package net.jahus.maxenchant.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jahus.maxenchant.network.EffectiveMaxLevelsPayload;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClientNetworking {

    // null = not received yet, or connected to a server without nomaxenchant
    // (or without a mod broadcasting on this channel). Reset on disconnect
    // so a later connection to a different server doesn't keep stale values.
    private static Map<Identifier, Integer> serverMaxLevels = null;

    public static void init() {
        // Registered here, once, client-side only. maxenchant has no common
        // ModInitializer anymore (it's a client-only mod), so this is the
        // single registration point - no risk of the unconfirmed "double
        // register" issue raised earlier.
        ClientPlayNetworking.registerGlobalReceiver(EffectiveMaxLevelsPayload.ID, (payload, context) -> {
            if (payload.protocolVersion() != EffectiveMaxLevelsPayload.EXPECTED_PROTOCOL_VERSION) {
                // Unknown wire format from a newer/older nomaxenchant.
                // Ignore rather than risk misreading the data; fall back
                // to local vanilla registry reading as if no server data
                // had ever arrived.
                MaxEnchantStarClient.LOGGER.warn(
                        "Received nomaxenchant:effective_max_levels with unexpected protocol version {} (expected {}); ignoring, falling back to vanilla max levels",
                        payload.protocolVersion(), EffectiveMaxLevelsPayload.EXPECTED_PROTOCOL_VERSION);
                serverMaxLevels = null;
                return;
            }
            serverMaxLevels = Collections.unmodifiableMap(new HashMap<>(payload.maxLevels()));
        });
    }

    /** Effective server max level for this enchantment, or null if not (yet) known. */
    public static Integer getServerMaxLevel(Identifier enchantmentId) {
        return serverMaxLevels != null ? serverMaxLevels.get(enchantmentId) : null;
    }

    public static boolean hasServerData() {
        return serverMaxLevels != null;
    }

    /** Call on disconnect so stale caps from a previous server aren't reused. */
    public static void reset() {
        serverMaxLevels = null;
    }
}
