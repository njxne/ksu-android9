# KernelSU Manager (Android 9 fork)

基于官方 [tiann/KernelSU](https://github.com/tiann/KernelSU) 的 `manager/` 模块，改造成可在
**Android 9 (API 28)** 上运行的版本。

## 改动内容

- **minSdk 31 → 28**：`build.gradle.kts` 中的 `androidMinSdkVersion` 改为 28，让 APK 能装到 Android 9 设备。
- **底栏换成官方液态玻璃 demo**：底栏改用
  [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) 官方仓库里的
  `LiquidBottomTabs` / `LiquidBottomTab` demo 代码（`app/src/main/java/me/weishu/kernelsu/ui/component/liquidglass/`），
  并通过 `MiuixBackdrop` 适配层复用应用原有的 backdrop，直接画在应用内容之上。
- **root 检测段改为伪装 root**：`ui/util/KsuCli.kt` 里的 `rootAvailable()` 不再探测真实的 root
  shell，恒返回 `true`。设备实际并没有 root，只是让管理器认为 root 可用。其余界面与代码未改动。

## 构建

构建在 GitHub Actions 远程完成，不在本地编译。

```bash
git push origin main
```

推送后 `.github/workflows/build.yml` 会在 `ubuntu-latest` 上执行
`./gradlew clean assembleRelease`，产物为 `manager-android9-release` artifact。

## 说明

- 上游源码来自官方仓库，未做模仿或改写，仅按上述三点做了针对性修改。
- 伪装出来的 root 是假的：任何真正需要 root 的操作（装模块、改 sepolicy、写 allowlist）都不会成功。
