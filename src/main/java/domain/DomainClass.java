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
@Table(name = Tables.DOMAIN_TABLE)
public class DomainClass {

	private Long id;
	private String name;
	private Currency currency;

	private Set<Child> children = new HashSet<Child>();

	// Hibernate
	@SuppressWarnings("unused")
	private DomainClass() {
	}

	public DomainClass(long l, String string,
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

	public void setChildren(Set<Child> children) {
		this.children = children;
	}

	@OneToMany(cascade = CascadeType.ALL)
	public Set<Child> getChildren() {
		return children;
	}

	// Hibernate
	@SuppressWarnings("unused")
	private void setCurrency(Currency values) {
		this.currency = values;
	}

	public void addChild(Child child) {
		children.add(child);
	}

}
