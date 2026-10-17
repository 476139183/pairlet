# 2026-10-17 GitHub issue 核查与处理

本次从 GitHub API 读取全部 16 个 open issue 及评论，核对本地代码、远端 `main`（均为 `c842a0003f8ab56270885c36f96a5da753deedf8`）与 `v2.5.0`。以下是本次核查结果；历史评论中的承诺不作为新任务授权。

本次继续核验 #403、#404、#406、#409。JDK 17 已安装并通过官方 SHA-256 校验，Gradle 9.4.1 已启动；daemon、desktop 与 iOS 模拟器回归通过。本机 daemon 已更新并通过单实例与 relay 连接校验，尚未对外发布或修改 GitHub issue 状态。用户明确将 #406 的 Windows 真机验收交给反馈用户，复测前保持 open。

## 全量分诊

| Issue | 核查结论 | 本次处理／下一步 |
| --- | --- | --- |
| [#409 DSH 会话无法读取](https://github.com/heypandax/pairlet/issues/409) | 明确的格式兼容缺口：读取器只接受 v0–v3，反馈的 DSH Desktop 0.2.0-rc.2 写 v4。v4 工具结果的消息结构也有变化。 | v4 读取、工具结果与文件变更解析回归通过，本机 daemon 已更新；Windows 目标环境仍需安装新版验证。保持 open。 |
| [#408 Codex dot](https://github.com/heypandax/pairlet/issues/408) | 命中 [D7](../DECLINED-REQUIREMENTS.md)：现有只读观察面向本机会话，历史调查没有取得云端 Dot 对话。新反馈本身没有提供可用接口证据。 | 保持暂缓；后续先核实官方接口或主动上报可行性，不把本机 Codex 会话观察宣传成 Dot 支持。 |
| [#407 新 Codex 模型不可选](https://github.com/heypandax/pairlet/issues/407) | 动态目录改造 `e5051ff3` 已包含在 `v2.5.0`，不是简单漏更新静态型号表。现有代码按 CLI 的执行 ID 展示，支持刷新、分页和失败回退。 | 根因未确认。需用户实际运行 daemon 版本、模型菜单来源／错误提示，以及同账号同项目的 CLI `model/list` 是否包含目标执行 ID。不要凭显示名称猜 ID 或硬编码追加。 |
| [#406 Windows 模型菜单与窗口](https://github.com/heypandax/pairlet/issues/406) | `ModelPopover` 的普通 Column 无滚动，长目录不可达；拖动在越过阈值后才记录锚点，丢失第一段位移，标题可拖区域也只有文本行高。尚无反馈者 Windows 环境复现证据。 | 已补菜单限高和滚动、按下时记录拖动锚点、Windows 38dp 标题拖动区域；自动化覆盖后由反馈用户复测，整单保持 open。 |
| [#405 App 内局域网开关](https://github.com/heypandax/pairlet/issues/405) | 已被 [D6](../DECLINED-REQUIREMENTS.md) 后续决定否决；issue 正文仍是旧方案。 | 建议按不计划实施结案，引用 D6。加固后默认开启是另一个需要独立评审的方向，不在本次重启。 |
| [#404 relay 空闲回切直连](https://github.com/heypandax/pairlet/issues/404) | 初版已进入 `v2.5.0`，但本轮真实 WebSocket 测试发现重挂消息误入旧 relay 队列，以及推送短连接与回退 relay 重叠。 | 已调整首批消息与推送登记时序，5 个真实连接场景及 desktop 全套通过。继续 iPhone 验证，保持 open。 |
| [#403 直连尝试总预算](https://github.com/heypandax/pairlet/issues/403) | 已有总预算与拨号前资格预检实现（`db6e8c1d`），与 #404 共用选路方案。 | 保持待验收；重点检查黑洞地址、总预算、冷却和 relay 回退，不能仅据代码存在关闭。 |
| [#402 提问卡遮住正文](https://github.com/heypandax/pairlet/issues/402) | 反馈者在 2026-10-07 明确确认新版本已修复，卡片可收起、正文可看全。 | **具备结案证据，建议关闭为 completed。** 本次未重复编码。 |
| [#400 Codex 压缩后退出](https://github.com/heypandax/pairlet/issues/400) | 原反馈为 daemon 1.3.1；维护者已说明新版走 Codex 压缩接口，并请其升级复测，暂无后续反馈。 | 等用户在新版复测；若失败，取精确 CLI／daemon 版本与当次日志。 |
| [#391 Kimi 后台任务状态](https://github.com/heypandax/pairlet/issues/391) | 评论记录修复已随 2.3.0 发布；尚无反馈者验收。 | 保持等待复测，不重复实现任务记录轮询。 |
| [#390 iOS HTML 不能滚动](https://github.com/heypandax/pairlet/issues/390) | 已改原生 WebView 触摸分发，维护者用五类模拟器样例验证，未取得用户原文件与真机结果。 | 等脱敏 HTML 样例、设备及 iOS 版本；不能把模拟器通过等同用户问题解决。 |
| [#389 通知](https://github.com/heypandax/pairlet/issues/389) | 注册恢复、签名和子任务通知聚合均已有发布记录，最新评论仍等待实际通知验收。 | 依 [排障流程](../observability/FEEDBACK-DIAGNOSTICS.md) 收取目标版本复现时间与脱敏推送诊断，不把注册 ACK 当作已送达。 |
| [#388 DSH 双向接管](https://github.com/heypandax/pairlet/issues/388) | 评论记录 2.3.0 已补 Web 项目会话索引；仍要求目录已是 DSH 项目，Web 重启后才刷新。 | 等两个方向复测；#409 的 v4 读取修复与此相关，但不能代替双向接管验收。 |
| [#386 Windows ZCode](https://github.com/heypandax/pairlet/issues/386) | 最新评论记录 ZCode 3.14 不接受旧 `runtimeModel` 已修复，并在 Windows 3.14.3 验证，随 2.3.0 发布。 | 等原反馈者复测；正文的初始配置缺失猜测不能盖过后续证据。 |
| [#381 下载进度与速度](https://github.com/heypandax/pairlet/issues/381) | 进度原位刷新已有 2.1.2 和 Windows CI 验证；目标用户复测及下载加速仍未闭环。 | 分开验收正在运行的 updater 的进度显示与慢网速度；对象存储供应商未定，不自动部署。 |
| [#342 海外激活](https://github.com/heypandax/pairlet/issues/342) | 最新决定撤回“发到电脑”设计，先积累观测；对应 [D1](../DECLINED-REQUIREMENTS.md)。 | 本次不改首启流程；是否重启实验依新的内部证据判断，公开材料不写用户行为数据。 |

## #409 实现依据与边界

核对了用户指定的 [DSH Desktop 仓库](https://github.com/anywhere-labs/dsh-desktop) 中 `vendor/dsh-runtime/0.2.0-rc.2/` 的运行时包，未执行其代码：

- `deepseek-ai-dsh-session-format-v3-to-v4-0.2.0-rc.2.tgz`：README 的 Header/physical framing、Tool-result representation，以及 `lib/index.js` 的 `liftToolResult` 和原生 v4 校验。SHA-256：`eeebf4628f7932659f760885514d449e43db83e37dbc58db80a7332961ade38a`。
- `deepseek-ai-dsh-session-0.2.0-rc.2.tgz`：`lib/types/types.d.ts` 的消息、工具调用／结果事件。SHA-256：`65634fe86469d64f761fc1f154264dcfc55c14d7f36028e8102d4c620303d285`。

v4 保留人类／助手消息与 `surfaceOp` 范围语义；工具结果改为 `data.message.role = "tool"`，`toolCallId`／`isError` 上移到 message，正文直接位于其 content。新解析器核对 `source.callId` 与 `toolCallId` 一致，再恢复工具输出、错误状态或提问答案。文件变更列表使用独立的严格解析器，同步适配 v4；失败、畸形、身份不匹配的结果不能成为读取项目外文件的授权依据。

读取器支持上限改为 v4，未知 v5 及以后仍拒绝，文件名／头版本冲突仍拒绝，不退回旧副本，不迁移或改写 DSH 文件。新增／扩展的合成回归覆盖 v0–v4 的 JSONL 和 zstd、v4 优先于旧副本、工具成功／失败、问答恢复、身份冲突、v3→v4 游标迁移及未知版本保护；生产文件读取入口覆盖 v4 成功编辑与失败／畸形／身份冲突结果。相关用例与 daemon 全套回归已通过；依赖真实 CLI 的 DSH LiveIT 因环境条件跳过，不能据此声称 Windows DSH Desktop 实机验收完成。

## #406 实现与验证

模型弹层增加 `heightIn(max = 560.dp)`，并在限高之内滚动；父容器更小时服从父约束。保持模型原始 ID、自定义输入与关闭逻辑。新增 UI 回归在 320dp 高容器内放入 60 个网关模型，检查自定义输入可达、末项可滚动点击并关闭菜单。

窗口拖动改为按下时记录全局鼠标与窗口位置，越过拖动阈值的第一段位移也会应用；Windows 标题提供 38dp 高拖动区域，按钮仍在区域外。新增回归检查 1×／2× 密度下单次移动、双击缩放以及关闭按钮点击。

该修复仍不能替代反馈者 Windows 环境验证。用户已明确由反馈者检查鼠标拖动、滚轮、高 DPI、小窗口、最大化／恢复状态。尚未发布，不应要求反馈者在旧版验证新代码。

## #403／#404 补充验证

新增 `RepathWebSocketTest` 使用临时本地端口和真实 TCP、WebSocket、Noise、加密业务帧，覆盖空闲回切并携带历史游标、TCP 可达但密钥确认超时后回落、忙时延后切换、探测失败保留 relay，以及失败后的冷却与 relay 单连接约束。没有替换生产拨号／探测函数，也不启动产品 daemon。

测试实际暴露并修复了两处时序问题：

- 重连时旧 socket 尚未退休，首批 `ClientCaps`／列表／`OpenSession` 就已排入 relay 队列；随后直连成功，这批消息不会自动进入直连。改为旧 socket 退休并选定路径之后，再排入首批消息。
- 直连尝试尚未确认时收到推送 token，会启动控制短连接；直连失败回退又建立业务 relay，测试观测到总计 3 次 relay 建连。推送登记现等本次 `Attached` 后才选择路径；重连、断线与主动断开先关闭登记条件，避免 `NO_ROUTE` 与残留已连接状态导致空转。

上述 5 个真实连接场景，以及推送登记、取消、窗口拖动、模型滚动的 41 项定向回归已通过。推送 token 在测试专用平台对象内注入，避免影响同一测试进程的其他配对。

新增 `DirectEstablishBudgetIosTest` 在 iOS 模拟器中使用真正的 Darwin 网络引擎，覆盖 WebSocket 升级前黑洞的总预算和调用者取消；不将 JVM CIO 引擎结果替代 Darwin 结果。真实 iPhone 的蜂窝资格预检与 Wi-Fi 来回切换仍需单独验收。2026-10-18 已安装本分支新编译的本地 Debug 包，版本标签仍为 2.5.0（build 28）；以本次构建、二进制新鲜度与安装回执区分旧包，不以相同版本标签证明更新。

## 本次验证与安装结果

- `git diff --check`：通过。
- `python3 scripts/check-repository-content.py`：通过，提交前已检查包含本轮新增文件的 Git index。
- JDK 17 与 Android SDK 已安装，Gradle 9.4.1 已通过官方校验并启动。此前无 JDK 的环境问题已解除。
- 首次构建中 JUnit、JediTerm 等依赖下载遇到 TLS 握手中断；从官方源补齐并校验后继续运行。
- daemon：2,784 项中 2,766 项通过、18 项按环境条件跳过，0 失败；包括本轮 DSH v4 与既有 `OutboxRetireTest`。
- desktop 主代码与测试编译通过；连接／推送／窗口／模型菜单定向 41 项全部通过，全套报告 2,597 项、0 失败。未开启可选的真实 daemon Live 测试；该入口自行打印跳过并返回，不等于已完成真实安装包验收。
- iOS 模拟器：207 项、0 失败。额外直接运行 Darwin 两项预算测试通过：400ms 预算的黑洞用例耗时 421ms，取消用例耗时 216ms（包括主动等待 200ms）。原始 Gradle XML 的单项时间均为 0，耗时取直接运行的原生测试输出。
- iPhone 真机 arm64 完整不签名 Debug 构建通过（`xcodebuild ... CODE_SIGNING_ALLOWED=NO build`）。本地缺少 Firebase 配置，已按仓库现有预览构建流程复制占位模板，结构检查通过；该包 Firebase 遥测关闭，不用于验收其云端上报。
- iPhone 签名问题已解除：账号恢复后命令行一度仍报 `No Accounts`，在 Xcode 项目的 Signing & Capabilities 完成自动签名同步，取得包含目标设备和 development 推送权限的专用描述文件。完整签名构建成功，`codesign --verify --deep --strict` 与二进制新鲜度检查通过；新包已安装并启动。没有移除推送权限来绕过签名。
- 已通过 `scripts/update-local-daemon-detached.sh` 更新本机 daemon；会话检查未阻挡，更新后进程数为 1、relay socket 数为 1。
- 尚未对外发版或关闭 issue；Windows 及 iPhone 的待验收项不能由本地自动化结果代替。

本机验证命令（先选择已安装的 JDK 17）：

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :daemon:test :mobile:composeApp:desktopTest
./gradlew :mobile:composeApp:iosSimulatorArm64Test
```

用户已同意在本地测试完成后配合 iPhone Wi-Fi／蜂窝切换；真实 Windows／DSH Desktop 验收及发布另外取证。

本机已通过 `config --direct-connect lan` 与仓库更新脚本开启局域网直连，会话检查未阻挡；8765 监听所有接口，8799 仍只监听回环。更新后进程数为 1、relay socket 数为 1。新包启动后已观察到 relay 握手及随后直连 E2E 建立；用户切网及消息恢复的完整验收仍待回传。

修复提交 `9f5472ae` 与核验记录已推送到 `codex/verify-issues-403-409`，创建草稿 [PR #410](https://github.com/heypandax/pairlet/pull/410)。PR 仅引用四个 issue，不设置自动关单；尚未合并或对外发版。
