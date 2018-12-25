function removePublicFile(ele){
	
	fileName = ele.parentElement.id.slice(2);

	var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function(){
                if(this.readyState == 4 && this.status == 200){
			ele.parentElement.remove();
                }
        };

        xhttp.open("POST", "/removePublicFile", true);
	xhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
        xhttp.send("fileName="+fileName);
}
