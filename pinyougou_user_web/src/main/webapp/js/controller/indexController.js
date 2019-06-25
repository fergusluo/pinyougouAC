window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            loginName:""
        },
        methods:{
            //加载用户信息
            showUserInfo:function () {
                axios.get("/login/info.do").then(function (response) {
                    app.loginName = response.data.loginName;
                })
            }
        },
        created:function () {
            this.showUserInfo();
        }
    });
}