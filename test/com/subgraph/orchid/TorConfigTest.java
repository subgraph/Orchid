package com.subgraph.orchid;

import static org.junit.Assert.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.subgraph.orchid.Tor;
import com.subgraph.orchid.TorConfig;

public class TorConfigTest {

	private TorConfig config;
	
	@Before
	public void setup() {
		config = Tor.createConfig();
	}


	@Test
	public void testCircuitBuildTimeout() {
		final long timeout = config.getCircuitBuildTimeout();
		assertEquals(TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS), timeout);
		config.setCircuitBuildTimeout(2, TimeUnit.MINUTES);
		assertTrue(config.getCircuitBuildTimeout() > timeout);
	}
	
	@Test
	public void testDataDirectory() {
		final File dd = config.getDataDirectory();
		assertTrue(dd.getPath().charAt(0) != '~');
		final String testPath = "/foo/dir";
		config.setDataDirectory(new File(testPath));
		assertEquals(new File(testPath), config.getDataDirectory());
	}
	
	@Test
	public void testMaxCircuitsPending() {
		assertEquals(32, config.getMaxClientCircuitsPending());
		config.setMaxClientCircuitsPending(23);
		assertEquals(23, config.getMaxClientCircuitsPending());
	}
	
	@Test
	public void testEnforceDistinctSubnets() {
		assertEquals(true, config.getEnforceDistinctSubnets());
		config.setEnforceDistinctSubnets(false);
		assertEquals(false, config.getEnforceDistinctSubnets());
	}
	
	@Test
	public void testCircuitStreamTimeout() {
		assertEquals(0, config.getCircuitStreamTimeout());
		config.setCircuitStreamTimeout(30, TimeUnit.SECONDS);
		assertEquals(30 * 1000, config.getCircuitStreamTimeout());
	}
}
