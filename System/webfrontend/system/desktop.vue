<template>
    <div class="desktop">
        <el-row :span="24" class="top-panel">
            <el-col :span="18">
                <div style="width:100%">&nbsp;
                </div>
            </el-col>
            <el-col :span="6">
                <div>
                    <el-statistic title="待办事项"><template slot="formatter">
                            2 / 8
                        </template></el-statistic>
                </div>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="12">
                <el-card class="box-card">
                    <div slot="header" class="clearfix">
                        <span>待办事项 （2/8）</span>
                        <el-button style="float: right; padding: 3px 0" type="text">操作按钮</el-button>
                    </div>
                    <el-table :data="tableData" style="width: 100%">

                        <el-table-column prop="name" label="事项类型" width="100">
                        </el-table-column>
                        <el-table-column prop="address" label="事项描述">
                        </el-table-column>
                        <el-table-column prop="date" label="日期" width="180">
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>
            <el-col :span="12">
            </el-col>
        </el-row>
    </div>
</template>
  
<script>
export default {
    data() {
        return {
            like: true,
            value1: 4154.564,
            value2: 1314,
            title: "增长人数",
            tableData: [{
                date: '2016-05-02',
                name: '王小虎',
                address: '上海市普陀区金沙江路 1518 弄'
            }, {
                date: '2016-05-04',
                name: '王小虎',
                address: '上海市普陀区金沙江路 1517 弄'
            }, {
                date: '2016-05-01',
                name: '王小虎',
                address: '上海市普陀区金沙江路 1519 弄'
            }, {
                date: '2016-05-03',
                name: '王小虎',
                address: '上海市普陀区金沙江路 1516 弄'
            }]
        };
    },
};
</script>
<style lang="scss">
.app-main {
    margin: 0px !important;
    height: 100px;
}

.desktop {
    background-color: #f8f8f9;
    padding-top: 5px;
    padding-left: 10px;
    height: 100%;
}

.top-panel {
    margin-left: 2px;
    margin-bottom: 10px;
    margin-top: 6px;
    margin-right: 10px;
    padding: 20px;
    background-color: #ffffff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1);
}

.box-card {}

.like {
    cursor: pointer;
    font-size: 25px;
    display: inline-block;
}
</style>