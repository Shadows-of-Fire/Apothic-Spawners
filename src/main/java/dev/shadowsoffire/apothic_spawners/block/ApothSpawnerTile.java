package dev.shadowsoffire.apothic_spawners.block;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStat;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck;

public class ApothSpawnerTile extends SpawnerBlockEntity {

    private static final int INSTABILITY_DURATION_TICKS = 60;
    private static final float INSTABILITY_RADIUS = 8.0F;
    private static final int UNSTABLE_MOB_COUNT = 12;
    private static final double LOOT_SCATTER_RADIUS = 2.0;

    protected final Map<SpawnerStat<?>, Object> customStats = new IdentityHashMap<>();

    /**
     * Flag to determine if the spawner has been silk-touched or modified by a player.
     */
    boolean hasBeenModified = false;

    /**
     * Active instability countdown, -1 if not active.
     */
    private int unstableTicks = -1;

    /**
     * Snapshot of {@link BaseSpawner#nextSpawnData} taken at instability start so detonation always references
     * the mob the spawner was about to produce, even if a tick mutates the field. Persisted under
     * {@code captured_spawn_data} so a chunk unload mid-countdown still detonates the correct mob on reload.
     */
    @Nullable
    private SpawnData capturedSpawnData;

    public ApothSpawnerTile(BlockPos pos, BlockState state) {
        super(pos, state);
        this.spawner = new SpawnerLogicExt();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void saveAdditional(ValueOutput output) {
        CompoundTag stats = new CompoundTag();
        this.customStats.forEach((stat, value) -> {
            try {
                Tag encoded = ((Codec<Object>) stat.valueCodec()).encodeStart(NbtOps.INSTANCE, value).getOrThrow();
                stats.put(stat.getId().toString(), encoded);
            }
            catch (Exception ex) {
                ApothicSpawners.LOGGER.error("Failed saving spawner stat " + stat.getId(), ex);
            }
        });
        output.store("stats", CompoundTag.CODEC, stats);
        output.putBoolean("modified", this.hasBeenModified);
        if (this.unstableTicks > -1) {
            output.putInt("unstable_ticks", this.unstableTicks);
        }
        if (this.capturedSpawnData != null) {
            output.store("captured_spawn_data", SpawnData.CODEC, this.capturedSpawnData);
        }
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        CompoundTag stats = input.read("stats", CompoundTag.CODEC).orElse(new CompoundTag());
        for (String key : stats.keySet()) {
            SpawnerStat<?> stat = SpawnerStats.REGISTRY.get(Identifier.tryParse(key)).map(ref -> ref.value()).orElse(null);
            if (stat != null) {
                Tag value = stats.get(key);
                try {
                    Object realValue = stat.valueCodec().decode(NbtOps.INSTANCE, value).getOrThrow().getFirst();
                    this.customStats.put(stat, realValue);
                }
                catch (Exception ex) {
                    ApothicSpawners.LOGGER.error("Failed loading spawner stat " + key, ex);
                }
            }
        }

        this.hasBeenModified = input.getBooleanOr("modified", false);
        this.unstableTicks = input.getIntOr("unstable_ticks", -1);
        this.capturedSpawnData = input.read("captured_spawn_data", SpawnData.CODEC).orElse(null);
        super.loadAdditional(input);
    }

    public Map<SpawnerStat<?>, Object> getStatsMap() {
        return this.customStats;
    }

    public boolean hasBeenModified() {
        return this.hasBeenModified;
    }

    public boolean isUnstable() {
        return this.unstableTicks >= 0;
    }

    /**
     * Starts the instability process. At the end of the process, the spawner will explode, spawn a cluster of enemies, and spawn a loot table.
     */
    public void beginInstability() {
        if (this.isUnstable()) return;
        this.capturedSpawnData = this.spawner.nextSpawnData;
        if (this.capturedSpawnData == null && this.level != null) {
            this.capturedSpawnData = this.spawner.spawnPotentials.getRandom(this.level.getRandom()).orElse(null);
        }
        this.unstableTicks = INSTABILITY_DURATION_TICKS;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.level.getBlockState(this.getBlockPos());
            this.level.sendBlockUpdated(this.getBlockPos(), state, state, 3);
        }
    }

    private void detonateUnstable(ServerLevel level, BlockPos pos) {
        SpawnData data = this.capturedSpawnData != null ? this.capturedSpawnData : this.spawner.nextSpawnData;
        RandomSource rand = level.getRandom();

        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, INSTABILITY_RADIUS, Level.ExplosionInteraction.BLOCK);

