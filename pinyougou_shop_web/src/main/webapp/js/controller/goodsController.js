//页面初始化完成后再创建Vue对象
//window.onload=function () {
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
			/*声明对象{
			  goods:{typeTemplateId:模板Id},
			  goodsDesc:{itemImages:[图片列表],customAttributeItems:[扩展属性列表],specificationItems:[用户勾选了规格列表]},
			  itemList:[商品sku列表]
		   }*/
			entity:{goods:{typeTemplateId:0},goodsDesc:{itemImages:[],
					customAttributeItems:[],specificationItems:[]},itemList:[]},
			//将要删除的id列表
			ids:[],
			//搜索包装对象
			searchEntity:{},
			//图片上传成功后保存的对象
			image_entity:{url:''},
			//商品一级分类列表
			itemCatList1:[],
			//商品二级分类列表
			itemCatList2:[],
			//商品三级分类列表
			itemCatList3:[],
			//品牌列表
			brandIds:[],
			//规格列表
			specIds:[],
			//商品状态
			status:['未审核','已审核','审核未通过','关闭'],
			//商品分类对象
			itemCatMap: {}
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
				//获取富文本内容
				this.entity.goodsDesc.introduction = editor.html();

				var url = "../goods/add.do";
				if(this.entity.goods.id != null){
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
				let id = this.getUrlParam()["id"];
				if(id != null) {
					axios.get("../goods/getById.do?id=" + id).then(function (response) {
						app.entity = response.data;
						//绑定富文本内容
						editor.html(response.data.goodsDesc.introduction);
						//把图片json字符串转换为数组对象
						app.entity.goodsDesc.itemImages = JSON.parse(app.entity.goodsDesc.itemImages);
						//把扩展属性json字符串转换为数组对象
						app.entity.goodsDesc.customAttributeItems = JSON.parse(app.entity.goodsDesc.customAttributeItems);
						//把规格信息json字符串转换为数组对象
						app.entity.goodsDesc.specificationItems = JSON.parse(app.entity.goodsDesc.specificationItems);

						//把商品sku列表的规格json串转换为数组
						for(let i = 0; i < app.entity.itemList.length; i++){
							app.entity.itemList[i].spec = JSON.parse(app.entity.itemList[i].spec);
						}
					})
				}
			},
			/**
			 * 识别规格checkbox是否要勾选
			 * @param specName 规格名称
			 * @param optionName 名称
			 * @return {查找结果：true|false}
			 */
			checkAttributeValue:function(specName,optionName){
				//检测规格名称存在否
				let obj = this.searchObjectByKey(this.entity.goodsDesc.specificationItems, "attributeName", specName);
				if(obj != null){
					//如果选项名称在列表中存在
					if(obj.attributeValue.indexOf(optionName) > -1){
						return true;
					}
				}
				return false;
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
			//文件上传
			upload:function(){
				//创建<form>表单对象
				var formData = new FormData() // 声明一个FormData对象
				// 'file' 这个名字要和后台获取文件的名字一样-表单的name属性
				//<input name="file" type="file" id="file"/>
				formData.append('file', document.querySelector('input[type=file]').files[0]);

				//post提交
				axios({
					url: '/upload.do',
					data: formData,
					method: 'post',
					headers: {
						'Content-Type': 'multipart/form-data'
					}
				}).then(function (response) {
					if(response.data.success){
						//上传成功
						app.image_entity.url=response.data.message;
					}else{
						//上传失败
						alert(response.data.message);
					}
				})
			},
			//把图片添加到图片数组中
			add_image_entity:function () {
				this.entity.goodsDesc.itemImages.push(this.image_entity);
			},
			//从图片数组中删除图片
			delete_image_entity:function (index) {
				this.entity.goodsDesc.itemImages.splice(index, 1);
			},
			/**
			 * 查询商品分类列表
			 * @param parentId 父id
			 * @param grade 当前分类级别1|2|3
			 */
			findItemCatList:function (parentId,grade) {
				axios.get("/itemCat/findByParentId.do?parentId=" + parentId).then(function (response) {
					app["itemCatList" + grade] = response.data;
				})
			},
			/**
			 * 检查一个对象数组中某个属性是否等于某个值
			 * @param list 要检索的对象数组
			 * @param key 查找的属性名
			 * @param keyValue 对比的属性值
			 * @return 查找的结果，返回null说明没有
			 */
			searchObjectByKey:function(list,key,keyValue){
				for(let i = 0; i < list.length; i++){
					if(list[i][key] == keyValue){
						return list[i];
					}
				}
				return null;
			},
			/**
			 * 页面规格checkbox点击事件
			 * @param event checkbox本身
			 * @param specName 规格名称
			 * @param optionName 选项名称
			 */
			updateSpecAttribute:function (event,specName,optionName) {
				// 1: 检查我们的规格名称有没有被勾选过
				let obj = this.searchObjectByKey(this.entity.goodsDesc.specificationItems, 'attributeName', specName);
				// 2:如果当前勾选的规格名称不存在
				if(obj == null){
					// 2.1: 规格列表追加一个元素
					this.entity.goodsDesc.specificationItems.push({
						"attributeName": specName,
						"attributeValue": [
							optionName
						]
					});
				}else{ // 3:如果当前勾选的规格名称存在
					// 3.1:如果checkbox是是选中状态
					if(event.target.checked){
						// 3.1.1: 追加规格选项元素
						obj.attributeValue.push(optionName);
					}else{ // 3.2: 如果checkbox是取消勾选
						// 3.2.1: 删除规格选项元素
						let optionIndex = obj.attributeValue.indexOf(optionName);
						obj.attributeValue.splice(optionIndex, 1);
						// 3.2.2: 如果取消勾选后，选项列表已经没有了
						if(obj.attributeValue.length < 1){
							// 3.2.3:移除整个规格名称列表
							let specIndex = this.entity.goodsDesc.specificationItems.indexOf(obj);
							this.entity.goodsDesc.specificationItems.splice(specIndex, 1);
						}
					}
				}

				//刷新sku列表
				this.createItemList();
			},
			// 1. 	创建createItemList方法，同时创建一条有基本数据，不带规格的初始数据
			createItemList: function () {
				// 参考: entity.itemList:[{spec:{},price:0,num:99999,status:'0',isDefault:'0' }]
				this.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];
				// 2. 	查找遍历所有已选择的规格列表，后续会重复使用它，所以我们可以抽取出个变量items
				let items = this.entity.goodsDesc.specificationItems;
				for(let i = 0; i < items.length; i++){
					//动态构建表格
					// 9. 	回到createItemList方法中，在循环中调用addColumn方法，并让itemList重新指向返回结果;
					this.entity.itemList = this.addColumn(this.entity.itemList,items[i].attributeName,items[i].attributeValue)
				}
			},
			// 3. 	抽取addColumn(当前的表格，列名称，列的值列表)方法，用于每次循环时追加列
			addColumn:function (list,specName,optionName) {
				// 4. 	编写addColumn逻辑，当前方法要返回添加所有列后的表格，定义新表格变量newList
				let newList = [];
				// 5. 	在addColumn添加两重嵌套循环，一重遍历之前表格的列表，二重遍历新列值列表
				for(let i = 0; i < list.length; i++){
					for(let j = 0; j < optionName.length; j++){
						// 6. 	在第二重循环中，使用深克隆技巧，把之前表格的一行记录copy所有属性，
						// 用到var newRow = JSON.parse(JSON.stringify(之前表格的一行记录));
						let newRow = JSON.parse(JSON.stringify(list[i]));
						// 7. 	接着第6步，向newRow里追加一列
						newRow.spec[specName] = optionName[j];
						// 8. 	把新生成的行记录，push到newList中
						newList.push(newRow);
					}
				}
				return newList;
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
			/**
			 * http://localhost:8082/admin/goods_edit.html?id=12372327&name=steven
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
			/**
			 * 上下架
			 */
			isSale:function (status) {
				if ("1"==status){
					axios.get("/goods/onsale.do?ids="+this.ids+"&status="+status).then(function (response) {
						if(response.data.success){
							//弹窗上架成功
							alert(response.data.message);
							//刷新当前页
							app.findPage(app.pageNo);
							//清空ids
							app.ids = [];
						}else{
							alert(response.data.message);
						}
					})
				}
				if ("2"==status){
					axios.get("/goods/offsale.do?ids="+this.ids+"&status="+status).then(function (response) {
						if(response.data.success){
							//弹窗上架成功
							alert(response.data.message);
							//刷新当前页
							app.findPage(app.pageNo);
							//清空ids
							app.ids = [];
						}else{
							alert(response.data.message);
						}
					})
				}

			}
		},
		//监听变量值变化
		watch:{
			//监听商品一级分类的值
			//function (改后的值,改前的值)
			"entity.goods.category1Id":function (newValue,oldVaue) {
				//触发二级分类列表加载
				this.findItemCatList(newValue, 2);
				//清空三级分类列表
				this.itemCatList3 = [];
				//清空模板id
				this.entity.goods.typeTemplateId = 0;
			},
			//监听商品二级分类的值
			"entity.goods.category2Id":function (newValue,oldVaue) {
				//触发三级分类列表加载
				this.findItemCatList(newValue, 3);
				//清空模板id
				this.entity.goods.typeTemplateId = 0;
			},
			//监听商品三级分类的值
			"entity.goods.category3Id":function (newValue,oldVaue) {
				//触发模板id列表加载
				//先查询整个商品分类对象
				axios.get("/itemCat/getById.do?id=" + newValue).then(function (response) {
					app.entity.goods.typeTemplateId = response.data.typeId;
				})
			},
			//监听模板id的值
			"entity.goods.typeTemplateId":function (newValue,oldValue) {
				//查询模板对象
				axios.get("/typeTemplate/getById.do?id=" + newValue).then(function (response) {
					//获取品牌列表
					app.brandIds = JSON.parse(response.data.brandIds);
					//获取扩展属性列表
					let id = app.getUrlParam()["id"];
					if(id == null){
						app.entity.goodsDesc.customAttributeItems = JSON.parse(response.data.customAttributeItems);
					}
					//查询规格与选项列表
					axios.get("/typeTemplate/findSpecList.do?id="+newValue).then(function (response) {
						app.specIds = response.data;
					})
				})
			}
		},
		//Vue对象初始化后，调用此逻辑
		created:function () {
			//调用用分页查询，初始化时从第1页开始查询
			this.findPage(1);

			//加载一级分类
			this.findItemCatList(0,1);

			//查询所有商品分类列表
			this.findAllItemCat();
			//跟据id查询商品信息
			this.getById();
		}
	});
//}
