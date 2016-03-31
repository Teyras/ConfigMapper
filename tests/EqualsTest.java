

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import cz.cuni.mff.ConfigMapper.Nodes.ListValue;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import cz.cuni.mff.ConfigMapper.Nodes.SimpleValue;

public class EqualsTest {

	@Test
	public void testSimpleValueEquals() {
		SimpleValue simpleOne1 = new SimpleValue("One", "1");
		SimpleValue simpleOne2 = new SimpleValue("One", "1");
		SimpleValue simpleOneFake = new SimpleValue("Two", "1");
		SimpleValue simpleTwo = new SimpleValue("Two", "2");
		
		assertEquals(simpleOne1, simpleOne2);
		assertNotEquals(simpleOne1, simpleOneFake);
		assertNotEquals(simpleOne1, simpleTwo);
	}
	
	ListValue listOnes1 = new ListValue("Ones", Arrays.asList("One", "1", "Uno"));
	ListValue listOnes2 = new ListValue("Ones", Arrays.asList("One", "1", "Uno"));
	ListValue listTwos1= new ListValue("Twos", Arrays.asList("Two", "2", "Due"));
	@Test
	public void testListValueEquals() {
		ListValue listOnesFake= new ListValue("Twos", Arrays.asList("One", "1", "Uno"));
		
		ListValue listOnesEmpty1 = new ListValue("Empty", Collections.emptyList());
		ListValue listOnesEmpty2 = new ListValue("Empty", Collections.emptyList());
		ListValue listOnesEmptyFake = new ListValue("Ones", Collections.emptyList());
		
		assertEquals(listOnes1, listOnes2);
		assertEquals(listOnesEmpty1, listOnesEmpty2);
		
		// wrong name
		assertNotEquals(listOnes1, listOnesFake);
		// full X empty
		assertNotEquals(listOnes1, listOnesEmpty1);
		// full X empty + same name
		assertNotEquals(listOnes1, listOnesEmptyFake);
		// wrong name + empty list
		assertNotEquals(listOnesEmpty1, listOnesEmptyFake);
		// completely different
		assertNotEquals(listOnes1, listTwos1);
	}
	
	@Test
	public void testSectionEquals() {
		Section flatListOnes1 = new Section("flatOnes", Arrays.asList(listOnes1,listOnes2));
		Section flatListOnes2 = new Section("flatOnes", Arrays.asList(listOnes2,listOnes1));
		Section flatListOnesFake = new Section("flatOnes", Arrays.asList(listTwos1));
		Section flatListTwos  = new Section("flatTwos", Arrays.asList(listTwos1));
		Section struct1 = new Section("structOnes", Arrays.asList(flatListOnes1, flatListTwos));
		Section struct2 = new Section("structOnes", Arrays.asList(flatListOnes1, flatListTwos));
		Section structDif = new Section("structOnes", Arrays.asList(flatListOnes1, flatListOnesFake,flatListTwos));
		
		assertEquals(flatListOnes1, flatListOnes2);
		assertEquals(struct1, struct2);
		
		// completely different
		assertNotEquals(flatListOnes1, flatListTwos);
		// same name different children
		assertNotEquals(flatListOnes1, flatListOnesFake);
		// same children different name
		assertNotEquals(flatListOnesFake, flatListTwos);
		// different structured 
		assertNotEquals(struct1, structDif);
	}

}
