<template>
  <div>
    <simple-table2 ref="table" meta="Department" :oneTimeSave="false"
      label="部门" :searchMethod="fetchData" :loading="loading"
      searchVisible :actions="defaultCrudActions" 
      createButton
      @do-save="doSaveDepartment"  @do-delete="doDeleteDepartment"
    >
    </simple-table2>
  </div>
</template>

<script>
import SimpleTable2 from '@/components/SimpleTable2'
import { departmentApi } from './department_api'
import { defaultCrudActions } from '@/utils/utils';

export default {
  name: 'Department',
  components: { SimpleTable2 },
  data() {
    return {
      defaultCrudActions,
      loading: false,
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData(params) {
      return await departmentApi.getDepartments(params, {This: this, loading: true})
    },
    onAdd() {
      this.$refs.table.showAddDialog();
    },
    async doSaveDepartment(department) {
      await departmentApi.save(department, {This: this, loading: true});
      this.$refs.table.refreshTable();
    },
    async doDeleteDepartment(department) {
      await departmentApi.delete(department.departmentId);
      this.$refs.table.refreshTable();
    }
  }
}
</script>
