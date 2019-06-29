window.onload=function () {
    var app = new Vue({
        el:"#app",
        data:{
            list:{},
            entity:{provinceId:'',cityId:'',alias:''},
            provinceList:{},
            citiesList:{},
            areasList:{},
            addressMap:{}
        },
        methods:{
            //跟据用户名查询
            findListByLoginUser:function () {
                axios.get("/address/findListByLoginUser.do").then(function (response) {
                    app.list = response.data;
                })
            },
            findProvinces:function () {
                axios.get("/address/findProvinces.do").then(function (response) {
                    app.provinceList = response.data;
                })
            },
            findCities:function (parentId) {
                axios.get("/address/findCities.do?parentId="+parentId).then(function (response) {
                    app.citiesList = response.data;
                })
            },
            findAreas:function (parentId) {
                axios.get("/address/findAreas.do?parentId="+parentId).then(function (response) {
                    app.areasList = response.data;
                })
            },
            alias:function (alias) {
                this.entity.alias=alias;
            },
            submit:function () {
                axios.post("/address/add.do",this.entity).then(function () {
                    //提示操作结果
                    alert(response.data.message);
                    if (response.data.success){
                        this.findListByLoginUser();
                    }
                })
            },
            findAddressMap:function () {
                axios.get("/address/addressMap.do").then(function (response) {
                    app.addressMap = response.data;
                })
            },
            deleteOne:function (id) {
                axios.get("/address/deleteOne.do?id="+id).then(function (response) {
                    if (response.data.success) {
                        alert("删除成功！");
                        window.location.href="home-setting-address.html"
                    }else{
                        alert(response.data.message);
                    }
                })
            },
            setDefault:function (id) {
                axios.get("/address/setDefault.do?id="+id).then(function (response) {
                    if (response.data.success) {
                        alert("设置成功！");
                        window.location.href="home-setting-address.html"
                    }else{
                        alert(response.data.message);
                    }
                })
            },
            getById:function (id) {
                axios.get("/address/getById.do?id="+id).then(function (response) {
                    app.entity=response.data;
                })
            },
            update:function () {
                axios.post("/address/update.do",this.entity).then(function (response) {
                    //提示操作结果
                    alert(response.data.message);
                    if (response.data.success){
                        app.entity={};
                        window.location.href="home-setting-address.html";
                    }else{
                        alert(response.data.message);
                    }
                })
            }
        },
        watch:{
            //监听省份ID的值
            //function (改后的值,改前的值)
            "entity.provinceId":function (newValue,oldVaue) {
                //触发城市列表加载
                this.findCities(newValue);
                //清空地区列表
                this.areasList = [];
            },
            //监听城市Id的值
            //function (改后的值,改前的值)
            "entity.cityId":function (newValue,oldVaue) {
                //触发地区列表加载
                this.findAreas(newValue);
            },
        },
        //Vue对象初始化后，调用此逻辑
        created:function () {
            this.findListByLoginUser();
            this.findProvinces();
            this.findAddressMap();
        }
    });
};