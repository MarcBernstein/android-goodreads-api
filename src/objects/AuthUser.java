package objects;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

// <GoodreadsResponse>
// <Request>
// <authentication>true</authentication>
// <key><![CDATA[84uSfOCs4L6R8VdgnDrOLQ]]></key>
// <method><![CDATA[api_auth_user]]></method>
// </Request>
// <user id="2448689">
// <name>Marc Bernstein</name>
// <link><![CDATA[https://www.goodreads.com/user/show/2448689-marc-bernstein?utm_medium=api]]></link>
// </user>
//
// </GoodreadsResponse>
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
