package domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "DOMAIN_TABLE")
public class DomainClass {

	private Long id;
	private String name;
	private SimpleValueObject value;

	// Hibernate
	@SuppressWarnings("unused")
	private DomainClass() {
	}

	public DomainClass(long l, String string,
			SimpleValueObject simpleValueObject) {
		this.id = l;
		this.name = string;
		this.value = simpleValueObject;
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

	@ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
	public SimpleValueObject getValue() {
		return this.value;
	}

	// Hibernate
	@SuppressWarnings("unused")
	private void setValue(SimpleValueObject values) {
		this.value = values;
	}

}
