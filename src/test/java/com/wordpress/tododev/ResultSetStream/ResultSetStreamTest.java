package com.wordpress.tododev.ResultSetStream;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ResultSetStreamTest {

	private class City{
		String city;
		String country; 
		public City(String city, String country) {
			this.city = city;
			this.country = country;
		}
		public String getCountry() {
			return country;
		}
		@Override
		public String toString() {
			return "City [city=" + city + ", country=" + country + "]";
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((city == null) ? 0 : city.hashCode());
			result = prime * result
					+ ((country == null) ? 0 : country.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			City other = (City) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (city == null) {
				if (other.city != null)
					return false;
			} else if (!city.equals(other.city))
				return false;
			if (country == null) {
				if (other.country != null)
					return false;
			} else if (!country.equals(other.country))
				return false;
			return true;
		}
		private ResultSetStreamTest getOuterType() {
			return ResultSetStreamTest.this;
		}
	}
	
	private String[][] data = new String[][]{
			{"Karachi", "Pakistan"},
			{"Istanbul", "Turkey"},
			{"Hong Kong", "China"},
			{"Saint Petersburg", "Russia"},
			{"Sydney", "Australia"},
			{"Berlin", "Germany"},
			{"Madrid", "Spain"}
		};

	private int timesCalled;
	private PreparedStatement mockPST;
	private ResultSet mockRS;
	
	@Before
	public void setup() throws SQLException{
		timesCalled = -1;
		mockRS = mock(ResultSet.class);
		mockPST = mock(PreparedStatement.class);
		
		when(mockRS.next()).thenAnswer(new Answer<Boolean>() {

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				if (timesCalled++ >= data.length)
					return false;
				return true;
			}
		});
	
		when(mockRS.getString(eq("city"))).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return data[timesCalled][0];
			}
		});
		when(mockRS.getString(eq("country"))).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return data[timesCalled][1];
			}
		});
		
		when(mockPST.executeQuery()).thenReturn(mockRS);
	}
	
	@Test
	public void simpleTest() throws SQLException{
		
		try (Stream<City> testStream = new ResultSetStream<City>().getStream(mockPST, 
				(ResultSet rs) -> {try {
					return new City(rs.getString("city"), rs.getString("country"));
				} catch (Exception e) {
					return null;
				}})){
		
			Iterator<City> cities = testStream.filter(
					city -> !city.getCountry().equalsIgnoreCase("China"))
					.limit(3).iterator();
		
			assertTrue(cities.hasNext());
			assertEquals(new City("Karachi", "Pakistan"), cities.next());
		
			assertTrue(cities.hasNext());
			assertEquals(new City("Istanbul", "Turkey"), cities.next());
		
			assertTrue(cities.hasNext());
			assertEquals(new City("Saint Petersburg", "Russia"), cities.next());
			
			assertFalse(cities.hasNext());
		}
		
	}
	
}
