package com.example.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class UnbreakableGlassBlock extends AbstractGlassBlock {
    public UnbreakableGlassBlock() {
        super(Properties.of(Material.GLASS)
                .sound(SoundType.GLASS)
                .strength(-1.0F, 3600000.0F)
                .noOcclusion()
                .noDrops());
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos){
        return true;
    }
}
