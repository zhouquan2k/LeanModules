<template>
    <el-select :value="value" @change="onSelect" filterable :filter-method="filterMethod" placeholder="用户"
        value-key="userId" clearable>
        <el-option v-for="item in users" :key="item.userId"
            :label="`${item.username}${item.userCode ? '-' + item.userCode : ''}`" :value="item.userId" />
    </el-select>
</template>
<script>
import pinyin from "pinyin";
import { userApi } from '../user_api';

export default {
    model: {
        prop: 'value',
        event: 'change'
    },
    props: {
        orgId: { default: () => null },
        value: {},
        params: {},
    },
    data() {
        return {
            users: [],
            allUsers: [],
            // user: {},
        };
    },
    methods: {
        async onSelect(obj) {
            this.$emit('change', !obj || obj == '' ? null : obj);
        },
        filterMethod(val, param) {
            if (val && val != ' ') {
                this.users = this.allUsers.filter(item => {
                    let optionPinyin = pinyin(item.username, {
                        style: pinyin.STYLE_FIRST_LETTER
                    }).join('');
                    // 检查输入的拼音首字母是否匹配
                    return optionPinyin.indexOf(val.toLowerCase()) === 0 || item.userCode?.indexOf(val) > -1
                        || item.username.indexOf(val) > -1;
                });
            } else {
                this.users = this.allUsers;
            }
        }
    },
    async created() {
        this.allUsers = await userApi.search({ orgId: this.orgId, ...this.params });
        this.users = this.allUsers;
    }
};
</script>