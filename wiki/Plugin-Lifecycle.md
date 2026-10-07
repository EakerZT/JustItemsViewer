[English](Plugin-Lifecycle-en.md) | [简体中文](Plugin-Lifecycle.md)

# 插件与生命周期

## 插件入口

```java
package example.jiv;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.JivPlugin;
import net.minecraft.resources.Identifier;

@JivPlugin
public final class ExamplePlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("examplemod", "main");
    }
}
```

NeoForge 实现扫描模组文件中的 `@JivPlugin` 注解，随后通过反射调用无参构造器。插件需要实现 `IModPlugin`，不能是接口、抽象类或需要外部参数的类。使用公开顶层类或公开静态嵌套类，并提供公开无参构造器。

`getPluginUid()` 是插件的唯一标识，建议使用自己的 mod ID 作为命名空间。一个模组可以有多个插件，但各插件应使用不同路径。构造器只做轻量初始化；不要在这里读取玩家、世界或 JIV 运行时。

## 注册回调

`IModPlugin` 只有 `getPluginUid()` 必须实现，其余方法有默认实现，只覆写需要的功能。下表是用途索引，不表示全部方法的严格调用顺序。

| 回调 | 用途 |
| --- | --- |
| `configureJiv` | 在收集内容前配置 JIV 功能，例如关闭默认 GUI |
| `registerItemSubtypes` / `registerFluidSubtypes` | 定义如何区分原料变体 |
| `registerIngredients` | 注册新原料类型及其 helper、renderer、codec |
| `registerExtraIngredients` | 向已注册类型添加额外原料 |
| `registerIngredientAliases` / `registerModInfo` | 注册原料和模组搜索别名 |
| `registerSlotDisplayInterpreters` | 保留槽位显示中的标签、子类型等语义 |
| `registerCategories` | 注册配方分类 |
| `registerVanillaCategoryExtensions` | 扩展原版合成、锻造和酿造分类 |
| `registerRecipes` | 向分类添加配方及原料说明 |
| `registerRecipeCatalysts` | 注册工作站入口 |
| `registerRecipeTransferHandlers` | 注册容器配方填充及监听器 |
| `registerGuiHandlers` | 注册点击区、避让区域和拖拽处理 |
| `registerAdvancedSearch` | 替换搜索存储或构建器 |
| `registerAdvanced` | 高级配方查询、装饰器、按钮扩展 |
| `registerRuntime` | 替换运行时组件，面向高级集成 |
| `onRuntimeAvailable` / `onRuntimeUnavailable` | 获取运行时和清理连接级引用 |

可以依赖的关键关系：先注册子类型和原料，再使用原料管理器；先注册分类，再注册配方；全部模组注册完成后才交付运行时。需要工具类时，从当前 registration 的 `getJivHelpers()` 获取，不要提前访问内部全局状态。

## 运行时引用

在 `onRuntimeAvailable(IJivRuntime runtime)` 中保存当前实例，在 `onRuntimeUnavailable()` 中清空。世界或连接改变后，需要使用新交付的实例；不要把第一次获得的运行时当作游戏全程有效的单例。

自己的按钮或快捷键应先检查运行时是否可用，再调用 API。界面操作放在 Minecraft 客户端线程上；从网络回调或后台任务发起操作时，应先调度到客户端线程。完整写法见[示例插件](Examples.md#demojivpluginjava)。

## 可选依赖与专用服务器

JIV 集成涉及 Minecraft 客户端类型。将集成代码放在独立的客户端兼容包，避免普通模组入口、公共静态初始化、服务端菜单或网络数据类直接引用插件类。

当 JIV 是可选依赖时，所有主动调用集成代码的入口都要先确认客户端环境及 `ModList.get().isLoaded("jiv")`。仅在插件内部检查是否安装 JIV 无法避免调用方提前加载该类时缺失 API 的问题。插件自身由 JIV 扫描发现，无需从模组主入口手动创建。

验证时应覆盖：客户端安装 JIV、客户端未安装 JIV，以及专用服务器启动。若模组的功能明确要求 JIV，则按需求声明必需依赖。

源码：[IModPlugin](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/IModPlugin.java)、[ForgePluginFinder](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/neoforge/startup/ForgePluginFinder.java)。
