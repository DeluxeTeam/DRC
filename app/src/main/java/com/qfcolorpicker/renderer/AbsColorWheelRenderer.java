package com.qfcolorpicker.renderer;

import com.qfcolorpicker.ColorCircle;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsColorWheelRenderer implements ColorWheelRenderer {
	ColorWheelRenderOption colorWheelRenderOption;
	final List<ColorCircle> colorCircleList = new ArrayList<>();

	public void initWith(ColorWheelRenderOption colorWheelRenderOption) {
		this.colorWheelRenderOption = colorWheelRenderOption;
		this.colorCircleList.clear();
	}

	@Override
	public ColorWheelRenderOption getRenderOption() {
		if (colorWheelRenderOption == null) colorWheelRenderOption = new ColorWheelRenderOption();
		return colorWheelRenderOption;
	}

	public List<ColorCircle> getColorCircleList() {
		return colorCircleList;
	}

	int getAlphaValueAsInt() {
		return Math.round(colorWheelRenderOption.alpha * 255);
	}

	int calcTotalCount(float radius, float size) {
		return Math.max(1, (int) ((1f - GAP_PERCENTAGE) * Math.PI / (Math.asin(size / radius)) + 0.5f));
	}
}