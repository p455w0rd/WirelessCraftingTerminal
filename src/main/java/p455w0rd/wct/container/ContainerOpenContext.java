package p455w0rd.wct.container;

import appeng.api.parts.IPart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerOpenContext {

	private final boolean isItem;
	private World w;
	private int x;
	private int y;
	private int z;
	private EnumFacing side;

	public ContainerOpenContext(final Object myItem) {
		final boolean isWorld = myItem instanceof IPart || myItem instanceof TileEntity;
		isItem = !isWorld;
	}

	public TileEntity getTile() {
		if (isItem) {
			return null;
		}
		return getWorld().getTileEntity(new BlockPos(getX(), getY(), getZ()));
	}

	public EnumFacing getSide() {
		return side;
	}

	public void setSide(final EnumFacing side) {
		this.side = side;
	}

	private int getZ() {
		return z;
	}

	public void setZ(final int z) {
		this.z = z;
	}

	private int getY() {
		return y;
	}

	public void setY(final int y) {
		this.y = y;
	}

	private int getX() {
		return x;
	}

	public void setX(final int x) {
		this.x = x;
	}

	private World getWorld() {
		return w;
	}

	public void setWorld(final World w) {
		this.w = w;
	}
}
