Arbiter.Util.Server = function(_type, _name, _url, _username, _password){
	var type = null;
	var name = null;
	var url = null;
	var username = null;
	var password = null;
	
	var Server = function(_type, _name, _url, _username, _password){
		type = _type;
		name = _name;
		url = _url;
		username = _username;
		password = _password;
	};
	
	Server(_type, _name, _url, _username, _password);
	
	return {
		getType: function(){
			return type;
		},
		
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