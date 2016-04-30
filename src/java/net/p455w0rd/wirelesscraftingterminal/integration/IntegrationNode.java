package net.p455w0rd.wirelesscraftingterminal.integration;

import java.lang.reflect.Field;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;
import net.p455w0rd.wirelesscraftingterminal.api.exceptions.ModNotInstalled;
import net.p455w0rd.wirelesscraftingterminal.common.utils.WCTLog;


public final class IntegrationNode
{

	private final String displayName;
	private final String modID;
	private final IntegrationType shortName;
	private IntegrationStage state = IntegrationStage.PRE_INIT;
	@SuppressWarnings("unused")
	private IntegrationStage failedStage = IntegrationStage.PRE_INIT;
	private Throwable exception = null;
	private String name = null;
	private Class<?> classValue = null;
	private Object instance;
	private IIntegrationModule mod = null;

	public IntegrationNode( final String displayName, final String modID, final IntegrationType shortName, final String name )
	{
		this.displayName = displayName;
		this.shortName = shortName;
		this.modID = modID;
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.getShortName().name() + ':' + this.getState().name();
	}

	boolean isActive()
	{
		if( this.getState() == IntegrationStage.PRE_INIT )
		{
			this.call( IntegrationStage.PRE_INIT );
		}

		return this.getState() != IntegrationStage.FAILED;
	}

	void call( final IntegrationStage stage )
	{
		if( this.getState() != IntegrationStage.FAILED )
		{
			if( this.getState().ordinal() > stage.ordinal() )
			{
				return;
			}

			try
			{
				switch( stage )
				{
					case PRE_INIT:
						final ModAPIManager apiManager = ModAPIManager.INSTANCE;
						boolean enabled = this.modID == null || Loader.isModLoaded( this.modID ) || apiManager.hasAPI( this.modID );

						if( enabled )
						{
							this.classValue = this.getClass().getClassLoader().loadClass( this.name );
							this.mod = (IIntegrationModule) this.classValue.getConstructor().newInstance();
							final Field f = this.classValue.getField( "instance" );
							f.set( this.classValue, this.setInstance( this.mod ) );
						}
						else
						{
							throw new ModNotInstalled( this.modID );
						}

						this.setState( IntegrationStage.INIT );

						break;
					case INIT:
						this.mod.init();
						this.setState( IntegrationStage.POST_INIT );

						break;
					case POST_INIT:
						this.mod.postInit();
						this.setState( IntegrationStage.READY );

						break;
					case FAILED:
					default:
						break;
				}
			}
			catch( final Throwable t )
			{
				this.failedStage = stage;
				this.exception = t;
				this.setState( IntegrationStage.FAILED );
			}
		}

		if( stage == IntegrationStage.POST_INIT )
		{
			if( this.getState() == IntegrationStage.FAILED )
			{
				WCTLog.info( this.displayName + " - Integration Disabled" );
				if( !( this.exception instanceof ModNotInstalled ) )
				{
					WCTLog.integration( this.exception );
				}
			}
			else
			{
				WCTLog.info( this.displayName + " - Integration Enable" );
			}
		}
	}

	Object getInstance()
	{
		return this.instance;
	}

	private Object setInstance( final Object instance )
	{
		this.instance = instance;
		return instance;
	}

	IntegrationType getShortName()
	{
		return this.shortName;
	}

	IntegrationStage getState()
	{
		return this.state;
	}

	private void setState( final IntegrationStage state )
	{
		this.state = state;
	}
}
