package com.example.examplemod.worldgen.structures;

import com.example.examplemod.ExampleMod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class PrisonSavedData extends SavedData {
    private static final String DATA_NAME = ExampleMod.MODID + "_prisondata";
    private int pos[];

    public int[] getDefaultPrisonPos() {
        return pos;
    }

    public void setDefaultPrisonPos(int new_pos[]) {
        pos = new_pos;
        this.setDirty();
    }

    public PrisonSavedData(CompoundTag nbt) {this.load(nbt);}
	public PrisonSavedData() {}

    public void load(CompoundTag nbt) {
        pos = nbt.getIntArray("default_prison");
        ExampleMod.LOGGER.info("Loaded default prison position from file");
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound = new CompoundTag();
        compound.putIntArray("default_prison", pos);
        ExampleMod.LOGGER.info("Saved default prison position to file");
        return compound;
    }
    
    public static PrisonSavedData get(ServerLevel world) {
		return world.getDataStorage().computeIfAbsent(PrisonSavedData::new, PrisonSavedData::new, DATA_NAME);
	}
}
