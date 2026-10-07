[English](GUI-Integration-en.md) | [简体中文](GUI-Integration.md)

# 界面集成

所有 GUI 注册都放在插件的 `registerGuiHandlers(IGuiHandlerRegistration registration)` 中。这里的 `MachineScreen` 表示自己模组的界面类，示例片段需要按自己的字段和坐标调整。

## 点击进度条打开分类

```java
registration.addRecipeClickArea(
    MachineScreen.class, 78, 32, 28, 18, DemoRecipe.TYPE);
```

`x`、`y` 是相对于容器界面左上角的坐标；`width`、`height` 是命中区域尺寸。可以在末尾传多个配方类型。这个方法适用于 `AbstractContainerScreen`，并通过容器 GUI handler 提供点击区。

点击区只打开分类，不负责执行配方或转移物品。

## 让原料侧栏避开额外面板

默认容器矩形以外的标签页、储罐面板或升级区域，应通过 `IGuiContainerHandler<T>.getGuiExtraAreas` 返回：

```java
registration.addGuiContainerHandler(MachineScreen.class,
    new IGuiContainerHandler<MachineScreen>() {
        @Override
        public List<Rect2i> getGuiExtraAreas(MachineScreen screen) {
            return List.of(new Rect2i(
                screen.getGuiLeft() + screen.getXSize(),
                screen.getGuiTop(), 24, screen.getYSize()));
        }
    });
```

上述界面边界 getter 名称按自己的 Screen 实现调整。`Rect2i` 来自 `net.minecraft.client.renderer.Rect2i`。避让矩形使用 **屏幕绝对坐标**，与点击区的 GUI 相对坐标不同。动态面板应根据当前展开状态返回实际占用范围。

多个容器界面共享的额外区域可通过 `addGlobalGuiHandler` 注册。对于普通 `Screen`，使用 `addGuiScreenHandler` 和 `IScreenHandler` 提供 GUI 边界信息，使 JIV 知道如何排列侧栏。

## 让 JIV 识别储罐或自绘原料

标准物品槽通常已经支持悬停按 `R` / `U`。储罐或自绘图标可以覆写 `getClickableIngredientUnderMouse`，在鼠标命中时，通过传入的 `IClickableIngredientFactory` 返回可点击原料。

该方法的鼠标参数是屏幕坐标。返回对象的原料类型必须已经注册；例如储罐可用 `NeoForgeTypes.FLUID_STACK`。未命中区域时返回 `Optional.empty()`。

## 接受幽灵原料拖拽

筛选器和自动合成模板可以调用 `addGhostIngredientHandler(screenClass, handler)` 注册 `IGhostIngredientHandler<T>`。

处理流程：

1. `getTargetsTyped(gui, typedIngredient, doStart)` 返回能够接受该原料的目标矩形。
2. 玩家释放鼠标时，对应 `Target<I>.accept(I ingredient)` 接收原料。
3. `onComplete()` 在拖拽结束时调用，无论拖拽是否成功。

目标区域使用屏幕坐标。`doStart=false` 是预览查询，不应更改筛选器；真正成功接收时再保存筛选条件。原料是模板数据，不会从玩家背包扣除真实物品；需要服务端保存的筛选器，应发送自己的设置请求并由服务端验证。

源码：[IGuiHandlerRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IGuiHandlerRegistration.java)、[IGuiContainerHandler](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/gui/handlers/IGuiContainerHandler.java)、[IGhostIngredientHandler](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/gui/handlers/IGhostIngredientHandler.java)。

## 配方窗口扩展（alpha-2 起）

配方专属标题、原生左右面板、原料查询、每页数量限制及直接鼠标捕获见 [配方窗口扩展 API](Recipe-Screen-Extensions.md)，包含完整 Java 示例。这些 API 从 `0.0.1-alpha-2` 起提供；`0.0.1-alpha-1` 不包含它们。
