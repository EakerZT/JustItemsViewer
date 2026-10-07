[English](Recipe-Screen-Extensions-en.md) | [简体中文](Recipe-Screen-Extensions.md)

# 配方窗口扩展 API

**适用范围：JIV `0.0.1-alpha-2` 起，已发布到 Maven Central。** 编译和运行均需使用 alpha-2 或更新版本；`0.0.1-alpha-1` 不包含这些方法。

该 API 用于在 JIV 配方窗口中添加配方专属标题、左右原料面板和直接鼠标操作，不需要 Mixin 或访问 `common`、`gui`、`library` 实现类。分类通过 `createRecipeExtras` 注册扩展；扩展状态属于具体配方布局。它不改变全局工作站 / 触媒注册，也不负责服务端构建、复制或其他游戏操作。

## 注册与分页

在已有的 `IRecipeCategory<DemoRecipe>` 中，`gui` 是注册分类时保存的 `IGuiHelper`，`recipe.title()`、`recipe.materials()`、`recipe.alternatives()` 表示自己的配方数据方法：

```java
@Override
public int getMaxRecipesPerPage() {
    return 1;
}

@Override
public void createRecipeExtras(IRecipeExtrasBuilder builder, DemoRecipe recipe, IFocusGroup focuses) {
    builder.addScreenExtension(new RecipePanelExtension(gui, recipe.materials(), false, recipe.title()));
    builder.addScreenExtension(new RecipePanelExtension(gui, recipe.alternatives(), true, recipe.title()));
    // 预览、文本等普通控件仍通过 builder.addWidget(...) 注册。
}
```

`IRecipeCategory.getMaxRecipesPerPage()` 默认返回 `Integer.MAX_VALUE`，不额外限制物理空间允许的条数；小于 1 的值按 1 处理。限制每页一条适合三维预览及配方专属标题。多条配方同时显示时，窗口采用按布局 / 扩展注册顺序遇到的第一个非空扩展标题。

分类的 `getWidth()` / `getHeight()` 变化后，JIV 在下一次更新窗口布局时重建缓存，例如 GUI 缩放引起的高度变化。这不代表每帧自动调用分类重新布局；不要用分类尺寸保存某一条配方的可变状态。

## 方法参考

所有下列方法均有默认实现；按需要覆写：

| `IRecipeScreenExtension` 方法 | 默认行为与用途 |
| --- | --- |
| `getScreenTitle()` | 返回 `null`，使用分类标题；非空标题使用 JIV 原有居中、截断和完整标题 Tooltip |
| `updateScreenLayout(IGuiProperties, int availableSideHeight)` | 无操作；接收窗口本体位置与侧栏可用高度，不包含扩展边界 |
| `getExtraGuiAreas()` | 返回空列表；实际占用矩形加入窗口范围，供原料列表避让和窗口命中使用 |
| `drawScreen(GuiGraphicsExtractor, int mouseX, int mouseY)` | 无操作；绘制面板及屏幕装饰 |
| `drawScreenTooltips(GuiGraphicsExtractor, int mouseX, int mouseY)` | 无操作；绘制自己的 Tooltip，已有交互式原料 Tooltip 时跳过此回调 |
| `getScreenSlotUnderMouse(double mouseX, double mouseY)` | 返回 `Optional.empty()`；提供命中的原料槽，用于原有原料查询和交互式 Tooltip |
| `mouseClicked(double x, double y, int button)` | 返回 `false`；返回 `true` 后消费按下并捕获该按钮 |
| `mouseDragged(double x, double y, int button, double dx, double dy)` | 返回 `false`；仅对捕获的按钮调用 |
| `mouseReleased(double x, double y, int button)` | 返回 `false`；送达捕获的按钮，并结束该按钮的捕获 |
| `mouseScrolled(double x, double y, double scrollX, double scrollY)` | 返回 `false`；返回 `true` 后消费滚轮，阻止普通翻页 / 切分类处理 |
| `cancelScreenInteraction()` | 无操作；布局切换、关闭或移除窗口时清理扩展自己的操作状态 |

布局初始化时创建扩展对象，之后会重复调用布局更新 / 查询。不要在这些回调中重复注册扩展、原料或全局工作站。`IRecipeLayoutDrawable.getScreenExtensions()` 返回当前布局的扩展列表，并在首次访问时创建配方 extras；返回列表是只读快照。由 JIV 实现布局接口，插件不应自行实现它。

## 坐标约定

| 回调 / 数据 | 坐标 |
| --- | --- |
| `updateScreenLayout` 的 `IGuiProperties` | 配方窗口本体的绝对屏幕位置，包含窗口标题 / 导航区域 |
| `getExtraGuiAreas`、绘制方法、槽位查询 | 绝对屏幕坐标 |
| `mouseClicked` / `mouseDragged` / `mouseReleased` / `mouseScrolled` | 相对于当前配方布局左上角的坐标，可能落在布局范围外 |
| `mouseDragged` 的 `dx` / `dy` | 本次鼠标移动增量 |

鼠标回调不会自动命中测试；扩展自行决定可操作区域。没有 `getArea()` 的二次坐标变换。绝对定位的原料槽返回 `new RecipeSlotUnderMouse(slot, 0, 0)`；不要再次减去窗口或配方偏移。

## 鼠标捕获与清理

`mouseClicked` 是真实按下回调，位于普通输入处理之前，不使用普通 JIV 输入的“按下模拟、释放执行”流程。消费按下后，拖动和释放即使离开预览区也会送达，并保持消费状态，即使对应回调返回 `false`。

