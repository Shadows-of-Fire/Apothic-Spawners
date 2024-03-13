package dev.shadowsoffire.apothic_spawners.block;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import dev.shadowsoffire.apothic_spawners.ASConfig;
import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
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
import net.neoforged.neoforge.common.util.Lazy;

public class ApothSpawnerBlock extends SpawnerBlock {

    private static final Supplier<ApothSpawnerTile> tooltipTile = Lazy.of(() -> new ApothSpawnerTile(BlockPos.ZERO, Blocks.AIR.defaultBlockState()));

    public ApothSpawnerBlock(Properties props) {
        super(props);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = level.getBlockEntity(pos);
        if (te != null) s.getOrCreateTag().put("BlockEntityTag", te.saveWithoutMetadata());
        return s;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null && stack.hasTag()) te.load(stack.getOrCreateTagElement("BlockEntityTag"));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ApothSpawnerTile(pPos, pState);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, te, stack);
        if (ASConfig.spawnerSilkLevel != -1 && stack.getEnchantmentLevel(Enchantments.SILK_TOUCH) >= ASConfig.spawnerSilkLevel) {
            if (ASConfig.spawnerSilkDamage > 1) {
                player.getMainHandItem().hurtAndBreak(ASConfig.spawnerSilkDamage - 1, player, pl -> pl.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            }
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.035F);
        }
    }

    @Override
    @Deprecated
    public List<ItemStack> getDrops(BlockState state, Builder params) {
        ItemStack tool = params.getParameter(LootContextParams.TOOL);

        if (ASConfig.spawnerSilkLevel != -1 && tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) >= ASConfig.spawnerSilkLevel) {
            ItemStack s = new ItemStack(this);
            BlockEntity te = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (te != null) s.getOrCreateTag().put("BlockEntityTag", te.saveWithoutMetadata());
            return List.of(s);
        }

        return super.getDrops(state, params);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity te = world.getBlockEntity(pos);
        ItemStack stack = player.getItemInHand(hand);
        ItemStack otherStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (te instanceof ApothSpawnerTile tile) {
            SpawnerModifier match = SpawnerModifier.findMatch(tile, stack, otherStack);
            if (match != null && match.apply(tile)) {
                if (world.isClientSide) return InteractionResult.SUCCESS;
                if (!player.isCreative()) {
                    stack.shrink(1);
                    if (match.consumesOffhand()) otherStack.shrink(1);
                }
                ASObjects.MODIFIER_TRIGGER.get().trigger((ServerPlayer) player, tile, match);
                world.sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (stack.hasTag() && stack.getTag().contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            if (Screen.hasShiftDown()) {
                CompoundTag tag = stack.getTag().getCompound("BlockEntityTag");
                tooltipTile.get().load(tag);
                SpawnerStats.generateTooltip(tooltipTile.get(), tooltip::add);
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

    @Override
    public int getExpDrop(BlockState state, LevelReader world, RandomSource randomSource, BlockPos pos, int fortune, int silktouch) {
        return silktouch == 0 ? super.getExpDrop(state, world, randomSource, pos, fortune, silktouch) : 0;
    }

}
