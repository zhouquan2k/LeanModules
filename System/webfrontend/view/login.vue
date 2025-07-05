<template>
    <div id="app">
        <div class="login-container">
            <div class="left-side">
                <!-- 这里可以放置一些文本或额外的图标/图片 -->
            </div>
            <div class="right-side">
                <div class="login-container">
                    <!-- 登录表单 -->
                    <el-form :model="loginForm" class="login-form">
                        <h2 class="login-title">登录</h2>

                        <!-- 用户名输入框 -->
                        <el-form-item>
                            <el-input size="medium" v-model="loginForm.username" placeholder="用户名"></el-input>
                        </el-form-item>

                        <!-- 密码输入框 -->
                        <el-form-item>
                            <el-input size="medium" v-model="loginForm.password" type="password" placeholder="密码"
                                @keyup.enter.native="handleLogin"></el-input>
                        </el-form-item>

                        <!-- 登录按钮 -->
                        <el-form-item>
                            <el-button class="button" size="medium" type="primary" :autofocus="true"
                                @click="handleLogin">登录</el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
export default {
    data() {
        return {
            loginForm: {
                username: '',
                password: ''
            }
        };
    },
    methods: {
        async handleLogin() {
            // 在这里处理登录逻辑
            // await login(this.loginForm);
            this.$store.dispatch("Login", this.loginForm).then(() => {
                // this.$router.replace(this.$route.query.from || '/');
                window.location.replace(this.$route.query.from || '/');
            });

        }
    }
};
</script>
<style scoped>
.login-container {
    display: flex;
    min-height: 100vh;
    justify-content: center;
    align-items: center;
    height: 100vh;
    margin-bottom: 60px;
}

.left-side,
.right-side {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
}

.button {
    background-color: #0960bd;
    width: 100%
}

.left-side {
    background-color: #1f2d3d;
    /* 深色背景 */
    /* background-image: url('path/to/your/left-side-image.jpg'); */
    /* 左侧背景图片 */
    background-size: cover;
    background-position: center;
    color: #ffffff;
    height: 100%
        /* 假设左侧文本是白色 */
}

.right-side {
    background-color: #ffffff !important;
    color: #000;
    /* 白色背景 */
}

.login-form {
    width: 300px;
    /* 其他样式 */
    font-size: 16px;
    line-height: 1.5;
    font-weight: bold;
}

.login-form>>>.el-input--medium .el-input__inner {
    background-color: #ffffff;
    font-size: 16px;
}

.login-title {
    color: #1f2d3d;
    font-family: sans-serif;
    text-align: left;
    font-size: 1.875rem;
    margin-bottom: 20px;
    font-weight: 700;
    /* 其他样式 */
}
</style>
  