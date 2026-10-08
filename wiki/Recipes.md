[English](Recipes-en.md) | [简体中文](Recipes.md)

# 配方 API

## 选择接入方式

普通原版合成、熔炼、烟熏、切石等配方优先使用 Minecraft 自身的配方系统及客户端显示数据。JIV 已有相应分类，不需要为每条标准配方重新创建分类或重复注册。

自定义机器、特殊加工或独立的显示数据适合创建自己的 `IRecipeType<T>` 和 `IRecipeCategory<T>`。如果仍属于原版合成、锻造或酿造，但显示规则特殊，可使用[原版分类扩展](Advanced.md)。

## 定义配方类型和数据

```java
public record DemoRecipe(Identifier id, ItemStack input, ItemStack output) {
    public static final IRecipeType<DemoRecipe> TYPE =
        IRecipeType.create("examplemod", "demo_processing", DemoRecipe.class);
}
```

`IRecipeType<T>` 标识 JIV 分类，不是 `net.minecraft.world.item.crafting.RecipeType<T>`。其中的 UID 必须唯一，`T` 必须与分类和注册的数据一致。统一保存一个类型常量，供分类、注册、工作站和转移使用。

如果已经有 Minecraft `RecipeType<R>`，`IRecipeType.create(vanillaRecipeType)` 会创建 `IRecipeHolderType<R>`，此时分类和注册处理的是 `RecipeHolder<R>`。创建这个类型本身不会自动注册分类或收集配方。

当前 Minecraft 的客户端配方显示通过 `SlotDisplay` 和上下文解析。接入真实数据时，明确数据来自客户端配方显示还是自己的服务端同步；不要假定客户端一定能读取所有服务端配方对象。快速开始中的固定数据仅用于展示 API 的连接方式。

## 分类与槽位

继承 `AbstractRecipeCategory<T>` 可以一次设置配方类型、标题、图标、宽度和高度，然后实现 `setRecipe`：

```java
@Override
public void setRecipe(IRecipeLayoutBuilder builder, DemoRecipe recipe,
                      IFocusGroup focuses) {
    builder.addInputSlot(8, 10)
        .setStandardSlotBackground()
        .add(recipe.input());
    builder.addOutputSlot(76, 10)
        .setStandardSlotBackground()
        .add(recipe.output());
}
```

坐标相对于当前配方布局左上角。布局宽高应容纳原料、槽位背景以及额外控件。`setRecipe` 同时用于建立查询索引，所以不要只在 `draw` 中绘制输入和输出。

| `RecipeIngredientRole` | 含义 |
| --- | --- |
| `INPUT` | 消耗的输入，参与用途查询 |
| `OUTPUT` | 产物，参与配方查询 |
| `CRAFTING_STATION` | 必要但不消耗的物品或工作站 |
| `RENDER_ONLY` | 仅显示，不加入该配方的查询索引 |

槽位支持 `.add(ItemStack)`、`.add(ItemLike)`、`.add(Ingredient)`、`.add(SlotDisplay)`、`.add(ingredientType, ingredient)` 和 `.addItemStacks(list)`。同一个槽位中的多个原料是轮换显示的候选项；需要同时消耗两个物品时，应创建两个输入槽。

如果用原始 `SlotDisplay` 表达输入，直接 `.add(display)` 能让 JIV 使用槽位上下文并保留解释器语义。自行解析时使用 `builder.getContextMap()`，避免用不完整上下文解析。

## 注册分类、配方与工作站

在插件的三个回调中分别调用：

```java
// registerCategories
registration.addRecipeCategories(
    new DemoCategory(registration.getJivHelpers().getGuiHelper()));

// registerRecipes：recipes 是 List<DemoRecipe>
registration.addRecipes(DemoRecipe.TYPE, recipes);

// registerRecipeCatalysts
registration.addCraftingStation(DemoRecipe.TYPE, Items.FURNACE);
```

