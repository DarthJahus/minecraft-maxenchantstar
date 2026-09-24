package net.jahus.maxenchant.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Mirror of nomaxenchant's S2C payload. No compile-time dependency on
 * nomaxenchant — the Id and wire format MUST stay identical.
 */
public record EffectiveMaxLevelsPayload(int protocolVersion, Map<Identifier, Integer> maxLevels)
        implements CustomPacketPayload {

    public static final int EXPECTED_PROTOCOL_VERSION = 1;

    public static final CustomPacketPayload.Type<EffectiveMaxLevelsPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("nomaxenchant", "effective_max_levels"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EffectiveMaxLevelsPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.protocolVersion());
                buf.writeVarInt(payload.maxLevels().size());
                for (Map.Entry<Identifier, Integer> entry : payload.maxLevels().entrySet()) {
                    buf.writeIdentifier(entry.getKey());
                    buf.writeVarInt(entry.getValue());
                }
            },
            (buf) -> {
                int version = buf.readVarInt();
                int size = buf.readVarInt();
                Map<Identifier, Integer> map = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    map.put(buf.readIdentifier(), buf.readVarInt());
                }
                return new EffectiveMaxLevelsPayload(version, map);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
