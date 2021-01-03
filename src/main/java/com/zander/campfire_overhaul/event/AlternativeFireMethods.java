package com.zander.campfire_overhaul.event;

import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.util.ICampfireExtra;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.IceBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class AlternativeFireMethods {
    @SubscribeEvent
    public void fireStarterFlint(PlayerInteractEvent.RightClickBlock event) {

        if(!CampfireOverhaulConfig.DOUBLE_FLINT_IGNITION.get())
            return;

        World world = event.getWorld();
        PlayerEntity player = event.getPlayer();
        BlockPos blockPos = event.getPos();
        BlockState blockState = event.getWorld().getBlockState(event.getPos());

        if (!CampfireBlock.canBeLit(blockState))
            return;

        if (event.getHand() == Hand.MAIN_HAND)
            if (player.getHeldItemMainhand().getItem() == Items.FLINT && player.getHeldItemOffhand().getItem() == Items.FLINT) {
                Random rand = event.getWorld().rand;
                player.swingArm(Hand.MAIN_HAND);

                if (rand.nextFloat() < CampfireOverhaulConfig.FLINT_IGNITE_CHANCE.get() && !world.isRemote) {
                    world.setBlockState(blockPos, blockState.with(BlockStateProperties.LIT, Boolean.valueOf(true)), 3);
                }

                world.playSound(null, blockPos, SoundEvents.BLOCK_STONE_STEP, SoundCategory.BLOCKS, 1.0F, 2F + rand.nextFloat() * 0.4F);

                if (world.isRemote) {
                    for (int i = 0; i < 5; i++)
                    {
                        world.addParticle(ParticleTypes.SMOKE,player.getPosX() + player.getLookVec().getX() + rand.nextFloat() * 0.25, player.getPosY() + 0.5f + rand.nextFloat() * 0.25, player.getPosZ() + player.getLookVec().getZ() + rand.nextFloat() * 0.25, 0,0.01,0);
                    }
                    world.addParticle(ParticleTypes.FLAME,player.getPosX() + player.getLookVec().getX() + rand.nextFloat() * 0.25, player.getPosY() + 0.5f + rand.nextFloat() * 0.25, player.getPosZ() + player.getLookVec().getZ() + rand.nextFloat() * 0.25, 0,0.01,0);

                }
            }
    }

    @SubscribeEvent
    public void checkCampfireLifeTime(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        BlockPos pos = event.getPos();
        World world = event.getWorld();
        BlockState blockState = world.getBlockState(pos);

        if (!(blockState.getBlock() instanceof CampfireBlock) || !player.getHeldItemMainhand().isEmpty())
            return;

        if (event.getHand() == Hand.MAIN_HAND) {
            ICampfireExtra info = (ICampfireExtra) world.getTileEntity(pos);

            if(!world.isRemote)
                player.sendMessage(new StringTextComponent("This campfire have " + info.getLifeTime() / 20 + " seconds of burn time left."), player.getUniqueID());
        }
    }
}