这里三行 `registration` 的类型不同，必须分别放入对应回调。工作站注册方法的第一个参数是 **配方类型**。演示中的熔炉只是入口；实际机器应替换为自己的方块物品。注册工作站不修改机器逻辑，也不注册配方转移。

## 配方标识和书签

自定义数据分类应返回稳定唯一的配方 ID：

```java
@Override
public Identifier getIdentifier(DemoRecipe recipe) {
    return recipe.id();
}
```

ID 用于配方信息与书签恢复，不应每次加载都随机生成。`RecipeHolder` 默认使用自身 ID；其他数据类型默认返回 `null`。默认 `getCodec` 可按 ID 查找配方，但大型或复杂分类可提供专用 codec，避免低效查找。

## 多候选项的关联

输入候选 `[橡木木板, 云杉木板]` 对应输出 `[橡木楼梯, 云杉楼梯]` 时，对两个槽位调用：

```java
builder.createFocusLink(inputSlot, outputSlot);
```

关联槽位必须有相同数量的候选项，且顺序一一对应。`onDisplayedIngredientsUpdate` 可以计算复杂的显示覆盖，但覆盖原料不会自动进入查询索引；需要查询的真实输入输出仍应在 `setRecipe` 中声明。需要索引但不显示的原料可放入 `addInvisibleIngredients(role)`。

## 文本、动画和工具提示

`createRecipeExtras` 用于创建每个配方布局自己的文本、滚动区域或控件；`draw` 用于附加绘制，当前参数类型是 `GuiGraphicsExtractor`。分类通常由多条配方共享，不要把某一条配方的可变 UI 状态直接存入分类字段。

为槽位补充说明使用 `addRichTooltipCallback`；分类的 `getTooltip` 用于槽位之外的区域。仅需给一个物品添加介绍时，在 `registerRecipes` 中使用：

```java
registration.addIngredientInfo(Items.STONE,
    Component.translatable("examplemod.jiv.info.stone"));
```

源码：[IRecipeCategory](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/recipe/category/IRecipeCategory.java)、[IRecipeLayoutBuilder](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/gui/builder/IRecipeLayoutBuilder.java)、[IRecipeRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IRecipeRegistration.java)。

## 每页数量限制与屏幕扩展（alpha-2 起）

覆写 `getMaxRecipesPerPage()` 限制每页条数，例如三维预览返回 `1`；小于一的值按一处理，默认不限。通过 `createRecipeExtras` 中的 `IRecipeExtrasBuilder.addScreenExtension()` 注册 `IRecipeScreenExtension`，接入配方标题、左右面板及直接鼠标操作。坐标、捕获和原生绘制约定见 [配方窗口扩展 API](Recipe-Screen-Extensions.md)。分类尺寸变化会在下一次布局更新时使缓存失效。这些新增方法从 `0.0.1-alpha-2` 起提供，alpha-1 不包含它们。

## 槽内流体数量文字（alpha-2 起）

`IRecipeSlotBuilder.setShowFluidAmount(boolean)` 开启或关闭槽右下角的流体数量文字，默认关闭。它与 `setFluidRenderer` 的液面配置和 `showCapacity` 的 Tooltip 设置独立，不需要替换 renderer 或使用自己的数量 overlay。

```java
builder.addInputSlot(20, 40)
    .add(Fluids.WATER, 10000)
    .setFluidRenderer(16000, true, 16, 32)
    .setShowFluidAmount(true);
```

这里显示 `10K`，液面按 10,000 / 16,000 渲染，Tooltip 可显示数量和容量。也可以仅对默认的 16×16 流体 renderer 调用 `setShowFluidAmount(true)`，或通过 `false` 再次关闭；调用先后不影响结果。

