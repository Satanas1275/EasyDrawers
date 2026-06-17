package com.satanas1275.easydrawers.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class BasicDrawerBlock extends BaseEntityBlock {
    public static final MapCodec<BasicDrawerBlock> CODEC = simpleCodec(BasicDrawerBlock::new);

    public BasicDrawerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DrawerBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext ctx) {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof DrawerBlockEntity drawer)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty()) {
            if (player.isShiftKeyDown()) {
                int added = drawer.tryAdd(held);
                if (added > 0) {
                    held.shrink(added);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            } else {
                int added = drawer.tryAdd(held.copyWithCount(1));
                if (added > 0) {
                    held.shrink(1);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
        }

        if (held.isEmpty()) {
            int amount = player.isShiftKeyDown() ? 64 : 1;
            ItemStack retrieved = drawer.tryRemove(amount);
            if (!retrieved.isEmpty()) {
                player.setItemInHand(hand, retrieved);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
