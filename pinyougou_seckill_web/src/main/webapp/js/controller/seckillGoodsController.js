window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //商品列表
            list:[]
        },
        methods:{
            //查询所有正在秒杀的商品列表
            findList:function () {
                axios.get("/seckillGoods/findList.do").then(function (response) {
                    app.list = response.data;
                })
            }
        },
        created:function () {
            this.findList();
        }
    });
}