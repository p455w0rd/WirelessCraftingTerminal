package net.p455w0rd.wirelesscraftingterminal.client.gui;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.api.storage.ITerminalHost;
import appeng.core.localization.GuiText;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.client.gui.widgets.GuiTabButton;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftingStatus;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketSwitchGuis;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketValueConfig;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.items.ItemEnum;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;


public class GuiCraftingStatus extends GuiCraftingCPU
{

	private final ContainerCraftingStatus status;
	private GuiButton selectCPU;

	private GuiTabButton originalGuiBtn;
	private int originalGui;
	private ItemStack myIcon = null;

	@SuppressWarnings("unused")
	public GuiCraftingStatus( final InventoryPlayer inventoryPlayer, final ITerminalHost te )
	{
		super( new ContainerCraftingStatus( inventoryPlayer, te ) );

		this.status = (ContainerCraftingStatus) this.inventorySlots;
		final Object target = this.status.getTarget();
		final IDefinitions definitions = AEApi.instance().definitions();
		final IParts parts = definitions.parts();

		if( target instanceof WirelessTerminalGuiObject )
		{
			for( final ItemStack wirelessTerminalStack : definitions.items().wirelessTerminal().maybeStack( 1 ).asSet() )
			{
				this.myIcon = wirelessTerminalStack;
			}
			
			ItemStack is = new ItemStack(ItemEnum.WIRELESS_CRAFTING_TERMINAL.getItem());
			((ItemWirelessCraftingTerminal) is.getItem()).injectAEPower(is, 6400001);
			myIcon = is;

			this.originalGui = Reference.GUI_WCT;
		}
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		super.actionPerformed( btn );

		final boolean backwards = Mouse.isButtonDown( 1 );

		if( btn == this.selectCPU )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Terminal.Cpu", backwards ? "Prev" : "Next" ) );
			}
			catch( final IOException e )
			{
				WCTLog.debug( e.getMessage() );
			}
		}

		if( btn == this.originalGuiBtn )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( this.originalGui ) );
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();

		this.selectCPU = new GuiButton( 0, this.guiLeft + 8, this.guiTop + this.ySize - 25, 150, 20, GuiText.CraftingCPU.getLocal() + ": " + GuiText.NoCraftingCPUs );
		// selectCPU.enabled = false;
		this.buttonList.add( this.selectCPU );

		if( this.myIcon != null )
		{
			this.buttonList.add( this.originalGuiBtn = new GuiTabButton( this.guiLeft + 213, this.guiTop - 4, this.myIcon, this.myIcon.getDisplayName(), itemRender ) );
			this.originalGuiBtn.setHideEdge( 13 );
		}
	}

	@Override
	public void drawScreen( final int mouseX, final int mouseY, final float btn )
	{
		this.updateCPUButtonText();
		super.drawScreen( mouseX, mouseY, btn );
	}

	private void updateCPUButtonText()
	{
		String btnTextText = GuiText.NoCraftingJobs.getLocal();

		if( this.status.selectedCpu >= 0 )// && status.selectedCpu < status.cpus.size() )
		{
			if( this.status.myName.length() > 0 )
			{
				final String name = this.status.myName.substring( 0, Math.min( 20, this.status.myName.length() ) );
				btnTextText = GuiText.CPUs.getLocal() + ": " + name;
			}
			else
			{
				btnTextText = GuiText.CPUs.getLocal() + ": #" + this.status.selectedCpu;
			}
		}

		if( this.status.noCPU )
		{
			btnTextText = GuiText.NoCraftingJobs.getLocal();
		}

		this.selectCPU.displayString = btnTextText;
	}

	@Override
	protected String getGuiDisplayName( final String in )
	{
		return in; // the cup name is on the button
	}
}
