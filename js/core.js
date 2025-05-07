var AJAX = {
	call: function (url, params, func, isfd) {
		var callobj = {
			url: url,
			type: "post",
			data: params,
			dataType: "text",
			success: func,
			error: function (xhr, status, error) {
				if (xhr.status == 0) {
					console.error(xhr.responseText);
					alert("네트워크 접속이 원할하지 않습니다.");
				}
				else {
					console.error(xhr.responseText);
					alert("에러가 발생하였습니다. 관리자에게 문의해주세요.");
					}
			},
		};
		if (isfd) {
			callobj.processData = false;
			callobj.contentType = false;
		}
		jQuery.ajax(callobj);
	}
};		//	end of AJAX

var Page = {
	init: function (cbfunc, url) {
		AJAX.call("jsp/session.jsp", null, function(data) {
			var uid = data.trim();
			if (uid == "null") {
				alert("로그인이 필요한 서비스 입니다.");
				window.location.href = "Login.html";
			}
			else {
				var param = (url == null) ? null : SessionStore.get(url);
				if (cbfunc != null) cbfunc(uid, param);
			}
		});
	},
	
	go: function(url, param) {
		SessionStore.set(url, param);
		window.location.href = url;
	},
};

var SessionStore = {
	set: function (name, val) {
		sessionStorage.setItem(name, JSON.stringify(val));
		},
		
		get: function (name) {
			var str = sessionStorage.getItem(name);
			return (str == null || str == "null") ? null : JSON.parse(str);
		},
		
		remove: function (name) {
			sessionStorage.removeItem(name);
		},
};

var DataCache = {
	set: function (name, data) {
		var obj = { ts: Date.now(), data: data };
		SessionStore.set(name, obj);	
	},
	
	get: function (name) {
		var obj = SessionStore.get(name);
		if (obj == null) {
			return null;
		}
		var diff = (Date.now() - obj.ts) / 60000;
		if (diff > 10) {
			SessionStore.remove(name);
			return null;
		}
		return obj.data;
	},
};