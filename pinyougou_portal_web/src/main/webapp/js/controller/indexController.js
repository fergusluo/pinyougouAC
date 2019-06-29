//window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //所有广告列表,以分类id为下标存储数据列表
            contentList:[],
            keyword:'',
            //商品一级分类列表
            itemCatList1:[],
            //商品二级分类列表
            itemCatList2:[],
            //商品三级分类列表
            itemCatList3:[],
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
            },
            findAllItemCat:function () {
                axios.get("/itemCat/findAll.do").then(function (response) {
                    //组装商品分类数组[id:商品分类名称]
                    for(let i = 0; i < response.data.length; i++){
                        //构建所有商品分类的Map对象{id:name}，这种写法显示不了
                        //app.itemCatMap[response.data[i].id] = response.data[i].name;

                        //$set(操作的变量名,修改的属性,修改的值) = Vue.set()
                        app.$set(app.itemCatMap,response.data[i].id,response.data[i].name);

                    }
                });
            },
        },
        //初始化调用
        created:function () {
            //先查询轮播图
            this.findContentList(1);
            //查询一级分类
            this.findAllItemCat();
        }
    });
//}