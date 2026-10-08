[English](Getting-Started-en.md) | [简体中文](Getting-Started.md)

# 快速开始

目标：在自己的 NeoForge 模组中注册一个 JIV 插件，显示一条“圆石 → 石头”的演示配方，并添加工作站入口和物品说明。

本例只演示 **JIV 的显示与查询**。它不会注册 Minecraft 配方、增加机器或改变游戏中的合成规则。实际接入时，应把示例数据替换为你自己的客户端配方数据。

## 1. 添加开发依赖

JIV **0.0.1-alpha-3** 已发布到 [Maven Central](https://central.sonatype.com/artifact/io.github.eakerzt/jiv-26.1.2-neoforge/0.0.1-alpha-3)。正式 Maven 坐标为：

```text
io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3
```

在使用 NeoForge ModDevGradle 的模组工程中添加以下依赖，即可从 Maven Central 获取：

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3")
    runtimeOnly("io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3")
}
```

当前发布的是包含 API 与实现的模组 JAR，没有单独的 `api` classifier。`compileOnly` 让集成代码能够编译，`runtimeOnly` 让开发运行环境加载 JIV；不要把 JIV 的类打包或重定位进自己的模组 JAR。

### 可选：使用本地源码构建

需要调试未发布的修改时，可以在 JIV 仓库中构建：

```powershell
.\gradlew.bat build
```

将 `build/libs/jiv-26.1.2-neoforge-0.0.1-alpha-3.jar` 复制到自己的模组工程 `libs/` 目录，再使用：

```kotlin
dependencies {
    compileOnly(files("libs/jiv-26.1.2-neoforge-0.0.1-alpha-3.jar"))
    runtimeOnly(files("libs/jiv-26.1.2-neoforge-0.0.1-alpha-3.jar"))
}
```

本地构建和模组开发工程均应使用 Java 25、Minecraft 26.1.2 与匹配的 NeoForge。依赖声明不会替代最终玩家环境中的模组安装；根据自己的功能是否依赖 JIV，在模组元数据中声明对应依赖关系。可选集成的客户端隔离见[插件与生命周期](Plugin-Lifecycle.md)。

## 2. 放入完整示例

将以下三个文件放到自己模组的 `src/main/java/example/jiv/` 中。完整文件位于 [完整示例](Examples.md)：

| 文件 | 用途 |
| --- | --- |
| `DemoRecipe.java` | 配方显示数据和统一的 JIV 配方类型 |
| `DemoCategory.java` | 分类标题、图标、尺寸及输入输出槽位 |
| `DemoJivPlugin.java` | 插件入口、注册回调和运行时示例 |

把示例的 `examplemod` 替换为自己的 mod ID，并按工程组织修改包名。插件必须是可实例化的公开类，带可访问的无参构造器。示例使用隐式的公开无参构造器。

核心入口如下：

```java
@JivPlugin
public final class DemoJivPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("examplemod", "jiv_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new DemoCategory(registration.getJivHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(DemoRecipe.TYPE, List.of(
            new DemoRecipe(
                Identifier.fromNamespaceAndPath("examplemod", "demo_stone"),
                new ItemStack(Items.COBBLESTONE),
                new ItemStack(Items.STONE)
            )
        ));
    }
}
```

这段代码展示入口结构；所需的 imports、分类实现以及其他回调都已包含在完整示例文件中。

## 3. 添加翻译

在自己模组的 `assets/examplemod/lang/zh_cn.json` 中合并以下键值：

```json
{
  "examplemod.jiv.category.demo": "演示加工",
  "examplemod.jiv.info.stone": "这是一条 JIV API 演示说明。",
  "examplemod.jiv.alias.stone": "建筑石材"
}
```

同时在 `en_us.json` 中提供对应的默认翻译。

## 4. 验证集成

1. 同时加载自己的模组和 JIV，进入世界并打开背包。
2. 悬停石头按 `R`，确认“演示加工”分类显示圆石输入和石头输出。
3. 悬停圆石按 `U`，确认能找到同一条配方。
4. 查看分类的熔炉工作站入口以及石头的说明页面。
5. 启用原料别名搜索后，搜索“建筑石材”。
6. 退出世界再进入，确认插件能够重新收到运行时，旧引用已清理。

完整示例还提供 `showRecipes`、`showUses`、`setSearch` 和 `bookmark` 方法，供自己的客户端按钮或快捷键调用；它们不会在加载世界时自动弹出界面。

下一步：[配方 API](Recipes.md)介绍如何把这个演示替换为真实机器配方。
