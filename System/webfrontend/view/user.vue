<template>
  <div>
    <el-row>
      <el-col :span="orgId ? 4 : 0">
        <el-tree v-if="orgId" style="padding:5px;" :data="treeData" :props="treeProps" default-expand-all
          highlight-current :expand-on-click-node="false" :current-node-key="currentRole" node-key="id"
          @current-change="roleSelected"></el-tree>
      </el-col>
      <el-col :span="orgId ? 20 : 24">
        <FullTable ref="full-table" entity="User" label="用户"
          :searches="getEntityFields('User', [{ name: 'user' }, 'userCode', 'status', 'phone', { name: 'role' }])"
          :columns="[...getEntityFields('User', 'listable'), { name: 'roles', label: '角色' }]"
          :fixedSearchParams="{ userId: userId, roleId: roleId }" :searchMethod="onSearch" :formCols="1"
          :apis="user_api" :actions="actions" @reset="userId = null; roleId = null;" @assign-role="openAssignRolesDlg"
          @reset-password="onResetPassword">
          <template #simple-table_searches-user>
            <UserSelect v-model="userId" />
          </template>
          <template #simple-table_searches-role>
            <DictionarySelect v-model="roleId" dictionary="Role" placeholder="角色" />
          </template>
          <template #simple-table_columns-roles="scope">
            <el-tag v-for="(item, index) in getUniqueRoleNames(scope.data.roles)" :key="`tag-roles-${index}`">{{
              item }}
            </el-tag>
          </template>
        </FullTable>
      </el-col>
    </el-row>
    <el-dialog :title="`分配角色 - ${user.username} （${user.loginName}）`" :visible.sync="assignRolesVisible" width="500px"
      append-to-body>
      <el-form label-width="80px">
        <el-form-item>
          <el-checkbox-group v-model="roles">
            <el-checkbox v-for="role in allRoles" :label="role.roleId" :key="role.roleId">{{ role.roleName
              }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onAssignRoles">确 定</el-button>
        <el-button @click="assignRolesVisible = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { userApi as user_api } from './user_api.js';
import role_api from './role_api.js';
import FullTable from '@/components/FullTable';
import { defaultCrudActions, defaultActionProc } from '@/utils/utils';
import UserSelect from './components/user_select';
import DictionarySelect from '@/components/dictionary_select.vue';

export default {
  name: 'User',
  components: {
    UserSelect, FullTable, DictionarySelect
  },
  props: {
    orgId: {},
  },
  data() {
    return {
      user_api,
      userId: null,
      roleId: null,
      user: {},
      treeData: [{
        id: null,
        label: '所有',
        children: []
      }],
      treeProps: {
        children: 'children',
        label: 'label'
      },
      currentRole: '0',
      actions: [
        defaultCrudActions[0],
        {
          desc: "分配角色",
          event: 'assign-role',
          permission: ''
        },
        {
          desc: '重置密码',
          event: 'reset-password',
        }, defaultCrudActions[1]
      ],
      searches: {},
      assignRolesVisible: false,
      roles: [], //selected roles
      allRoles: [],
      toManySelectData: {},
    };
  },
  methods: {
    async onTest() {
      await user_api.test();
    },
    getUniqueRoleNames(roles) {
      return Array.from(new Map(roles.map(role => [role.roleName, role.roleName])).values());
    },
    roleSelected(role) {
      this.searches = { roleId: role.id };
      setTimeout(() => this.$refs.crud.onSearch(), 100);
    },
    async openAssignRolesDlg(user) {
      this.user = await user_api.get(user.userId);
      this.roles = this.user.roles.map(role => role.roleId); //filter(role => role.orgId == this.orgId || !this.orgId)
      this.assignRolesVisible = true;
    },
    refresh() {
      this.$refs['full-table'].refresh();
    },
    async onAssignRoles() {
      await user_api.assignRoles(this.user.userId, null, this.roles.map(role => ({ roleId: role, orgId: this.orgId })));
      this.assignRolesVisible = false;
      this.refresh();
      this.$message.success('分配角色成功.')
    },
    async onSearch(params) {
      return await user_api.search(params);
    },
    async onResetPassword(user) {
      try {
        await this.$confirm('此操作将重置该用户密码到默认密码, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
      }
      catch (error) {
        return;
      }
      await user_api.resetPassword(user.userId);
      this.$message({
        type: 'success',
        message: '密码重置成功!'
      });
    },
  },
  async created() {
    if (this.orgId) {
      this.allRoles = await role_api.queryRolesByOrgId(this.orgId);
    }
    else {
      this.allRoles = await role_api.list({ roleType: 'Global' });
    }
    this.treeData[0].children.push(...this.allRoles.map(role => ({ id: role.roleId, label: role.roleName })));
  }
};
</script>
<style scoped>
.el-checkbox {
  display: block;
  line-height: 30px;
}
</style>