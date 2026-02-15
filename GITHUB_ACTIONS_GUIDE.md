# GitHub Actions 构建指南

## 📋 步骤1：创建GitHub仓库

### 1.1 访问GitHub
1. 打开浏览器，访问：https://github.com/new
2. 登录你的GitHub账号

### 1.2 创建新仓库
1. **Repository name**: 输入 `AutoClicker`
2. **Description**: 输入 `智能手机自动点击器`
3. **Public/Private**: 选择公开或私有
4. **不要勾选**：
   - ❌ Add a README file
   - ❌ Add .gitignore
   - ❌ Choose a license
5. 点击 **Create repository** 按钮

### 1.3 记录仓库地址
创建成功后，会显示类似这样的地址：
```
https://github.com/你的用户名/AutoClicker.git
```
请记下这个地址，后面会用到。

---

## 📤 步骤2：初始化Git仓库

### 2.1 打开命令提示符
1. 按 `Win + R` 键
2. 输入 `cmd` 并回车
3. 或者在项目文件夹按住 `Shift` + 右键，选择"在此处打开命令窗口"

### 2.2 切换到项目目录
```cmd
cd d:\AutoClicker
```

### 2.3 初始化Git仓库
```cmd
git init
```

### 2.4 添加所有文件
```cmd
git add .
```

### 2.5 提交更改
```cmd
git commit -m "Initial commit"
```

### 2.6 重命名分支为main
```cmd
git branch -M main
```

---

## 🔗 步骤3：连接GitHub仓库

### 3.1 添加远程仓库
将步骤1.3中记录的地址替换下面的 `你的用户名`：

```cmd
git remote add origin https://github.com/你的用户名/AutoClicker.git
```

**示例**（如果你的用户名是 `zhangsan`）：
```cmd
git remote add origin https://github.com/zhangsan/AutoClicker.git
```

### 3.2 推送代码到GitHub
```cmd
git push -u origin main
```

**如果提示输入用户名和密码**：
- **Username**: 你的GitHub用户名
- **Password**: 使用GitHub Personal Access Token（不是登录密码）

---

## 🔑 步骤4：创建GitHub Personal Access Token

### 4.1 访问Token设置
1. 访问：https://github.com/settings/tokens
2. 点击 **Generate new token** 按钮

### 4.2 配置Token
1. **Note**: 输入 `AutoClicker Build`
2. **Expiration**: 选择 `No expiration` 或 `90 days`
3. **Select scopes**: 勾选以下权限：
   - ✅ `repo`（完整仓库访问权限）
4. 点击 **Generate token** 按钮

### 4.3 复制Token
1. Token生成后会显示在页面顶部
2. 点击复制按钮
3. **立即复制**，关闭页面后无法再次查看

### 4.4 使用Token推送代码
如果步骤3.2推送时提示输入密码：
- **Username**: 你的GitHub用户名
- **Password**: 粘贴刚才复制的Token

---

## 🚀 步骤5：手动触发GitHub Actions

### 5.1 访问Actions页面
1. 打开你的GitHub仓库
2. 点击顶部的 **Actions** 标签页

### 5.2 选择工作流
1. 在左侧列表中找到 **Build Android Release APK**
2. 点击进入工作流详情页面

### 5.3 手动触发构建
1. 点击右侧的 **Run workflow** 按钮
2. 在弹出的对话框中，选择分支：`main`
3. 点击绿色的 **Run workflow** 按钮

### 5.4 等待构建完成
1. 页面会自动跳转到工作流运行页面
2. 等待所有步骤完成（通常2-5分钟）
3. 所有步骤显示绿色勾号✅表示构建成功

---

## 📥 步骤6：下载构建的APK

### 6.1 访问Actions运行记录
1. 点击顶部的 **Actions** 标签页
2. 点击最近的工作流运行记录（通常是第一个）

### 6.2 查看构建详情
1. 滚动到页面底部
2. 找到 **Artifacts** 区域

### 6.3 下载APK
1. 点击 **autoclicker-release-apk** 链接
2. 浏览器会自动下载ZIP文件
3. 解压ZIP文件
4. 找到里面的 `.apk` 文件

---

## ❓ 常见问题

### 1. 推送代码时提示认证失败

**错误**：
```
fatal: Authentication failed for 'https://github.com/...'
```

**解决方法**：
1. 创建GitHub Personal Access Token（参考步骤4）
2. 使用Token代替密码
3. 确保Token有 `repo` 权限

### 2. 推送代码时提示SSL错误

**错误**：
```
SSL certificate problem: unable to get local issuer certificate
```

**解决方法**：
```cmd
git config --global http.sslVerify false
```

### 3. Actions构建失败

**错误**：
```
Build failed
```

**解决方法**：
1. 点击失败的步骤，查看详细错误日志
2. 检查代码是否有语法错误
3. 确保所有依赖项都可以正常下载

### 4. 找不到Artifacts

**问题**：
```
构建成功但找不到下载链接
```

**解决方法**：
1. 确保构建状态是绿色✅
2. 等待几分钟后刷新页面
3. 检查是否在正确的工作流运行记录中

### 5. Token过期

**错误**：
```
fatal: Authentication failed for 'https://github.com/...'
```

**解决方法**：
1. 访问：https://github.com/settings/tokens
2. 删除旧的Token
3. 生成新的Token
4. 使用新Token推送代码

---

## 🔄 重新构建APK

### 方法1：推送新代码触发自动构建
```cmd
# 修改代码后
git add .
git commit -m "Update code"
git push origin main
```
推送后，GitHub Actions会自动触发构建。

### 方法2：手动触发构建
1. 访问仓库的 **Actions** 标签页
2. 点击 **Build Android Release APK** 工作流
3. 点击 **Run workflow** 按钮
4. 选择分支：`main`
5. 点击 **Run workflow** 按钮

---

## 📁 完整命令总结

### 第一次上传代码
```cmd
cd d:\AutoClicker
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/你的用户名/AutoClicker.git
git push -u origin main
```

### 后续更新代码
```cmd
cd d:\AutoClicker
git add .
git commit -m "Update code"
git push origin main
```

---

## 🎯 快速参考

| 操作 | 命令 |
|------|--------|
| 初始化Git | `git init` |
| 添加文件 | `git add .` |
| 提交更改 | `git commit -m "message"` |
| 连接仓库 | `git remote add origin URL` |
| 推送代码 | `git push -u origin main` |
| 更新代码 | `git add . && git commit && git push` |

---

**祝你构建成功！** 🎉
