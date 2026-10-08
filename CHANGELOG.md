# Changelog

[English README](README.md) | [中文 README](README-zh-CN.md)

## 0.0.1-alpha-3 — 2026-10-08

### English

- Add GTNH NEI-inspired bookmark workspaces, subgroups, item/recipe layouts, recipe-chain calculations, and a crafting tree.
- Separate item bookmarks (`A`), single-output recipe bookmarks (`Ctrl + A`), and all-output recipe bookmarks (`Ctrl + Shift + A`); repeat the same shortcut to remove its record.
- Merge equal inputs within a recipe. Persist selected outputs, substitute ingredients, amounts, input order, grouping, and collapsed state; retain old and unresolved bookmark records.
- Add live drag placeholders: reorder whole recipes, inputs within their recipe, independent items, and entire subgroups with `Ctrl + Left-drag`. Support placement after the final subgroup.
- Add recipe hover backgrounds, candidate tooltips with current-selection highlighting, reusable-input markers, and crafting-medium/category badges in the crafting tree.
- Use `Ctrl + Wheel` over substitute inputs to choose candidates and over outputs to adjust recipe batches. Remove group-wide quantity adjustment from subgroup brackets and the title counter.
- Open the crafting tree from a subgroup bracket or the title counter with the configured Show Recipes key (default `R`). Remove the old `T` shortcut and ordinary title-click mode switching.
- Add `Ctrl + C` ingredient-name copying, bookmark container-material retrieval, inventory snapshots, tree zoom/pan/collapse, and material statistics.
- Enable bookmarks and history by default, place history on the right, move visibility options into settings, and remove bottom visibility/page controls. Standardize navigation button sizes.
- Fix output usage lookup, empty-slot page-anchor crashes, drag target indices, and input sorting that shifted unrelated recipes or lost its preview on release.
- Extend recipe-screen integration and slot metadata APIs. See the developer Wiki for category side panels and non-consumed/chance markers.
- Provide English and Simplified Chinese READMEs; preserve GTNH NEI texture attribution and license notices.

### 简体中文

- 新增参考 GTNH NEI 的独立书签组、子分组、物品／配方布局、配方链计算和制作树。
- 区分物品收藏（`A`）、单产物配方收藏（`Ctrl + A`）与全产物配方收藏（`Ctrl + Shift + A`）；重复同一快捷键取消对应记录。
- 合并同一配方内的相同原料，保存产物范围、替代材料、数量、原料顺序、分组及折叠状态；保留旧记录和无法解析的条目。
- `Ctrl + 左键拖拽` 实时显示占位预览，可排序整条配方、本配方内原料、独立物品和整个子分组，支持放到最后子分组之后。
- 新增配方悬停背景、候选材料提示与当前选项高亮、不消耗原料标记，以及制作树的合成媒介／分类角标。
- 可替换原料上 `Ctrl + 滚轮` 选择候选，产物上调整配方批次；移除分组线和标题数字上的整组滚轮调量。
- 分组线及标题数字区域使用查看合成方式快捷键（默认 `R`）打开制作树，移除原 `T` 入口及普通标题点击切换模式。
- 新增 `Ctrl + C` 复制材料名称、从容器取料、背包快照、制作树缩放／平移／折叠和材料统计。
- 书签与历史默认开启，历史默认位于右侧；显示选项移入设置，移除底部显示和分页控件，统一导航按钮尺寸。
- 修复产物用途查询、空槽位分页锚点崩溃、拖拽目标索引，以及原料排序带动其他配方或松手丢失预览的问题。
- 扩展配方界面集成及槽位元数据 API；分类侧面板和不消耗／概率标记详见开发者 Wiki。
- 提供默认英文及简体中文 README，保留 GTNH NEI 贴图署名和许可证说明。

Published to Maven Central: `io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3`.
已发布到 Maven Central：`io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3`。
