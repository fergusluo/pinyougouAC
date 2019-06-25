//页面初始化完成后再创建Vue对象
window.onload=function () {
	//创建Vue对象
	var app = new Vue({
		//接管id为app的区域
		el:"#app",
		data:{
			//声明数据列表变量，供v-for使用
			list:[],
			//总页数
			pages:1,
			//当前页
			pageNo:1,
			//声明对象
			entity:{},
			//将要删除的id列表
			ids:[],
			//搜索包装对象,默认只查询未审核商品
			searchEntity:{auditStatus:0},
			//商品状态
			status:['未审核','已审核','审核未通过','关闭'],
			//商品分类对象
			itemCatMap: {1:"手机"}
		},
		methods:{
			//查询所有
			findAll:function () {
				axios.get("../goods/findAll.do").then(function (response) {
					//vue把数据列表包装在data属性中
					app.list = response.data;
				}).catch(function (err) {
					console.log(err);
				});
			},
			//分页查询
			findPage:function (pageNo) {
				axios.post("../goods/findPage.do?pageNo="+pageNo+"&pageSize="+10,this.searchEntity)
					.then(function (response) {
						app.pages = response.data.pages;  //总页数
						app.list = response.data.rows;  //数据列表
						app.pageNo = pageNo;  //更新当前页
					});
			},
			//让分页插件跳转到指定页
			goPage:function (page) {
				app.$children[0].goPage(page);
			},
			//新增
			add:function () {
				var url = "../goods/add.do";
				if(this.entity.id != null){
					url = "../goods/update.do";
				}
				axios.post(url, this.entity).then(function (response) {
					if (response.data.success) {
						//刷新数据，刷新当前页
						app.findPage(app.pageNo);
					} else {
						//失败时显示失败消息
						alert(response.data.message);
					}
				});
			},
			//跟据id查询
			getById:function (id) {
				axios.get("../goods/getById.do?id="+id).then(function (response) {
					app.entity = response.data;
				})
			},
			//批量删除数据
			dele:function () {
				axios.get("../goods/delete.do?ids="+this.ids).then(function (response) {
					if(response.data.success){
						//刷新数据
						app.findPage(app.pageNo);
						//清空勾选的ids
						app.ids = [];
					}else{
						alert(response.data.message);
					}
				})
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
			//批量审核
			updateStatus:function (status) {
				axios.get("/goods/updateStatus.do?ids=" + this.ids + "&status="+status).then(function (response) {
					if(response.data.success){
						//刷新当前页
						app.findPage(app.pageNo);
						//清空ids
						app.ids = [];
					}else{
						alert(response.data.message);
					}
				})
			}
		},
		//Vue对象初始化后，调用此逻辑
		created:function () {
			//调用用分页查询，初始化时从第1页开始查询
			this.findPage(1);
			//加载所有商品分类
			this.findAllItemCat();
		}
	});
}
