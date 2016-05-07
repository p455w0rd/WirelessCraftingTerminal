package net.p455w0rd.wirelesscraftingterminal.integration;

import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;

public enum IntegrationRegistry {
	INSTANCE;

	private static final String PACKAGE_PREFIX = "net.p455w0rd.wirelesscraftingterminal.integration.modules.";

	private final Collection<IntegrationNode> modules = new LinkedList<IntegrationNode>();

	public void add(final IntegrationType type) {
		if (type.side == IntegrationSide.CLIENT && FMLLaunchHandler.side() == Side.SERVER) {
			return;
		}

		if (type.side == IntegrationSide.SERVER && FMLLaunchHandler.side() == Side.CLIENT) {
			return;
		}

		this.modules.add(new IntegrationNode(type.dspName, type.modID, type, PACKAGE_PREFIX + type.name()));
	}

	public void init() {
		for (final IntegrationType type : IntegrationType.values())
		{
			IntegrationRegistry.INSTANCE.add(type);
		}
		
		for (final IntegrationNode node : this.modules) {
			node.call(IntegrationStage.PRE_INIT);
		}

		for (final IntegrationNode node : this.modules) {
			node.call(IntegrationStage.INIT);
		}
	}

	public void postInit() {
		for (final IntegrationNode node : this.modules) {
			node.call(IntegrationStage.POST_INIT);
		}
	}

	public String getStatus() {
		final StringBuilder builder = new StringBuilder(this.modules.size() * 3);

		for (final IntegrationNode node : this.modules) {
			if (builder.length() != 0) {
				builder.append(", ");
			}

			final String integrationState = node.getShortName() + ":" + (node.getState() == IntegrationStage.FAILED ? "OFF" : "ON");
			builder.append(integrationState);
		}

		return builder.toString();
	}

	public boolean isEnabled(final IntegrationType name) {
		for (final IntegrationNode node : this.modules) {
			if (node.getShortName() == name) {
				return node.isActive();
			}
		}
		return false;
	}

	@Nonnull
	public Object getInstance(final IntegrationType name) {
		for (final IntegrationNode node : this.modules) {
			if (node.getShortName() == name && node.isActive()) {
				return node.getInstance();
			}
		}

		throw new IllegalStateException("integration with " + name.name() + " is disabled.");
	}

}
