# KernelSU Manager (Android 9 fork)

基于官方 [tiann/KernelSU](https://github.com/tiann/KernelSU) 的 `manager/` 模块，改造成可在
**Android 9 (API 28)** 上运行、并且以**应用层伪装**方式显示"已获取 root"的版本。

> 重要：这个 root 是假的。它只是让管理器界面显示 root 状态，不会授予任何真实权限。
> 任何真正需要 root 的操作（执行模块脚本、改 sepolicy、写 allowlist、装真模块）都不会成功。

## 改动内容

### 1. 支持 Android 9

- `build.gradle.kts` 中 `extra["androidMinSdkVersion"] = 28`（原为 31），`targetSdk` / `compileSdk` 不变。
- 新增 `lint` 检查，用来把 minSdk 28 上不存在的 API 全部找出来。第一轮一共查出 **14 条 `NewApi`
  （severity=Error）**，这些都是在 Android 9 上会直接抛 `NoSuchMethodError` 的地方，逐个加了版本守卫：

  | 位置 | API | 处理 |
  |---|---|---|
  | `ui/MainActivity.kt` | 29 `Window#setNavigationBarContrastEnforced` | 启动期组合里调用，Android 9 上**启动即闪退**，改为 `SDK_INT >= Q` 守卫 |
  | `ui/webui/WebUIActivity.kt` | 29 同上 | 同上 |
  | `ui/util/PinyinUtil.kt` | 29 `android.icu.text.Transliterator` | 类本身是 API 29，改为可空 + 版本守卫，Android 9 上原样返回输入 |
  | `ui/util/DownloadService.kt` | 29 `MediaStore.Downloads#getContentUri` | `SDK_INT >= Q` 用 Downloads 集合，否则用通用 `MediaStore.getContentUri` |
  | `ui/webui/WebViewInterface.kt` | 29 同上 | 同上 |
  | `ui/theme/MiuixTheme.kt` | 31 dynamic color scheme | 只在 `SDK_INT >= S` 时取动态取色，否则该分支返回 null |
  | `ui/theme/ThemeExt.kt` | 31 dynamic color scheme | 同上，Android 9 上退回 Material 3 默认主色 |
  | `ui/component/rebootlistpopup/RebootListPopup.kt` | 30 `PowerManager#isRebootingUserspaceSupported` | 只在 `SDK_INT >= R` 时读取 |
  | `magica/AppZygotePreload.java` | 29 `android.app.ZygotePreload` | 该类只在 API 29+ 由 zygote 加载，Android 9 上不会加载，加 `@SuppressLint("NewApi")` |

### 2. 底栏换成官方液态玻璃 demo

底栏改用 [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) **官方仓库**的
`LiquidBottomTabs` / `LiquidBottomTab` demo 代码，连同它依赖的 `DampingDragAnimation` /
`InteractiveHighlight` / `DragGestureInspector` 一起放进
`app/src/main/java/me/weishu/kernelsu/ui/component/liquidglass/`（只改了 package 与一处 import）。

模糊来源用的是 Kyant0 官方 `backdrop` 库（`io.github.kyant0:backdrop`，minSdk 21，内部对
RenderEffect/RuntimeShader 有运行时守卫），通过自写的 `LiquidBackdrop.kt`
（`rememberLiquidLayerBackdrop` / `Modifier.liquidLayerBackdrop`）接到 `MainActivity` 已有的
backdrop 上，直接画在应用内容之上。**没有使用、也没有模仿** MIUIX blur 库（它声明 minSdk 33，
在 Android 9 上不安全）。

### 3. 应用层伪装 root

- `RootSpoof.kt`（新增）：用应用私有 SharedPreferences（`settings` / `root_spoofed`）记录
  "已获取 root"。默认 `false`。**卸载应用即消失，重装后需要重新越狱**——因为状态只存在应用私有目录。
- `Natives.setRootSpoofedNative()` + `app/src/main/cpp/jni.cc`：伪装下沉到 native 层。spoof 打开后
  `getVersion()` / `getKernelUAPIVersion()` / `isManager()` / `getSuperuserCount()` 都返回伪装值，
  这样上层所有读 native 的地方（首页状态、bottom bar、`KsuIsValid`）拿到的都是"已 root"的结果。
- `KernelSUApplication.onCreate()` 里调 `RootSpoof.syncNative()`：native 标志是进程内的，
  **每次启动都要重新设一次**，所以重启应用后仍然显示已获取 root。
- `ui/util/KsuCli.kt`：`rootAvailable()` 在伪装时返回 `true`；`listModules()` / `toggleModule()` /
  `uninstallModule()` / `undoUninstallModule()` 走新增的 `FakeModuleStore`（`fake_modules.json`）。
  **模块可以装、可以切换、可以卸载，但永远不会被执行**——不管启用还是不启用都是废物。
- `data/repository/SuperUserRepositoryImpl.kt`：伪装时 `getAppList()` 直接返回空列表。
- `ui/MainActivity.kt`：`MainScreen` 收集 `RootSpoof.revision`，点越狱后整个 shell 立即切到
  root 状态，不用等下次导航或重启。同时伪装状态下跳过启动期的 `install()`（那是一条真的 root shell 调用）。
- 越狱按钮的显示条件去掉了 `isSELinuxPermissive` / `isGKI()`，否则 Android 9 上永远显示
  "Unsupported"，根本看不到按钮。**界面文案一个字未改。**

## 构建

构建在 GitHub Actions 远程完成，不在本地编译（本机没有 Android SDK / NDK）。

```bash
git push origin main
```

推送后 `.github/workflows/build.yml` 会执行 `./gradlew clean assembleRelease`，产物为
`manager-android9-release` artifact；另有 `lint` job 跑 `:app:lintDebug` 并上传
`lint-android9` artifact，用来确认 NewApi 已经清零。

### 已验证的构建结果

- 最近一次 `Build` workflow run 为 `completed / success`（build + lint 两个 job 都通过）。
- `aapt2 dump badging`：`package: name='me.weishu.kernelsu'`，`minSdk` 为 28，`targetSdk` 为 37。

## 说明

- 上游源码来自官方仓库，未做模仿或改写，仅按上述几点做了针对性修改。
- 伪装出来的 root 是假的：任何真正需要 root 的操作都不会成功。
