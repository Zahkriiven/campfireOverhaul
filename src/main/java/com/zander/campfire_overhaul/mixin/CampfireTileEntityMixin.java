package com.zander.campfire_overhaul.mixin;

import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.util.ICampfireExtra;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireTileEntity.class)
public abstract class CampfireTileEntityMixin extends TileEntity implements ICampfireExtra {

    @Shadow public abstract void dropAllItems();

    private int lifeTime = 0;

    @Override
    public int getLifeTime() {
        return lifeTime;
    }

    @Override
    public void addLifeTime(int add) {
        lifeTime+=add;
    }

    @Override
    public void setLifeTime(int set) {
        lifeTime = set;
    }

    public CampfireTileEntityMixin(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn.CAMPFIRE);
    }

    private void extinguish() {
        if (!this.world.isRemote) {
            this.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            this.world.setBlockState(this.pos, this.getBlockState().with(CampfireBlock.LIT, false));
        }
    }

    @Inject(at = @At("RETURN"), method = "tick()V")
    private void tick(CallbackInfo ci)
    {
        if(world != null)
        {
            if(lifeTime != -1) {
                if(CampfireBlock.isLit(world.getBlockState(getPos())))
                    if (lifeTime > 0)
                        lifeTime--;
                    else {
                        extinguish();
                    }
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "<init>()V")
    private void init(CallbackInfo ci)
    {
        if(!CampfireOverhaulConfig.CAMPFIRE_INFINITE_LIFE_TIME.get())
            lifeTime = CampfireOverhaulConfig.CAMPFIRE_DEFAULT_LIFE_TIME.get();
        else
            lifeTime = -1;
    }

    @Inject(at = @At("RETURN"), method = "read(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)V")
    private void readAdditional(BlockState state, CompoundNBT nbt, CallbackInfo ci)
    {
        if(nbt.contains("LifeTime", 3))
        {
            setLifeTime(nbt.getInt("LifeTime"));
        }
    }

    @Inject(at = @At("RETURN"), method = "write(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;", cancellable = true)
    private void writeAdditional(CompoundNBT compound, CallbackInfoReturnable<CompoundNBT> cir)
    {
        CompoundNBT nbt = cir.getReturnValue();
        nbt.putInt("LifeTime", lifeTime);
        cir.setReturnValue(nbt);
    }

}