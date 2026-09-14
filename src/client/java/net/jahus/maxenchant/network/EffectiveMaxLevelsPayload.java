package net.jahus.maxenchant.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side mirror of nomaxenchant's EffectiveMaxLevelsPayload
 * (net.jahus.nomaxenchant.network.EffectiveMaxLevelsPayload).
 *
 * This is intentionally a DUPLICATE, not a shared class: maxenchant has no
 * compile-time dependency on nomaxenchant, by design (maxenchant works
 * standalone with a vanilla-registry fallback if nomaxenchant is absent).
 * The Id and wire format below MUST stay byte-for-byte identical to
 * nomaxenchant's version, since the two mods only agree on the protocol,
 * not on shared code. If nomaxenchant's payload format ever changes, this
 * file needs to be updated to match - that's the tradeoff of not having a
 * compile-time dependency.
 */
public record EffectiveMaxLevelsPayload(int protocolVersion, Map<Identifier, Integer> maxLevels)
        implements CustomPayload {

    /** Must match nomaxenchant.network.EffectiveMaxLevelsPayload.PROTOCOL_VERSION. */
    public static final int EXPECTED_PROTOCOL_VERSION = 1;

    // Same namespaced id as nomaxenchant - this IS the contract between the
    // two mods. Must match exactly.
    public static final CustomPayload.Id<EffectiveMaxLevelsPayload> ID =
            new CustomPayload.Id<>(Identifier.of("nomaxenchant", "effective_max_levels"));

    public static final PacketCodec<RegistryByteBuf, EffectiveMaxLevelsPayload> CODEC = PacketCodec.of(
            (EffectiveMaxLevelsPayload payload, RegistryByteBuf buf) -> {
                buf.writeVarInt(payload.protocolVersion());
                buf.writeVarInt(payload.maxLevels().size());
                for (Map.Entry<Identifier, Integer> entry : payload.maxLevels().entrySet()) {
                    buf.writeIdentifier(entry.getKey());
                    buf.writeVarInt(entry.getValue());
                }
            },
            (RegistryByteBuf buf) -> {
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
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
