package domain;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class DomainClass {

	private Long id;
	private String name;

	public DomainClass(Long i, String string) {
		this.id = i;
		this.name = string;
	}

	@Id
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
