package dev.shadowsoffire.apothic_spawners.advancements;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile.SpawnerLogicExt;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class ModifierTrigger extends SimpleCriterionTrigger<ModifierTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return ModifierTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ApothSpawnerTile tile, SpawnerModifier modif) {
        this.trigger(player, inst -> inst.test(player, tile, modif));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Ints minDelay, Ints maxDelay, Ints spawnCount, Ints nearbyEnts,
        Ints playerRange, Ints spawnRange, Optional<Boolean> ignorePlayers, Optional<Boolean> ignoreConditions, Optional<Boolean> redstone,
        Optional<Boolean> ignoreLight, Optional<Boolean> noAI, Optional<Boolean> silent, Optional<Boolean> youthful) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Ints.CODEC.optionalFieldOf("min_delay", Ints.ANY).forGetter(TriggerInstance::minDelay),
                Ints.CODEC.optionalFieldOf("max_delay", Ints.ANY).forGetter(TriggerInstance::maxDelay),
                Ints.CODEC.optionalFieldOf("spawn_count", Ints.ANY).forGetter(TriggerInstance::spawnCount),
                Ints.CODEC.optionalFieldOf("max_nearby_entities", Ints.ANY).forGetter(TriggerInstance::nearbyEnts),
                Ints.CODEC.optionalFieldOf("req_player_range", Ints.ANY).forGetter(TriggerInstance::playerRange),
                Ints.CODEC.optionalFieldOf("spawn_range", Ints.ANY).forGetter(TriggerInstance::spawnRange),
                Codec.BOOL.optionalFieldOf("ignore_players").forGetter(TriggerInstance::ignorePlayers),
                Codec.BOOL.optionalFieldOf("ignore_conditions").forGetter(TriggerInstance::ignoreConditions),
                Codec.BOOL.optionalFieldOf("redstone_control").forGetter(TriggerInstance::redstone),
                Codec.BOOL.optionalFieldOf("ignore_light").forGetter(TriggerInstance::ignoreLight),
                Codec.BOOL.optionalFieldOf("no_ai").forGetter(TriggerInstance::noAI),
                Codec.BOOL.optionalFieldOf("silent").forGetter(TriggerInstance::silent),
                Codec.BOOL.optionalFieldOf("youthful").forGetter(TriggerInstance::youthful))
            .apply(inst, TriggerInstance::new));

        public boolean test(ServerPlayer player, ApothSpawnerTile tile, SpawnerModifier modif) {
            SpawnerLogicExt logic = (SpawnerLogicExt) tile.spawner;
            if (!this.minDelay.matches(logic.minSpawnDelay)) return false;
            if (!this.maxDelay.matches(logic.maxSpawnDelay)) return false;
            if (!this.spawnCount.matches(logic.spawnCount)) return false;
            if (!this.nearbyEnts.matches(logic.maxNearbyEntities)) return false;
            if (!this.playerRange.matches(logic.requiredPlayerRange)) return false;
            if (!this.spawnRange.matches(logic.spawnRange)) return false;
            if (!this.check(tile, SpawnerStats.IGNORE_PLAYERS, this.ignorePlayers)) return false;
            if (!this.check(tile, SpawnerStats.IGNORE_CONDITIONS, this.ignoreConditions)) return false;
            if (!this.check(tile, SpawnerStats.REDSTONE_CONTROL, this.redstone)) return false;
            if (!this.check(tile, SpawnerStats.IGNORE_LIGHT, this.ignoreLight)) return false;
            if (!this.check(tile, SpawnerStats.NO_AI, this.noAI)) return false;
            if (!this.check(tile, SpawnerStats.SILENT, this.silent)) return false;
            if (!this.check(tile, SpawnerStats.YOUTHFUL, this.youthful)) return false;
            return true;
        }

        private <T> boolean check(ApothSpawnerTile tile, SpawnerStat<T> stat, Optional<T> target) {
            return target.isEmpty() || target.get() == stat.getValue(tile);
        }

    }
}
