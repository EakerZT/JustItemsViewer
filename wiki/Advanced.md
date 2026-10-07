[English](Advanced-en.md) | [简体中文](Advanced.md)

# 高级扩展

常规接入通常只需要分类、原料和 GUI 注册。以下扩展适用于特殊查询、复杂显示或替换 JIV 默认行为的集成。

## 扩展原版分类

在 `registerVanillaCategoryExtensions` 中取得对应分类：

| 注册对象方法 | 扩展接口 |
| --- | --- |
| `getCraftingCategory()` | `ICraftingCategoryExtension<T extends CraftingRecipe>` |
| `getSmithingCategory()` | `ISmithingCategoryExtension<T extends SmithingRecipe>` |
| `getBrewingCategory()` | `IBrewingCategoryExtension<T>` |

通过对应分类接口注册自己配方类的扩展，并实现布局和行为。具体注册重载取决于分类，见[原版扩展接口目录](https://github.com/EakerZT/JustItemsViewer/tree/main/src/main/java/eakerzt/jiv/api/recipe/category/extensions/vanilla)。普通合成配方无需专门扩展；只有默认显示无法描述的行为才需要接入。

## 替换搜索索引

`registerAdvancedSearch(IAdvancedSearchRegistration registration)` 可替换索引存储，适合拼音、转写或自定义匹配规则。

| 重载 | 适用情况 |
| --- | --- |
| `replaceSearchStorage(ISearchStorageFactory)` | 直接创建可用的动态存储 |
| `replaceSearchStorage(ISearchStorageBuilderFactory)` | 先收集初始数据，再预处理或烘焙索引 |

工厂每次调用都必须返回新的空存储或空构建器。构建器是一次性的；`build()` 返回的存储仍要支持运行时 `put`。接口使用泛型工厂，具体实现应按[ISearchStorageFactory](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/search/ISearchStorageFactory.java)或[ISearchStorageBuilderFactory](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/search/ISearchStorageBuilderFactory.java)的方法签名编写。

`ISearchStorage<T>` 的职责：

- `put(key, value)`：添加可搜索文本及对应值。
- `getSearchResults(token, consumer)`：将匹配结果交付 consumer。
- `getAllElements(consumer)`：交付当前存储的所有值。
- `statistics()`：返回用于日志的统计信息。

替换会影响全部索引存储，包括 limited string storage 的底层索引。如果多个插件替换搜索，最后一次替换生效，因此应考虑与其他搜索插件的组合行为。

希望保留默认匹配并添加额外逻辑时，使用 `getDefaultSearchStorageBuilderFactory()` 包装默认构建器；对插入文本与查询 token 使用一致的归一化规则。

## 配方管理器插件、装饰器和按钮

`registerAdvanced` 提供高级配方管理器插件、分类装饰器及配方按钮工厂的注册入口。可用于动态推导查询、在已有分类附加显示信息，或在配方旁提供额外操作。

优先通过公开接口扩展，不要直接访问内部配方索引。分类装饰器用于已有布局的附加展示，不能替代正确的输入输出注册。

接口入口：[IAdvancedRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IAdvancedRegistration.java)。

## 功能与运行时替换

`configureJiv(IJivFeatures features)` 在收集原料、配方和 GUI 注册前调用。例如：

```java
@Override
public void configureJiv(IJivFeatures features) {
    features.disableJivGui();
}
```

这会影响整体 JIV GUI，适合提供替代界面的集成，不适合普通机器插件。`registerRuntime` 则用于替换运行时组件；需要确认替代实现满足其他插件通过 `IJivRuntime` 调用的契约。

源码：[IAdvancedSearchRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IAdvancedSearchRegistration.java)、[IJivFeatures](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/runtime/IJivFeatures.java)、[IRuntimeRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IRuntimeRegistration.java)。