- 文字读取当前显示的流体，跟随候选轮换和 `createDisplayOverrides()`，不是绑定注册时的固定数量。
- 数量单位为 NeoForge 的 mB，槽内不附加单位：小于 10,000 显示原值；更大值使用截断整数 `K/M/G/T/P/E`，精确值仍可查看 Tooltip。
- 默认在槽右下角绘制白色阴影文字，按槽宽高缩小以容纳；支持自定义尺寸。
- 空槽、非流体及非正数量不绘制；物品堆叠数量保持原有行为。
- 自定义 `setOverlay(...)` 不会被覆盖；绘制顺序为原料、自定义 overlay、流体数量文字、无消耗/概率标记、候选徽标与悬停前景。
- 此显示选项不影响查询索引、原料消耗或服务端数量。

该方法已包含在 Maven Central 的 `0.0.1-alpha-2` 中，编译与运行均需依赖 alpha-2 或更新版本。

## 无消耗与概率标记（alpha-2 起）

JIV 内置槽位标记，物品与流体的输入、输出均可使用；也适用于其他原料类型。标记默认关闭，通过 `IRecipeSlotBuilder` 开启，无需模组提供贴图、overlay 或 Tooltip 回调。

| 方法 | 行为 |
| --- | --- |
| `setNonConsumed(true)` | 槽内左上角绘制绿色 `∞`，Tooltip 添加“不消耗”；`false` 关闭 |
| `setChance(0.25)` | 保存 0–1 范围的概率并开启显示，示例为 25% |
| `setShowChance(false)` | 隐藏概率文字与 Tooltip，保留已设置的值；`true` 重新显示 |

```java
// 物品输入：无消耗。
builder.addInputSlot(20, 20).add(new ItemStack(Items.STONE))
    .setStandardSlotBackground().setNonConsumed(true);
// 物品输出：25% 概率。
builder.addOutputSlot(60, 20).add(new ItemStack(Items.COBBLESTONE))
    .setStandardSlotBackground().setChance(0.25);
// 流体输入：同样支持无消耗/概率/数量组合。
builder.addInputSlot(20, 50).add(Fluids.WATER, 1000)
    .setStandardSlotBackground().setNonConsumed(true)
    .setChance(0.5).setShowFluidAmount(true);
// 流体输出：概率与默认流体 renderer 一起使用。
builder.addOutputSlot(60, 50).add(Fluids.LAVA, 1000)
    .setStandardSlotBackground().setChance(0.1).setShowFluidAmount(true);
```

- `setChance` 参数是概率小数：`0.25` 表示 25%，不是 `25`；`0` 与 `1` 合法，负数、超过 1、NaN 和 Infinity 抛出 `IllegalArgumentException`。EndlessTech 的万分制数值需除以 `10000.0`。
- 概率文字绘制在槽位内部右上角，右对齐，无需在槽位外预留空间。文字按宽度缩小，最多显示两位小数；极小概率显示 `<0.01%`，接近 100% 但不等于 100% 显示 `>99.99%`，避免误报为 0% 或 100%。Tooltip 保留传入值对应的精确十进制百分比。
- `0` 显示 `0%`；`1` 不绘制概率文字，也不显示概率 Tooltip。再次调用 `setChance` 会重新开启显示，即使之前调用过 `setShowChance(false)`，但概率为 `1` 时仍然隐藏。
- 无消耗与概率可同时开启，独立于 renderer、背景、数量文字及自定义 overlay；不覆盖 `setOverlay` 或 `addRichTooltipCallback`。空槽不显示标记及其 Tooltip。
- 标记属于槽位，跟随当前候选原料和显示覆盖；概率不随候选分别变化，需要分类在创建布局时提供槽位对应值。
- 这些 API 只提供展示，不决定物品/流体是否真正消耗、是否随机产出，不修改服务端处理或查询索引。输入槽的概率也使用通用“概率”说明，具体含义由配方定义。

这些方法从 Maven Central 的 `0.0.1-alpha-2` 起提供。EndlessTech 的 `-PjivDev` 仍可用于源码联调，其旧版 Mixin 兼容路径仍保留。
