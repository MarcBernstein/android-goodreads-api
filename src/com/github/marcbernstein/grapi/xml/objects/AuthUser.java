package com.github.marcbernstein.grapi.xml.objects;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "GoodreadsResponse", strict = false)
public class AuthUser {

	@Element
	private User user;

	public String getUserId() {
		return user == null ? null : user.getId();
	}

	public String getUserName() {
		return user == null ? null : user.getName();
	}

	public String getUserLink() {
		return user == null ? null : user.getLink();
	}
}
