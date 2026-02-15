# Android SDK 安装配置指南

## 问题描述
Android Studio 提示需要 SDK，这是因为项目配置需要特定版本的 Android SDK。

## 项目 SDK 需求
根据项目的 `build.gradle` 文件，您的项目需要：

- **Compile SDK Version**: 34
- **Minimum SDK Version**: 21
- **Target SDK Version**: 34
- **Gradle Plugin Version**: 8.2.0

## 安装步骤

### 1. 打开 Android Studio

### 2. 打开 SDK Manager
- 方法 1: 点击顶部工具栏中的 "SDK Manager" 图标
- 方法 2: 从菜单中选择 "File" > "Settings" > "Appearance & Behavior" > "System Settings" > "Android SDK"
- 方法 3: 使用快捷键 `Ctrl+Alt+Shift+S` 打开 Project Structure，然后选择 "SDK Location"

### 3. 安装所需 SDK

在 SDK Manager 中，确保安装以下组件：

#### SDK Platforms 标签页
- 勾选 **Android API 34** (Android 14)
- 可选：如果需要支持其他版本，也可以勾选其他 API 级别

#### SDK Tools 标签页
- 确保勾选 **Android SDK Build-Tools** (版本 34.0.0 或更高)
- 确保勾选 **Android SDK Command-line Tools (latest)**
- 确保勾选 **Android SDK Platform-Tools**
- 确保勾选 **Android SDK Tools** (Obsolete)
- 确保勾选 **Android Emulator** (如果需要使用模拟器)

### 4. 配置 SDK 路径

在 Project Structure 中：
1. 选择 "SDK Location"
2. 确保 "Android SDK location" 指向正确的 SDK 安装目录
3. 点击 "Apply" 保存设置

### 5. 同步项目

- 点击 Android Studio 右上角的 "Sync Project with Gradle Files" 按钮
- 或从菜单中选择 "File" > "Sync Project with Gradle Files"

## 常见问题解决

### 1. SDK 下载失败
- 检查网络连接
- 尝试使用代理服务器
- 手动下载 SDK 包并解压到 SDK 目录

### 2. Gradle 同步失败
- 确保 Gradle 插件版本与 SDK 版本匹配
- 清理项目：选择 "Build" > "Clean Project"
- 重建项目：选择 "Build" > "Rebuild Project"

### 3. 找不到 SDK 路径
- 在 Android Studio 首次启动时，会提示设置 SDK 路径
- 如果已安装 SDK，可以手动指定路径
- 如果未安装 SDK，可以通过 SDK Manager 自动下载

## 验证安装

安装完成后，可以通过以下方式验证：

1. 打开终端或命令提示符
2. 运行以下命令：
   ```
   adb --version
   ```
   如果显示版本信息，说明 SDK 安装成功

3. 运行项目，查看是否还有 SDK 相关的错误

## 其他建议

- 定期更新 SDK 组件，以获取最新的功能和安全修复
- 为不同的项目创建不同的 SDK 配置，以避免版本冲突
- 使用 Android Studio 的 "Recommended SDK" 功能，自动安装推荐的 SDK 版本

## 参考链接

- [Android Developer Documentation](https://developer.android.com/studio/intro/update)
- [Android SDK 安装指南](https://developer.android.com/studio/install)
- [Gradle 插件与 SDK 版本对应关系](https://developer.android.com/studio/releases/gradle-plugin#updating-plugin)

---

如果按照以上步骤操作后仍有问题，请尝试重启 Android Studio 或联系技术支持。