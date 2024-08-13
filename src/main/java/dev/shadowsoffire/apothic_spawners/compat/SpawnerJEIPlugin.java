package dev.shadowsoffire.apothic_spawners.compat;

import java.util.List;

import dev.shadowsoffire.apothic_spawners.ASConfig;
import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.ApothicSpawners;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class SpawnerJEIPlugin implements IModPlugin {

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        List<SpawnerModifier> recipes = Minecraft.getInstance().level.getRecipeManager()
            .getAllRecipesFor(ASObjects.SPAWNER_MODIFIER.get())
            .stream()
            .sorted((r1, r2) -> -r1.id().compareNamespaced(r2.id()))
            .map(RecipeHolder::value)
            .toList();

        reg.addRecipes(SpawnerCategory.TYPE, recipes);

        if (ASConfig.spawnerSilkLevel == -1) {
            reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM_STACK, Component.translatable("info.apothic_spawners.spawner.no_silk"));
        }
        else if (ASConfig.spawnerSilkLevel == 0) {
            reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM_STACK, Component.translatable("info.apothic_spawners.spawner.always_drop"));
        }
        else {
            Minecraft.getInstance().level.holder(Enchantments.SILK_TOUCH).ifPresent(silk -> {
                reg.addIngredientInfo(new ItemStack(Blocks.SPAWNER), VanillaTypes.ITEM_STACK,
                    Component.translatable("info.apothic_spawners.spawner", ((MutableComponent) Enchantment.getFullname(silk, ASConfig.spawnerSilkLevel)).withStyle(ChatFormatting.DARK_BLUE)));
            });
        }

        for (Item i : BuiltInRegistries.ITEM) {
            if (i instanceof SpawnEggItem) {
                reg.addIngredientInfo(new ItemStack(i), VanillaTypes.ITEM_STACK, Component.translatable("info.apothic_spawners.capturing", ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(ASConfig.capturingDropChance * 100)));
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(Blocks.SPAWNER), SpawnerCategory.TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new SpawnerCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ApothicSpawners.loc("spawner");
    }

}
