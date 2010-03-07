package domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import db.Tables;

@Entity
@Table(name = Tables.OWNER_TABLE)
public class Owner {

	private Long id;
	private String name;
	private Currency currency;

	private Set<Pet> pets = new HashSet<Pet>();

	// Hibernate
	@SuppressWarnings("unused")
	private Owner() {
	}

	public Owner(long l, String string,
			Currency currency) {
		this.id = l;
		this.name = string;
		this.currency = currency;
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

	@ManyToOne(fetch = FetchType.LAZY)
	public Currency getCurrency() {
		return this.currency;
	}

	public void setPets(Set<Pet> children) {
		this.pets = children;
	}

	@OneToMany(cascade = CascadeType.ALL)
	public Set<Pet> getPets() {
		return pets;
	}

	// Hibernate
	@SuppressWarnings("unused")
	private void setCurrency(Currency values) {
		this.currency = values;
	}

	public void addChild(Pet child) {
		pets.add(child);
	}

}
