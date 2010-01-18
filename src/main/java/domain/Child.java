package domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import db.Tables;

@Entity
@Table(name = Tables.CHILD_TABLE)
public class Child {

	@Id
	private String id;
	private String name;

	public Child(String id, String string) {
		this.id = id;
		this.name = string;
	}

}
