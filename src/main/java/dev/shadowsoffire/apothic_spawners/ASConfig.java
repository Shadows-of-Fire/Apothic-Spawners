package dev.shadowsoffire.apothic_spawners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ASConfig {

    public static final String[] DEFAULT_BANNED_MOBS = { "minecraft:warden", "minecraft:elder_guardian" };

    public static int spawnerSilkLevel;
    public static int spawnerSilkDamage;
    public static float capturingDropChance;
    public static boolean spawnersDropEmpty;
    public static Set<ResourceLocation> bannedMobs = new HashSet<>();

    public static void load() {
        Configuration config = new Configuration(ApothicSpawners.MODID);
        config.setTitle("Apotheosis Spawner Module Configuration");
        spawnerSilkLevel = config.getInt("Spawner Silk Level", "general", 1, -1, 127,
            "The level of silk touch needed to harvest a spawner.  Set to -1 to disable, 0 to always drop.  The enchantment module can increase the max level of silk touch.\nSynced.");

        spawnerSilkDamage = config.getInt("Spawner Silk Damage", "general", 100, 0, 100000, "The durability damage dealt to an item that silk touches a spawner.\nServer-authoritative.");

        capturingDropChance = config.getFloat("Capturing Drop Chance", "general", 0.005F, 0.001F, 1F, "The per-level drop chance (1 = 100%) of Spawn Eggs when using Capturing.\nSynced.");

        spawnersDropEmpty = config.getBoolean("Spawners Drop Empty", "general", false, "If spawners should clear their contained entity when broken.\nServer-authoritative.");

        bannedMobs.clear();
        String[] bans = config.getStringList("Banned Mobs", "spawn_eggs", DEFAULT_BANNED_MOBS, "A list of entity registry names that cannot be applied to spawners via egg. Supports regex.\nSynced.");

        List<Predicate<String>> patterns = new ArrayList<>(bans.length);
        for (String banRegex : bans) {
            try {
                Pattern p = Pattern.compile(banRegex);
                patterns.add(s -> p.matcher(s).matches());
            }
            catch (PatternSyntaxException | ResourceLocationException ex) {
                ApothicSpawners.LOGGER.error("Invalid entry {} detected in the spawner banned mobs list.", banRegex);
                ex.printStackTrace();
            }
        }
        Predicate<String> filter = Predicates.or(patterns);
        BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(rl -> filter.apply(rl.toString())).forEach(bannedMobs::add);
        ApothicSpawners.LOGGER.info("Banned {} mobs from being applicable to spawners.", bannedMobs.size());

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static record ConfigPayload(int spawnerSilkLevel, float capturingDropChance, Set<ResourceLocation> bannedMobs) implements CustomPacketPayload {

        public static final Type<ConfigPayload> TYPE = new Type<>(ApothicSpawners.loc("config"));

        public static final StreamCodec<FriendlyByteBuf, ConfigPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ConfigPayload::spawnerSilkLevel,
            ByteBufCodecs.FLOAT, ConfigPayload::capturingDropChance,
            ByteBufCodecs.collection(HashSet::new, ResourceLocation.STREAM_CODEC), ConfigPayload::bannedMobs,
            ConfigPayload::new);

        public ConfigPayload() {
            this(ASConfig.spawnerSilkLevel, ASConfig.capturingDropChance, ASConfig.bannedMobs);
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
            public void handle(ConfigPayload msg, IPayloadContext ctx) {
                ASConfig.spawnerSilkLevel = msg.spawnerSilkLevel;
                ASConfig.capturingDropChance = msg.capturingDropChance;
                ASConfig.bannedMobs = msg.bannedMobs;
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
                return "1";
            }

        }

    }

}
