package dev.shadowsoffire.apothic_spawners;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ASConfig {

    public static final String[] DEFAULT_BANNED_MOBS = { "minecraft:warden", "minecraft:elder_guardian" };

    public static int spawnerSilkLevel;
    public static int spawnerSilkDamage;
    public static float capturingDropChance;
    public static Set<ResourceLocation> bannedMobs = new HashSet<>();

    public static void load() {
        Configuration config = new Configuration(ApothicSpawners.MODID);
        config.setTitle("Apotheosis Spawner Module Configuration");
        spawnerSilkLevel = config.getInt("Spawner Silk Level", "general", 1, -1, 127,
            "The level of silk touch needed to harvest a spawner.  Set to -1 to disable, 0 to always drop.  The enchantment module can increase the max level of silk touch.\nSynced.");

        spawnerSilkDamage = config.getInt("Spawner Silk Damage", "general", 100, 0, 100000, "The durability damage dealt to an item that silk touches a spawner.\nServer-authoritative.");

        capturingDropChance = config.getFloat("Capturing Drop Chance", "general", 0.005F, 0.001F, 1F, "The per-level drop chance (1 = 100%) of Spawn Eggs when using Capturing.\nSynced.");

        bannedMobs.clear();
        String[] bans = config.getStringList("Banned Mobs", "spawn_eggs", DEFAULT_BANNED_MOBS, "A list of entity registry names that cannot be applied to spawners via egg.\nSynced.");
        for (String s : bans)
            try {
                bannedMobs.add(new ResourceLocation(s));
            }
            catch (ResourceLocationException ex) {
                ApothicSpawners.LOGGER.error("Invalid entry {} detected in the spawner banned mobs list.", s);
                ex.printStackTrace();
            }
        if (config.hasChanged()) config.save();
    }

    public static record ConfigPayload(int spawnerSilkLevel, float capturingDropChance, Set<ResourceLocation> bannedMobs) implements CustomPacketPayload {

        public static final ResourceLocation ID = ApothicSpawners.loc("config");

        public ConfigPayload() {
            this(ASConfig.spawnerSilkLevel, ASConfig.capturingDropChance, ASConfig.bannedMobs);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeByte(this.spawnerSilkLevel);
            buf.writeFloat(this.capturingDropChance);
            buf.writeCollection(this.bannedMobs, FriendlyByteBuf::writeResourceLocation);
        }

        @Override
        public ResourceLocation id() {
            return ID;
        }

        public static class Provider implements PayloadProvider<ConfigPayload, IPayloadContext> {

            @Override
            public ResourceLocation id() {
                return ID;
            }

            @Override
            public ConfigPayload read(FriendlyByteBuf buf) {
                return new ConfigPayload(buf.readByte(), buf.readFloat(), buf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
            }

            @Override
            public void handle(ConfigPayload msg, IPayloadContext ctx) {
                ctx.workHandler().execute(() -> {
                    ASConfig.spawnerSilkLevel = msg.spawnerSilkLevel;
                    ASConfig.bannedMobs = msg.bannedMobs;
                });
            }

            @Override
            public List<ConnectionProtocol> getSupportedProtocols() {
                return List.of(ConnectionProtocol.CONFIGURATION, ConnectionProtocol.PLAY);
            }

            @Override
            public Optional<PacketFlow> getFlow() {
                return Optional.of(PacketFlow.CLIENTBOUND);
            }

        }

    }

}
