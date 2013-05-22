package objects;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "GoodreadsResponse", strict = false)
public class Author {

	@Element
	private String id;

	@Element
	private String name;

	@Element(data = true)
	private String link;

	@Element(name = "fans_count")
	private int fansCount;

	@Element(name = "image_url", data = true)
	private String imageUrl;

	@Element(name = "small_image_url", data = true)
	private String smallImageUrl;

	@Element
	private String about;

	@Element
	private String influences;

	@Element(name = "works_count")
	private int worksCount;

	@Element
	private String gender;

	@Element
	private String hometown;

	@Element(name = "born_at")
	private String bornAt;

	@Element(name = "died_at")
	private String diedAt;

	@Element(required = false)
	private User user;

	/**
	 * The Goodreads user id of the author.
	 * 
	 * @return
	 */
	public int getUserId() {
		return user == null ? -1 : user.id;
	}

	@Root
	private static class User {

		@Element
		private int id;
	}

}