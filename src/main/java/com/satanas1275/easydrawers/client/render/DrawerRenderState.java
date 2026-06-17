package com.satanas1275.easydrawers.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class DrawerRenderState extends BlockEntityRenderState {
    public ItemStack storedItem = ItemStack.EMPTY;
    public int count = 0;
    public Direction facing = Direction.NORTH;
}
