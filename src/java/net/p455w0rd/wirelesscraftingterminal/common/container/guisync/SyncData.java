package net.p455w0rd.wirelesscraftingterminal.common.container.guisync;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.common.container.WCTBaseContainer;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketValueConfig;

public class SyncData
{
	private final Container source;
	private final Field field;
	private final int channel;
	private Object clientVersion;

	public SyncData( final Container container, final Field field, final GuiSync annotation )
	{
		if (container instanceof ContainerWirelessCraftingTerminal) {
			this.source = (ContainerWirelessCraftingTerminal) container;
		}
		else if (container instanceof WCTBaseContainer) {
			this.source = (WCTBaseContainer) container;
		}
		else {
			this.source = container;
		}
		this.clientVersion = null;
		this.field = field;
		this.channel = annotation.value();
	}

	public int getChannel()
	{
		return this.channel;
	}

	public void tick( final ICrafting c )
	{
		try
		{
			final Object val = this.field.get( this.source );
			if( val != null && this.clientVersion == null )
			{
				this.send( c, val );
			}
			else if( !val.equals( this.clientVersion ) )
			{
				this.send( c, val );
			}
		}
		catch( final IllegalArgumentException e )
		{
			WCTLog.debug( e.getMessage() );
		}
		catch( final IllegalAccessException e )
		{
			WCTLog.debug( e.getMessage() );
		}
		catch( final IOException e )
		{
			WCTLog.debug( e.getMessage() );
		}
	}

	@SuppressWarnings("rawtypes")
	private void send( final ICrafting o, final Object val ) throws IOException
	{
		if( val instanceof String )
		{
			if( o instanceof EntityPlayerMP )
			{
				NetworkHandler.instance.sendTo( new PacketValueConfig( "SyncDat." + this.channel, (String) val ), (EntityPlayerMP) o );
			}
		}
		else if( this.field.getType().isEnum() )
		{
			o.sendProgressBarUpdate( this.source, this.channel, ( (Enum) val ).ordinal() );
		}
		else if( val instanceof Long || val.getClass() == long.class )
		{
			//NetworkHandler.instance.sendTo( new PacketProgressBar( this.channel, (Long) val ), (EntityPlayerMP) o );
		}
		else if( val instanceof Boolean || val.getClass() == boolean.class )
		{
			o.sendProgressBarUpdate( this.source, this.channel, ( (Boolean) val ) ? 1 : 0 );
		}
		else
		{
			o.sendProgressBarUpdate( this.source, this.channel, (Integer) val );
		}

		this.clientVersion = val;
	}

	public void update( final Object val )
	{
		try
		{
			final Object oldValue = this.field.get( this.source );
			if( val instanceof String )
			{
				this.updateString( oldValue, (String) val );
			}
			else
			{
				this.updateValue( oldValue, (Long) val );
			}
		}
		catch( final IllegalArgumentException e )
		{
			WCTLog.debug( e.getMessage() );
		}
		catch( final IllegalAccessException e )
		{
			WCTLog.debug( e.getMessage() );
		}
	}

	private void updateString( final Object oldValue, final String val )
	{
		try
		{
			this.field.set( this.source, val );
		}
		catch( final IllegalArgumentException e )
		{
			WCTLog.debug( e.getMessage() );
		}
		catch( final IllegalAccessException e )
		{
			WCTLog.debug( e.getMessage() );
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateValue( final Object oldValue, final long val )
	{
		try
		{
			if( this.field.getType().isEnum() )
			{
				final EnumSet<? extends Enum> valList = EnumSet.allOf( (Class<? extends Enum>) this.field.getType() );
				for( final Enum e : valList )
				{
					if( e.ordinal() == val )
					{
						this.field.set( this.source, e );
						break;
					}
				}
			}
			else
			{
				if( this.field.getType().equals( int.class ) )
				{
					this.field.set( this.source, (int) val );
				}
				else if( this.field.getType().equals( long.class ) )
				{
					this.field.set( this.source, val );
				}
				else if( this.field.getType().equals( boolean.class ) )
				{
					this.field.set( this.source, val == 1 );
				}
				else if( this.field.getType().equals( Integer.class ) )
				{
					this.field.set( this.source, (int) val );
				}
				else if( this.field.getType().equals( Long.class ) )
				{
					this.field.set( this.source, val );
				}
				else if( this.field.getType().equals( Boolean.class ) )
				{
					this.field.set( this.source, val == 1 );
				}
			}

			if (this.source instanceof ContainerWirelessCraftingTerminal) {
				((ContainerWirelessCraftingTerminal) this.source).onUpdate( this.field.getName(), oldValue, this.field.get( this.source ) );
			}
			if (this.source instanceof WCTBaseContainer) {
				((WCTBaseContainer) this.source).onUpdate( this.field.getName(), oldValue, this.field.get( this.source ) );
			}
		}
		catch( final IllegalArgumentException e )
		{
			WCTLog.debug( e.getMessage() );
		}
		catch( final IllegalAccessException e )
		{
			WCTLog.debug( e.getMessage() );
		}
	}
}
