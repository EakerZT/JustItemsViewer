[English](Configuration-en.md) | [简体中文](Configuration.md)

# 配置 API

JIV 内置配置系统提供 `eakerzt.jiv.config.api.Configs` 入口。使用本项目 JAR 时，无需另外安装 MezzConfig 或 MezzConfigGUI；部分源码注释中的这些名称来自内置组件的上游。

配置注册独立于 `IModPlugin`：在自己的模组初始化阶段创建 schema，而不是每次进入世界时重复注册。如果 JIV 是可选依赖，配置初始化也需要隔离到确认 JIV 已加载的代码路径。

## 创建配置

完整示例见[DemoConfig.java](Examples.md#democonfigjava)。在模组初始化时调用一次 `new DemoConfig()` 并保存该对象：

```java
var builder = Configs.forMod("examplemod")
    .createClientSchemaBuilder("client.ini", "examplemod.config.client");
var category = builder.addCategory("display");

IConfigValue<Boolean> enabled = category.addBoolean("enabled", true).build();
IConfigValue<Integer> rows = category.addInteger("rows", 8, 1, 16).build();

IConfigSchema schema = builder.build();
```

顺序为：获取 registration → 创建 schema builder → 添加分类 → build 每个值 → build schema。每个值 builder 只能 build 一次，同一配置文件不能重复注册。

## 选择配置作用域

| 方法 | 用途 |
| --- | --- |
| `createClientSchemaBuilder` | 所有世界共用的客户端偏好 |
| `createClientPerWorldSchemaBuilder` | 按单人世界或多人服务器区分的客户端偏好 |
| `createServerSchemaBuilder` | 世界拥有、同步到客户端的服务端配置 |

字符串文件名相对于对应 mod 的配置目录。`createClientSchemaBuilder(Path, localizationPath)` 重载接受完整文件路径，不会再追加 mod ID 或文件名；通常优先使用约定目录。

客户端 schema 在专用服务器上处于非活动默认状态；按世界配置仅在对应世界或连接活动时可用。远程服务端配置的值可能已经生效，但 `getPath()` 为空，因为文件属于服务端。可用 `schema.isActive()` 判断是否有当前上下文，不要只按文件路径判断。

## 读取与修改

```java
boolean showPanel = enabled.get();
rows.set(10);
```

`get()` 返回当前生效值，`set()` 验证并保存新值。越界值会抛出 `IllegalArgumentException`；缺少活动本地文件或试图直接修改远程服务端配置时，会抛出 `IllegalStateException`。

有关联的值用批量更新：

```java
schema.batchUpdate(batch -> {
    batch.set(enabled, true);
    batch.set(rows, 10);
});
```

批量中的值必须属于同一 schema；任一项非法或回调抛出异常时，不应用整个批次。

## 生效时机与监听

值 builder 的 `setRestartRequirement(...)` 控制何时生效。需要重启的设置编辑后，`get()` 仍返回当前值，`getEditorInfo().getPendingValue()` 返回已保存但等待生效的值。

`value.addListener(listener)` 观察生效值变化，返回用于移除监听器的 `Runnable`；`schema.addBatchListener(listener)` 观察完整批次。监听器同步执行，捕获界面或连接对象时，应在该对象结束生命周期时调用移除回调。监听器中操作客户端 GUI 时，也应遵循客户端线程约束。

## 编辑界面与高级配置

schema build 后会注册到配置系统，可通过 `Configs.getSchemas()` 发现。`eakerzt.jiv.config.gui.api` 提供独立的配置 GUI 插件与界面工厂入口，使用配置 GUI 插件时应遵循它自己的接口，不能把 `@JivPlugin` 当作配置 GUI 注解。

已有存储结构变化时使用 `setLegacySources`、`addLegacyName`、`addLegacyValue` 或迁移器；迁移用于目标尚不存在时的导入，不是每次读取时执行。自定义序列化值必须具有稳定相等性并保持有效不可变。

源码：[Configs](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/config/api/Configs.java)、[IConfigRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/config/api/IConfigRegistration.java)、[IConfigSchema](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/config/api/schema/IConfigSchema.java)。
