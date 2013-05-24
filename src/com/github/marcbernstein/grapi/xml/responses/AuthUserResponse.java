package com.github.marcbernstein.grapi.xml.responses;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.github.marcbernstein.grapi.xml.objects.AuthUser;

@Root(name = "GoodreadsResponse", strict = false)
public class AuthUserResponse {

	@Element
	private AuthUser user;

	public AuthUser getAuthUser() {
		return user;
	}
}
