package domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "VALUE_TABLE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SimpleValueObject {

	private String key;
	private String value;

	// Hibernate
	protected SimpleValueObject() {
	}

	public SimpleValueObject(String string, String string2) {
		this.key = string;
		this.value = string2;
	}

	@Id
	public String getKey() {
		return this.key;
	}

	// Hibernate
	@SuppressWarnings("unused")
	private void setKey(String key) {
		this.key = key;
	}
	
	public String getValue() {
		return this.value;
	}
	
	// Hibernate
	@SuppressWarnings("unused")
	private void setValue(String value) {
		this.value = value;
	}

}
