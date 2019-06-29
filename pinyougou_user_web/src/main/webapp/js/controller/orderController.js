window.onload = function () {
    var app = new Vue({
            el: "#app",
            data: {
                order:{},

                entity: {
                    order: {},

                },
                status: ["未付款", "已付款", "未发货", "已发货", "交易成功", "交易关闭", "待评价",],

                ids: [],
                //总页数
                pages:1,
                //当前页
                pageNo:1,

            },
            methods: {
                findPage: function (pageNo) {
                    axios.post("../order/findPage.do?pageNo=" + pageNo + "&pageSize=4",this.order).then(function (response) {
                        app.entity.order = response.data.rows;
                        for (var i = 0; i < response.data.rows.length; i++) {
                            app.ids.push(response.data.rows[i].idString);
                        }

                        axios.get("../order/getOrderItems.do?ids=" + app.ids).then(function (response) {
                            for (var i = 0; i < response.data.length; i++) {
                                app.$set(app.entity.order[i], "orderItem", response.data[i]);
                            }
                        });
                        app.pages = response.data.pages;
                        app.pageNo = pageNo;
                    });
                },
                //让分页插件跳转到指定页
                goPage:function (page) {
                    app.$children[0].goPage(page);
                }
            },
            created: function () {
                this.findPage(1);
            }
        })
    ;
}



