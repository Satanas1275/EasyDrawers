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

import java.util.ArrayList;
import java.util.List;

public class EasyDrawers implements ModInitializer {
    public static final String MOD_ID = "easydrawers";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);

    static final String[] WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "crimson", "warped"};
    static final List<Block> DRAWER_BLOCKS = new ArrayList<>();

    private static final List<Identifier> DRAWER_IDS = new ArrayList<>();
    public static final Identifier CREATIVE_TAB_ID = Identifier.fromNamespaceAndPath(MOD_ID, "main");
    public static final BlockEntityType<DrawerBlockEntity> DRAWER_BLOCK_ENTITY;

    static {
        Block[] blocks = new Block[WOODS.length];
        for (int i = 0; i < WOODS.length; i++) {
            Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, WOODS[i] + "_drawer");
            DRAWER_IDS.add(id);
            Block block = new BasicDrawerBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, id))
                            .strength(2.0f)
                            .sound(SoundType.WOOD)
            );
            blocks[i] = block;
            DRAWER_BLOCKS.add(block);
        }
        DRAWER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(DrawerBlockEntity::new, blocks).build();
    }

    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, CREATIVE_TAB_ID);

    @Override
    public void onInitialize() {
        for (int i = 0; i < WOODS.length; i++) {
            Identifier id = DRAWER_IDS.get(i);
            Block block = DRAWER_BLOCKS.get(i);
            Registry.register(BuiltInRegistries.BLOCK, id, block);
            Registry.register(BuiltInRegistries.ITEM, id,
                    new DrawerBlockItem(block, new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, id))
                            .useBlockDescriptionPrefix()));
        }

        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, "drawer"), DRAWER_BLOCK_ENTITY);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_ID,
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .title(Component.translatable("itemGroup.easydrawers.main"))
                        .icon(() -> new ItemStack(DRAWER_BLOCKS.getFirst()))
                        .displayItems((params, output) -> {
                            for (Block block : DRAWER_BLOCKS) {
                                output.accept(block);
                            }
                        })
                        .build());

        LOGGER.info("EasyDrawers initialized");
    }
}
