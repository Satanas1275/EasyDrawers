package com.satanas1275.easydrawers.item;

import com.satanas1275.easydrawers.EasyDrawers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DrawerBlockItem extends BlockItem {
    public DrawerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (state.is(EasyDrawers.BASIC_DRAWER)) {
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
