package p455w0rd.wct.client.gui.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

/**
 * A modified version of the Minecraft text field.
 * You can initialize it over the full element span.
 * The mouse click area is increased to the full element
 * subtracted with the defined padding.
 *
 * The rendering does pay attention to the size of the '_' caret.
 */
public class MEGuiTextField extends GuiTextField {
	private static final int PADDING = 2;

	private final int _xPos;
	private final int _yPos;
	private final int _width;
	private final int _height;

	/**
	 * Uses the values to instantiate a padded version of a text field.
	 * Pays attention to the '_' caret.
	 *
	 * @param fontRenderer renderer for the strings
	 * @param xPos absolute left position
	 * @param yPos absolute top position
	 * @param width absolute width
	 * @param height absolute height
	 */
	public MEGuiTextField(final FontRenderer fontRenderer, final int xPos, final int yPos, final int width, final int height) {
		super(0, fontRenderer, xPos, yPos, width, height - 2 * PADDING);

		_xPos = xPos;
		_yPos = yPos;
		_width = width;
		_height = height;
	}

	@Override
	public boolean mouseClicked(final int xPos, final int yPos, final int button) {
		super.mouseClicked(xPos, yPos, button);

		final boolean requiresFocus = isMouseIn(xPos, yPos);

		if (!isFocused()) {
			setFocused(requiresFocus);
		}
		return true;
	}

	/**
	 * Checks if the mouse is within the element
	 *
	 * @param xCoord current x coord of the mouse
	 * @param yCoord current y coord of the mouse
	 *
	 * @return true if mouse position is within the text field area
	 */
	public boolean isMouseIn(final int xCoord, final int yCoord) {
		final boolean withinXRange = xCoord >= _xPos && xCoord <= _xPos + _width;
		final boolean withinYRange = _yPos <= yCoord && yCoord < _yPos + _height;

		return withinXRange && withinYRange;
	}
}
