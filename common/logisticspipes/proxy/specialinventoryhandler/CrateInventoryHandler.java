package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class CrateInventoryHandler extends SpecialInventoryHandler {

	private static Class <? extends Object> crateClass;
	private static Method getPileData;
	private static Class <? extends Object> cratePileDataClass;
	private static Method getNumItems;
	private static Method removeItems;
	private static Method getItemStack;
	private static Method getItemCount;
	private static Method spaceForItem;

	private final TileEntity _tile;
	private final boolean _hideOnePerStack;

	private CrateInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = tile;
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public CrateInventoryHandler() {
		_tile = null;
		_hideOnePerStack = false;
	}

	@Override
	public boolean init() {
		try {
			crateClass = Class.forName("net.mcft.copy.betterstorage.blocks.TileEntityCrate");
			getPileData = crateClass.getDeclaredMethod("getPileData", new Class[]{});
			cratePileDataClass = Class.forName("net.mcft.copy.betterstorage.blocks.CratePileData");
			getNumItems = cratePileDataClass.getDeclaredMethod("getNumItems", new Class[]{});
			removeItems = cratePileDataClass.getDeclaredMethod("removeItems", new Class[]{ItemStack.class, int.class});
			getItemStack = cratePileDataClass.getDeclaredMethod("getItemStack", new Class[]{int.class});
			getItemCount = cratePileDataClass.getDeclaredMethod("getItemCount", new Class[]{ItemStack.class});
			spaceForItem = cratePileDataClass.getDeclaredMethod("spaceForItem", new Class[]{ItemStack.class});
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean isType(TileEntity tile) {
		return crateClass.isAssignableFrom(tile.getClass());
	}

	@Override
	public IInventoryUtil getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new CrateInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}


	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int numitems = (Integer) getNumItems.invoke(cratePileData, new Object[]{});
			HashMap<ItemIdentifier, Integer> map = new HashMap<ItemIdentifier, Integer>((int)(numitems * 1.5));
			for(int i = 0; i < numitems; i++) {
				ItemStack itemStack = (ItemStack) getItemStack.invoke(cratePileData, new Object[]{i});
				ItemIdentifier itemId = ItemIdentifier.get(itemStack);
				int stackSize = itemStack.stackSize - (_hideOnePerStack?1:0);
				if (!map.containsKey(itemId)){
					map.put(itemId, stackSize);
				} else {
					map.put(itemId, map.get(itemId) + stackSize);
				}
			}
			return map;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return new HashMap<ItemIdentifier, Integer>();
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int count = (Integer) getItemCount.invoke(cratePileData, new Object[]{itemIdent.makeNormalStack(1)});
			if (count <= (_hideOnePerStack?1:0)) return null;
			return (ItemStack) removeItems.invoke(cratePileData, new Object[]{itemIdent.makeNormalStack(1), 1});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean containsItem(ItemIdentifier itemIdent) {
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int count = (Integer) getItemCount.invoke(cratePileData, new Object[]{itemIdent.makeNormalStack(1)});
			return (count > 0);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent) {
		try {
			Object cratePileData = getPileData.invoke(_tile, new Object[]{});
			int space = (Integer) spaceForItem.invoke(cratePileData, new Object[]{itemIdent.makeNormalStack(1)});
			return space;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
