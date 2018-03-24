<?php
$method = $_SERVER['REQUEST_METHOD'];
$request = explode('/', trim($_SERVER['PATH_INFO'],'/'));

if($method == 'GET'){
	//列表
	$conn = mysqli_connect("localhost","root","","messageboard");
	if ($conn){
		$query = "SELECT id,title,message,time FROM message ORDER BY time DESC";
		mysqli_query($conn,"SET NAMES UTF8");
		$result = mysqli_query($conn,$query);
		$i = 0;
		if(mysqli_num_rows($result) > 0){
			while($row = mysqli_fetch_assoc($result)){
				$Data[$i]["id"] = $row["id"];
				$Data[$i]["title"] = $row["title"];
				$Data[$i]["message"] = $row["message"];
				$Data[$i]["time"] = $row["time"];
				$i++;
			}
		}
		mysqli_close($conn);
		echo json_encode($Data);
	}else{
        echo json_encode('連線錯誤', JSON_UNESCAPED_UNICODE);
    }
}else if($method == 'POST'){
	//新增
	$_POST = array();
    parse_str(file_get_contents('php://input'),$_POST); 	
	$conn = mysqli_connect("localhost","root","","messageboard");
    if ($conn){
        $query = "INSERT INTO message(title,message) VALUES ('$_POST[Title]','$_POST[Message]');";
        mysqli_query($conn,"SET NAMES UTF8");
		mysqli_query($conn,$query);
        mysqli_close($conn);
        echo json_encode('新增成功', JSON_UNESCAPED_UNICODE);
    }else{
        echo json_encode('連線錯誤', JSON_UNESCAPED_UNICODE);
    }
}else if($method == 'PUT'){
	//修改
	$_PUT = array();
    parse_str(file_get_contents('php://input'),$_PUT);
    $conn = mysqli_connect("localhost","root","","messageboard");
    if ($conn){
        $query = "UPDATE message SET title = '$_PUT[Title]' , message = '$_PUT[Message]' WHERE id = '$request[0]'";
        mysqli_query($conn,"SET NAMES UTF8");
		mysqli_query($conn,$query);
        mysqli_close($conn);
        echo json_encode('修改成功', JSON_UNESCAPED_UNICODE);
    }else{
        echo json_encode('連線錯誤', JSON_UNESCAPED_UNICODE);
    }
}else if($method == 'DELETE'){
	//刪除
	$conn = mysqli_connect("localhost","root","","messageboard");
    if ($conn){
        $query = "DELETE FROM message WHERE id = ".$request[0];
        mysqli_query($conn,"SET NAMES UTF8");
		mysqli_query($conn,$query);
        mysqli_close($conn);
		echo json_encode('刪除成功', JSON_UNESCAPED_UNICODE);
    }else{
        echo json_encode('連線錯誤', JSON_UNESCAPED_UNICODE);
    }
}
?>