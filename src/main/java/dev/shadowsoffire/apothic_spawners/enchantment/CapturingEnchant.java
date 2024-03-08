package dev.shadowsoffire.apothic_spawners.enchantment;

import dev.shadowsoffire.apothic_spawners.ASConfig;
import dev.shadowsoffire.apothic_spawners.ASObjects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class CapturingEnchant extends Enchantment {

    public CapturingEnchant() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return 200;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) || stack.canPerformAction(ToolActions.AXE_DIG);
    }

    public void handleCapturing(LivingDropsEvent e) {
        Entity killer = e.getSource().getEntity();
        if (killer instanceof LivingEntity living) {
            int level = living.getMainHandItem().getEnchantmentLevel(ASObjects.CAPTURING.get());
            LivingEntity killed = e.getEntity();
            if (ASConfig.bannedMobs.contains(EntityType.getKey(killed.getType()))) return;
            if (killed.level().random.nextFloat() < level * ASConfig.capturingDropChance) {
                Item eggItem = SpawnEggItem.byId(killed.getType());
                if (eggItem == null) return;
                ItemStack egg = new ItemStack(eggItem);
                e.getDrops().add(new ItemEntity(killed.level(), killed.getX(), killed.getY(), killed.getZ(), egg));
            }
        }
    }

}
