package dev.shadowsoffire.apothic_spawners.block;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ApothSpawnerItem extends BlockItem {

    public ApothSpawnerItem(Block block, Item.Properties props) {
        super(block, props);
    }

    @Override
    public String getCreatorModId(Provider registries, ItemStack itemStack) {
        return ApothicSpawners.MODID;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Component getName(ItemStack stack) {
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            CompoundTag tag = data.getUnsafe();
            if (tag.contains("SpawnData")) {
                try {
                    String name = tag.getCompound("SpawnData").map(sd -> sd.getCompound("entity").map(ent -> ent.getStringOr("id", "")).orElse("")).orElse("");
                    EntityType<?> t = BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse(name)).map(ref -> ref.value()).orElse(null);
                    if (t == null || t == EntityType.PIG && !"minecraft:pig".equals(name)) {
                        return super.getName(stack);
                    }
                    MobCategory cat = t.getCategory();
                    ChatFormatting color = switch (cat) {
                        case AMBIENT, CREATURE -> ChatFormatting.DARK_GREEN;
                        case MONSTER -> ChatFormatting.RED;
                        case WATER_AMBIENT, UNDERGROUND_WATER_CREATURE, WATER_CREATURE, AXOLOTLS -> ChatFormatting.BLUE;
                        default -> ChatFormatting.WHITE;
                    };
                    return ApothicSpawners.lang("item", "spawner", Component.translatable(t.getDescriptionId())).withStyle(color);
                }
                catch (Exception ex) {
                    // Fall through
                }
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            if (Minecraft.getInstance().hasShiftDown()) {
                TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                ApothSpawnerTile tooltipTile = new ApothSpawnerTile(BlockPos.ZERO, Blocks.SPAWNER.defaultBlockState());
                data.loadInto(tooltipTile, context.registries());
                SpawnerStats.generateTooltip(tooltipTile, tooltip);
            }
            else {
                tooltip.accept(ApothicSpawners.lang("misc", "shift_stats").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public boolean shouldPrintOpWarning(ItemStack stack, @Nullable Player player) {
        return false;
    }

}
