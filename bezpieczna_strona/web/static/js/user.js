document.addEventListener("DOMContentLoaded", function() {	
	var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function(){
                if(this.readyState == 4 && this.status == 200){
			if(this.responseText.length !== 0){
				var info = document.createElement("p");
				var text = document.createTextNode("Nowe połączenie do Twojego konta: " + this.responseText);
				info.appendChild(text);
				
				var pUnder = document.getElementById("login");
				var body = document.getElementsByTagName("BODY")[0];
				body.insertBefore(info, pUnder);
			}
                }
        };

        xhttp.open("GET", "/checkLogs", true);
        xhttp.send();
}, false);

function removePrivateFile(ele){
	
	fileName = ele.parentElement.id.slice(2);

	var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function(){
                if(this.readyState == 4 && this.status == 200){
			ele.parentElement.remove();
                }
        };

        xhttp.open("post", "/removePrivateFile", true);
	xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhttp.send("fileName="+fileName);
}
