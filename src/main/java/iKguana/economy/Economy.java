package iKguana.economy;

import cn.nukkit.plugin.PluginBase;

public class Economy extends PluginBase {
	private static Economy $instance;;

	public void onEnable() {
		$instance = this;

		new MoneyManager();
		if (getConfig().getBoolean("enable-online-shop"))
			new OnlineShopManager();
		if (getConfig().getBoolean("enable-offline-shop"))
			getServer().getPluginManager().registerEvents(new OfflineShopManager(), this);
	}

	public static Economy getInstance() {
		return $instance;
	}
}
