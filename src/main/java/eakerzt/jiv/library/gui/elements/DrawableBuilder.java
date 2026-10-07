package eakerzt.jiv.library.gui.elements;

import eakerzt.jiv.api.gui.ITickTimer;
import eakerzt.jiv.api.gui.drawable.IDrawableAnimated;
import eakerzt.jiv.api.gui.drawable.IDrawableBuilder;
import eakerzt.jiv.api.gui.drawable.IDrawableStatic;
import eakerzt.jiv.common.gui.elements.DrawableAnimated;
import eakerzt.jiv.common.gui.elements.DrawableResource;
import eakerzt.jiv.common.util.ErrorUtil;
import net.minecraft.resources.Identifier;

public class DrawableBuilder implements IDrawableBuilder {
	private final Identifier id;
	private int u;
	private int v;
	private int width;
	private int height;
	private int textureWidth = 256;
	private int textureHeight = 256;
	private int paddingTop = 0;
	private int paddingBottom = 0;
	private int paddingLeft = 0;
	private int paddingRight = 0;

	public DrawableBuilder(Identifier id, int u, int v, int width, int height) {
		ErrorUtil.checkNotNull(id, "id");
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
		this.id = id;
	}

	@Override
	public IDrawableBuilder setTextureSize(int width, int height) {
		this.textureWidth = width;
		this.textureHeight = height;
		return this;
	}

	@Override
	public IDrawableBuilder addPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
		this.paddingTop = paddingTop;
		this.paddingBottom = paddingBottom;
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
		return this;
	}

	@Override
	public IDrawableBuilder trim(int trimTop, int trimBottom, int trimLeft, int trimRight) {
		this.u += trimLeft;
		this.v += trimTop;
		this.width -= trimLeft + trimRight;
		this.height -= trimTop + trimBottom;
		return this;
	}

	@Override
	public IDrawableStatic build() {
		return new DrawableResource(id, u, v, width, height, paddingTop, paddingBottom, paddingLeft, paddingRight, textureWidth, textureHeight);
	}

	@Override
	public IDrawableAnimated buildAnimated(int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
		ErrorUtil.checkNotNull(startDirection, "startDirection");
		IDrawableStatic drawable = build();
		return new DrawableAnimated(drawable, ticksPerCycle, startDirection, inverted);
	}

	@Override
	public IDrawableAnimated buildAnimated(ITickTimer tickTimer, IDrawableAnimated.StartDirection startDirection) {
		IDrawableStatic drawable = build();
		return new DrawableAnimated(drawable, tickTimer, startDirection);
	}
}
