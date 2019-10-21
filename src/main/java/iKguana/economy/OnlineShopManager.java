package iKguana.economy;

import java.io.File;
import java.util.Set;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import iKguana.simpledialog.SimpleDialog;

public class OnlineShopManager extends ShopBase implements CommandExecutor {
	public Config tradeList;

	public OnlineShopManager() {
		Economy.getInstance().getDataFolder().mkdirs();
		tradeList = new Config(Economy.getInstance().getDataFolder() + File.separator + "tradeList.yml", Config.YAML);

		PluginCommand<Economy> cmd = new PluginCommand<Economy>("shop", Economy.getInstance());
		cmd.setDescription("상점");
		cmd.setUsage("/shop");
		cmd.setExecutor(this);
		Economy.getInstance().getServer().getCommandMap().register("Shop", cmd);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getLabel().equals("shop")) {
			if (!sender.isOp() || (args.length > 0 && args[0].equals("user"))) {
				FormWindowModal window = new FormWindowModal("확인", "전체 목록을 확인하시겠습니까?\n아니요를 누르면 검색창으로 넘어갑니다.", "예", "아니요");
				SimpleDialog.sendDialog(this, "form_check1", (Player) sender, window);
			} else {
				if (args.length > 0 && args[0].equalsIgnoreCase("add")) {
					FormWindowCustom window = new FormWindowCustom("관리자 전용");
					window.addElement(new ElementInput("상품의 이름을 입력해주세요."));
					window.addElement(new ElementInput("상품의 아이템 코드를 입력해주세요."));
					window.addElement(new ElementInput("상품의 아이템 데미지를 입력해주세요.", "공백 가능"));
					window.addElement(new ElementInput("상품의 구매 가격을 입력해주세요.", "-1 : 구매 불가"));
					window.addElement(new ElementInput("상품의 판매가격을 입력해주세요.", "-1 : 판매 불가"));
					SimpleDialog.sendDialog(this, "form_addElement", (Player) sender, window);
				} else if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
					FormWindowCustom window = new FormWindowCustom("관리자 전용");
					window.addElement(new ElementInput("상품명을 입력해주세요."));
					SimpleDialog.sendDialog(this, "form_search", (Player) sender, window, true);
				} else
					onCommand(sender, command, label, new String[] { "user" });
			}
			return true;
		}
		return false;
	}

	public void form_check1(PlayerFormRespondedEvent event, Object data) {
		if (((FormResponseModal) event.getResponse()).getClickedButtonId() == 0) {
			FormWindowSimple window = new FormWindowSimple("상품검색", "구매 또는 판매할 상품을 선택해주세요.");
			Set<String> tl = tradeList.getKeys(false);
			for (String name : tl)
				window.addButton(new ElementButton(name));
			if (tl.size() == 0)
				window.setContent("등록된 상품이 없습니다.");
			SimpleDialog.sendDialog(this, "form_lookingElement", event.getPlayer(), window, false);
		} else {
			FormWindowCustom window = new FormWindowCustom("상품 검색");
			window.addElement(new ElementInput("상품명을 입력해주세요."));
			SimpleDialog.sendDialog(this, "form_search", event.getPlayer(), window, false);
		}
	}

	public void form_search(PlayerFormRespondedEvent event, Object data) {
		FormWindowSimple window = new FormWindowSimple("상품검색", "구매 또는 판매할 상품을 선택해주세요.");
		Set<String> tl = tradeList.getKeys(false);
		for (String name : tl)
			if (name.toLowerCase().indexOf(((FormResponseCustom) event.getResponse()).getInputResponse(0).toLowerCase()) != -1)
				window.addButton(new ElementButton(name));
		if (tl.size() == 0)
			window.setContent("등록된 상품이 없습니다.");
		SimpleDialog.sendDialog(this, "form_lookingElement", event.getPlayer(), window, data);
	}

	public void form_lookingElement(PlayerFormRespondedEvent event, Object data) {
		String element = ((FormResponseSimple) event.getResponse()).getClickedButton().getText();
		if ((Boolean) data) {
			FormWindowModal window = new FormWindowModal("상품 삭제", "정말로 삭제하시겠습니까?", "예", "아니요");
			SimpleDialog.sendDialog(this, "form_removeElement", event.getPlayer(), window, element);
		} else {
			FormWindowCustom window = new FormWindowCustom("상품 구매/판매");
			window.addElement(new ElementLabel(element + "를 선택하셨습니다."));
			window.addElement(new ElementLabel("구매가격 : " + tradeList.getInt(element + ".buyPrice") + " 판매가격 : " + tradeList.getInt(element + ".sellPrice")));
			window.addElement(new ElementToggle("구매 / 판매"));
			window.addElement(new ElementInput("구매 / 판매 할 갯수를 입력해주세요."));
			SimpleDialog.sendDialog(this, "form_tradeElement", event.getPlayer(), window, element);
		}
	}

	public void form_tradeElement(PlayerFormRespondedEvent event, Object data) {
		String element = (String) data;
		FormResponseCustom response = (FormResponseCustom) event.getResponse();

		Item item = Item.get(tradeList.getInt(element + ".itemId"), tradeList.getInt(element + ".itemDamage"));
		int sellPrice = tradeList.getInt(element + ".sellPrice");
		int buyPrice = tradeList.getInt(element + ".buyPrice");

		if (!response.getToggleResponse(2)) {// 구매
			if (buyPrice > 0) {
				if (isInteger(response.getInputResponse(3)) && Integer.parseInt(response.getInputResponse(3)) > 0) {
					int count = Integer.parseInt(response.getInputResponse(3));
					if (MoneyAPI.getInstance().getMoney(event.getPlayer().getName()) >= count * buyPrice) {
						item.setCount(count);
						if (event.getPlayer().getInventory().canAddItem(item)) {
							MoneyAPI.getInstance().takeMoney(event.getPlayer().getName(), buyPrice * count);
							event.getPlayer().getInventory().addItem(item);
							event.getPlayer().sendMessage("구매에 성공하였습니다.");
						} else {
							event.getPlayer().sendMessage("인벤토리가 부족합니다.");
						}
					} else
						event.getPlayer().sendMessage("잔액이 부족합니다.");
				} else
					event.getPlayer().sendMessage("수량은 양수인 정수로만 입력해주세요.");
			} else
				event.getPlayer().sendMessage("구매할수 없는 상품입니다." + buyPrice);
		} else {// 판매
			if (sellPrice > 0) {
				if (isInteger(response.getInputResponse(3)) && Integer.parseInt(response.getInputResponse(3)) > 0) {
					int count = Integer.parseInt(response.getInputResponse(3));
					item.setCount(count);
					if (event.getPlayer().getInventory().contains(item)) {
						MoneyAPI.getInstance().addMoney(event.getPlayer().getName(), sellPrice * count);
						takeItem(event.getPlayer(), item);
						event.getPlayer().sendMessage("판매에 성공하였습니다.");
					} else
						event.getPlayer().sendMessage("아이템이 부족합니다.");
				} else
					event.getPlayer().sendMessage("수량은 양수인 정수로만 입력해주세요.");
			} else
				event.getPlayer().sendMessage("판매할수 없는 상품입니다." + tradeList.get(element));
		}
	}

	public void form_addElement(PlayerFormRespondedEvent event, Object data) {
		FormResponseCustom response = (FormResponseCustom) event.getResponse();
		String name = response.getInputResponse(0);
		if (name.trim().length() == 0 || !isInteger(response.getInputResponse(1), (response.getInputResponse(2).trim().length() == 0 ? "0" : response.getInputResponse(2)), response.getInputResponse(3), response.getInputResponse(4))) {
			event.getPlayer().sendMessage("올바르지 않은 자료형이있습니다.");
			return;
		}

		tradeList.set(name + ".itemId", Integer.parseInt(response.getInputResponse(1)));
		tradeList.set(name + ".itemDamage", Integer.parseInt(response.getInputResponse(2).trim().length() == 0 ? "0" : response.getInputResponse(2)));
		tradeList.set(name + ".buyPrice", Integer.parseInt(response.getInputResponse(3)));
		tradeList.set(name + ".sellPrice", Integer.parseInt(response.getInputResponse(4)));
		tradeList.save();

		event.getPlayer().sendMessage("정상적으로 등록되었습니다.");
	}

	public void form_removeElement(PlayerFormRespondedEvent event, Object data) {
		if (((FormResponseModal) event.getResponse()).getClickedButtonId() == 0) {
			String name = (String) data;

			tradeList.remove(name);
			tradeList.save();

			event.getPlayer().sendMessage("삭제되었습니다.");
		} else
			event.getPlayer().sendMessage("취소하였습니다.");
	}

}
