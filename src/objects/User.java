package objects;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class User {

	@Attribute
	private String id;

	@Element
	private String name;

	@Element
	private String link;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLink() {
		return link;
	}
}