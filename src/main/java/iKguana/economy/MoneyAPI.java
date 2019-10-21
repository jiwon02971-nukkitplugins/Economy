package iKguana.economy;

import iKguana.profiler.Profiler;

public class MoneyAPI {
	private static MoneyAPI $instance = null;
	private static final String name = "MoneyAPI";
	private int DEFAULT_MONEY = 10000;

	public MoneyAPI() {
		if ($instance != null)
			return;

		$instance = this;

		Economy.getInstance().getDataFolder().mkdirs();

		Economy.getInstance().saveResource("config.yml", false);
		DEFAULT_MONEY = Economy.getInstance().getConfig().getInt("DEFAULT-MONEY");
	}

	public long getMoney(String player) {
		return Profiler.getInstance().open(player).getLong(p("Money"), DEFAULT_MONEY);
	}

	public void setMoney(String player, long money) {
		Profiler.getInstance().open(player).set(p("Money"), money);
	}

	public void addMoney(String player, long money) {
		long currentMoney = getMoney(player);
		setMoney(player, currentMoney + money);
	}

	public void takeMoney(String player, long money) {
		long currentMoney = getMoney(player);
		setMoney(player, currentMoney - money);
	}

	private String p(String iKguana) {
		return name + "." + iKguana;
	}

	public static MoneyAPI getInstance() {
		return $instance;
	}
}
