package p455w0rd.wct.container.slot;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

public class SlotArmor extends AppEngSlot {

	final EntityEquipmentSlot armorType;

	final EntityPlayer player;

	public SlotArmor(EntityPlayer player, IItemHandler inventory, int slot, int x, int y, EntityEquipmentSlot armorType) {
		super(inventory, slot, x, y);
		this.player = player;
		this.armorType = armorType;
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		Item item = (stack == null ? null : stack.getItem());
		return item != null && item.isValidArmor(stack, EntityEquipmentSlot.values()[armorType.ordinal() + 1], player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBackgroundSprite() {
		String name = ItemArmor.EMPTY_SLOT_NAMES[armorType.ordinal() - 1];
		return name == null ? null : getBackgroundMap().getAtlasSprite(name);
	}

}
