<%--
  Created by IntelliJ IDEA.
  User: DBQ
  Date: 2016/11/24
  Time: 19:41
  To change this template use File | Settings | File Templates.
--%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="st" uri="http://www.springframework.org/tags" %>
<html>
  <head>
    <title>$Title$</title>
    <script type="text/javascript" src="jQuery/jquery-3.1.1.js"></script>
    <script type="text/javascript" src="bootstrap/js/bootstrap.js"></script>
  </head>
  <body>
  <div id="test">
    <a href="modeler.html?modelId=b92d430b-beec-11e7-8055-34f39a4ae408">编辑1号流程</a>
    <a id="add" <%--href="/model/create?name='test002'&key='002'&description='002'"--%> >创建一个流程</a>

  </div>


  <form class="form-horizontal" id="myForm" action="<st:url value='/model/create'></st:url>"
              method="post">
            <div class="form-group">
              <label for="name" class="col-sm-2 control-label">模型名字</label>
              <div class="col-sm-10">
                <input type="text" class="form-control" id="name" name="name"
                       placeholder="name">
              </div>
            </div>
            <div class="form-group">
              <label for="key" class="col-sm-2 control-label">模型key</label>
              <div class="col-sm-10">
                <input type="text" class="form-control" id="key" name="key"
                       placeholder="key">
              </div>
            </div>
            <div class="form-group">
              <label for="description" class="col-sm-2 control-label">模型名字</label>
              <div class="col-sm-10">
                <input type="text" class="form-control" id="description"
                       name="description" placeholder="模型描述">
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
              <button type="submit" class="btn btn-primary" >提交</button>
            </div>
        </form>

  <div class="row">
    <div class="col-md-3" id="modelroot"></div>
    <div class="col-md-9" id="image"></div>
  </div>

  </body>

  <script>
    $("#add").click(function () {
        alert("add")
        /*$.ajax({
            type:'post',
            url:window.location.href +'/model/create',
            data:{name:'test001',key:'123',description:'测试添加模板'},
            async:false,
            success:function () {
                alert("添加成功");
            },
            error:function () {
                alert("添加失败");
            }
        })*/

        //$("#addmodel").modal("show");

    });
    var ctx = null;
$(function () {
    ctx='<%=request.getContextPath()%>';
    getAllModel();

})
    function getAllModel() {
        $.get(ctx+"/model/list",null,function(data){
            $("#modelroot").empty();
            $("#image").empty();
            console.log(data);
            var str='<div class="row" style="font-size: 20px;">所有流程模型</div>';
            data.map((item)=>{
                str+="<div class='row'><a href='#' onclick=\"getOne('"+item.id+"')\">"+item.name+"</a></div>";
        });
            $("#modelroot").append(str);
        })
    }

    function getOne(id){
        modelId=id;
        //$.get("/model/getImage?modelId="+id,null,function(data){
        $("#image").empty();
        var str="<img src="+ctx+"/model/getImage?modelId="+id+" ></img>";
        $("#image").append(str);
        //})
    }
  </script>
</html>