        if (data != null) {
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this::toString, ApothicSpawners.LOGGER)) {
                ValueInput input = TagValueInput.create(reporter, level.registryAccess(), data.getEntityToSpawn());
                EntityType<?> type = EntityType.by(input).orElse(null);
                if (type != null) {
                    for (int i = 0; i < UNSTABLE_MOB_COUNT; i++) {
                        double x = pos.getX() + 0.5 + (rand.nextDouble() - rand.nextDouble()) * 1.5;
                        double y = pos.getY() + (rand.nextDouble() - 0.5);
                        double z = pos.getZ() + 0.5 + (rand.nextDouble() - rand.nextDouble()) * 1.5;
                        Entity entity = EntityType.loadEntityRecursive(input, level, EntitySpawnReason.SPAWNER, fresh -> {
                            fresh.snapTo(x, y, z, rand.nextFloat() * 360.0F, 0.0F);
                            return fresh;
                        });
                        if (entity != null) {
                            level.tryAddFreshEntityWithPassengers(entity);
                            if (entity instanceof Mob mob) {
                                mob.spawnAnim();
                            }
                        }
                    }
                }
            }
        }

        LootTable table = level.getServer().reloadableRegistries().getLootTable(ASObjects.UNSTABLE_SPAWNER_LOOT);
        if (table != LootTable.EMPTY) {
            LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .create(LootContextParamSets.CHEST);
            table.getRandomItems(params, rand.nextLong(), stack -> spawnUnstableLoot(level, pos, stack, rand));
        }
    }

    private static void spawnUnstableLoot(ServerLevel level, BlockPos pos, ItemStack stack, RandomSource rand) {
        if (stack.isEmpty()) return;
        double x = pos.getX() + 0.5 + (rand.nextDouble() - rand.nextDouble()) * LOOT_SCATTER_RADIUS;
        double y = pos.getY() + 0.5 + rand.nextDouble() * 0.5;
        double z = pos.getZ() + 0.5 + (rand.nextDouble() - rand.nextDouble()) * LOOT_SCATTER_RADIUS;
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        item.setDeltaMovement(
            (rand.nextDouble() - 0.5) * 0.4,
            0.2 + rand.nextDouble() * 0.3,
            (rand.nextDouble() - 0.5) * 0.4);
        level.addFreshEntity(item);
    }

    public class SpawnerLogicExt extends BaseSpawner {

        @Override
        public void setEntityId(EntityType<?> type, @Nullable Level level, RandomSource rand, BlockPos pos) {
            this.nextSpawnData = new SpawnData();
            super.setEntityId(type, level, rand, pos);
            this.spawnPotentials = WeightedList.of(this.nextSpawnData);
            if (level != null) this.delay(level, pos);
        }

        @Override
        public void broadcastEvent(Level level, BlockPos pos, int id) {
            level.blockEvent(pos, Blocks.SPAWNER, id, 0);
        }

        @Override
        public void setNextSpawnData(Level level, BlockPos pos, SpawnData nextSpawnData) {
            super.setNextSpawnData(level, pos, nextSpawnData);

            if (level != null) {
                BlockState state = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, state, 4);
            }
        }

        @Override
        public Either<BlockEntity, Entity> getOwner() {
            return Either.left(ApothSpawnerTile.this);
        }

        protected boolean isActivated(Level level, BlockPos pos) {
            boolean hasPlayer = this.getStatValue(SpawnerStats.IGNORE_PLAYERS) || this.isNearPlayer(level, pos);
            return hasPlayer && (!this.getStatValue(SpawnerStats.REDSTONE_CONTROL) || ApothSpawnerTile.this.level.hasNeighborSignal(pos));
        }

        private void delay(Level pLevel, BlockPos pPos) {
            if (this.maxSpawnDelay <= this.minSpawnDelay) {
                this.spawnDelay = this.minSpawnDelay;
            }
            else {
                this.spawnDelay = this.minSpawnDelay + pLevel.getRandom().nextInt(this.maxSpawnDelay - this.minSpawnDelay);
            }

            this.spawnPotentials.getRandom(pLevel.getRandom()).ifPresent(potential -> {
                this.setNextSpawnData(pLevel, pPos, potential);
            });
            this.broadcastEvent(pLevel, pPos, 1);
        }

        @Override
        public void clientTick(Level pLevel, BlockPos pPos) {
            if (ApothSpawnerTile.this.unstableTicks >= 0) {
                RandomSource rand = pLevel.getRandom();
                for (int i = 0; i < 8; i++) {
                    double x = pPos.getX() + 0.5 + (rand.nextDouble() - rand.nextDouble()) * 2.5;
                    double y = pPos.getY() + rand.nextDouble();
                    double z = pPos.getZ() + 0.5 + (rand.nextDouble() - rand.nextDouble()) * 2.5;
                    double xd = rand.nextGaussian();
                    double yd = rand.nextFloat() * 0.01F;
                    double zd = rand.nextGaussian();
                    pLevel.addParticle(ParticleTypes.FLAME, x, y, z, xd, yd, zd);
                    pLevel.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, xd, yd, zd);
                }
                return;
            }
            if (!this.isActivated(pLevel, pPos)) {
                this.oSpin = this.spin;
            }
            else {
                double d0 = pPos.getX() + pLevel.getRandom().nextDouble();
                double d1 = pPos.getY() + pLevel.getRandom().nextDouble();
                double d2 = pPos.getZ() + pLevel.getRandom().nextDouble();
                pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                pLevel.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                }

                this.oSpin = this.spin;
                this.spin = (this.spin + 1000.0F / (this.spawnDelay + 200.0F)) % 360.0D;
            }

        }

        @Override
        @SuppressWarnings("deprecation")
        public void serverTick(ServerLevel level, BlockPos pPos) {
            if (ApothSpawnerTile.this.isUnstable()) {
                if (ApothSpawnerTile.this.unstableTicks-- == 0) {
                    ApothSpawnerTile.this.detonateUnstable(level, pPos);
                }
                return;
            }

            if (this.isActivated(level, pPos) && level.isSpawnerBlockEnabled()) {
                if (this.spawnDelay == -1) {
                    this.delay(level, pPos);
                }

                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                }
                else {
                    boolean flag = false;
                    RandomSource rand = level.getRandom();
                    SpawnData spawnData = this.getOrCreateNextSpawnData(level, rand, pPos);

                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this::toString, ApothicSpawners.LOGGER)) {
                        ValueInput input = TagValueInput.create(reporter, level.registryAccess(), spawnData.getEntityToSpawn());
                        EntityType<?> entityType = EntityType.by(input).orElse(null);
                        if (entityType == null) {
                            this.delay(level, pPos);
                            return;
                        }

                        for (int i = 0; i < this.spawnCount; ++i) {
                            Vec3 spawnPos = input.read("Pos", Vec3.CODEC)
                                .orElseGet(() -> new Vec3(
                                    pPos.getX() + (rand.nextDouble() - rand.nextDouble()) * this.spawnRange + 0.5D,
                                    pPos.getY() + rand.nextInt(3) - 1,
                                    pPos.getZ() + (rand.nextDouble() - rand.nextDouble()) * this.spawnRange + 0.5D));
                            double x = spawnPos.x;
                            double y = spawnPos.y;
                            double z = spawnPos.z;

                            if (level.noCollision(entityType.getSpawnAABB(x, y, z))) {
                                BlockPos blockpos = BlockPos.containing(x, y, z);

                                // LOGIC CHANGE : Ability to ignore conditions set in the spawner and by the entity.
                                LyingLevel liar = new LyingLevel(level);
                                boolean useLiar = false;
                                if (!this.getStatValue(SpawnerStats.IGNORE_CONDITIONS)) {
                                    if (this.getStatValue(SpawnerStats.IGNORE_LIGHT)) {
                                        boolean pass = false;
                                        for (int light = 0; light < 16; light++) {
                                            liar.setFakeLightLevel(light);
                                            if (this.checkSpawnRules(spawnData, entityType, liar, blockpos)) {
                                                pass = true;
                                                break;
                                            }
                                        }
                                        if (!pass) continue;
                                        else useLiar = true;
                                    }
                                    else if (!this.checkSpawnRules(spawnData, entityType, level, blockpos)) continue;
                                }

                                Entity entity = EntityType.loadEntityRecursive(input, level, EntitySpawnReason.SPAWNER, freshEntity -> {
                                    freshEntity.snapTo(x, y, z, freshEntity.getYRot(), freshEntity.getXRot());
                                    return freshEntity;
                                });

                                if (entity == null) {
                                    this.delay(level, pPos);
                                    return;
                                }

                                int nearby = level.getEntities(
                                    EntityTypeTest.forExactClass(entity.getClass()),
                                    new AABB(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX() + 1, pPos.getY() + 1, pPos.getZ() + 1).inflate(this.spawnRange),
                                    EntitySelector.NO_SPECTATORS).size();
                                if (nearby >= this.maxNearbyEntities) {
                                    this.delay(level, pPos);
                                    return;
                                }

                                entity.getSelfAndPassengers().forEach(selfOrPassenger -> {
                                    if (this.getStatValue(SpawnerStats.NO_AI) && selfOrPassenger instanceof Mob mob) {
                                        mob.setNoAi(true);
                                        mob.getPersistentData().putBoolean("apotheosis:movable", true);
                                    }

                                    if (this.getStatValue(SpawnerStats.YOUTHFUL) && selfOrPassenger instanceof Mob mob) {
                                        mob.setBaby(true);
                                    }

                                    if (this.getStatValue(SpawnerStats.SILENT)) {
                                        selfOrPassenger.setSilent(true);
                                    }

                                    if (this.getStatValue(SpawnerStats.INITIAL_HEALTH) != 1 && selfOrPassenger instanceof LivingEntity living) {
                                        living.setHealth(living.getHealth() * this.getStatValue(SpawnerStats.INITIAL_HEALTH));
                                    }

                                    if (this.getStatValue(SpawnerStats.BURNING) && !selfOrPassenger.fireImmune()) {
                                        selfOrPassenger.setRemainingFireTicks(Integer.MAX_VALUE);
                                    }

                                    if (this.getStatValue(SpawnerStats.ECHOING) > 0) {
                                        selfOrPassenger.getPersistentData().putInt(SpawnerStats.ECHOING.getId().toString(), this.getStatValue(SpawnerStats.ECHOING));
                                    }
                                });

                                entity.snapTo(entity.getX(), entity.getY(), entity.getZ(), rand.nextFloat() * 360.0F, 0.0F);
                                if (entity instanceof Mob mob) {
                                    if (!this.checkSpawnPositionSpawner(mob, useLiar ? liar : level, EntitySpawnReason.SPAWNER, spawnData, this)) {
                                        continue;
                                    }

                                    // #6: Match vanilla's getString().isPresent() check
                                    boolean hasNoConfiguration = spawnData.getEntityToSpawn().size() == 1 & spawnData.getEntityToSpawn().getString(Entity.TAG_ID).isPresent();
                                    EventHooks.finalizeMobSpawnSpawner(mob, useLiar ? liar : level, level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.SPAWNER, null, this,
                                        hasNoConfiguration);

                                    spawnData.getEquipment().ifPresent(mob::equip);
                                }

                                if (!level.tryAddFreshEntityWithPassengers(entity)) {
                                    this.delay(level, pPos);
                                    return;
                                }

                                level.levelEvent(2004, pPos, 0);
                                // #5: Dispatch GameEvent.ENTITY_PLACE for sculk sensor compatibility
                                level.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos);
                                if (entity instanceof Mob) {
                                    ((Mob) entity).spawnAnim();
                                }

                                flag = true;
                            }
                        }

                        if (flag) {
                            this.delay(level, pPos);
                        }

                    }
                }
            }
        }

        public boolean checkSpawnPositionSpawner(Mob mob, ServerLevelAccessor level, EntitySpawnReason spawnType, SpawnData spawnData, BaseSpawner spawner) {
            var event = new PositionCheck(mob, level, spawnType, spawner);
            NeoForge.EVENT_BUS.post(event);
            if (event.getResult() == PositionCheck.Result.DEFAULT) {
                return mob.checkSpawnObstruction(level) &&
                    (this.getStatValue(SpawnerStats.IGNORE_CONDITIONS)
                        || spawnData.getCustomSpawnRules().isPresent()
                        || mob.checkSpawnRules(level, EntitySpawnReason.SPAWNER));
            }
            return event.getResult() == PositionCheck.Result.SUCCEED;
        }

        /**
         * Checks if the requested entity passes spawn rule checks or not.
         */
        private boolean checkSpawnRules(SpawnData spawnData, EntityType<?> entityType, ServerLevelAccessor pServerLevel, BlockPos blockpos) {
            if (spawnData.getCustomSpawnRules().isPresent()) {
                if (!entityType.getCategory().isFriendly() && pServerLevel.getDifficulty() == Difficulty.PEACEFUL) {
                    return false;
                }

                SpawnData.CustomSpawnRules customRules = spawnData.getCustomSpawnRules().get();
                if (this.getStatValue(SpawnerStats.IGNORE_LIGHT)) return true; // All custom spawn rules are light-based, so if we ignore light, we can short-circuit here.
                if (!customRules.blockLightLimit().isValueInRange(pServerLevel.getBrightness(LightLayer.BLOCK, blockpos))
                    || !customRules.skyLightLimit().isValueInRange(pServerLevel.getBrightness(LightLayer.SKY, blockpos))) {
                    return false;
                }
            }
            else if (!SpawnPlacements.checkSpawnRules(entityType, pServerLevel, EntitySpawnReason.SPAWNER, blockpos, pServerLevel.getRandom())) {
                return false;
            }
            return true;
        }

        private <T> T getStatValue(SpawnerStat<T> stat) {
            return stat.getValue(ApothSpawnerTile.this);
        }

    }

}
