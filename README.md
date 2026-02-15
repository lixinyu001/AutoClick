# AutoClicker - 智能手机自动点击器

## 📁 项目结构

```
app/src/main/java/com/example/autoclicker/
├── adapter/                    # 适配器类
│   └── StepAdapter.java       # 步骤列表适配器
├── model/                     # 数据模型类
│   └── ClickScript.java       # 脚本和步骤数据模型
├── service/                   # 服务类
│   ├── AutoClickService.java               # 核心自动点击服务
│   ├── FloatingWindowService.java          # 基础悬浮窗服务
│   └── EnhancedFloatingWindowService.java   # 增强版悬浮窗服务
├── ui/                        # UI相关类
│   └── activity/            # Activity类
│       ├── MainActivity.java               # 主界面
│       └── ScriptEditorActivity.java       # 脚本编辑器
└── utils/                     # 工具类
    ├── ActionRecorder.java               # 操作录制器
    └── SmartElementFinder.java          # 智能元素识别器
```

## 🎯 核心功能

### 1. 智能元素识别 (SmartElementFinder)
- **按文本查找** - 根据屏幕上的文字定位控件
- **按ID查找** - 根据控件ID精确定位
- **按内容描述查找** - 根据contentDescription定位
- **智能匹配算法** - 自动选择最佳匹配元素
- **可点击元素查找** - 快速定位所有可交互元素

### 2. 操作录制 (ActionRecorder)
- **录制用户操作** - 自动记录点击、长按、滑动等操作
- **智能元素录制** - 录制时识别并记录元素信息
- **脚本自动生成** - 将录制的操作转换为可执行脚本
- **操作回放** - 一键重放录制的操作序列
- **时间延迟管理** - 精确记录和重现操作间隔

### 3. 增强版悬浮窗 (EnhancedFloatingWindowService)
- **可视化编辑模式** - 点击屏幕直接添加点击点
- **快速操作菜单** - 常用功能一键访问
- **实时录制控制** - 悬浮窗直接控制录制状态
- **点击点可视化** - 在屏幕上显示所有点击点位置
- **拖拽移动** - 自由拖动悬浮窗到任意位置

### 4. 脚本系统 (ClickScript)
- **多种操作类型** - 点击、长按、滑动、等待、滚动
- **参数配置** - 重复次数、点击间隔、随机延迟
- **步骤管理** - 添加、编辑、删除、排序
- **脚本保存** - 支持保存和加载脚本

## 🚀 使用方法

### 基本流程
1. **开启辅助功能** - 在系统设置中启用AutoClicker服务
2. **打开悬浮窗** - 在主界面点击"显示悬浮窗"
3. **选择操作模式**：
   - **手动添加** - 点击"添加点击点"进入编辑模式
   - **录制操作** - 点击"开始录制"记录操作
   - **智能识别** - 在脚本编辑器中使用智能识别功能

### 智能识别使用
1. 打开脚本编辑器
2. 点击"智能识别"按钮
3. 选择查找方式（文本/ID/内容描述）
4. 输入要查找的内容
5. 系统自动定位并添加到脚本

### 录制功能使用
1. 在悬浮窗点击"开始录制"
2. 执行你想要自动化的操作
3. 点击"停止录制"
4. 点击"执行脚本"回放录制的操作

## 🛠️ 构建APK

### 使用GitHub Actions（推荐）
1. 将代码推送到GitHub仓库
2. 进入仓库的 **Actions** 标签页
3. 选择 **Build Android Release APK** 工作流
4. 点击 **Run workflow**
5. 等待2-5分钟构建完成
6. 下载生成的APK

### 使用Android Studio
1. 打开项目
2. 点击 **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**
3. 等待构建完成
4. 在 `app/build/outputs/apk/debug/` 目录找到APK

## 📱 权限说明

应用需要以下权限：
- **SYSTEM_ALERT_WINDOW** - 显示悬浮窗
- **BIND_ACCESSIBILITY_SERVICE** - 辅助功能服务（必需）
- **WRITE_EXTERNAL_STORAGE** - 保存脚本文件

## 🔧 技术栈

- **语言**: Java
- **最低SDK**: 21 (Android 5.0)
- **目标SDK**: 34 (Android 14)
- **构建工具**: Gradle 8.2
- **JDK版本**: 17

## 📝 注意事项

1. **辅助功能权限** - 这是Android系统的安全限制，跨应用自动点击必需
2. **屏幕分辨率** - 智能识别功能可适应不同分辨率
3. **电池优化** - 建议将应用加入白名单
4. **安全警告** - 请谨慎使用，避免用于非法用途

## 🎉 特性总结

✅ 智能元素识别（文本/ID/描述）
✅ 操作录制和回放
✅ 增强版悬浮窗（可视化编辑）
✅ 完整的脚本系统
✅ 高精度点击算法
✅ 多种操作类型支持
✅ 参数化配置
✅ 跨应用点击

## 📄 许可证

Apache License 2.0

---

**最后更新**: 2026年2月15日
**版本**: 2.0
