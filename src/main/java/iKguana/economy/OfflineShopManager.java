package iKguana.economy;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;

public class OfflineShopManager extends ShopBase implements Listener {
	public Config signShopList;

	public OfflineShopManager() {
		Economy.getInstance().getDataFolder().mkdirs();
		signShopList = new Config(Economy.getInstance().getDataFolder() + File.separator + "signShopList.yml", Config.YAML);
	}

// TODO REMOVE #1  
	HashMap<String, Long> points = new HashMap<>();

	public boolean restrictCPS(PlayerInteractEvent event) {
		if (points.containsKey(event.getPlayer().getName())) {
			if (points.get(event.getPlayer().getName()) + 100 < (new Date()).getTime()) {
				points.replace(event.getPlayer().getName(), (new Date()).getTime());
			} else
				return true;
		} else
			points.put(event.getPlayer().getName(), (new Date()).getTime());
		return false;
	}
//END

	@EventHandler
	public void playerInteractEvent(PlayerInteractEvent event) {
		if (restrictCPS(event))// TODO REMOVE #1
			return;

		if (event.getBlock().getLevel().getBlockEntity(event.getBlock()) instanceof BlockEntitySign)
			if (isShop(event.getBlock())) {
				if (getType(event.getBlock()).equals("SELL")) {
					if (sell(event.getPlayer(), getItem(event.getBlock()), getPrice(event.getBlock())))
						event.getPlayer().sendMessage("판매에 성공하셨습니다.");
					else
						event.getPlayer().sendMessage("판매에 실패하셨습니다.");
				} else if (getType(event.getBlock()).equals("BUY")) {
					if (buy(event.getPlayer(), getItem(event.getBlock()), getPrice(event.getBlock()))) {
						event.getPlayer().sendMessage("구매에 성공하셨습니다.");
					} else
						event.getPlayer().sendMessage("구매에 실패하셨습니다.");
				}
				event.setCancelled();
			} else {
				if (event.getPlayer().isOp()) {
					BlockEntitySign sign = (BlockEntitySign) event.getBlock().getLevel().getBlockEntity(event.getBlock());
					// [SELL] OR [BUY]
					// price
					String[] arr = sign.getText();
					if (arr[0].equals("[SELL]") || arr[0].equals("[BUY]")) {
						if (isInteger(arr[1]) && Integer.parseInt(arr[1]) > 0) {
							if (!event.getItem().isNull()) {
								registerShop(event.getBlock(), arr[0].equals("[SELL]") ? "SELL" : "BUY", event.getItem(), Integer.parseInt(arr[1]));
								String[] str = new String[] { arr[0].equals("[SELL]") ? "[ 판매 ]" : "[ 구매 ]", event.getItem().getName(), String.valueOf(event.getItem().getCount()), arr[1] + "α" };
								sign.setText(str);
								event.getPlayer().sendMessage("정상적으로 등록되었습니다.");
							}
						}
					}
				}
			}
	}

	@EventHandler
	public void blockBreakEvent(BlockBreakEvent event) {
		if (event.getBlock().getLevel().getBlockEntity(event.getBlock()) instanceof BlockEntitySign) {
			if (isShop(event.getBlock())) {
				if (event.getPlayer().isOp()) {
					removeShop(event.getBlock());
					event.getPlayer().sendMessage("삭제되었습니다.");
				} else
					event.setCancelled();
			}
		}
	}

	public boolean isShop(Position pos) {
		String coord = pos.getLevel().getName() + "." + pos.getFloorX() + "." + pos.getFloorY() + "." + pos.getFloorZ();
		if (signShopList.exists(coord) && !(signShopList.get(coord) instanceof String && signShopList.getString(coord).equals("NULL")))
			return true;
		return false;
	}

	public void registerShop(Position pos, String type, Item item, int price) {
		String coord = pos.getLevel().getName() + "." + pos.getFloorX() + "." + pos.getFloorY() + "." + pos.getFloorZ();
		signShopList.set(coord + ".TYPE", type);
		signShopList.set(coord + ".ITEMID", item.getId());
		signShopList.set(coord + ".ITEMDATA", item.getDamage());
		signShopList.set(coord + ".COUNT", item.getCount());
		signShopList.set(coord + ".PRICE", price);
		signShopList.save();
	}

	public String getType(Position pos) {
		String coord = pos.getLevel().getName() + "." + pos.getFloorX() + "." + pos.getFloorY() + "." + pos.getFloorZ();
		return signShopList.getString(coord + ".TYPE");
	}

	public Item getItem(Position pos) {
		String coord = pos.getLevel().getName() + "." + pos.getFloorX() + "." + pos.getFloorY() + "." + pos.getFloorZ();
		return Item.get(signShopList.getInt(coord + ".ITEMID"), signShopList.getInt(coord + ".ITEMDATA"), signShopList.getInt(coord + ".COUNT"));
	}

	public int getPrice(Position pos) {
		String coord = pos.getLevel().getName() + "." + pos.getFloorX() + "." + pos.getFloorY() + "." + pos.getFloorZ();
		return signShopList.getInt(coord + ".PRICE");
	}

	public void removeShop(Position pos) {
		String coord = pos.getLevel().getName() + "." + pos.getFloorX() + "." + pos.getFloorY() + "." + pos.getFloorZ();
		signShopList.set(coord, "NULL");
		signShopList.save();
	}
}
