[English](FAQ-en.md) | [简体中文](FAQ.md)

# 常见问题

## 插件没有加载

检查是否同时使用 `@JivPlugin` 和 `IModPlugin`，类是否公开、可实例化、带公开无参构造器，以及插件是否真正包含在模组 JAR 中。日志中的 `Failed to load` 后面会给出加载失败的类和异常原因。

如果提示缺少类，核对 JIV 和 Minecraft 版本、依赖是否进入开发运行环境，以及插件是否引用了未安装的其他可选模组。

## 分类有了，但找不到配方

检查 `registerRecipes` 是否调用 `addRecipes`，配方类型是否与分类共用同一常量，数据泛型是否匹配，分类 `isHandled` 是否过滤了数据，以及输入输出是否通过 `setRecipe` 添加。

仅在 `draw` 中画一个物品不会为它建立配方索引。`RENDER_ONLY` 也不会加入该配方的输入输出查询。

## 查配方与查用途反了

查询 `OUTPUT` 表示“如何获得此物品”，查询 `INPUT` 表示“此物品用来做什么”。核对槽位角色和 runtime focus 的角色。

## 变体合并、重复显示或匹配不准确

确认为自己的物品注册了真正决定身份的数据组件或 subtype interpreter。额外 ItemStack 的添加、子类型身份与搜索别名是不同功能。

如果候选来自 `SlotDisplay`，尽量保留原始显示；先解析为普通 ItemStack 可能丢失标签和全部子类型匹配语义。

## 搜索别名没有效果

确认翻译键存在、语言正确，玩家已允许别名参与搜索，以及添加别名的对象身份与目标原料一致。给 `Item` 添加别名可覆盖该物品的全部子类型。

## 工作站入口存在，但没有配方填充

工作站注册只建立入口关系。还需注册当前 `Menu` 对应的转移处理器，核对配方类型、菜单类型和槽位索引。标准转移还需要服务端安装 JIV。

## 转移监听器没有收到完成结果

检查自定义 handler 是否在实际转移完成后调用了 `context.completeRecipeTransfer(...)`。`doTransfer=false` 只是检查，不会触发一次实际转移的生命周期。异步请求需等待实际确认，再报告成功或拒绝。

## 退出世界后调用 API 报错

清空旧的 `IJivRuntime` 引用，并在每次回调获得新实例。按钮、异步回调和缓存不要继续使用上一世界的运行时。

## 可以直接使用 JEI 插件吗？

不能直接加载。JIV 的入口注解、包名和当前接口是独立的。移植时重新核对签名、泛型和平台版本，不要只全局替换包名。

| 老教程中可能出现的写法 | 当前 JIV 写法 |
| --- | --- |
| `mezz.jei.api.*` / `@JeiPlugin` | `eakerzt.jiv.api.*` / `@JivPlugin` |
| `IJeiRuntime` / `getJeiHelpers()` | `IJivRuntime` / `getJivHelpers()` |
| `ResourceLocation` | 当前 Minecraft 使用 `Identifier` |
| 旧的具体 JIV `RecipeType` | `IRecipeType<T>`，避免与原版类型混淆 |
| `getBackground()` 返回分类背景 | 提供 `getWidth()` / `getHeight()`，按需绘制背景 |
| `RecipeIngredientRole.CATALYST` | `CRAFTING_STATION` |
| 旧版多参数 `transferRecipe` | `transferRecipe(context, doTransfer)` |
| 旧教程中的绘制类型 | 当前签名使用 `GuiGraphicsExtractor` |

这些是迁移提示，具体代码以当前仓库公共接口为准。

## Maven 依赖无法解析

当前版本 `0.0.1-alpha-2` 已发布到 Maven Central。核对 group ID `io.github.eakerzt`、完整 artifact ID `jiv-26.1.2-neoforge` 和版本号，并确认声明了 `mavenCentral()`。若 Gradle 曾缓存发布前的解析失败，可使用 `--refresh-dependencies` 重新解析；还需确认未启用离线模式且网络能够访问 Maven Central。完整配置见[快速开始](Getting-Started.md)。

## 示例是否包含完整机器实现？

入门示例包含可以接入模组的 JIV 分类、插件和运行时调用，不包含 Minecraft 模组入口、机器菜单、网络同步或服务端加工逻辑。GUI 和转移章节中的 `MachineScreen`、`MachineMenu`、`ModMenus` 等是需要替换的工程类型。