捕获按按钮区分；释放另一个按钮不会结束原按钮的捕获。配方 / 分类切换导致布局更换、关闭窗口或通过其他界面替换窗口时，JIV 取消捕获并调用 `cancelScreenInteraction()`。取消不等同于正常释放，不应在取消回调中提交右键选择等操作。

例如扩展中的左键拖动逻辑可写为：

```java
private boolean dragging;
private float yaw;

@Override
public boolean mouseClicked(double x, double y, int button) {
    if (button != 0 || x < 0 || x >= 176 || y < 0 || y >= 140) return false;
    dragging = true;
    return true;
}

@Override
public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
    if (!dragging || button != 0) return false;
    yaw += (float) dx * 0.025f;
    return true;
}

@Override
public boolean mouseReleased(double x, double y, int button) {
    if (button != 0) return false;
    boolean handled = dragging;
    dragging = false;
    return handled;
}

@Override
public void cancelScreenInteraction() {
    dragging = false;
}
```

这里的区域尺寸只是示例，应根据自己的预览与按钮调整。需要右键选择或中键平移时，在自己的扩展中实现对应按钮规则。

## 原生左右面板示例

`IGuiHelper.drawRecipeSidePanel(GuiGraphicsExtractor graphics, Rect2i bounds, boolean rightSide)` 绘制原生边框和槽背景；`rightSide=true` 仅镜像边框，不镜像物品。正尺寸至少 17×16 像素；任一尺寸不大于零时不绘制；正尺寸不足时抛出 `IllegalArgumentException`。方法不绘制物品，也不自动提供 Tooltip 或原料命中。

下面的完整类保留物品数量，按可用高度增加列数而不滚动；空列表隐藏面板，一项列表仍显示。物品变化时可在自己的扩展中重建槽位，然后由布局回调刷新位置。占用矩形用于避让，不会自动裁剪或保证任意宽度面板都能容纳在屏幕内，面板尺寸由扩展负责。

```java
package examplemod.jiv;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawable;
import eakerzt.jiv.api.gui.inputs.RecipeSlotUnderMouse;
import eakerzt.jiv.api.gui.widgets.IRecipeScreenExtension;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Source-build example: native frame, counted slots, column overflow, and ingredient lookup. */
public final class RecipePanelExtension implements IRecipeScreenExtension {
    private final IGuiHelper gui;
    private final List<IRecipeSlotDrawable> slots;
    private final boolean right;
    private final Component title;
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    public RecipePanelExtension(IGuiHelper gui, List<ItemStack> contents, boolean right, Component title) {
        this.gui = gui;
        this.right = right;
        this.title = title;
        this.slots = contents.stream().map(stack -> {
            ItemStack copy = stack.copy();
            return gui.createRecipeSlotDrawable(RecipeIngredientRole.INPUT,
                acceptor -> acceptor.add(VanillaTypes.ITEM_STACK, copy), Set.of(), 0);
        }).toList();
    }

    @Override
    public Component getScreenTitle() { return title; }

    @Override
    public void updateScreenLayout(IGuiProperties area, int availableSideHeight) {
        if (slots.isEmpty()) {
            bounds = new Rect2i(0, 0, 0, 0);
            return;
        }
        int maxRows = Math.max(1, (availableSideHeight - 12) / 16);
        int columns = Math.ceilDiv(slots.size(), maxRows);
        int rows = Math.ceilDiv(slots.size(), columns);
        int width = 12 + columns * 16;
        int x = right ? area.guiRight() - 6 : area.guiLeft() - width + 6;
        bounds = new Rect2i(x, area.guiTop(), width, 12 + rows * 16);
        for (int i = 0; i < slots.size(); i++) {
            int column = right ? i / rows : columns - 1 - i / rows;
            slots.get(i).setPosition(x + 6 + column * 16, area.guiTop() + 6 + i % rows * 16);
        }
    }

    @Override
    public List<ScreenRectangle> getExtraGuiAreas() {
        if (slots.isEmpty()) return List.of();
        return List.of(new ScreenRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
    }

    @Override
    public void drawScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (slots.isEmpty()) return;
        gui.drawRecipeSidePanel(graphics, bounds, right);
        slots.forEach(slot -> slot.draw(graphics, slot.isMouseOver(mouseX, mouseY)));
    }

    @Override
    public Optional<RecipeSlotUnderMouse> getScreenSlotUnderMouse(double mouseX, double mouseY) {
        return slots.stream().filter(slot -> slot.isMouseOver(mouseX, mouseY))
            .findFirst().map(slot -> new RecipeSlotUnderMouse(slot, 0, 0));
    }

    @Override
    public void drawScreenTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        getScreenSlotUnderMouse(mouseX, mouseY)
            .ifPresent(hovered -> hovered.slot().drawTooltip(graphics, mouseX, mouseY));
    }
}
```

## 生命周期与适用范围

扩展状态与具体布局绑定，切换配方或尺寸重建可能创建新对象。不要把旋转角度、选中位置和拖动状态保存为分类共享字段或全局单例。普通 `IRecipeWidget` 与屏幕扩展可共用同一配方视图对象，但要分别注册它需要的接口。

扩展只参与 JIV 的配方窗口。书签、物品 Tooltip 或其他地方嵌入的配方布局不会执行这些屏幕回调；普通 widget 仍按原有方式工作。需要影响服务器的按钮操作由自己的网络协议处理，并在服务端验证。
