package dev.shadowsoffire.apothic_spawners.advancements;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile.SpawnerLogicExt;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

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
        Optional<Boolean> ignoreLight, Optional<Boolean> noAI, Optional<Boolean> silent, Optional<Boolean> baby) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(Ints.CODEC, "min_delay", Ints.ANY).forGetter(TriggerInstance::minDelay),
                ExtraCodecs.strictOptionalField(Ints.CODEC, "max_delay", Ints.ANY).forGetter(TriggerInstance::maxDelay),
                ExtraCodecs.strictOptionalField(Ints.CODEC, "spawn_count", Ints.ANY).forGetter(TriggerInstance::spawnCount),
                ExtraCodecs.strictOptionalField(Ints.CODEC, "max_nearby_entities", Ints.ANY).forGetter(TriggerInstance::nearbyEnts),
                ExtraCodecs.strictOptionalField(Ints.CODEC, "req_player_range", Ints.ANY).forGetter(TriggerInstance::playerRange),
                ExtraCodecs.strictOptionalField(Ints.CODEC, "spawn_range", Ints.ANY).forGetter(TriggerInstance::spawnRange),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "ignore_players").forGetter(TriggerInstance::ignorePlayers),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "ignore_conditions").forGetter(TriggerInstance::ignoreConditions),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "redstone_control").forGetter(TriggerInstance::redstone),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "ignore_light").forGetter(TriggerInstance::ignoreLight),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "no_ai").forGetter(TriggerInstance::noAI),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "silent").forGetter(TriggerInstance::silent),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "baby").forGetter(TriggerInstance::baby))
            .apply(inst, TriggerInstance::new));    

        public boolean test(ServerPlayer player, ApothSpawnerTile tile, SpawnerModifier modif) {
            SpawnerLogicExt logic = (SpawnerLogicExt) tile.spawner;
            if (!this.minDelay.matches(logic.minSpawnDelay) || !this.maxDelay.matches(logic.maxSpawnDelay) || !this.spawnCount.matches(logic.spawnCount) || !this.nearbyEnts.matches(logic.maxNearbyEntities)) return false;
            if (!this.playerRange.matches(logic.requiredPlayerRange)) return false;
            if (!this.spawnRange.matches(logic.spawnRange)) return false;
            if (this.ignorePlayers.isPresent() && tile.ignoresPlayers != this.ignorePlayers.get()) return false;
            if (this.ignoreConditions.isPresent() && tile.ignoresConditions != this.ignoreConditions.get()) return false;
            if (this.redstone.isPresent() && tile.redstoneControl != this.redstone.get()) return false;
            if (this.ignoreLight.isPresent() && tile.ignoresLight != this.ignoreLight.get()) return false;
            if (this.noAI.isPresent() && tile.hasNoAI != this.noAI.get()) return false;
            if (this.silent.isPresent() && tile.silent != this.silent.get()) return false;
            if (this.baby.isPresent() && tile.baby != this.baby.get()) return false;
            return true;
        }

    }
}
