function submitForm(){	
	pass = document.getElementById("oldPassword").value;
	newPass = document.getElementById("newPassword").value;
	submitInfo = document.getElementById("submitInfo");
	form = document.getElementById("formularz");

	var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function(){
                if(this.readyState == 4 && this.status == 200){
			if(this.responseText == "good"){
				form.submit();
			}else{
				submitInfo.innerHTML = "Złe dane";
			}
                }
        };

        xhttp.open("POST", "/checkPassword", true)
	xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");	
        xhttp.send("oldPassword="+pass);

	return false;
}

function comparePasswords(){
	
	pass = document.getElementById("newPassword").value;
	passAgain = document.getElementById("newPasswordAgain").value;
	passAgainInfo = document.getElementById("passwordAgainInfo");

	if(pass.length === 0){
		passAgainInfo.innerHTML = "";
		return;
	}

	if(pass != passAgain){
		passAgainInfo.innerHTML = "Hasła niezgodne";
	}else{
		passAgainInfo.innerHTML = "Hasła zgodne";
	}
}

function measureStrength(){
	
	pass = document.getElementById("newPassword").value;
	passInfo = document.getElementById("passwordInfo");

	if(pass.length <= 0){
		passInfo.innerHTML = ""
		return; 
	}

	var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function(){
                if(this.readyState == 4 && this.status == 200){
			console.log(this.responseText);
			strength = assessStrength(this.responseText);
			passInfo.innerHTML = strength
                }
        };

        xhttp.open("GET", "/checkStrength?pass="+pass, true);
        xhttp.send();
}

function assessStrength(entropy){
	if(entropy < 28){
		return "Bardzo słabe";
	}else if(entropy < 36){
		return "Słabe";
	}else if(entropy < 60){
		return "Średnie";
	}else if(entropy < 128){
		return "Dobre";
	}else{
		return "Bardzo dobre";
	}
}
