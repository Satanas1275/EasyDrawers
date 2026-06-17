package com.satanas1275.easydrawers;

import com.satanas1275.easydrawers.block.BasicDrawerBlock;
import com.satanas1275.easydrawers.block.DrawerBlockEntity;
import com.satanas1275.easydrawers.item.DrawerBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyDrawers implements ModInitializer {
    public static final String MOD_ID = "easydrawers";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier BASIC_DRAWER_ID = Identifier.fromNamespaceAndPath(MOD_ID, "basic_drawer");
    public static final Identifier CREATIVE_TAB_ID = Identifier.fromNamespaceAndPath(MOD_ID, "main");

    public static final Block BASIC_DRAWER = new BasicDrawerBlock(
            BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, BASIC_DRAWER_ID))
                    .strength(1.0f)
                    .sound(SoundType.WOOD)
    );

    public static final BlockEntityType<DrawerBlockEntity> DRAWER_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BASIC_DRAWER_ID,
                    FabricBlockEntityTypeBuilder.create(DrawerBlockEntity::new, BASIC_DRAWER).build());

    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, CREATIVE_TAB_ID);

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.BLOCK, BASIC_DRAWER_ID, BASIC_DRAWER);
        Registry.register(BuiltInRegistries.ITEM, BASIC_DRAWER_ID,
                new DrawerBlockItem(BASIC_DRAWER, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, BASIC_DRAWER_ID)).useBlockDescriptionPrefix()));

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_ID,
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .title(Component.translatable("itemGroup.easydrawers.main"))
                        .icon(() -> new ItemStack(BASIC_DRAWER))
                        .displayItems((params, output) -> {
                            output.accept(BASIC_DRAWER);
                        })
                        .build());

        LOGGER.info("EasyDrawers initialized");
    }
}
