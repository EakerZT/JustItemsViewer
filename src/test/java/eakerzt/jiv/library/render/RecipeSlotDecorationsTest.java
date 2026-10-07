package eakerzt.jiv.library.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeSlotDecorationsTest {
    @Test void validatesProbabilityRangeAndRejectsNonFiniteValues() {
        for(double chance:new double[]{-0.01,1.01,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY})
            assertThrows(IllegalArgumentException.class,()->new RecipeSlotDecorations(false,true,chance));
        assertDoesNotThrow(()->new RecipeSlotDecorations(true,true,0));
        assertDoesNotThrow(()->new RecipeSlotDecorations(true,true,1));
    }
    @Test void formatsExactTooltipAndCompactLabelWithoutFalseZeroOrCertainty() {
        assertEquals("12.3456789",RecipeSlotDecorations.exactPercent(0.123456789));
        assertEquals("12.35%",RecipeSlotDecorations.compactPercent(0.123456789));
        assertEquals("0%",RecipeSlotDecorations.compactPercent(0));
        assertEquals("100%",RecipeSlotDecorations.compactPercent(1));
        assertEquals("<0.01%",RecipeSlotDecorations.compactPercent(0.000001));
        assertEquals(">99.99%",RecipeSlotDecorations.compactPercent(0.999999));
    }
}
