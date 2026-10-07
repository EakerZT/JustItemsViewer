[English](Home-en.md) | [简体中文](Home-zh-CN.md)

# Just Items Viewer 开发者 Wiki

这份 Wiki 面向希望为模组接入 **Just Items Viewer（JIV）** 的开发者，介绍如何显示自定义配方、扩展原料搜索、接入容器界面，以及调用运行时 API。

文档基于当前仓库：**JIV 0.0.1-alpha-2 / Minecraft 26.1.2 / NeoForge 26.1.2.99 / Java 25**。API 包名为 `eakerzt.jiv.api`，内置配置 API 位于 `eakerzt.jiv.config.api`。源码中的部分 `@since` 标记继承自上游，不能当作 JIV 的发行版本号。

## 从哪里开始

| 目标 | 阅读页面 |
| --- | --- |
| 添加依赖、创建插件并跑通一个配方显示 | [快速开始](Getting-Started.md) |
| 理解插件发现、注册回调与生命周期 | [插件与生命周期](Plugin-Lifecycle.md) |
| 创建配方类型、分类、槽位和工作站入口 | [配方 API](Recipes.md) |
| 添加物品变体、子类型、流体、自定义原料和别名 | [原料 API](Ingredients.md) |
| 添加配方点击区、避让侧栏、接受幽灵原料拖拽 | [界面集成](GUI-Integration.md) |
| 把背包原料放入机器输入槽，监听转移结果 | [配方转移](Recipe-Transfer.md) |
| 打开配方、设置搜索、管理书签和动态内容 | [运行时 API](Runtime.md) |
| 替换搜索索引、扩展原版分类和高级行为 | [高级扩展](Advanced.md) |
| 使用 JIV 内置配置系统 | [配置 API](Configuration.md) |
| 排查插件不加载、配方不显示、版本迁移问题 | [常见问题](FAQ.md) |

## API 的基本模型

一个集成插件实现 `IModPlugin` 并标注 `@JivPlugin`。JIV 发现插件后，将注册对象传入对应的回调方法。注册完成后，通过 `onRuntimeAvailable` 提供 `IJivRuntime`；离开世界时通过 `onRuntimeUnavailable` 通知插件清理引用。

**分类**负责配方布局，**配方数据**描述输入和输出，**原料类型**负责识别、搜索和渲染物品或其他对象。配方转移是额外的容器集成：显示配方本身不会让机器自动获得填充按钮。

优先使用公共 API。`common`、`library`、`gui` 中的实现类及 `@ApiStatus.Internal` 类型不作为这份教程的集成入口；`@ApiStatus.NonExtendable` 接口由 JIV 提供，插件应调用它们，而不是自行实现。

## 文档与源码

JIV **0.0.1-alpha-2** 已发布到 [Maven Central](https://central.sonatype.com/artifact/io.github.eakerzt/jiv-26.1.2-neoforge/0.0.1-alpha-2)，完整依赖配置见[快速开始](Getting-Started.md)。

- [完整入门示例源码](Examples.md)
- [JIV 公共 API 源码](https://github.com/EakerZT/JustItemsViewer/tree/main/src/main/java/eakerzt/jiv/api)
- [内置插件示例](https://github.com/EakerZT/JustItemsViewer/tree/main/src/main/java/eakerzt/jiv/library/plugins)
- [配置 API 源码](https://github.com/EakerZT/JustItemsViewer/tree/main/src/main/java/eakerzt/jiv/config/api)

这些 Markdown 页面同时可在仓库中阅读和维护。发布到 GitHub Wiki 时，将页面文件复制到独立的 Wiki 仓库，并把页面间链接的 `.md` 后缀移除；`_Sidebar.md` 是 Wiki 侧栏文件。完整代码同时收录于[完整示例](Examples.md)页面，便于直接复制使用。
