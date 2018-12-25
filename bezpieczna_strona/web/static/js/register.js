function submitForm(){	
	login = document.getElementById("login").value;
	pass = document.getElementById("password").value;
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

        xhttp.open("POST", "/checkCredentials", true);
	xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhttp.send("login="+login+"&password="+pass);

	return false;
}

function comparePasswords(){
	
	pass = document.getElementById("password").value;
	passAgain = document.getElementById("passwordAgain").value;
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
	
	pass = document.getElementById("password").value;
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

        xhttp.open("POST", "/checkStrength", true);
	xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhttp.send("pass="+pass);
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
