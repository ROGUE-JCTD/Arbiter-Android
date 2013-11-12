Arbiter.Util.Server = function(_name, _url, _username, _password){
	var name = null;
	var url = null;
	var username = null;
	var password = null;
	
	var Server = function(_name, _url, _username, _password){
		name = _name;
		url = _url;
		username = _username;
		password = _password;
	};
	
	Server(_name, _url, _username, _password);
	
	return {
		getName: function(){
			return name;
		},
		
		getUrl: function(){
			return url;
		},
		
		getUsername: function(){
			return username;
		},
		
		getPassword: function(){
			return password;
		}
	};
};