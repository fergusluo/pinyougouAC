//window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //所有广告列表,以分类id为下标存储数据列表
            contentList:[],
            keyword:''
        },
        methods:{
            //加载所有广告
            findContentList:function (categoryId) {
                axios.get("/content/findByCategoryId.do?categoryId=" + categoryId).then(function (response) {
                    //app.contentList[categoryId] = response.data;
                    //要注意动态数组，要用app.$set()来绑定变量
                    app.$set(app.contentList,categoryId,response.data);
                })
            },
            search:function () {
                window.location.href = "http://localhost:8084/search.html?keyword=" + this.keyword;
            }
        },
        //初始化调用
        created:function () {
            //先查询轮播图
            this.findContentList(1);
        }
    });
//}