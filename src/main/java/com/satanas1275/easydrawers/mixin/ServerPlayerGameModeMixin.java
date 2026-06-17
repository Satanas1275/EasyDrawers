package com.satanas1275.easydrawers.mixin;

import com.satanas1275.easydrawers.EasyDrawers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @ModifyVariable(method = "useItemOn", at = @At(value = "STORE", ordinal = 0), index = 9)
    private boolean modifySkipBlockInteraction(boolean skip, ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult) {
        if (skip) {
            BlockState state = level.getBlockState(hitResult.getBlockPos());
            if (state.is(EasyDrawers.BASIC_DRAWER)) {
                return false;
            }
        }
        return skip;
    }
}
