package eakerzt.jiv.library.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FluidAmountRendererTest {
    @Test void preservesSmallAmountsAndAbbreviatesLargeAmounts() {
        assertEquals("1", FluidAmountRenderer.format(1));
        assertEquals("1000", FluidAmountRenderer.format(1000));
        assertEquals("9999", FluidAmountRenderer.format(9999));
        assertEquals("10K", FluidAmountRenderer.format(10000));
        assertEquals("999K", FluidAmountRenderer.format(999999));
        assertEquals("1M", FluidAmountRenderer.format(1000000));
        assertEquals("1G", FluidAmountRenderer.format(1000000000));
        assertEquals("1T", FluidAmountRenderer.format(1000000000000L));
        assertEquals("1P", FluidAmountRenderer.format(1000000000000000L));
        assertEquals("1E", FluidAmountRenderer.format(1000000000000000000L));
        assertEquals("9E", FluidAmountRenderer.format(Long.MAX_VALUE));
    }
}
