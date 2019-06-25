window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            loginName:""
        },
        methods:{
            getLoginInfo:function () {
                axios.get("/login/info.do").then(function (response) {
                    app.loginName = response.data.loginName;
                })
            }
        },
        //初始化调用
        created:function () {
            this.getLoginInfo();
        }
    });
}