[English](Recipe-Transfer-en.md) | [简体中文](Recipe-Transfer.md)

# 配方转移

配方转移将玩家已有原料放入容器输入槽。需要同时注册匹配的容器、配方类型和槽位规则；仅注册分类、配方或工作站不会自动建立转移支持。

## 标准连续槽位

在 `registerRecipeTransferHandlers` 回调中调用：

```java
registration.addRecipeTransferHandler(
    MachineMenu.class,
    ModMenus.MACHINE.get(),
    DemoRecipe.TYPE,
    0, 2,
    3, 36
);
```

`MachineMenu` 和 `ModMenus` 是自己的模组类型。此例假设菜单槽位排列如下：

| 菜单槽位索引 | 用途 |
| --- | --- |
| `0..1` | 两个配方输入槽 |
| `2` | 输出槽，不参与输入转移 |
| `3..38` | 36 个玩家背包和快捷栏槽位 |

参数依次为：容器类、可选 `MenuType`、JIV 配方类型、输入起始索引、输入数量、可用库存起始索引、库存数量。

这些索引对应 **`AbstractContainerMenu.slots` 中的顺序**，不是某个物品存储对象内部的局部索引，也不是 GUI 坐标。先核对菜单的 `addSlot` 顺序，排除输出槽、升级槽和无关槽位。`MenuType` 可以传 `null`，但有可用类型时建议提供以限定匹配。

## 非连续槽位

输入槽或库存分散时，实现 `IRecipeTransferInfo<C, R>`，然后使用：

```java
registration.addRecipeTransferHandler(myTransferInfo);
```

它让插件明确列出菜单的输入槽和可用库存槽，继续使用 JIV 的标准转移逻辑。虚拟物品、特殊模板或完全不同的储存系统可以实现 `IRecipeTransferHandler<C, R>`。

## 自定义处理器的当前签名

```java
IRecipeTransferError transferRecipe(
    IRecipeTransferContext<R, C> context,
    boolean doTransfer
);
```

处理器还需提供 `getContainerClass()`、`getMenuType()` 和 `getRecipeType()`，注册方式为 `addRecipeTransferHandler(handler, recipeType)`。

`context` 提供配方、容器、界面、玩家、当前显示槽位、转移 ID，以及 `isMaxTransfer()`。

- `doTransfer=false`：仅检查是否可转移；不得修改物品或发送实际转移请求。
- 检查失败：返回 `IRecipeTransferError`，可用 `registration.getTransferHelper()` 创建错误信息。
- `doTransfer=true`：执行转移请求。完成后调用 `context.completeRecipeTransfer(RecipeTransferResult.SUCCESS)` 或 `REJECTED`。
- 返回 `null` 表示本次调用没有转移错误；异步发送成功不代表服务端已完成转移，应在确认结果后报告完成。

不要在检查阶段报告完成，也不要对同一次实际请求重复报告结果。支持多个分类的模板系统可使用 `IUniversalRecipeTransferHandler<C>`。

## 监听实际转移

```java
@Override
public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferListener(new IRecipeTransferListener() {
        @Override
        public void beforeRecipeTransfer(IRecipeTransferContext<?, ?> context) {
            // 记录 context.getTransferId() 等请求信息。
        }

        @Override
        public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context,
                                        RecipeTransferResult result) {
            // 根据 SUCCESS 或 REJECTED 更新反馈。
        }
    });
}
```

`beforeRecipeTransfer` 在实际调用选定处理器前通知，不用于每次可转移性检查。`afterRecipeTransfer` 在处理器报告完成后通知，使用同一个 context；不要用它代替真正的转移实现。

## 服务端支持与验证

JIV 的标准物品转移需要服务端安装 JIV。自定义处理器如果使用自己的网络协议，则由自己的服务端实现保证一致性：验证当前菜单、槽位、物品数量和放入规则，再更新物品。

验证时至少覆盖：原料充足、原料缺失、输入被占用、输出槽不被改写、最大转移，以及服务端拒绝请求。检查模式不能产生库存变化。

源码：[IRecipeTransferRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IRecipeTransferRegistration.java)、[IRecipeTransferHandler](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/recipe/transfer/IRecipeTransferHandler.java)、[IRecipeTransferContext](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/recipe/transfer/IRecipeTransferContext.java)。
