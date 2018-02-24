package com.infinityraider.agricraft.items;

import com.agricraft.agricore.config.AgriConfigCategory;
import com.agricraft.agricore.config.AgriConfigurable;
import com.google.common.collect.ImmutableList;
import com.infinityraider.agricraft.api.v1.AgriApi;
import com.infinityraider.agricraft.api.v1.crop.IAgriCrop;
import com.infinityraider.agricraft.api.v1.items.IAgriTrowelItem;
import com.infinityraider.agricraft.api.v1.seed.AgriSeed;
import com.infinityraider.agricraft.items.tabs.AgriTabs;
import com.infinityraider.agricraft.reference.AgriNBT;
import com.infinityraider.infinitylib.item.IItemWithModel;
import com.infinityraider.infinitylib.item.ItemBase;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTrowel extends ItemBase implements IAgriTrowelItem, IItemWithModel {

    @AgriConfigurable(
            category = AgriConfigCategory.TOOLS,
            key = "Enable Trowel",
            comment = "Set to false to disable the Trowel."
    )
    public static boolean enableTrowel = true;

    public ItemTrowel() {
        super("trowel");
        this.maxStackSize = 1;
        this.setCreativeTab(AgriTabs.TAB_AGRICRAFT);
    }

    //I'm overriding this just to be sure
    @Override
    public boolean canItemEditBlocks() {
        return true;
    }

    // this is called when you right click with this item in hand
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitx, float hity, float hitz) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IAgriCrop) {
            IAgriCrop crop = (IAgriCrop) te;
            Optional<AgriSeed> seed = AgriApi.getSeedRegistry().valueOf(stack);
            if (crop.isCrossCrop()) {
                // Cross-crops cannot hold seeds, so the trowel cannot extract from or insert into them.
                return EnumActionResult.FAIL;
            } else if (crop.hasSeed() && !seed.isPresent()) {
                seed = Optional.ofNullable(crop.getSeed());
                crop.setSeed(null);
                if (seed.isPresent()) {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setString(AgriNBT.SEED, seed.get().getPlant().getId());
                    seed.get().getStat().writeToNBT(tag);
                    stack.setTagCompound(tag);
                    stack.setItemDamage(1);
                    return EnumActionResult.SUCCESS;
                } else {
                    return EnumActionResult.FAIL;
                }
            } else if (seed.isPresent() && !crop.hasSeed()) {
                if (crop.setSeed(seed.get())) {
                    stack.setTagCompound(new NBTTagCompound());
                    stack.setItemDamage(0);
                    return EnumActionResult.SUCCESS;
                } else {
                    return EnumActionResult.FAIL;
                }
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public boolean isEnabled() {
        return enableTrowel;
    }

    @Override
    public List<Tuple<Integer, ModelResourceLocation>> getModelDefinitions() {
        return ImmutableList.of(
                new Tuple<>(0, new ModelResourceLocation(this.getRegistryName() + "")),
                new Tuple<>(1, new ModelResourceLocation(this.getRegistryName() + "_full"))
        );
    }

}
