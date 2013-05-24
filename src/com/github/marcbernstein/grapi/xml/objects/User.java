package com.github.marcbernstein.grapi.xml.objects;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
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