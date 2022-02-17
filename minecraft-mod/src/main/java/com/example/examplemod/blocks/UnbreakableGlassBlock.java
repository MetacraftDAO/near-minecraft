package com.example.examplemod.blocks;

import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class UnbreakableGlassBlock extends GlassBlock {
    public UnbreakableGlassBlock() {
        super(Properties.of(Material.GLASS)
                .sound(SoundType.GLASS)
                .strength(-1.0F, 3600000.0F)
                .noDrops());
    }
}
