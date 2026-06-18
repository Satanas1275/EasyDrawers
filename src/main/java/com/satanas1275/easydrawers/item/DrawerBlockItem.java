package com.satanas1275.easydrawers.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class DrawerBlockItem extends BlockItem {
    private static final net.minecraft.tags.TagKey<Block> DRAWERS = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.BLOCK,
            net.minecraft.resources.Identifier.fromNamespaceAndPath("easydrawers", "drawers"));

    public DrawerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (state.is(DRAWERS)) {
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        TypedEntityData<?> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            net.minecraft.nbt.CompoundTag tag = data.getUnsafe();
            ItemStack stored = ItemStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, tag.get("StoredItem")).result().orElse(ItemStack.EMPTY);
            int count = tag.getInt("Count").orElse(0);
            if (!stored.isEmpty() && count > 0) {
                tooltip.accept(Component.literal("x" + count + " ")
                        .append(stored.getHoverName())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
