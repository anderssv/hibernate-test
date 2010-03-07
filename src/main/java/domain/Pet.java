package domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import db.Tables;

@Entity
@Table(name = Tables.PET_TABLE)
public abstract class Pet {

	@Id
	private String id;
	private String name;

	public Pet(String id, String string) {
		this.id = id;
		this.name = string;
	}

}
