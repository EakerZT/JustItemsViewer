# Just Items Viewer Wiki / 开发者文档

**[English documentation](Home-en.md) | [简体中文文档](Home-zh-CN.md)**

Integrate JIV with your NeoForge mod: recipes, ingredients, GUIs, transfer, search, runtime access, and configuration.

为 NeoForge 模组接入 JIV：配方、原料、界面、配方转移、搜索、运行时操作与配置。

**JIV 0.0.1-alpha-3 · Minecraft 26.1.2 · NeoForge 26.1.2.99 · Java 25**

## Maven Central / 获取依赖

JIV is published to [Maven Central](https://central.sonatype.com/artifact/io.github.eakerzt/jiv-26.1.2-neoforge/0.0.1-alpha-3). Add the following dependencies to your NeoForge ModDevGradle project.

JIV 已发布到 Maven Central。在 NeoForge ModDevGradle 工程中添加以下依赖即可获取：

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3")
    runtimeOnly("io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3")
}
```

The mod JAR contains the API and implementation. `compileOnly` supports compilation; `runtimeOnly` loads JIV for development. Do not embed JIV in your mod JAR.

模组 JAR 包含 API 和实现。`compileOnly` 提供编译依赖，`runtimeOnly` 在开发环境加载 JIV；不要将 JIV 打包进自己的模组 JAR。

## Documentation / 文档导航

| Topic / 主题 | English | 简体中文 |
| --- | --- | --- |
| Overview / 总览 | [Overview](Home-en.md) | [总览](Home-zh-CN.md) |
| Setup / 接入 | [Getting started](Getting-Started-en.md) | [快速开始](Getting-Started.md) |
| Plugin callbacks / 插件回调 | [Plugin lifecycle](Plugin-Lifecycle-en.md) | [插件与生命周期](Plugin-Lifecycle.md) |
| Recipe display / 配方显示 | [Recipes](Recipes-en.md) | [配方 API](Recipes.md) |
| Ingredient types / 原料类型 | [Ingredients](Ingredients-en.md) | [原料 API](Ingredients.md) |
| Screens / 界面 | [GUI integration](GUI-Integration-en.md) | [界面集成](GUI-Integration.md) |
| Recipe-screen extensions / 配方窗口扩展（alpha-2 起） | [Screen extensions](Recipe-Screen-Extensions-en.md) | [配方窗口扩展](Recipe-Screen-Extensions.md) |
| Inventory filling / 背包填充 | [Recipe transfer](Recipe-Transfer-en.md) | [配方转移](Recipe-Transfer.md) |
| Runtime operations / 运行时操作 | [Runtime API](Runtime-en.md) | [运行时 API](Runtime.md) |
| Extension hooks / 扩展入口 | [Advanced extensions](Advanced-en.md) | [高级扩展](Advanced.md) |
| Settings / 设置 | [Configuration](Configuration-en.md) | [配置 API](Configuration.md) |
| Troubleshooting / 排错 | [FAQ](FAQ-en.md) | [常见问题](FAQ.md) |
| Complete Java source / 完整源码 | [Complete examples](Examples-en.md) | [完整示例](Examples.md) |
