package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;
import java.util.Optional;

public record PrescriptRewards(int experience, List<ResourceKey<LootTable>> loot, List<ResourceKey<Recipe<?>>> recipes, Optional<CacheableFunction> function) {
    public static final Codec<PrescriptRewards> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.INT.optionalFieldOf("experience", 0).forGetter(PrescriptRewards::experience), LootTable.KEY_CODEC.listOf().optionalFieldOf("loot", List.of()).forGetter(PrescriptRewards::loot), Recipe.KEY_CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(PrescriptRewards::recipes), CacheableFunction.CODEC.optionalFieldOf("function").forGetter(PrescriptRewards::function)).apply(instance, PrescriptRewards::new));
    public static final PrescriptRewards EMPTY = new PrescriptRewards(0, List.of(), List.of(), Optional.empty());

    public PrescriptRewards(int experience, List<ResourceKey<LootTable>> loot, List<ResourceKey<Recipe<?>>> recipes, Optional<CacheableFunction> function) {
        this.experience = experience;
        this.loot = loot;
        this.recipes = recipes;
        this.function = function;
    }

    public void grant(ServerPlayer player) {
        player.giveExperiencePoints(this.experience);
        ServerLevel serverlevel = player.level();
        MinecraftServer minecraftserver = serverlevel.getServer();
        LootParams lootparams = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.ORIGIN, player.position()).withLuck(player.getLuck()).create(LootContextParamSets.ADVANCEMENT_REWARD);
        boolean flag = false;
        for (ResourceKey<LootTable> lootTableResourceKey : this.loot) {
            for (ItemStack itemstack : minecraftserver.reloadableRegistries().getLootTable(lootTableResourceKey).getRandomItems(lootparams)) {
                if (player.addItem(itemstack)) {
                    serverlevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    flag = true;
                } else {
                    ItemEntity itementity = player.drop(itemstack, false);
                    if (itementity != null) {
                        itementity.setNoPickUpDelay();
                        itementity.setTarget(player.getUUID());
                    }
                }
            }
        }
        if (flag) {
            player.containerMenu.broadcastChanges();
        }
        if (!this.recipes.isEmpty()) {
            player.awardRecipesByKey(this.recipes);
        }
        this.function.flatMap((mapper) -> mapper.get(minecraftserver.getFunctions())).ifPresent((consumer) -> {
            minecraftserver.getFunctions().execute(consumer, player.createCommandSourceStack().withSuppressedOutput().withPermission(LevelBasedPermissionSet.GAMEMASTER));
        });
    }

    public int experience() {
        return this.experience;
    }

    public List<ResourceKey<LootTable>> loot() {
        return this.loot;
    }

    public List<ResourceKey<Recipe<?>>> recipes() {
        return this.recipes;
    }

    public Optional<CacheableFunction> function() {
        return this.function;
    }

    public static class Builder {
        private int experience;
        private final ImmutableList.Builder<ResourceKey<LootTable>> loot = ImmutableList.builder();
        private final ImmutableList.Builder<ResourceKey<Recipe<?>>> recipes = ImmutableList.builder();
        private Optional<Identifier> function = Optional.empty();

        public Builder() {
        }

        public static PrescriptRewards.Builder experience(int experience) {
            return (new PrescriptRewards.Builder()).addExperience(experience);
        }

        public PrescriptRewards.Builder addExperience(int experience) {
            this.experience += experience;
            return this;
        }

        public static PrescriptRewards.Builder loot(ResourceKey<LootTable> lootTable) {
            return (new PrescriptRewards.Builder()).addLootTable(lootTable);
        }

        public PrescriptRewards.Builder addLootTable(ResourceKey<LootTable> lootTable) {
            this.loot.add(lootTable);
            return this;
        }

        public static PrescriptRewards.Builder recipe(ResourceKey<Recipe<?>> recipe) {
            return (new PrescriptRewards.Builder()).addRecipe(recipe);
        }

        public PrescriptRewards.Builder addRecipe(ResourceKey<Recipe<?>> recipe) {
            this.recipes.add(recipe);
            return this;
        }

        public static PrescriptRewards.Builder function(Identifier function) {
            return (new PrescriptRewards.Builder()).runs(function);
        }

        public PrescriptRewards.Builder runs(Identifier function) {
            this.function = Optional.of(function);
            return this;
        }

        public PrescriptRewards build() {
            return new PrescriptRewards(this.experience, this.loot.build(), this.recipes.build(), this.function.map(CacheableFunction::new));
        }
    }
}