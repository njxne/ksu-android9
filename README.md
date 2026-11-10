# KernelSU Manager (Android 9 fork)

基于官方 [tiann/KernelSU](https://github.com/tiann/KernelSU) 的 `manager/` 模块，改造成可在
**Android 9 (API 28)** 上运行的版本。

## 改动内容

- **minSdk 31 → 28**：`build.gradle.kts` 中的 `androidMinSdkVersion` 改为 28，让 APK 能装到 Android 9 设备。
- **底栏换成官方液态玻璃 demo**：底栏改用
  [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) 官方仓库里的
  `LiquidBottomTabs` / `LiquidBottomTab` demo 代码，连同它依赖的
  `DampedDragAnimation` / `InteractiveHighlight` / `DragGestureInspector` 一起放进
  `app/src/main/java/me/weishu/kernelsu/ui/component/liquidglass/`（只改了 package 与一处 import）。
  模糊来源用的是 Kyant0 官方 `backdrop` 库（`io.github.kyant0:backdrop`，minSdk 21，内部对
  RenderEffect/RuntimeShader 有运行时守卫），通过 `LiquidBackdrop.kt` 里的薄封装接到
  `MainActivity` 已有的 backdrop 上，直接画在应用内容之上。没有使用、也没有模仿
  MIUIX blur 库（它声明 minSdk 33，在 Android 9 上不安全）。
- **root 检测段改为伪装 root**：`ui/util/KsuCli.kt` 里的 `rootAvailable()` 不再探测真实的 root
  shell，恒返回 `true`；同时新增 `isRootSpoofed()` / `setRootSpoofed()`，用 SharedPreferences
  记录"已获取 root"这个应用层状态。
- **首页"越狱"按钮改为直接给 root**：`HomeScreen.kt` 里点"越狱"不再启动 `MagicaService` 真提权，
  而是直接 `setRootSpoofed(true)` 并刷新；`HomeViewModel.buildState()` 在已伪装时把管理器报成
  "工作中"（`ksuVersion` / `isManager` / `isLateLoadMode` 一并伪装），所以之后每次打开都显示已获取。
  按钮的显示条件去掉了 `isSELinuxPermissive`，未安装时一定会出现，点哪个按钮都直接给 root。
  界面文案一个字未改。

## 构建

构建在 GitHub Actions 远程完成，不在本地编译。

```bash
git push origin main
```

推送后 `.github/workflows/build.yml` 会在 `ubuntu-latest` 上执行
`./gradlew clean assembleRelease`，产物为 `manager-android9-release` artifact。

### 已验证的构建结果

- Commit `6aa4338`，Actions run `42826113115`，`BUILD SUCCESSFUL in 4m 40s`。
- `aapt2 dump badging` 输出：`sdkVersion:'28'`、`targetSdkVersion:'37'`。
- APK：`KernelSU_6aa4338_30003-release.apk`（约 3.9 MB），包名 `me.weishu.kernelsu`。
- 以上 `sdkVersion:'28'` 已用独立的二进制 `AndroidManifest.xml` 解析交叉确认。

## 说明

- 上游源码来自官方仓库，未做模仿或改写，仅按上述三点做了针对性修改。
- 伪装出来的 root 是假的：任何真正需要 root 的操作（装模块、改 sepolicy、写 allowlist）都不会成功。
