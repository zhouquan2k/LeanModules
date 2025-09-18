# System模块需求文档

## Department管理功能

### 需求描述
在System模块中实现部门（Department）的管理功能。

### 功能要求
1. 使用 SimpleTable2 组件实现部门的增删改查功能
2. 部门信息包含：
   - 部门名称（name）
   - 部门编码（code）
   - 部门主管（manager）

### 实现的文件
- `webfrontend/view/Department.vue` - 部门管理界面
- `webfrontend/view/department_api.js` - 部门管理相关API
- `webfrontend/router.js` - 添加部门管理路由

### API接口
1. 获取部门列表：GET /system/departments
2. 获取单个部门：GET /system/departments/{id}
3. 创建部门：POST /system/departments
4. 更新部门：PUT /system/departments/{id}
5. 删除部门：DELETE /system/departments/{id}

### 已完成功能
- [x] 创建部门管理界面
- [x] 实现部门列表展示
- [x] 实现部门删除功能
- [x] 添加路由配置

### 待实现功能
- [ ] 实现部门添加功能
- [ ] 实现部门编辑功能