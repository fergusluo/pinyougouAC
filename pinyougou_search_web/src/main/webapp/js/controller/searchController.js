window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //结果集包装
            resultMap:{brandIds:[]},
            //搜索条件集{keywords: 关键字, category: 商品分类, brand: 品牌,
            //          spec: {'网络'：'移动4G','机身内存':'64G'},price:价格区间,pageNo:当前页
            //          ,pageSize:查询的条数,sortField:排序的域名,sort:排序方式asc|desc}
            searchMap:{keyword:'',category:'',brand:'',spec:{},price:'',pageNo:1,pageSize:20,
                sortField:'',sort:''},
            //用于构建页面分页标签的，页面显示多少页，由此参数决定
            pageLable:[],
            //标识分页插件中是否显示前面的省略号
            firstDot:true,
            //标识分页插件中是否显示后面的省略号
            lastDot:true,
            //用于记录查询按钮点击后searchMap.keyword的值
            searchKeyword:''
        },
        methods:{
            //搜索商品
            searchList:function () {
                axios.post("item/search.do", this.searchMap).then(function (response) {
                    app.resultMap = response.data;
                    //构建分页标签
                    app.buildPageLabel();

                    //查询成功后，记录当前的keyword到searchKeyword
                    app.searchKeyword = app.searchMap.keyword;
                });
            },
            //构建分页标签
            buildPageLabel:function(){
                //每次查询后，分页标签，重新计算
                this.pageLable = [];
                //查询完后，重置省略号
                this.firstDot = true;
                this.lastDot = true;

                let firstPage = 1;  //开始页码
                let lastPage = this.resultMap.totalPages;  //结束页码
                //如果总页数 > 5
                if(this.resultMap.totalPages > 5){
                    //显示当前页为中心的5个页码[8   9   10   11   12]
                    //如果当前页码 <= 3{
                    if(this.searchMap.pageNo <= 3){
                        //显示前5页
                        lastPage = 5;
                        this.firstDot = false;
                    //如果当前页码 >= (总页数-2)，当前前在后2页
                    }else if(this.searchMap.pageNo >= (this.resultMap.totalPages - 2)){
                        //显示后5页
                        firstPage = this.resultMap.totalPages - 4;
                        this.lastDot = false;
                    }else{
                        //以当前页为中心，加减偏移量2
                        firstPage = this.searchMap.pageNo - 2;
                        lastPage = this.searchMap.pageNo + 2;

                        this.firstDot = true;
                        this.lastDot = true;
                    }
                }else{
                    this.firstDot = false;
                    this.lastDot = false;
                }
                //组装分页标签
                for(let i = firstPage; i <= lastPage; i++){
                    this.pageLable.push(i);
                }
            },
            /**
             * 添加搜索条件
             * @param key 操作的属性名 category|brand|{网络|内存}
             * @param value 操作的属性值
             */
            addSearchItem:function (key,value) {
                if(key == 'category' || key == 'brand' || key == 'price'){
                    //this.searchMap[key] = value;
                    app.$set(this.searchMap,key,value);
                }else{
                    //this.searchMap.spec[key] = value;
                    app.$set(this.searchMap.spec,key,value);
                }
                //刷新数据
                this.searchList();
            },
            /**
             * 添加搜索条件
             * @param key 操作的属性名 category|brand|{网络|内存}
             */
            deleteSearchItem:function (key) {
                if(key == 'category' || key == 'brand' || key == 'price'){
                    //this.searchMap[key] = value;
                    app.$set(this.searchMap,key,'');
                }else{
                    //this.searchMap.spec[key] = value;
                    //app.$set(this.searchMap.spec,key,value);
                    //delete this.searchMap.spec[key];
                    app.$delete(this.searchMap.spec, key);
                }
                //刷新数据
                this.searchList();
            },
            /**
             * 跳转分页查询
             * @param pageNo 跳转的页码
             */
            queryByPage:function (pageNo) {
                //先把参数转换为整数，由于页面input收集的参数默认为字符
                pageNo = parseInt(pageNo);
                if(pageNo < 1 || pageNo > this.resultMap.totalPages){
                    alert("请输入正确页码！");
                    return;
                }
                this.searchMap.pageNo = pageNo;
                //发起查询
                this.searchList();
            },
            /**
             * 排序查询
             * @param sortField 排序的域
             * @param sort 方式asc|desc
             */
            sortSearch:function (sortField,sort) {
                this.searchMap.sortField = sortField;
                this.searchMap.sort = sort;
                //刷新数据
                this.searchList();
            },
            /**
             * 用于识别关键中是否包含品牌
             * @return true|false
             */
            keywordsIsBrand:function () {
                for(let i = 0; i < this.resultMap.brandIds.length; i++){
                    //如果关键字包含品牌信息
                    if(this.searchKeyword.indexOf(this.resultMap.brandIds[i].text) > -1){
                        return true;
                    }
                }
                return false;
            },
            /**
             * 解析一个url中所有的参数
             * @return {参数名:参数值}
             */
            getUrlParam:function() {
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
            },
            //加载其它页面跳转过来的keyword
            loadkeywords:function () {
                let keyword = this.getUrlParam()["keyword"];
                if(keyword != null){
                    //decodeURI-把url的中文转换回来
                    this.searchMap.keyword = decodeURI(keyword);
                    this.searchList();
                }
            }

        },
        created:function () {
            //this.searchList();

            this.loadkeywords();
        }
    });
}