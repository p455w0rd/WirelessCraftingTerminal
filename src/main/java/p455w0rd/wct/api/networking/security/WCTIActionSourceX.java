package p455w0rd.wct.api.networking.security;

import java.util.Optional;

import javax.annotation.Nonnull;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author p455w0rd
 *
 */
public interface WCTIActionSourceX extends IActionSource {

	/**
	 * If present, AE will consider the player being the source for the action.
	 *
	 * This will take precedence over {@link IActionSource#machine()} in any case.
	 *
	 * @return An optional player issuing the action.
	 */
	@Override
	@Nonnull
	Optional<EntityPlayer> player();

	/**
	 * If present, it indicates the {@link IActionHost} of the source.
	 *
	 * Should {@link IActionSource#player()} be absent, it will consider a machine as source.
	 *
	 * It is recommended to include the machine even when a player is present.
	 *
	 * @return An optional machine issuing the action or acting as proxy for a player.
	 */
	@Override
	@Nonnull
	Optional<IActionHost> machine();

	/**
	 * An {@link IActionSource} can have multiple optional contexts.
	 *
	 * It is strongly recommended to limit the uses for absolutely necessary cases.
	 *
	 * Currently there are no public contexts made available by AE.
	 * An example would be the context interfaces use internally to avoid looping items between each other.
	 */
	@Override
	@Nonnull
	<T> Optional<T> context(@Nonnull Class<T> key);

}
