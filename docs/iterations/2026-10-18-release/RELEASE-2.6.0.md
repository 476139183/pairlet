# 2.6.0 发布与验收记录

状态：[2.6.0 已公开发布](https://github.com/heypandax/pairlet/releases/tag/v2.6.0)，Android、macOS／Windows／Linux 桌面端及五种 daemon 安装包已齐备并设为 latest，Homebrew 与 Scoop 已同步。iOS 2.6.0（71）已提交正式审核（`WAITING_FOR_REVIEW`），审核通过后自动发布；TestFlight 已提交 Beta 审核（`WAITING_FOR_BETA_REVIEW`）。国内镜像仍等待自动同步，尚未确认 2.6.0 已可通过镜像更新。HarmonyOS 不在本次默认范围。

## 本次内容

- 手机端语音输入 v2：默认放入输入框，可选校对后发送，首次开启前独立披露处理方和数据用途；可预览修改，异常或不确定修改保留供手动检查。
- 支持 DeepSeek Harness 0.2.0-rc.2 的 v4 历史，包括消息、工具结果和文件变更；未知 v5 及以上继续拒绝，保留身份和版本冲突检查。
- 修复网络切换重挂时消息遗漏与空闲直连回切重复连接。
- 长模型菜单可滚动；Windows 窗口从按下时记录拖动锚点，补齐标题栏可拖动区域。
- 会话列表过滤 Codex 内部子任务记录；改善 Claude 项目移动后的会话归属，隐藏目录已删除的项目。

## 来源与测试

- [PR #410](https://github.com/heypandax/pairlet/pull/410) 已合并，发布提交为 `a08de3df23281fd8fc99a8f553517c2ebf376efe`。其源码树与已验证候选 `4799a0d0` 相同。
- 本地 `CHECK_IOS=1 bash scripts/check-all.sh --no-daemon` 通过：6,614 项中 6,595 通过、19 条件跳过，0 失败／错误，覆盖 observability、Sentry、protocol、daemon、relay、mobile desktop 和 iOS Simulator。
- [候选 CI](https://github.com/heypandax/pairlet/actions/runs/39057041800) 全部通过，包含桌面编译、Android 构建和 Windows 检查；[主 CI](https://github.com/heypandax/pairlet/actions/runs/39058491887) 也已全部通过，之后才创建不可变标签 `v2.6.0`。
- 版本锁步、Harmony 静态契约、品牌兼容、仓库内容检查通过。Android versionCode 42，iOS 源码 build 29，正式 iOS build 使用工作流 run number。
- App Store 完整素材检查通过：2 个语言、18 个文本字段、24 张截图、2 个预览；官网、帮助和隐私 URL 以浏览器 User-Agent GET 返回 HTTP 200。
- iOS 2.6.0 模拟器 Debug 构建成功，实测首启隐私同意、免配对 Demo、会话内容与工具展示、返回配对页。语音校对默认关闭，独立披露取消后仍关闭。
- 本机 daemon 经官方单实例更新脚本更新到 2.6.0，包内版本回读一致；更新时无其他活会话，完成后进程数为 1、relay 443 连接为 1。

## 验证范围

维护者已完成 iPhone Wi-Fi → 蜂窝 → Wi-Fi 真机验证，#403／#404 已关闭。本机真实 DSH、mock 模型驱动真实 CLI 与生产 v4 读取均已验收，详见 [反馈核验](../../analysis/2026-10-17-github-issues.md)。#406 的 Windows 拖动和滚动、#409 的 Windows DSH Desktop／Android 原反馈场景仍待反馈者复测，保持开放。模拟器和 CI 不能替代目标平台真机安装与交互验收。

## 安装包与更新渠道

- [正式资产工作流](https://github.com/heypandax/pairlet/actions/runs/39059671205) 全部成功。共 28 个公开资产：27 个安装包／固定名称别名及 `SHA256SUMS`；构建时哈希、GitHub 资产 digest 和最终清单逐项一致，无缺项或多余条目。
- 五个 daemon 包均已下载，下载哈希与 GitHub digest 一致；逐包读取 jar 内 `cc-pocket-version.properties`，均为 2.6.0。
- macOS 两种 daemon 包均通过本地 `codesign --verify --deep --strict` 和 `stapler validate`。arm64 的新旧 CLI 命令、首次启动别名和安装归属烟雾检查通过，未额外启动 daemon。四个桌面 DMG 构建任务完成签名、公证与票据校验。
- Android 正式 APK 已下载验证签名、包名 `com.panda.ccpocket`、versionName 2.6.0 和 versionCode 42；签名证书 SHA-256 与 2.5.0 一致，为 `6baa504965d1697c93ce0e63657a75ea136bb3f3c244b155549e9c7b4361bcf3`。证书一致不替代真机覆盖安装验收。
- 更新清单签名沿用未配置状态，`sign-manifest` 明确报告没有 `RELEASE_SIGNING_KEY`；本版不带签名清单。此项与已完成的 macOS 应用签名／公证、Android APK 签名是不同机制。
- [Homebrew tap 更新](https://github.com/heypandax/homebrew-tap/commit/3bdbffcc72eadd93d77989faa60a180aaf1229c6) 已回读，与仓库模板逐字节一致。Scoop bucket 由工作流更新，版本、URL 和 hash 已回读核对，仓库模板同步相同内容。
- Homebrew SHA-256：arm64 `d2cb25b4fb514aab407be12ce3bba5a923325c484b6d89c164a461e07b9836d8`，x86_64 `440feea3df79dde242e4ee948d260eafeebdeebff81aa9741e0a4aefc4d61d71`。
- Windows daemon SHA-256：`2b388f17faa0f69413331151ae7ce71480f843c6c1bfdfa1225b4e863e99d0ad`。
- GitHub `releases/latest` 已回读为 v2.6.0、28 个资产；APK、Pairlet macOS arm64 DMG、Windows MSI、Linux x86_64 deb 和 SHA256SUMS 的 latest 下载链接均返回 HTTP 200。

## iOS 发布进度

- [正式 iOS 流水线](https://github.com/heypandax/pairlet/actions/runs/39059676082) 从同一标签执行，版本 2.6.0、build 71，配置为审核通过自动发布，并分发 TestFlight。
- 首次归档因账号开发证书达到上限而失败。按既有维护流程仅撤销一张最早的 `Created via API` 临时开发证书，[维护任务](https://github.com/heypandax/pairlet/actions/runs/39059919085) 回读撤销成功；本机开发证书、发行证书和 Developer ID 未动。第二次尝试沿用相同源码与 build 71，首次失败没有上传 IPA。
- 第二次归档与发行导出成功；归档／导出包分别通过 APNs development／production 签名及描述文件检查，版本、build 与 bundle ID 一致，Sentry 配置和符号检查通过。2026-10-17 19:26 UTC 上传回执为 `UPLOAD SUCCEEDED with no errors`。
- 发行 IPA SHA-256：`4b75806a3b6eb749685dd8fe5ff68da9945233324e09a3b702259ff12e0ece69`。
- Apple 于 2026-10-17 19:29 UTC 将 build 71 处理为 `VALID`。首次 deliver 因审核备注 4,386 字符超过 4,000 上限失败；IPA 无需重建。修复提交 `d993c7ad` 将备注压缩为 3,431 字符并保留 Demo 与数据披露，补齐非本地化备注校验、4 个边界／缺失测试和 CI 门禁。原无效备注已在本地重现拒绝，修正后的完整素材检查通过。
- 新增 `ios-submit-existing.yml` 复用精确的已有 VALID build，在版本保护及年龄分级检查后提审，不改二进制或发布标签。[恢复提审与 TestFlight 工作流](https://github.com/heypandax/pairlet/actions/runs/39062999902) 全部成功，执行源码为 `d993c7ad42ba1d5c2cd51d0d492a7544f07a8a6b`；IPA 来源仍为发布标签 `a08de3df`。
- 2026-10-17 19:35 UTC 正式提审回读为 `Version 2.6.0: WAITING_FOR_REVIEW; AFTER_APPROVAL`。关联 build 71、中英各 9 个字段和审核备注均与修正后的源码一致；年龄分级实时回读 `Messaging and Chat = Yes`。尚未取得正式审核批准或公开上架证据。
- 同一 build 71 已附加到既有公开 TestFlight 组，Beta 审核提交成功，状态为 `WAITING_FOR_REVIEW`，外测 build 状态为 `WAITING_FOR_BETA_REVIEW`；保留[原公开测试链接](https://testflight.apple.com/join/8z26MWWr)。审核待处理不代表该新 build 已可供外测安装。

## 国内镜像同步状态

GitHub latest 已切到 2.6.0，但截至本次回读，公开 `https://pocket.ark-nexus.cc/dl/latest.json` 仍为 2.5.0。现有同步任务每 30 分钟触发并在包下载与 SHA-256 验证完成后原子切换；上一版五个 daemon 镜像文件的时间戳相隔约 17 分钟，整个拉取约需 85 分钟，故暂不宣称镜像已同步。本次未取得可用的镜像维护连接，沿用既有自动同步。本次未改镜像脚本、未重启 relay；2.6.0 可先从 GitHub Release、Homebrew 或 Scoop 获取。
