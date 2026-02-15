# AutoClicker - 智能手机自动点击器

## 📱 项目简介

AutoClicker是一款功能强大的Android自动点击器应用，支持：
- ✅ 自动点击屏幕
- ✅ 脚本录制和回放
- ✅ 智能元素识别
- ✅ 悬浮窗控制
- ✅ 多步骤工作流

## 🚀 快速开始

### 使用GitHub Actions构建（推荐）

**最简单的方法：使用GitHub Actions自动构建APK！**

📖 **详细步骤请查看**：[GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md)

### 快速步骤概览

1. **创建GitHub仓库** - 访问 https://github.com/new
2. **上传代码** - 使用Git命令推送代码
3. **触发构建** - 推送后自动触发或手动触发
4. **下载APK** - 在Actions页面下载构建的APK

### 本地构建（可选）

如果需要在本地构建，请参考以下步骤：

#### 环境要求
1. **Java JDK 17**
   - 下载：https://adoptium.net/temurin/releases/?version=17
   - 配置环境变量：`JAVA_HOME`

2. **Android SDK**
   - 下载Android Studio：https://developer.android.com/studio
   - 配置环境变量：`ANDROID_HOME`

#### 构建步骤
```cmd
cd d:\AutoClicker
build-apk.bat
```

## 📋 GitHub Actions构建流程

### 工作流程

1. **Checkout code** - 拉取仓库代码
2. **Set up JDK 17** - 配置Java环境
3. **Setup Gradle** - 配置Gradle构建工具
4. **Grant execute permission** - 给gradlew添加执行权限
5. **Build with Gradle Wrapper** - 构建Release APK
6. **Upload APK artifact** - 上传APK到GitHub

### 触发条件

- **自动触发**：推送代码到main分支
- **手动触发**：在Actions页面点击 "Run workflow"

### 下载APK

1. 进入仓库的 **Actions** 标签页
2. 选择 **Build Android Release APK** 工作流
3. 点击最近的运行记录
4. 滚动到底部，找到 **Artifacts**
5. 点击 **autoclicker-release-apk** 下载

## 📁 项目结构

```
AutoClicker/
├── .github/workflows/         # GitHub Actions配置
│   └── build-release.yml
├── GITHUB_ACTIONS_GUIDE.md    # GitHub Actions详细指南
├── app/                        # 应用代码
│   ├── src/main/
│   │   ├── java/com/example/autoclicker/
│   │   │   ├── adapter/       # 适配器
│   │   │   ├── model/        # 数据模型
│   │   │   ├── service/      # 服务
│   │   │   ├── ui/activity/   # Activity
│   │   │   └── utils/        # 工具类
│   │   ├── res/              # 资源文件
│   │   └── AndroidManifest.xml
│   ├── build.gradle          # 应用级构建配置
│   └── proguard-rules.pro   # 混淆规则
├── gradle/wrapper/           # Gradle Wrapper
│   └── gradle-wrapper.properties
├── build-apk.bat            # 本地构建脚本
├── gradlew.bat              # Windows构建脚本
├── README.md               # 项目文档
├── build.gradle             # 项目级构建配置
├── gradle.properties        # Gradle属性
└── settings.gradle          # Gradle设置
```

## 📖 详细文档

- **GitHub Actions详细指南**：[GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md)
  - 创建GitHub仓库
  - 初始化Git仓库
  - 上传代码到GitHub
  - 手动触发Actions
  - 下载构建的APK
  - 常见问题解答

## ❓ 常见问题

### 1. GitHub Actions构建失败

**错误：Gradle构建失败**
- 检查代码是否有语法错误
- 查看Actions日志中的详细错误信息
- 确保所有依赖项都可以正常下载

**错误：签名配置错误**
- 检查 `app/build.gradle` 中的 `signingConfigs` 配置
- GitHub Actions会自动生成debug.keystore

### 2. 无法下载APK

**错误：Artifacts未显示**
- 确保构建成功完成（绿色勾号）
- 等待几分钟后刷新页面
- 检查是否在正确的工作流运行记录中

### 3. 推送代码失败

**错误：认证失败**
- 检查GitHub用户名和密码
- 使用GitHub Personal Access Token代替密码
- 确保仓库URL正确

## 🔧 高级配置

### 自定义构建触发条件

编辑 `.github/workflows/build-release.yml`，修改触发条件：

```yaml
on:
  push:
    branches: [ main, develop ]  # 多个分支
  pull_request:              # Pull Request时也触发
    branches: [ main ]
  workflow_dispatch:           # 允许手动触发
```

### 修改APK输出名称

编辑 `.github/workflows/build-release.yml`，修改artifact名称：

```yaml
- name: Upload APK artifact
  uses: actions/upload-artifact@v4
  with:
    name: autoclicker-v1.0.0-apk  # 自定义名称
    path: app/build/outputs/apk/release/*.apk
    retention-days: 30
```

## 📱 功能说明

### 核心功能
- **自动点击**：模拟手动点击屏幕
- **脚本录制**：记录点击操作
- **脚本回放**：执行录制的脚本
- **智能识别**：通过文本、ID、内容描述识别元素
- **悬浮窗**：便捷的悬浮控制界面

### 使用方法
1. 开启辅助功能权限
2. 打开悬浮窗
3. 录制点击操作
4. 保存脚本
5. 回放脚本

## 🛠 技术栈

- **Android SDK**: 34
- **Java**: 8
- **Gradle**: 8.2
- **AndroidX**: 最新版本
- **AccessibilityService**: 核心功能
- **GitHub Actions**: CI/CD

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！

---

**祝你使用愉快！** 🎉
