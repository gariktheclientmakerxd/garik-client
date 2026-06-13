package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.I18n;
import net.lax1dude.eaglercraft.v1_8.Keyboard;

public class GuiModMenu extends GuiScreen {
	private GuiButton flyButton;
	private GuiButton banButton;
	private GuiButton kickButton;
	private GuiButton spamButton;
	private GuiButton closeButton;
	private GuiButton prevTargetButton;
	private GuiButton nextTargetButton;
	private final List<NetworkPlayerInfo> playerInfoList = new ArrayList<>();
	private int selectedPlayerIndex = -1;

	/**+
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		int buttonWidth = 150;
		int buttonHeight = 20;
		int centerX = this.width / 2;
		int topY = this.height / 4;
		int spacing = 24;

		this.buttonList.add(this.flyButton = new GuiButton(0, centerX - buttonWidth / 2, topY, buttonWidth,
				buttonHeight, getFlyButtonText()));
		this.buttonList.add(this.banButton = new GuiButton(1, centerX - buttonWidth / 2, topY + spacing, buttonWidth,
				buttonHeight, "Ban selected player"));
		this.buttonList.add(this.kickButton = new GuiButton(2, centerX - buttonWidth / 2, topY + spacing * 2,
				buttonWidth, buttonHeight, "Kick selected player"));
		this.buttonList.add(this.spamButton = new GuiButton(3, centerX - buttonWidth / 2, topY + spacing * 3,
				buttonWidth, buttonHeight, "Spam chat"));
		this.buttonList.add(this.closeButton = new GuiButton(4, centerX - buttonWidth / 2, topY + spacing * 4,
				buttonWidth, buttonHeight, I18n.format("gui.done")));
		this.buttonList.add(this.prevTargetButton = new GuiButton(5, centerX - 160, topY + spacing * 5, 70,
				buttonHeight, "<"));
		this.buttonList.add(this.nextTargetButton = new GuiButton(6, centerX + 90, topY + spacing * 5, 70,
				buttonHeight, ">"));

		this.refreshPlayerList();
		this.updateButtonStates();
	}

	/**+
	 * Called when the screen is unloaded. Used to disable keyboard repeat events.
	 */
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	/**+
	 * Called by the controls from the buttonList when activated.
	 */
	protected void actionPerformed(GuiButton button) {
		if (!button.enabled) {
			return;
		}

		switch (button.id) {
		case 0:
			toggleFly();
			break;
		case 1:
			banSelectedPlayer();
			break;
		case 2:
			kickSelectedPlayer();
			break;
		case 3:
			spamChat();
			break;
		case 4:
			this.mc.displayGuiScreen((GuiScreen) null);
			return;
		case 5:
			selectPreviousTarget();
			break;
		case 6:
			selectNextTarget();
			break;
		}

		this.updateButtonStates();
	}

	protected void keyTyped(char parChar1, int parInt1) {
		if (parInt1 == 1) {
			this.mc.displayGuiScreen((GuiScreen) null);
		} else {
			super.keyTyped(parChar1, parInt1);
		}
	}

	public void updateScreen() {
		this.flyButton.displayString = getFlyButtonText();
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, "Mod Menu", this.width / 2, 20, 16777215);
		String targetLabel = "Selected player: " + getSelectedPlayerName();
		this.drawCenteredString(this.fontRendererObj, targetLabel, this.width / 2,
				this.height / 4 + 24 * 5 + 6, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void refreshPlayerList() {
		this.playerInfoList.clear();
		if (this.mc.getNetHandler() != null && this.mc.thePlayer != null) {
			for (NetworkPlayerInfo info : this.mc.getNetHandler().getPlayerInfoMap()) {
				String name = info.getGameProfile().getName();
				if (!name.equals(this.mc.thePlayer.getName())) {
					this.playerInfoList.add(info);
				}
			}
		}

		if (this.playerInfoList.isEmpty()) {
			this.selectedPlayerIndex = -1;
		} else if (this.selectedPlayerIndex < 0 || this.selectedPlayerIndex >= this.playerInfoList.size()) {
			this.selectedPlayerIndex = 0;
		}
	}

	private void selectPreviousTarget() {
		if (!this.playerInfoList.isEmpty()) {
			this.selectedPlayerIndex = (this.selectedPlayerIndex - 1 + this.playerInfoList.size())
				% this.playerInfoList.size();
		}
	}

	private void selectNextTarget() {
		if (!this.playerInfoList.isEmpty()) {
			this.selectedPlayerIndex = (this.selectedPlayerIndex + 1) % this.playerInfoList.size();
		}
	}

	private String getSelectedPlayerName() {
		if (this.selectedPlayerIndex >= 0 && this.selectedPlayerIndex < this.playerInfoList.size()) {
			return this.playerInfoList.get(this.selectedPlayerIndex).getGameProfile().getName();
		}
		return "None";
	}

	private void updateButtonStates() {
		this.refreshPlayerList();
		this.banButton.enabled = this.kickButton.enabled = this.selectedPlayerIndex >= 0;
		this.prevTargetButton.enabled = this.nextTargetButton.enabled = this.selectedPlayerIndex >= 0;
		this.flyButton.displayString = getFlyButtonText();
	}

	private String getFlyButtonText() {
		if (this.mc.thePlayer != null && this.mc.thePlayer.capabilities.allowFlying) {
			return "Disable Fly";
		}

		return "Enable Fly";
	}

	private void toggleFly() {
		if (this.mc.thePlayer == null) {
			return;
		}

		this.mc.thePlayer.capabilities.allowFlying = !this.mc.thePlayer.capabilities.allowFlying;
		if (!this.mc.thePlayer.capabilities.allowFlying) {
			this.mc.thePlayer.capabilities.isFlying = false;
		}
		this.mc.thePlayer.sendPlayerAbilities();
	}

	private void banSelectedPlayer() {
		String target = getSelectedPlayerName();
		if (!"None".equals(target)) {
			this.mc.thePlayer.sendChatMessage("/ban " + target);
		}
	}

	private void kickSelectedPlayer() {
		String target = getSelectedPlayerName();
		if (!"None".equals(target)) {
			this.mc.thePlayer.sendChatMessage("/kick " + target);
		}
	}

	private void spamChat() {
		if (this.mc.thePlayer == null) {
			return;
		}

		String target = getSelectedPlayerName();
		for (int i = 1; i <= 8; ++i) {
			this.mc.thePlayer.sendChatMessage("Spam " + i + (target.equals("None") ? "" : " " + target));
		}
	}
}
