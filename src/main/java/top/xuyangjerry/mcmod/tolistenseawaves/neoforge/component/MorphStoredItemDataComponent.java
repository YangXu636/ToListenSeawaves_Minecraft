package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record MorphStoredItemDataComponent(List<ItemStack> storedStacks) {
    public static final MorphStoredItemDataComponent EMPTY = new MorphStoredItemDataComponent(new ArrayList<>());
    public static final Codec<MorphStoredItemDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(ItemStack.CODEC).fieldOf("stored_stacks").forGetter(MorphStoredItemDataComponent::storedStacks)
            ).apply(instance, MorphStoredItemDataComponent::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, MorphStoredItemDataComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC),
                    MorphStoredItemDataComponent::storedStacks,
                    MorphStoredItemDataComponent::new
            );

    public MorphStoredItemDataComponent add(ItemStack stack) {
        if (stack.isEmpty()) return this;
        List<ItemStack> newList = new ArrayList<>(this.storedStacks);
        boolean isDuplicate = newList.stream()
                .anyMatch(s -> ItemStack.isSameItemSameComponents(s, stack));
        if (!isDuplicate) {
            newList.add(stack.copy());
        }
        return new MorphStoredItemDataComponent(newList);
    }

    public MorphStoredItemDataComponent remove(ItemStack stack) {
        if (stack.isEmpty() || this.storedStacks.isEmpty()) return this;
        List<ItemStack> newList = this.storedStacks.stream()
                .filter(s -> !ItemStack.isSameItemSameComponents(s, stack))
                .collect(Collectors.toList());
        return new MorphStoredItemDataComponent(newList);
    }

    public MorphStoredItemDataComponent copy(){
        List<ItemStack> newList = new ArrayList<>(this.storedStacks);
        return new MorphStoredItemDataComponent(newList);
    }

    public static MorphStoredItemDataComponent of(ItemStack stack) {
        List<ItemStack> list = new ArrayList<>();
        if (!stack.isEmpty()) {
            list.add(stack.copy());
        }
        return new MorphStoredItemDataComponent(list);
    }

    public static MorphStoredItemDataComponent of(List<ItemStack> stacks) {
        List<ItemStack> copyList = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                copyList.add(stack.copy());
            }
        }
        return new MorphStoredItemDataComponent(copyList);
    }
}