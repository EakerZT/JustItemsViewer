[English](Ingredients-en.md) | [简体中文](Ingredients.md)

# 原料 API

JIV 的原料（ingredient）不仅指合成输入，也包括侧栏、书签、搜索和配方槽位中的对象。普通物品使用 `VanillaTypes.ITEM_STACK`；NeoForge 流体使用 `NeoForgeTypes.FLUID_STACK`。

## 添加额外物品

对于已经注册的物品类型，想展示创造栏中没有的变体，在 `registerExtraIngredients` 中添加：

```java
@Override
public void registerExtraIngredients(IExtraIngredientRegistration registration) {
    ItemStack namedStone = new ItemStack(Items.STONE);
    namedStone.set(DataComponents.CUSTOM_NAME, Component.literal("演示石头"));
    registration.addExtraItemStacks(List.of(namedStone));
}
```

上面需要导入 `net.minecraft.core.component.DataComponents`。变体是否与默认物品区分，取决于子类型规则；仅增加 ItemStack 不等于定义了新的身份。

## 用数据组件区分子类型

```java
@Override
public void registerItemSubtypes(ISubtypeRegistration registration) {
    registration.registerFromDataComponentTypes(
        Items.STONE, DataComponents.CUSTOM_NAME);
}
```

这是可以编译的演示写法。实际集成应替换为自己的物品和真正决定配方身份的组件，例如材料种类或药液种类。避免将不断变化的能量、计时器等属性无条件加入身份，否则搜索索引可能产生大量无意义变体。

复杂情况使用 `registerSubtypeInterpreter(item, interpreter)`。解释器的 `getSubtypeData(ingredient, context)` 返回具有稳定 `equals` 和 `hashCode` 的值；没有子类型数据时返回 `null`。不要返回可变对象或依赖对象地址的默认身份。

## 搜索别名

```java
@Override
public void registerIngredientAliases(IIngredientAliasRegistration registration) {
    registration.addAlias(Items.STONE, "examplemod.jiv.alias.stone");
    registration.addAlias(new ItemStack(Items.COBBLESTONE), "粗石");
}
```

别名可以是普通字符串或翻译键。`Item` 重载覆盖该物品的全部子类型，`ItemStack` 重载针对特定原料身份。别名是否用于实际搜索受玩家搜索配置控制；不应强行修改玩家设置。

## 流体

配方槽位可直接添加流体及数量：

```java
builder.addInputSlot(8, 10).add(Fluids.WATER, 1000L);
```

`Fluids` 来自 `net.minecraft.world.level.material.Fluids`。NeoForge 当前平台的一桶是 1000 mB；无数量的 `.add(fluid)` 使用一桶。若已有 `FluidStack`，用 `.add(NeoForgeTypes.FLUID_STACK, fluidStack)`。额外流体变体可通过 `addExtraIngredients` 添加到该类型。

流体子类型在 `registerFluidSubtypes` 中注册，通过传入的 `IPlatformFluidHelper<T>` 获取平台类型信息；需要跨平台抽象时，不要把泛型 `T` 直接强制转换为某一加载器的流体类。

## 注册自定义原料类型

能量、气体或其他显示对象可以使用自定义类型。需要准备以下对象：

| 对象 | 责任 |
| --- | --- |
| `IIngredientType<T>` | 原料 Java 类型与稳定 UID |
| `IIngredientHelper<T>` | 显示名、标识、比较、复制、合法性等 |
| `IIngredientRenderer<T>` | 16×16 空间内的绘制与工具提示 |
| `Codec<T>` | 原料保存与恢复，例如书签序列化 |
| `Collection<T>` | 初始显示在原料列表中的对象 |

在 `registerIngredients` 中调用（以下名称是需要自行实现的占位对象）：

```java
registration.register(MY_TYPE, allIngredients, myHelper, myRenderer, myCodec);
```

`IIngredientType<T>` 可以用 `MyIngredient.class` 定义简单类型；默认 UID 为类名。需要跨重构保持存储身份时，覆写 `getUid()`，返回如 `examplemod:gas` 的稳定字符串。所有地方复用同一个类型实例。

helper 的必要入口包括 `getIngredientType`、`getDisplayName`、`getUid`、`getIdentifier`、`copyIngredient` 和 `getErrorInfo`。`getUid` 的结果用于比较和索引，`getIdentifier` 表达资源身份，两者用途不同。renderer 当前的绘制入口使用 `GuiGraphicsExtractor`。codec 应能处理 normalized 原料。

先注册自己的类型，再把它用于配方槽位。对于自己的新类型，初始原料直接传给 `register`；对于已有类型，使用 `registerExtraIngredients`；世界运行中才出现的对象使用[运行时添加](Runtime.md)。

## SlotDisplay 解释器

一个显示解析成 ItemStack 后，可能丢失“接受标签全部成员”或“匹配全部子类型”的语义。可在 `registerSlotDisplayInterpreters` 中，为自己的 `SlotDisplay.Type<D>` 注册 `ISlotDisplayInterpreter<D, T>`；所有原料类型通用的复合显示可用 `registerUniversal`。

解释器用来补足匹配和工具提示语义，不是新原料类型的注册入口。普通显示已经准确时无需注册。详细接口见[ISlotDisplayInterpreterRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/ISlotDisplayInterpreterRegistration.java)。

源码：[IModIngredientRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IModIngredientRegistration.java)、[IIngredientHelper](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/ingredients/IIngredientHelper.java)、[ISubtypeRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/ISubtypeRegistration.java)。
