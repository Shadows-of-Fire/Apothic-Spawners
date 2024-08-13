package dev.shadowsoffire.apothic_spawners.block;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import dev.shadowsoffire.apothic_spawners.ASConfig;
import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ApothSpawnerBlock extends SpawnerBlock {

    public ApothSpawnerBlock(Properties props) {
        super(props);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = level.getBlockEntity(pos);
        if (te != null) {
            te.saveToItem(s, level.registryAccess());
        }
        return s;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        BlockEntity be = level.getBlockEntity(pos);
        if (!data.isEmpty() && be instanceof ApothSpawnerTile spw) {
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
        ItemStack tool = params.getParameter(LootContextParams.TOOL);
        Optional<Reference<Enchantment>> silkTouch = params.getLevel().holder(Enchantments.SILK_TOUCH);

        if (silkTouch.isPresent() && ASConfig.spawnerSilkLevel != -1 && tool.getEnchantmentLevel(silkTouch.get()) >= ASConfig.spawnerSilkLevel) {
            ItemStack s = new ItemStack(this);
            BlockEntity te = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (te != null) {
                te.saveToItem(s, params.getLevel().registryAccess());
            }
            return List.of(s);
        }

        return super.getDrops(state, params);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity te = world.getBlockEntity(pos);
        ItemStack otherStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (te instanceof ApothSpawnerTile tile) {
            SpawnerModifier match = SpawnerModifier.findMatch(tile, stack, otherStack);
            if (match != null && match.apply(tile)) {
                if (world.isClientSide) {
                    return ItemInteractionResult.SUCCESS;
                }

                if (!player.isCreative()) {
                    stack.shrink(1);
                    if (match.consumesOffhand()) {
                        otherStack.shrink(1);
                    }
                }

                ASObjects.MODIFIER_TRIGGER.get().trigger((ServerPlayer) player, tile, match);
                world.sendBlockUpdated(pos, state, state, 3);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            if (Screen.hasShiftDown()) {
                CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                ApothSpawnerTile tooltipTile = new ApothSpawnerTile(BlockPos.ZERO, Blocks.SPAWNER.defaultBlockState());
                data.loadInto(tooltipTile, context.registries());
                SpawnerStats.generateTooltip(tooltipTile, tooltip::add);
            }
            else {
                tooltip.add(Component.translatable("misc.apothic_spawners.shift_stats").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public Item asItem() {
        return Items.SPAWNER;
    }

}
