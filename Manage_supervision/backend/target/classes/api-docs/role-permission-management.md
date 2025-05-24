# 角色权限管理 API 文档

## 1. 角色管理接口

### 1.1 获取所有角色

**请求方式**: GET

**URL**: `/api/roles`

**权限要求**: `role:view`

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "ADMIN",
      "description": "管理员角色",
      "permissions": ["system", "user:view", "user:edit", "user:delete", "role:view", "role:edit"],
      "createTime": "2023-05-10 12:00:00"
    },
    {
      "id": 2,
      "name": "USER",
      "description": "普通用户",
      "permissions": ["chat:view", "chat:send"],
      "createTime": "2023-05-10 12:00:00"
    }
  ]
}
```

### 1.2 创建角色

**请求方式**: POST

**URL**: `/api/roles`

**权限要求**: `role:add`

**请求参数**:

```json
{
  "name": "TEACHER",
  "description": "教师角色",
  "permissions": ["student:view", "student:edit", "chat:view", "chat:send"]
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 4,
    "name": "TEACHER",
    "description": "教师角色",
    "permissions": ["student:view", "student:edit", "chat:view", "chat:send"],
    "createTime": "2023-05-10 12:00:00"
  }
}
```

### 1.3 更新角色

**请求方式**: PUT

**URL**: `/api/roles/{id}`

**权限要求**: `role:edit`

**请求参数**:

```json
{
  "name": "TEACHER",
  "description": "高级教师角色",
  "permissions": ["student:view", "student:edit", "student:delete", "chat:view", "chat:send"]
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 4,
    "name": "TEACHER",
    "description": "高级教师角色",
    "permissions": ["student:view", "student:edit", "student:delete", "chat:view", "chat:send"],
    "createTime": "2023-05-10 12:00:00"
  }
}
```

### 1.4 删除角色

**请求方式**: DELETE

**URL**: `/api/roles/{id}`

**权限要求**: `role:delete`

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

## 2. 角色权限管理接口

### 2.1 获取角色的权限

**请求方式**: GET

**URL**: `/api/roles/{id}/permissions`

**权限要求**: `role:view`

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": ["system", "user:view", "user:edit", "user:delete", "role:view", "role:edit"]
}
```

### 2.2 分配角色权限

**请求方式**: POST

**URL**: `/api/roles/{id}/permissions`

**权限要求**: `role:edit`

**请求参数**:

```json
{
  "permissions": ["system", "user:view", "user:edit", "role:view"]
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 2.3 获取权限菜单树

**请求方式**: GET

**URL**: `/api/roles/permissions/tree`

**权限要求**: `role:view`

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "code": "system",
      "name": "系统管理",
      "description": "系统管理模块",
      "parentId": 0,
      "type": "menu",
      "path": "/system",
      "component": "Layout",
      "icon": "setting",
      "sort": 1,
      "isVisible": true,
      "children": [
        {
          "id": 2,
          "code": "user",
          "name": "用户管理",
          "description": "用户管理模块",
          "parentId": 1,
          "type": "menu",
          "path": "/system/user",
          "component": "system/user/index",
          "icon": "user",
          "sort": 1,
          "isVisible": true,
          "children": [
            {
              "id": 3,
              "code": "user:view",
              "name": "查看用户",
              "description": "查看用户列表和详情",
              "parentId": 2,
              "type": "button",
              "path": "",
              "component": "",
              "icon": "",
              "sort": 1,
              "isVisible": true,
              "children": []
            },
            {
              "id": 4,
              "code": "user:add",
              "name": "添加用户",
              "description": "添加新用户",
              "parentId": 2,
              "type": "button",
              "path": "",
              "component": "",
              "icon": "",
              "sort": 2,
              "isVisible": true,
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

## 3. 前端实现建议

### 3.1 角色管理页面

1. 创建角色列表页面，显示所有角色信息
2. 提供创建、编辑和删除角色的功能
3. 在角色列表中添加"分配权限"按钮，点击后打开权限分配对话框

### 3.2 权限分配对话框

1. 调用获取权限菜单树接口，以树形结构展示所有可用权限
2. 调用获取角色权限接口，预选中该角色已有的权限
3. 用户可以通过勾选树节点来选择权限
4. 提交时调用分配角色权限接口，保存用户的选择

### 3.3 权限控制

1. 在用户登录后，调用获取用户权限接口，获取用户所有权限
2. 根据用户权限动态生成菜单，只显示用户有权限访问的菜单
3. 对于按钮级别的权限，可以使用指令或组件包装，根据用户权限决定是否显示或启用按钮

### 3.4 权限树组件示例

```vue
<template>
  <div>
    <el-tree
      :data="permissionTree"
      show-checkbox
      node-key="id"
      :props="defaultProps"
      :default-checked-keys="checkedKeys"
      ref="permissionTree"
    ></el-tree>
    <div class="dialog-footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="savePermissions">确定</el-button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    roleId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      permissionTree: [],
      checkedKeys: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      }
    }
  },
  created() {
    this.fetchPermissionTree()
    this.fetchRolePermissions()
  },
  methods: {
    async fetchPermissionTree() {
      try {
        const res = await this.$http.get('/api/roles/permissions/tree')
        this.permissionTree = res.data.data
      } catch (error) {
        console.error('获取权限树失败', error)
        this.$message.error('获取权限树失败')
      }
    },
    async fetchRolePermissions() {
      try {
        const res = await this.$http.get(`/api/roles/${this.roleId}/permissions`)
        // 将权限编码转换为权限ID
        this.checkedKeys = this.getPermissionIdsByCode(this.permissionTree, res.data.data)
      } catch (error) {
        console.error('获取角色权限失败', error)
        this.$message.error('获取角色权限失败')
      }
    },
    getPermissionIdsByCode(tree, codes) {
      const ids = []
      const traverse = (nodes) => {
        if (!nodes) return
        for (const node of nodes) {
          if (codes.includes(node.code)) {
            ids.push(node.id)
          }
          if (node.children && node.children.length > 0) {
            traverse(node.children)
          }
        }
      }
      traverse(tree)
      return ids
    },
    getCheckedPermissionCodes() {
      const checkedNodes = this.$refs.permissionTree.getCheckedNodes()
      return checkedNodes.map(node => node.code)
    },
    async savePermissions() {
      try {
        const permissions = this.getCheckedPermissionCodes()
        await this.$http.post(`/api/roles/${this.roleId}/permissions`, { permissions })
        this.$message.success('权限分配成功')
        this.$emit('success')
      } catch (error) {
        console.error('权限分配失败', error)
        this.$message.error('权限分配失败')
      }
    }
  }
}
</script>
```

## 4. 权限编码说明

权限编码采用层级结构，格式为 `[模块]:[操作]`，例如：

- `system` - 系统管理模块
- `user:view` - 查看用户
- `user:add` - 添加用户
- `user:edit` - 编辑用户
- `user:delete` - 删除用户
- `role:view` - 查看角色
- `role:add` - 添加角色
- `role:edit` - 编辑角色
- `role:delete` - 删除角色

拥有模块权限的用户可以访问该模块的菜单，拥有具体操作权限的用户可以执行相应的操作。 