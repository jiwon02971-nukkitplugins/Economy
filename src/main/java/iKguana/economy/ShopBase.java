package iKguana.economy;

import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.item.Item;

public class ShopBase {
	public boolean buy(Player player, Item item, int price) {
		if (MoneyAPI.getInstance().getMoney(player.getName()) >= price) {
			if (player.getInventory().canAddItem(item)) {
				MoneyAPI.getInstance().takeMoney(player.getName(), price);
				player.getInventory().addItem(item);
				return true;
			}
		}
		return false;
	}

	public boolean sell(Player player, Item item, int price) {
		if (player.getInventory().contains(item)) {
			takeItem(player, item);
			MoneyAPI.getInstance().addMoney(player.getName(), price);
			return true;
		}
		return false;
	}

	public void takeItem(Player player, Item item) {
		int count = item.getCount();
		Map<Integer, Item> map = player.getInventory().getContents();
		for (int i : map.keySet()) {
			Item slot = map.get(i);
			if (slot.equals(item, true)) {
				if (slot.getCount() > count) {
					slot.setCount(slot.getCount() - count);
					map.replace(i, slot);
					break;
				} else if (slot.getCount() == count) {
					map.replace(i, Item.get(Item.AIR));
					break;
				} else if (slot.getCount() < count) {
					count = count - slot.getCount();
					map.replace(i, Item.get(Item.AIR));
				}
			}
		}
		player.getInventory().setContents(map);
		player.sendAllInventories();
	}

	public boolean isInteger(String... nums) {
		for (String num : nums)
			try {
				if (Integer.parseInt(num) != Double.parseDouble(num))
					return false;
			} catch (NumberFormatException err) {
				return false;
			}
		return true;
	}
}
