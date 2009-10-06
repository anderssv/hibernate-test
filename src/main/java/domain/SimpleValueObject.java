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
	private String content;

	// Hibernate
	protected SimpleValueObject() {
	}

	public SimpleValueObject(String string, String content) {
		this.key = string;
		this.content = content;
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

	public String getContent() {
		return this.content;
	}

	// Hibernate
	@SuppressWarnings("unused")
	private void setContent(String value) {
		this.content = value;
	}

}
