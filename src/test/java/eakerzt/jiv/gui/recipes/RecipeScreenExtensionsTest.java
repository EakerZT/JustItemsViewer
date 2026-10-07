package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.widgets.IRecipeScreenExtension;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.Rect2i;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RecipeScreenExtensionsTest {
    private static class Extension implements IRecipeScreenExtension {
        int releases, cancellations, drags;
        double lastX, lastY;
        public boolean mouseClicked(double x,double y,int button) { return x>=0 && x<100 && y>=0 && y<100; }
        public boolean mouseDragged(double x,double y,int button,double dx,double dy) { drags++;lastX=x;lastY=y;return false; }
        public boolean mouseReleased(double x,double y,int button) { releases++;lastX=x;lastY=y;return false; }
        public void cancelScreenInteraction() { cancellations++; }
    }
    @SuppressWarnings("unchecked")
    private static IRecipeLayoutWithButtons<?> layout(Extension extension) {
        var drawable=(IRecipeLayoutDrawable<?>) Proxy.newProxyInstance(IRecipeLayoutDrawable.class.getClassLoader(),
            new Class<?>[]{IRecipeLayoutDrawable.class},(proxy,method,args) -> switch(method.getName()) {
                case "getScreenExtensions" -> List.of(extension);
                case "getRect" -> new Rect2i(30,40,100,100);
                case "equals" -> proxy==args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                default -> throw new UnsupportedOperationException(method.getName());
            });
        return (IRecipeLayoutWithButtons<?>) Proxy.newProxyInstance(IRecipeLayoutWithButtons.class.getClassLoader(),
            new Class<?>[]{IRecipeLayoutWithButtons.class},(proxy,method,args) -> {
                if(method.getName().equals("getRecipeLayout"))return drawable;
                throw new UnsupportedOperationException(method.getName());
            });
    }
    private static MouseButtonEvent event(double x,double y,int button) { return new MouseButtonEvent(x,y,new MouseButtonInfo(button,0)); }

    @Test void capturesOutsideRecipeAndIgnoresOtherButtonRelease() {
        var manager=new RecipeScreenExtensions();var extension=new Extension();
        manager.setLayouts(List.of(layout(extension)));
        assertTrue(manager.clicked(event(50,60,2)));
        assertFalse(manager.released(event(500,600,0)));
        assertTrue(manager.dragged(event(500,600,2),450,540));
        assertEquals(470,extension.lastX);assertEquals(560,extension.lastY);
        assertTrue(manager.released(event(500,600,2)));
        assertEquals(1,extension.releases);
        assertFalse(manager.dragged(event(50,60,2),1,1));
    }
    @Test void switchingLayoutsCancelsCaptureAndDoesNotSendReleaseToNewRecipe() {
        var manager=new RecipeScreenExtensions();var first=new Extension();var second=new Extension();
        var old=layout(first);
        manager.setLayouts(List.of(old));assertTrue(manager.clicked(event(50,60,0)));
        manager.setLayouts(List.of(old));assertEquals(0,first.cancellations);
        manager.setLayouts(List.of(layout(second)));assertEquals(1,first.cancellations);
        assertFalse(manager.released(event(50,60,0)));assertEquals(0,second.releases);
    }
    @Test void closingClearsCaptureAndReopeningAllowsFreshGesture() {
        var manager=new RecipeScreenExtensions();var extension=new Extension();var layout=layout(extension);
        manager.setLayouts(List.of(layout));assertTrue(manager.clicked(event(50,60,1)));
        manager.clear();assertEquals(1,extension.cancellations);
        assertFalse(manager.released(event(50,60,1)));
        manager.setLayouts(List.of(layout));assertTrue(manager.clicked(event(50,60,1)));
        assertTrue(manager.released(event(50,60,1)));
    }
}
