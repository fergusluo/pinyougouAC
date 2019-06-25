window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //购买数量
            num:1,
            //记录用户选择的规格
            specificationItems: {},
            //当前将要购买的商品-用户选中的或者默认的
            sku:{}
        },
        methods:{
            addNum:function (num) {
                this.num = num;
                if(this.num < 1){
                    this.num = 1;
                }
            },
            /**
             * 用户选择规格
             * @param specName 规格名称
             * @param optionName 选项名称
             */
            selectSpecification:function (specName,optionName) {
                //this.specificationItems[specName] = optionName;
                app.$set(this.specificationItems,specName,optionName);

                //更新sku信息
                this.searchSku();
            },
            /**
             * 识别页面上的规格有没有被用户选中
             * @param specName 规格名称
             * @param optionName 选项名称
             * @return true|false
             */
            isSelected:function (specName,optionName) {
                if (this.specificationItems[specName] == optionName) {
                    return true;
                }
                return false;
            },
            /**
             * 加载默认商品sku信息
             */
            loadSku:function () {
                //记录默认要购买的商品，由于后台跟据默认排序，所以这里第一条就是默认商品
                this.sku = skuList[0];
                //选中默认的规格，注意这里要用深克隆
                this.specificationItems = JSON.parse(JSON.stringify(skuList[0].spec));
            },
            //匹配两个对象的内容是否一致
            matchObject:function(map1,map2){
                for(var k in map1){
                    if(map1[k]!=map2[k]){
                        return false;
                    }
                }
                for(var k in map2){
                    if(map2[k]!=map1[k]){
                        return false;
                    }
                }
                return true;
            },
            //选择规格后，查询相应的sku
            searchSku: function () {
                for (let i = 0; i < skuList.length; i++) {
                    //使用用户选中的规格信息与sku列表的规格信息对比
                    if (this.matchObject(skuList[i].spec, this.specificationItems)) {
                        this.sku = skuList[i];
                        return;
                    }
                }
                //如果没有匹配的
                this.sku = {id: 0, title: '--------', price: 0};
            },
            //添加购物车
            addToCart: function () {
                //先打印出来看看，后续课程使用到
                //alert('skuid:' + this.sku.id);

                //发起购物车添加异步请求
                axios.get("http://localhost:8088/cart/addGoodsToCartList.do?itemId="+this.sku.id
                    +"&num="+this.num,{'withCredentials':true}).then(function (response) {
                    if(response.data.success){
                        window.location.href = "http://localhost:8088/cart.html";
                    }else{
                        alert(response.data.message);
                    }
                });
            }

        },
        created:function () {
            this.loadSku();
        }
    });
}