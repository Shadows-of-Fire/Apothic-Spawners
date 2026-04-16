package dev.shadowsoffire.apothic_spawners.block;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apothic_spawners.ASConfig;
import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

public class ApothSpawnerBlock extends SpawnerBlock {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApothSpawnerBlock.class);

    public ApothSpawnerBlock(Properties props) {
        super(props);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = level.getBlockEntity(pos);
        if (te != null) {
            writeBlockEntityData(s, te, level.registryAccess());
        }
        return s;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        BlockEntity be = level.getBlockEntity(pos);
        if (data != null && be instanceof ApothSpawnerTile spw) {
            data.loadInto(spw, level.registryAccess());
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ApothSpawnerTile(pPos, pState);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(level, player, pos, state, te, stack);
        level.holder(Enchantments.SILK_TOUCH).ifPresent(silkTouch -> {
            if (ASConfig.spawnerSilkLevel != -1 && stack.getEnchantmentLevel(silkTouch) >= ASConfig.spawnerSilkLevel) {
                if (ASConfig.spawnerSilkDamage > 1) {
                    player.getMainHandItem().hurtAndBreak(ASConfig.spawnerSilkDamage - 1, player, EquipmentSlot.MAINHAND);
                }
            }
        });
    }

    @Override
    @Deprecated
    public List<ItemStack> getDrops(BlockState state, Builder params) {
        ItemInstance tool = params.getParameter(LootContextParams.TOOL);
        Optional<Reference<Enchantment>> silkTouch = params.getLevel().holder(Enchantments.SILK_TOUCH);

        if (silkTouch.isPresent() && ASConfig.spawnerSilkLevel != -1 && tool.getEnchantmentLevel(silkTouch.get()) >= ASConfig.spawnerSilkLevel) {
            ItemStack s = new ItemStack(this);
            BlockEntity te = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (te instanceof ApothSpawnerTile spawner) {
                spawner.hasBeenModified = true;
                if (ASConfig.spawnersDropEmpty) {
                    spawner.getSpawner().spawnPotentials = WeightedList.of();
                    spawner.getSpawner().nextSpawnData = null;
                    spawner.getSpawner().spawnDelay = 0;
                }
                writeBlockEntityData(s, te, params.getLevel().registryAccess());
            }
            return List.of(s);
        }

        return super.getDrops(state, params);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity te = world.getBlockEntity(pos);
        ItemStack otherStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (te instanceof ApothSpawnerTile tile) {
            SpawnerModifier match = SpawnerModifier.findMatch(tile, stack, otherStack);
            if (match != null && match.apply(tile)) {
                if (world.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }

                if (!player.isCreative()) {
                    stack.shrink(1);
                    if (match.consumesOffhand()) {
                        otherStack.shrink(1);
                    }
                }

                ASObjects.MODIFIER_TRIGGER.trigger((ServerPlayer) player, tile, match);
                world.sendBlockUpdated(pos, state, state, 3);
                tile.hasBeenModified = true;
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public Item asItem() {
        return Items.SPAWNER;
    }

    static void writeBlockEntityData(ItemStack stack, BlockEntity te, HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(te.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            te.saveCustomOnly(output);
            BlockItem.setBlockEntityData(stack, te.getType(), output);
        }
    }

}
