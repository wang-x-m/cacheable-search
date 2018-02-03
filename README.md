
# cacheable-search
参数缓存框架，将页面跳转需要的参数缓存后方便后续使用。
具体的项目演进与分析过程，详见gitchat：**http://gitbook.cn/gitchat/activity/5a52d334ebd9cc598adf1258**

## 如何使用？

 1. 添加依赖：

``` stylus
	<dependency>
		<groupId>com.github.cmlbeliever</groupId>
		<artifactId>cacheable-search-mvc</artifactId>
		<version>1.0</version>
	</dependency>
```

 2. 在Controller中添加注解：@SearchCache

``` stylus
 @RequestMapping("/list")
    public String userList(Model model, @SearchCache() User u) {
        model.addAttribute("searchParam", u);
        return "user-list";
    }
```



## Sample使用教程 ##
- 进入CacheableSearchMVCSample工程
- 执行SearchCacheMVCApplication
- 等待启动完成

## 测试 ##

**注：cacheToken每次都会随机生成，这里需要根据实际情况调用。为了测试方便，这里使用get的方式进行说明。**

项目启动后，访问：http://localhost:8080/user/list?age=123&name=zhangsan 可以看到页面输出：

    searchParam:
	User [age=123, name=zhangsan]
	cacheToken:
	90508986-33b6-44cd-a869-7be8de2612ed
	myToken:

将cacheToken作为参数，访问：http://localhost:8080/user/list?cacheToken=90508986-33b6-44cd-a869-7be8de2612ed 可以看到之前请求的参数缓存下来了：

    searchParam:
	User [age=123, name=zhangsan]
	cacheToken:
	90508986-33b6-44cd-a869-7be8de2612ed
	myToken:

关闭浏览器后，再次打开访问http://localhost:8080/user/list?cacheToken=90508986-33b6-44cd-a869-7be8de2612ed 可以看到缓存的信息已经没有了，数据被清空了。

## BUG、意见、反馈 ##
如果您在使用中有任何的bug或意见，可以在项目添加issue或者在gitchat添加评论，我会及时对项目及时修正，**持续维护中...**

### TODO
支持application/json格式访问，将会在版本1.2中发布
