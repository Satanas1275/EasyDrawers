package com.satanas1275.easydrawers.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DrawerBlockEntity extends BlockEntity {
    private static final String TAG_STORED_ITEM = "StoredItem";
    private static final String TAG_COUNT = "Count";

    private ItemStack storedItem = ItemStack.EMPTY;
    private int count = 0;

    public DrawerBlockEntity(BlockPos pos, BlockState state) {
        super(com.satanas1275.easydrawers.EasyDrawers.DRAWER_BLOCK_ENTITY, pos, state);
    }

    public boolean canStore(ItemStack stack) {
        return !stack.isEmpty() && stack.getMaxDamage() == 0 && (storedItem.isEmpty() || ItemStack.isSameItemSameComponents(storedItem, stack));
    }

    public int tryAdd(ItemStack stack) {
        if (!canStore(stack)) return 0;
        if (storedItem.isEmpty()) {
            storedItem = stack.copyWithCount(1);
        }
        int added = stack.getCount();
        count += added;
        setChanged();
        syncToClient();
        return added;
    }

    public ItemStack tryRemove(int amount) {
        if (count <= 0) return ItemStack.EMPTY;
        int maxStack = storedItem.getMaxStackSize();
        int taken = Math.min(Math.min(amount, count), maxStack);
        count -= taken;
        ItemStack result = storedItem.copyWithCount(taken);
        if (count <= 0) {
            storedItem = ItemStack.EMPTY;
        }
        setChanged();
        syncToClient();
        return result;
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveCustomOnly(provider);
    }

    public ItemStack getStoredItem() {
        return storedItem;
    }

    public int getCount() {
        return count;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.store(TAG_STORED_ITEM, ItemStack.OPTIONAL_CODEC, storedItem);
        output.putInt(TAG_COUNT, count);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        storedItem = input.read(TAG_STORED_ITEM, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        count = input.getIntOr(TAG_COUNT, 0);
    }
}
