package com.satanas1275.easydrawers.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public class BasicDrawerBlock extends BaseEntityBlock {
    public static final MapCodec<BasicDrawerBlock> CODEC = simpleCodec(BasicDrawerBlock::new);

    private static final Map<BlockPos, PendingData> PENDING_RESTORE = new HashMap<>();

    record PendingData(ItemStack storedItem, int count) {}

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
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DrawerBlockEntity drawer && drawer.hasItems()) {
            PENDING_RESTORE.put(pos.immutable(), new PendingData(drawer.getStoredItem().copy(), drawer.getCount()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide()) {
            PendingData pending = PENDING_RESTORE.remove(pos);
            if (pending != null && level.getBlockEntity(pos) instanceof DrawerBlockEntity drawer) {
                drawer.restoreItems(pending.storedItem, pending.count);
            }
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (blockEntity instanceof DrawerBlockEntity drawer) {
            if (!player.isCreative()) {
                ItemStack drop = new ItemStack(this);
                if (drawer.hasItems()) {
                    drawer.saveToItem(drop, level.registryAccess());
                }
                popResource(level, pos, drop);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        if (level.getBlockEntity(pos) instanceof DrawerBlockEntity drawer && drawer.hasItems()) {
            drawer.saveToItem(stack, level.registryAccess());
        }
        return stack;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DrawerBlockEntity drawer && drawer.hasItems()) {
            return Math.min(15, drawer.getCount() * 15 / 64);
        }
        return 0;
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
