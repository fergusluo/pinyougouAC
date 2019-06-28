//页面初始化完成后再创建Vue对象
window.onload=function () {

	//注册Vue组件
	Vue.component('v-select', VueSelect.VueSelect);

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
			//声明对象{customAttributeItems:[扩展属性列表]}
			entity:{customAttributeItems:[]},
			//将要删除的id列表
			ids:[],
			//模板状态
			status:['未审核','已审核','审核驳回','关闭'],
			//模板删除状态
			isDelete:['正常','已删除'],
			//搜索包装对象
			searchEntity:{},
			//品牌列表
			brandList:[{id:1,text:"联想"},{id:2,text:"vivo"},{id:3,text:"华为"}],
			//品牌列表
			specList:[]
		},
		methods:{
			//查询所有
			findAll:function () {
				axios.get("../typeTemplate/findAll.do").then(function (response) {
					//vue把数据列表包装在data属性中
					app.list = response.data;
				}).catch(function (err) {
					console.log(err);
				});
			},
			//分页查询
			findPage:function (pageNo) {
				axios.post("../typeTemplate/findPage.do?pageNo="+pageNo+"&pageSize="+10,this.searchEntity)
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
				var url = "../typeTemplate/add.do";
				if(this.entity.id != null){
					url = "../typeTemplate/update.do";
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
				axios.get("../typeTemplate/getById.do?id="+id).then(function (response) {
					app.entity = response.data;
					//把品牌json字符串转换为品牌列表
					app.entity.brandIds = JSON.parse(app.entity.brandIds);
					//把规格json字符串转换为规格列表
					app.entity.specIds = JSON.parse(app.entity.specIds);
					//把扩展属性json字符串转换为扩展属性列表
					app.entity.customAttributeItems = JSON.parse(app.entity.customAttributeItems);
				})
			},
			//批量伪删除数据
			<!--tbTypeTemplate表有更改请咨询罗强-->
			isDele:function () {
				axios.get("../typeTemplate/delete.do?ids="+this.ids).then(function (response) {
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
			//查询所有品牌列表
			findBrandList:function () {
				axios.get("/brand/findAll.do").then(function (response) {
					for(let i = 0; i < response.data.length; i++){
						delete response.data[i]["firstChar"];
						delete response.data[i]["name"];
					}
					app.brandList = response.data;
				})
			},
			//查询所有规格列表
			findSpecList:function () {
				axios.get("/specification/findAll.do").then(function (response) {
					for(let i = 0; i < response.data.length; i++){
						delete response.data[i]["specName"];
					}
					app.specList = response.data;
				})
			},
			//表格行添加逻辑
			addTableRow:function () {
				//向specificationOptionList数组追加一个空对象
				this.entity.customAttributeItems.push({});
			},
			/**
			 * 删除表格行
			 * @param index 删除的下标
			 */
			deleteTableRow:function (index) {
				//删除元素-splice(下标，删除的个数)
				this.entity.customAttributeItems.splice(index, 1);
			},
			/**
			 * 提取一个json字符串对象中，某个属性值，以逗号分隔
			 * @param jsonString 要解释的字符串对象
			 * @param key 提取的属性名
			 * @return 拼接后的结果
			 */
			jsonToString:function (jsonString,key) {
				let result = "";
				var list = JSON.parse(jsonString);
				for(let i = 0; i < list.length; i++){
					if(i > 0){
						result += ",";
					}
					//提取属性值
					result += list[i][key];
				}
				return result;
			},
			//更改审核状态
			<!--tbTypeTemplate表有更改请咨询罗强-->
			updateStatus:function (status) {
				axios.get("/typeTemplate/updateStatus.do?status=" + status + "&ids=" +this.ids )
					.then(function (response) {
						if (response.data.success) {
							//刷新数据
							app.findPage(app.pageNo);
							//清空勾选的ids
							app.ids = [];
						} else {
							alert(response.data.message);
						}
					});
			}
		},
		//Vue对象初始化后，调用此逻辑
		created:function () {
			//调用用分页查询，初始化时从第1页开始查询
			this.findPage(1);
			//查询所有品牌
			this.findBrandList();
			//查询所有规格
			this.findSpecList();
		}
	});
}
