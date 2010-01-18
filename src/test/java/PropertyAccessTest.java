import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import domain.Currency;

public class PropertyAccessTest {

	@Test
	public void shouldAccessPropertyWhenProtected()
			throws IntrospectionException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		Currency value = new Currency("id", "content");
		String propertyName = "key";

		BeanInfo info = Introspector.getBeanInfo(value.getClass());
		PropertyDescriptor[] descs = info.getPropertyDescriptors();

		PropertyDescriptor propertyDescriptor = null;

		for (PropertyDescriptor property : descs) {
			if (property.getName().equals(propertyName)) {
				propertyDescriptor = property;
			}
		}

		Method[] methods = value.getClass().getDeclaredMethods();
		Method writeMethod = null;
		for (Method method : methods) {
			if (method.getName().equals(
					"set" + propertyName.substring(0, 1).toUpperCase()
							+ propertyName.substring(1))) {
				method.setAccessible(true);
				writeMethod = method;
			}
		}

		writeMethod.invoke(value, "testing123");

		assertNotNull(propertyDescriptor);
		assertEquals("testing123", value.getKey());
	}

}
