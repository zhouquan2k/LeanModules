<template>
    <div>
        <!--crud ref="crud" name="OperateLog" desc="操作日志" :apis="apis" :actions="[]" :searchVisible="true">
        </crud-->
        <SimpleTable ref="table" :searchVisible="true" :columns="getEntityFields('OperateLog', 'listable')"
            idCol="logId" :searches="getEntityFields('OperateLog', 'searchable')" :searchMethod="onSearch">
            <template #expand="scope">
                {{ scope.data.resultMsg }}
            </template>
        </SimpleTable>
        <el-button v-if="hasPermission('***')" type="text" @click="apis.testException()">test</el-button>
    </div>
</template>
<script>
import operateLogApi from "./operate_log_api.js";
import Crud from '@/components/Crud';
import SimpleTable from '@/components/SimpleTable.vue';
import { hasPermission } from '@/utils/utils';
export default {
    components: {
        Crud, SimpleTable
    },
    data() {
        return {
            apis: operateLogApi,
        }
    },
    methods: {
        hasPermission,
        onSearch(params) {
            return operateLogApi.search(params);
        }
    }
}
</script>