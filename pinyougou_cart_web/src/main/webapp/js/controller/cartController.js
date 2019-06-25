window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            //购物车列表
            cartList:[],
            //统计业务实体{总数量，总金额}
            totalValue:{totalNum:0, totalMoney:0.00 },
            //收件人列表
            addressList:[],
            //用户选中的收件人
            address:{},
            //订单包装对象{paymentType:支付方式}
            order:{paymentType:1}
        },
        methods:{
            //查询当前用户购物车列表
            findCartList:function () {
                axios.get("/cart/findCartList.do").then(function (response) {
                    app.cartList = response.data;

                    //总金额与总数量清0
                    app.totalValue = {totalNum: 0, totalMoney: 0.00};
                    //刷新总数量与金额
                    for(let i = 0; i < app.cartList.length; i++){
                        let orderItems = app.cartList[i].orderItemList;
                        for(let j = 0; j < orderItems.length; j++){
                            //统计数量
                            app.totalValue.totalNum += orderItems[j].num;
                            //统计金额
                            app.totalValue.totalMoney += orderItems[j].totalFee;
                            //app.totalValue.totalMoney += 0.00001;
                        }
                    }
                })
            },
            //操作购物车
            //cart/addGoodsToCartList.do?itemId=1369293&num=-1
            addGoodsToCartList:function (itemId,num) {
                axios.get("/cart/addGoodsToCartList.do?itemId="+itemId+"&num=" + num).then(function (response) {
                    if(response.data.message){
                        //刷新购物列表
                        app.findCartList();
                    }else{
                        alert(response.data.message);
                    }
                })
            },
            //获取登录用户的收件人列表
            findAddressList:function () {
                axios.get("/address/findListByLoginUser.do").then(function (response) {
                    app.addressList = response.data;
                    //找出默认地址
                    for(let i = 0; i < app.addressList.length; i++){
                        if(app.addressList[i].isDefault == '1'){
                            app.address = app.addressList[i];
                            break;
                        }
                    }
                })
            },
            /**
             * 选择收件人
             * @param address 传入的收件人信息
             */
            selectAddress:function (address) {
                this.address = address;
            },
            /**
             * 用户选择支付方式
             * @param type
             */
            selectPayType:function (type) {
                this.order.paymentType = type;
            },
            //保存订单
            submitOrder:function () {
                // beSave.setReceiverAreaName(order.getReceiverAreaName());//收货地址
                this.order.receiverAreaName = this.address.address;
                // beSave.setReceiverMobile(order.getReceiverMobile());//手机号
                this.order.receiverMobile = this.address.mobile;
                // beSave.setReceiver(order.getReceiver());//收货人
                this.order.receiver = this.address.contact;

                //保存订单
                axios.post("/order/add.do",this.order).then(function (response) {
                    if(response.data.success){
                        //如果是在线支付
                        if(app.order.paymentType == 1){
                            window.location.href = "pay.html";
                        }else{
                            window.location.href = "ordersuccess.html";
                        }
                    }else{
                        alert(response.data.message);
                    }
                })
            }
        },
        created:function () {
            //查询购物车
            this.findCartList();
            //查询收件人
            this.findAddressList();
        }
    });
}