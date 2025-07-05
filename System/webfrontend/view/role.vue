<template>
  <div>
    <FullTable ref="crud" name="Role" entity="Role" label="角色" :apis="apis" :actions="actions" @action="defaultActionProc"
    :searchMethod="onSearch" :formCols="1"
    @assign="openAssignPermissionsDlg"/>
    <el-dialog x:title="`分配功能权限 - ${role.roleName}`" :visible.sync="assignPermissionsVisible" width="500px"
      append-to-body>
      <el-form label-width="80px">
        <el-form-item>
          <!--el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">展开/折叠</el-checkbox>
          <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">全选/全不选</el-checkbox-->
          <el-tree class="tree-border" :data="functions" show-checkbox ref="tree_functions" node-key="name"
            :check-strictly="true" empty-text="加载中，请稍后" :props="defaultTreeProps"></el-tree>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onAssignPermissions">确 定</el-button>
        <el-button @click="assignPermissionsVisible = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import role_api from './role_api.js';
import FullTable from '@/components/FullTable';
import { defaultCrudActions, defaultActionProc } from '@/utils/utils';

export default {
  components: {
    FullTable,
  },
  data() {
    return {
      apis: role_api,
      role: {},
      assignPermissionsVisible: false,
      defaultTreeProps: {
        label: "label",
        children: "permissions"
      },
      functions: [],
      actions: [
        defaultCrudActions[0],
        {
          desc: "分配权限",
          event: 'assign',
        },
        {
          desc: '删除',
          event: 'delete',
          // available: row => !row.fixed ,
        }
      ]
    };
  },
  methods: {
    defaultActionProc,
    async onSearch(params) {
      return await role_api.list(params);
    },
    async openAssignPermissionsDlg(role) {
      this.role = await role_api.get(role.roleId);
      this.assignPermissionsVisible = true;
      if (!this.functions || this.functions.length == 0) {
        const functions = await role_api.getAllFunctions();
        functions.forEach(func => {
          for (var perm of func.permissions) {
            perm.name = `${func.permissionDomain}.${perm.name}`;
          }
        });
        this.functions = functions;
      }
      this.$refs.tree_functions.setCheckedKeys(this.role.permissions);
    },
    async onAssignPermissions() {
      var permissions = this.$refs.tree_functions.getCheckedKeys();
      await role_api.assignRolePermissions(this.role.roleId, permissions);
      this.assignPermissionsVisible = false;
      this.$refs.crud.refresh();
      this.$message.success('分配权限成功.')
    }
  }
};
</script>
