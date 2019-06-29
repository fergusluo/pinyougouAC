//页面初始化完成后再创建Vue对象
window.onload = function () {
//创建Vue对象
    var app = new Vue({
        //接管id为app的区域
        el: "#app",
        data: {
            //声明数据列表变量，供v-for使用
            list: [{order: {orderId:0}, orderItem: [{title:''}]}],
            //总页数
            pages: 1,
            //当前页
            pageNo: 1,
            /*声明对象{
              goods:{typeTemplateId:模板Id},
              goodsDesc:{itemImages:[图片列表],customAttributeItems:[扩展属性列表],specificationItems:[用户勾选了规格列表]},
              itemList:[商品sku列表]
           }*/
            entity: {
                 order: {},
                 itemList: []
            },
            //将要删除的id列表
            ids: [],
            //搜索包装对象
            searchEntity: {},
            //图片上传成功后保存的对象
            image_entity: {url: ''},
            //商品一级分类列表
            itemCatList1: [],
            //商品二级分类列表
            itemCatList2: [],
            //商品三级分类列表
            itemCatList3: [],
            //品牌列表
            brandIds: [],
            //规格列表
            specIds: [],
            //商品状态
            status: ['未审核', '已审核', '审核未通过', '关闭'],
            //商品分类对象
            itemCatMap: {},
            //删除商品
            itemId:0
        },
        methods: {
            //查询所有
            findAll: function () {
                axios.get("../goods/findAll.do").then(function (response) {
                    //vue把数据列表包装在data属性中
                    app.list = response.data;
                }).catch(function (err) {
                    console.log(err);
                });
            },
            //分页查询
            findPage: function (pageNo) {
                axios.post("../order/findPage.do?pageNo=" + pageNo + "&pageSize=" + 10, this.searchEntity)
                    .then(function (response) {
                        app.pages = response.data.pages;  //总页数
                        app.list = response.data.rows;  //数据列表
                        app.pageNo = pageNo;  //更新当前页
                    });
            },
            //让分页插件跳转到指定页
            goPage: function (page) {
                app.$children[0].goPage(page);
            },
            //新增
            add: function () {
                //获取富文本内容
                this.entity.goodsDesc.introduction = editor.html();

                var url = "../goods/add.do";
                if (this.entity.goods.id != null) {
                    url = "../goods/update.do";
                }
                axios.post(url, this.entity).then(function (response) {
                    //提示操作结果
                    alert(response.data.message);
                    if (response.data.success) {
                        //刷新数据，刷新当前页
                        //app.entity = {goods:{typeTemplateId:0},goodsDesc:{itemImages:[],customAttributeItems:[]}};
                        //清空富文本
                        //editor.html("");

                        window.location.href = "goods.html";
                    }
                });
            },
            //跟据id查询
            getById:function () {
                //{参数名：参数值}
                let orderId = this.getUrlParam()["orderId"];
                if(orderId != null) {
                    axios.get("../order/getById.do?orderId=" + orderId).then(function (response) {
                        app.entity = response.data;
                    })
                }
            },
            //批量删除数据
            dele: function () {
                axios.get("../order/delete.do?ids=" + this.ids).then(function (response) {
                    if (response.data.success) {
                        //刷新数据
                        app.findPage(app.pageNo);
                        //清空勾选的ids
                        app.ids = [];
                    } else {
                        alert(response.data.message);
                    }
                })
            },
            //删除商品数据
            deleItem: function () {
                axios.get("../order/deleteItem.do?id=" + this.itemId).then(function (response) {
                    if (response.data.success) {
                        //刷新数据
                        app.findPage(app.pageNo);
                        //清空勾选的ids
                        app.ids = [];
                    } else {
                        alert(response.data.message);
                    }
                })
            },
            /**
             * 检查一个对象数组中某个属性是否等于某个值
             * @param list 要检索的对象数组
             * @param key 查找的属性名
             * @param keyValue 对比的属性值
             * @return 查找的结果，返回null说明没有
             */
            searchObjectByKey: function (list, key, keyValue) {
                for (let i = 0; i < list.length; i++) {
                    if (list[i][key] == keyValue) {
                        return list[i];
                    }
                }
                return null;
            },
            findAllItemCat: function () {
                axios.get("/itemCat/findAll.do").then(function (response) {
                    //组装商品分类数组[id:商品分类名称]
                    for (let i = 0; i < response.data.length; i++) {
                        //构建所有商品分类的Map对象{id:name}，这种写法显示不了
                        //app.itemCatMap[response.data[i].id] = response.data[i].name;

                        //$set(操作的变量名,修改的属性,修改的值) = Vue.set()
                        app.$set(app.itemCatMap, response.data[i].id, response.data[i].name);

                    }
                });
            },
            /**
             * http://localhost:8082/admin/goods_edit.html?id=12372327&name=steven
             * 解析一个url中所有的参数
             * @return {参数名:参数值}
             */
            getUrlParam: function () {
                //url上的所有参数
                var paramMap = {};
                //获取当前页面的url
                var url = document.location.toString();
                //获取问号后面的参数
                var arrObj = url.split("?");
                //如果有参数
                if (arrObj.length > 1) {
                    //解析问号后的参数
                    var arrParam = arrObj[1].split("&");
                    //读取到的每一个参数,解析成数组
                    var arr;
                    for (var i = 0; i < arrParam.length; i++) {
                        //以等于号解析参数：[0]是参数名，[1]是参数值
                        arr = arrParam[i].split("=");
                        if (arr != null) {
                            paramMap[arr[0]] = arr[1];
                        }
                    }
                }
                return paramMap;
            }
        },
        //监听变量值变化
        watch: {},
        //Vue对象初始化后，调用此逻辑
        created: function () {
            //调用用分页查询，初始化时从第1页开始查询
            this.findPage(1);
            this.getById();
        }
    });
};
