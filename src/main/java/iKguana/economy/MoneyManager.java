package iKguana.economy;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import iKguana.profiler.Profiler;

public class MoneyManager implements CommandExecutor {
	public MoneyManager() {
		registerCommand("money", "잔액 확인", "/money");
		registerCommand("sendmoney", "송금", "/sendmoney <target> <money>");
	}

	public void registerCommand(String label, String description, String usage) {
		PluginCommand<Economy> cmd = new PluginCommand<Economy>(label, Economy.getInstance());
		cmd.setDescription(description);
		cmd.setUsage(usage);
		cmd.setExecutor(this);
		Economy.getInstance().getServer().getCommandMap().register("Money", cmd);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getLabel().equalsIgnoreCase("money")) {
			sender.sendMessage("당신의 잔액은 " + MoneyAPI.getInstance().getMoney(sender.getName()) + "입니다.");
			return true;
		} else if (command.getLabel().equals("sendmoney")) {
			if (args.length >= 2)
				if (Profiler.getInstance().isPlayerRegistered(args[0]))
					if (isNumber(args[1]) && MoneyAPI.getInstance().getMoney(args[0]) >= Integer.parseInt(args[1])) {
						MoneyAPI.getInstance().takeMoney(Profiler.getInstance().getExactName(args[0]), Integer.parseInt(args[1]));
						MoneyAPI.getInstance().addMoney(Profiler.getInstance().getExactName(args[0]), Integer.parseInt(args[1]));
						sender.sendMessage("송금에 성공하였습니다.  잔액 : " + MoneyAPI.getInstance().getMoney(sender.getName()));
						return true;
					}
			sender.sendMessage("실패하였습니다.");
			return true;
		}
		return false;
	}

	public boolean isNumber(String num) {
		try {
			Integer.parseInt(num);
			return false;
		} catch (NumberFormatException err) {
		}
		return true;
	}
}
