package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.common.gui.JivTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Compact help hint and NEI-style, one-shortcut-per-line help. */
public final class BookmarkHotkeyTooltip {
	private BookmarkHotkeyTooltip() {}

	public static void hint(JivTooltip tooltip) {
		tooltip.add(Component.literal("[").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("Alt").withStyle(ChatFormatting.GOLD))
				.append(Component.literal("]").withStyle(ChatFormatting.GRAY)));
	}

	public static void add(JivTooltip tooltip, Component key, String action) {
		tooltip.add(key.copy().withStyle(ChatFormatting.GOLD)
				.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.translatable("jiv.bookmarks.hotkeys." + action).withStyle(ChatFormatting.GRAY)));
	}

	public static void add(JivTooltip tooltip, String key, String action) {
		add(tooltip, Component.literal(key), action);
	}

	public static Component mouse(String modifiers, String action) {
		return Component.literal(modifiers).append(Component.translatable("jiv.bookmarks.hotkeys.key." + action));
	}
}
