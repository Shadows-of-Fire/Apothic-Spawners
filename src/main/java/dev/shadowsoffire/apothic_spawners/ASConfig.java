package dev.shadowsoffire.apothic_spawners;

import java.io.File;
import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ASConfig {

    public static int spawnerSilkLevel;
    public static int spawnerSilkDamage;
    public static boolean spawnersDropEmpty;
    public static int entityDespawnDelay;

    public static void load() {
        File configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), "apotheosis");
        Configuration config = new Configuration(new File(configDir, ApothicSpawners.MODID + ".cfg"));
        config.setTitle("Apotheosis Spawner Module Configuration");
        spawnerSilkLevel = config.getInt("Spawner Silk Level", "general", 1, -1, 127,
            "The level of silk touch needed to harvest a spawner.  Set to -1 to disable, 0 to always drop.  The enchantment module can increase the max level of silk touch.\nSynced.");

        spawnerSilkDamage = config.getInt("Spawner Silk Damage", "general", 100, 0, 100000, "The durability damage dealt to an item that silk touches a spawner.\nServer-authoritative.");

        spawnersDropEmpty = config.getBoolean("Spawners Drop Empty", "general", false, "If spawners should clear their contained entity when broken.\nServer-authoritative.");

        entityDespawnDelay = config.getInt("Entity Despawn Delay", "general", 600, 0, 24000, "The time, in ticks, that spawner-spawned mobs will be prevented from despawning for after they have spawned.\nServer-authoritative.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static record ConfigPayload(int spawnerSilkLevel) implements CustomPacketPayload {

        public static final Type<ConfigPayload> TYPE = new Type<>(ApothicSpawners.loc("config"));

        public static final StreamCodec<FriendlyByteBuf, ConfigPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ConfigPayload::spawnerSilkLevel,
            ConfigPayload::new);

        public ConfigPayload() {
            this(ASConfig.spawnerSilkLevel);
        }

        @Override
        public Type<ConfigPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<ConfigPayload> {

            @Override
            public Type<ConfigPayload> getType() {
                return TYPE;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, ConfigPayload> getCodec() {
                return CODEC;
            }

            @Override
            public void handleClient(ConfigPayload msg, IPayloadContext ctx) {
                ASConfig.spawnerSilkLevel = msg.spawnerSilkLevel;
            }

            @Override
            public List<ConnectionProtocol> getSupportedProtocols() {
                return List.of(ConnectionProtocol.PLAY);
            }

            @Override
            public Optional<PacketFlow> getFlow() {
                return Optional.of(PacketFlow.CLIENTBOUND);
            }

            @Override
            public String getVersion() {
                return "3";
            }

        }

    }

}
