package com.github.marcbernstein.grapi.xml.objects;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Author {
	@Element
	private String id;

	/**
	 * The Goodreads user id of the author, if there is one.
	 * 
	 * @return
	 */
	public String getUserId() {
		return user == null ? null : user.id;
	}

	@Element
	private String name;

	public String getName() {
		return name;
	}

	@Element(data = true)
	private String link;

	@Element(name = "fans_count")
	private int fansCount;

	@Element(name = "image_url", data = true)
	private String imageUrl;

	@Element(name = "small_image_url", data = true)
	private String smallImageUrl;

	@Element(required = false)
	private String about;

	@Element(required = false)
	private String influences;

	@Element(name = "works_count")
	private int worksCount;

	@Element(required = false)
	private String gender;

	@Element(required = false)
	private String hometown;

	@Element(name = "born_at", required = false)
	private String bornAt;

	@Element(name = "died_at", required = false)
	private String diedAt;

	@Element(required = false)
	private UserInternal user;

	@ElementList(required = false)
	private List<Book> books;

	@Root
	private static class UserInternal {
		@Element
		String id;
	}
}