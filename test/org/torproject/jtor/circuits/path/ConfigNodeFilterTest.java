package org.torproject.jtor.circuits.path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.torproject.jtor.data.IPv4Address;

public class ConfigNodeFilterTest {

	@Test
	public void testIsAddressString() {
		final List<String> validStrings = Arrays.asList(
				"1.2.3.4/16",
				"0.0.0.0/1",
				"255.0.255.0/16");
		
		final List<String> invalidStrings = Arrays.asList(
				"1.2.3.256/16",
				"1.2.3.4/61",
				"1.2.3.4/0",
				"1.2.3.4/22x",
				"1.2.3.4/",
				"1.2.3.4");

		for(String s: validStrings) {
			assertTrue(s, ConfigNodeFilter.isAddressString(s));
		}
		for(String s: invalidStrings) {
			assertFalse(s, ConfigNodeFilter.isAddressString(s));
		}

	}
	
	@Test
	public void testIsCountryCode() {
		final List<String> validStrings = Arrays.asList("{CC}", "{xx}");
		final List<String> invalidStrings = Arrays.asList("US", "{xxx}");
		for(String s: validStrings) { assertTrue(s, ConfigNodeFilter.isCountryCodeString(s)); }
		for(String s: invalidStrings) { assertFalse(s, ConfigNodeFilter.isCountryCodeString(s)); }
	}
	
	@Test
	public void testMaskFilter() {
		RouterMock r1 = new RouterMock();
		RouterMock r2 = new RouterMock();
		r1.address = IPv4Address.createFromString("1.2.3.4");
		r2.address = IPv4Address.createFromString("1.3.3.4");
		
		RouterFilter f = ConfigNodeFilter.createFilterFor("1.2.3.0/16");
		System.out.println(f);
		assertTrue(f.filter(r1));
		assertFalse(f.filter(r2));
	}
	
	
}
