package dev.shadowsoffire.apothic_spawners.modifiers;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.compat.SpawnerRecipeCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

public record SpawnerModifier(Ingredient mainHand, Optional<Ingredient> offHand, boolean consumesOffhand, List<StatModifier<?>> statModifiers) implements Recipe<RecipeInput> {

    public static final MapCodec<SpawnerModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
        .group(
            Ingredient.CODEC.fieldOf("mainhand").forGetter(SpawnerModifier::mainHand),
            Ingredient.CODEC.optionalFieldOf("offhand").forGetter(SpawnerModifier::offHand),
            Codec.BOOL.optionalFieldOf("consumes_offhand", false).forGetter(SpawnerModifier::consumesOffhand),
            StatModifier.CODEC.listOf().fieldOf("stat_changes").forGetter(SpawnerModifier::statModifiers))
        .apply(inst, SpawnerModifier::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnerModifier> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, SpawnerModifier::mainHand,
        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, SpawnerModifier::offHand,
        ByteBufCodecs.BOOL, SpawnerModifier::consumesOffhand,
        StatModifier.STREAM_CODEC.apply(ByteBufCodecs.list()), SpawnerModifier::statModifiers,
        SpawnerModifier::new);

    public static final RecipeSerializer<SpawnerModifier> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public SpawnerModifier(Ingredient mainHand, Optional<Ingredient> offHand, boolean consumesOffhand, List<StatModifier<?>> statModifiers) {
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.consumesOffhand = consumesOffhand;
        this.statModifiers = ImmutableList.copyOf(statModifiers);
    }

    public boolean matches(ApothSpawnerTile tile, ItemStack mainhand, ItemStack offhand) {
        if (this.mainHand.test(mainhand)) {
            if (this.offHand.isEmpty()) return true;
            return this.offHand.get().test(offhand);
        }
        return false;
    }

    public boolean apply(ApothSpawnerTile tile) {
        boolean success = false;
        for (StatModifier<?> m : this.statModifiers) {
            if (m.apply(tile)) {
                success = true;
                tile.setChanged();
            }
        }
        return success;
    }

    public boolean consumesOffhand() {
        return this.offHand.isPresent() && this.consumesOffhand;
    }

    @Override
    @Deprecated
    public boolean matches(RecipeInput pContainer, Level pLevel) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack assemble(RecipeInput pContainer) {
        return ItemStack.EMPTY;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return SpawnerModifier.SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return ASObjects.SPAWNER_MODIFIER.get();
    }

    @Nullable
    public static SpawnerModifier findMatch(ApothSpawnerTile tile, ItemStack mainhand, ItemStack offhand) {
        return SpawnerRecipeCache.getRecipes()
            .stream()
            .sorted((r1, r2) -> r1.offHand.isEmpty() ? r2.offHand.isEmpty() ? 0 : 1 : -1)
            .filter(r -> r.matches(tile, mainhand, offhand))
            .findFirst()
            .orElse(null);
    }

}
