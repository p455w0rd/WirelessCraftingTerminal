package p455w0rd.wct.client.me;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import appeng.api.AEApi;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;
import appeng.integration.Integrations;
import appeng.items.storage.ItemViewCell;
import appeng.util.ItemSorters;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;
import net.minecraft.item.ItemStack;

public class ItemRepo {

	private final IItemList<IAEItemStack> list = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
	private final ArrayList<IAEItemStack> view = new ArrayList<IAEItemStack>();
	//private final ArrayList<ItemStack> dsp = new ArrayList<ItemStack>();
	private final IScrollSource src;
	private final ISortSource sortSrc;

	private int rowSize = 9;

	private String searchString = "";
	private IPartitionList<IAEItemStack> myPartitionList;
	private String innerSearch = "";
	private boolean hasPower;

	public ItemRepo(final IScrollSource src, final ISortSource sortSrc) {
		this.src = src;
		this.sortSrc = sortSrc;
	}

	public IAEItemStack getReferenceItem(int idx) {
		idx += src.getCurrentScroll() * rowSize;

		if (idx >= view.size()) {
			return null;
		}
		return view.get(idx);
	}

	void setSearch(final String search) {
		searchString = search == null ? "" : search;
	}

	public void postUpdate(final IAEItemStack is) {
		final IAEItemStack st = list.findPrecise(is);

		if (st != null) {
			st.reset();
			st.add(is);
		}
		else {
			list.add(is);
		}
	}

	public void setViewCell(final ItemStack[] list) {
		myPartitionList = ItemViewCell.createFilter(list);
		updateView();
	}

	public void updateView() {
		view.clear();
		//dsp.clear();

		view.ensureCapacity(list.size());
		//dsp.ensureCapacity(list.size());

		final Enum<?> viewMode = sortSrc.getSortDisplay();
		final Enum<?> searchMode = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);
		if (searchMode == SearchBoxMode.JEI_AUTOSEARCH || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH || searchMode == SearchBoxMode.JEI_AUTOSEARCH_KEEP || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH_KEEP) {
			updateJEI(searchString);
		}

		innerSearch = searchString;
		final boolean terminalSearchToolTips = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_TOOLTIPS) != YesNo.NO;

		boolean searchMod = false;
		if (innerSearch.startsWith("@")) {
			searchMod = true;
			innerSearch = innerSearch.substring(1);
		}

		Pattern m = null;
		try {
			m = Pattern.compile(innerSearch.toLowerCase(), Pattern.CASE_INSENSITIVE);
		}
		catch (final Throwable ignore) {
			try {
				m = Pattern.compile(Pattern.quote(innerSearch.toLowerCase()), Pattern.CASE_INSENSITIVE);
			}
			catch (final Throwable __) {
				return;
			}
		}

		boolean notDone = false;
		for (IAEItemStack is : list) {
			if (myPartitionList != null) {
				if (!myPartitionList.isListed(is)) {
					continue;
				}
			}

			if (viewMode == ViewItems.CRAFTABLE && !is.isCraftable()) {
				continue;
			}

			if (viewMode == ViewItems.CRAFTABLE) {
				is = is.copy();
				is.setStackSize(0);
			}

			if (viewMode == ViewItems.STORED && is.getStackSize() == 0) {
				continue;
			}

			final String dspName = searchMod ? Platform.getModId(is) : Platform.getItemDisplayName(is);
			notDone = true;

			if (m.matcher(dspName.toLowerCase()).find()) {
				view.add(is);
				notDone = false;
			}

			if (terminalSearchToolTips && notDone) {
				for (final Object lp : Platform.getTooltip(is)) {
					if (lp instanceof String && m.matcher((CharSequence) lp).find()) {
						view.add(is);
						notDone = false;
						break;
					}
				}
			}

			/*
			 * if ( terminalSearchMods && notDone ) { if ( m.matcher( Platform.getMod( is.getItemStack() ) ).find() ) {
			 * view.add( is ); notDone = false; } }
			 */
		}

		final Enum<?> SortBy = sortSrc.getSortBy();
		final Enum<?> SortDir = sortSrc.getSortDir();

		ItemSorters.setDirection((appeng.api.config.SortDir) SortDir);
		ItemSorters.init();

		if (SortBy == SortOrder.MOD) {
			Collections.sort(view, ItemSorters.CONFIG_BASED_SORT_BY_MOD);
		}
		else if (SortBy == SortOrder.AMOUNT) {
			Collections.sort(view, ItemSorters.CONFIG_BASED_SORT_BY_SIZE);
		}
		else if (SortBy == SortOrder.INVTWEAKS) {
			Collections.sort(view, ItemSorters.CONFIG_BASED_SORT_BY_INV_TWEAKS);
		}
		else {
			Collections.sort(view, ItemSorters.CONFIG_BASED_SORT_BY_NAME);
		}

	}

	private void updateJEI(final String filter) {
		Integrations.jei().setSearchText(filter);
	}

	public int size() {
		return view.size();
	}

	public void clear() {
		list.resetStatus();
	}

	public boolean hasPower() {
		return hasPower;
	}

	public void setPower(final boolean hasPower) {
		this.hasPower = hasPower;
	}

	public int getRowSize() {
		return rowSize;
	}

	public void setRowSize(final int rowSize) {
		this.rowSize = rowSize;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(@Nonnull final String searchString) {
		this.searchString = searchString;
	}
}
