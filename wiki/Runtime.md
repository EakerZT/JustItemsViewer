[English](Runtime-en.md) | [简体中文](Runtime.md)

# 运行时 API

`IJivRuntime` 由插件的 `onRuntimeAvailable` 提供。以下片段中的 `runtime` 表示当前可用实例；调用前先检查是否为空，在 `onRuntimeUnavailable` 清空引用。完整生命周期写法见[示例插件](Examples.md#demojivpluginjava)。

## 打开配方或用途

```java
var focus = runtime.getJivHelpers().getFocusFactory().createFocus(
    RecipeIngredientRole.OUTPUT,
    VanillaTypes.ITEM_STACK,
    new ItemStack(Items.STONE)
);
runtime.getRecipesGui().show(focus);
```

`OUTPUT` 查询“如何制作”，`INPUT` 查询“用于哪些配方”。不要将空 ItemStack 用于 focus。只有找到匹配配方时才会打开配方 GUI。

打开整个分类：

```java
runtime.getRecipesGui().showTypes(List.of(DemoRecipe.TYPE));
```

列表必须非空。若只展示指定配方，可调用 `showRecipes(category, recipes, focuses)`。

## 控制搜索

```java
String oldText = runtime.getIngredientFilter().getFilterText();
runtime.getIngredientFilter().setFilterText("@minecraft stone");
```

修改搜索会影响玩家当前输入，通常应由明确的按钮操作触发。仅为了后台查询配方，无需修改搜索框。

## 查询与隐藏配方

```java
var recipes = runtime.getRecipeManager()
    .createRecipeLookup(DemoRecipe.TYPE)
    .limitFocus(List.of(focus))
    .get()
    .toList();
```

查询默认排除隐藏配方；调用 `.includeHidden()` 后可包含隐藏条目。省略 `limitFocus` 可以查询该类型的全部可见配方。

```java
runtime.getRecipeManager().hideRecipes(DemoRecipe.TYPE, recipes);
runtime.getRecipeManager().unhideRecipes(DemoRecipe.TYPE, recipes);
```

隐藏整个分类使用 `hideRecipeCategory(type)`，恢复使用 `unhideRecipeCategory(type)`。隐藏只影响 JIV 显示，不能作为服务器合成权限或配方进度校验。

动态追加显示数据使用 `getRecipeManager().addRecipes(type, recipes)`；类型必须已经有注册分类。重复通知时应由插件避免重复追加。

## 动态原料

```java
runtime.getIngredientManager().addIngredientsAtRuntime(
    VanillaTypes.ITEM_STACK, extraStacks);
runtime.getIngredientManager().removeIngredientsAtRuntime(
    VanillaTypes.ITEM_STACK, removedStacks);
```

这里的列表由自己的客户端同步或其他运行中数据源提供。静态原料优先在注册回调中添加。读取现有物品可用 `getAllItemStacks()`，读取指定类型可用 `getAllIngredients(type)`；返回集合不可修改。

## 物品书签

```java
runtime.getIngredientManager()
    .createTypedIngredient(VanillaTypes.ITEM_STACK, stack.copy(), true)
    .ifPresent(typed -> runtime.getBookmarkManager().add(typed));
```

`createTypedIngredient` 对空或无效原料返回 `Optional.empty()`。`normalize=true` 使用标准化身份。书签管理器还提供 `contains(typed)` 和 `remove(typed)`；`add` / `remove` 的返回值表示集合是否发生变化。

`IBookmarkManager` 管理原料书签；配方书签的保存依赖分类 ID 和 codec，不应把原料书签接口当作配方书签接口。

## 其他运行时入口

| 方法 | 用途 |
| --- | --- |
| `getIngredientListOverlay()` | 查询侧栏显示和鼠标下原料 |
| `getBookmarkOverlay()` | 查询书签侧栏 |
| `getScreenHelper()` | 当前屏幕相关信息 |
| `getKeyMappings()` | 读取玩家绑定的 JIV 操作按键 |
| `getRecipeTransferManager()` | 已注册的配方转移处理器 |
| `getEditModeConfig()` | 玩家编辑模式的原料隐藏配置 |

源码：[IJivRuntime](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/runtime/IJivRuntime.java)、[IRecipeManager](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/recipe/IRecipeManager.java)、[IBookmarkManager](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/runtime/IBookmarkManager.java)。
