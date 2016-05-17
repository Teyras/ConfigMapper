

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import cz.cuni.mff.ConfigMapper.Nodes.ListOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;

public class EqualsTest {

	@Test
	public void testSimpleValueEquals() {
		ScalarOption simpleOne1 = new ScalarOption("One", "1");
		ScalarOption simpleOne2 = new ScalarOption("One", "1");
		ScalarOption simpleOneFake = new ScalarOption("Two", "1");
		ScalarOption simpleTwo = new ScalarOption("Two", "2");
		
		assertEquals(simpleOne1, simpleOne2);
		assertNotEquals(simpleOne1, simpleOneFake);
		assertNotEquals(simpleOne1, simpleTwo);
		assertNotEquals(simpleOne1, new Object());
		assertNotEquals(simpleOne1, null);
	}
	
	ListOption listOnes1 = new ListOption("Ones", Arrays.asList("One", "1", "Uno"),",");
	ListOption listOnes2 = new ListOption("Ones", Arrays.asList("One", "1", "Uno"),",");
	ListOption listTwos1= new ListOption("Twos", Arrays.asList("Two", "2", "Due"),",");
	@Test
	public void testListValueEquals() {
		ListOption listOnesFake= new ListOption("Twos", Arrays.asList("One", "1", "Uno"),",");
		ListOption listOnesWrongSep= new ListOption("Ones", Arrays.asList("One", "1", "Uno"),":");
		ListOption listOnesEmpty1 = new ListOption("Empty", Collections.emptyList(),",");
		ListOption listOnesEmpty2 = new ListOption("Empty", Collections.emptyList(),",");
		ListOption listOnesEmptyFake = new ListOption("Ones", Collections.emptyList(),",");
		ListOption listOnesNull = new ListOption("Ones", null,",");
		
		assertEquals(listOnes1, listOnes2);
		assertEquals(listOnesEmpty1, listOnesEmpty2);
		
		// wrong name
		assertNotEquals(listOnes1, listOnesFake);
		// full X empty
		assertNotEquals(listOnes1, listOnesEmpty1);
		// full X empty + same name
		assertNotEquals(listOnes1, listOnesEmptyFake);
		assertNotEquals(listOnes1, listOnesNull);
		// wrong name + empty list
		assertNotEquals(listOnesEmpty1, listOnesEmptyFake);
		// completely different
		assertNotEquals(listOnes1, listTwos1);
		// different object type
		assertNotEquals(listOnesEmpty1, new Object());
		assertNotEquals(listOnesEmpty1, null);
		// different separator
		assertNotEquals(listOnes1, listOnesWrongSep);
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
		Section flatNull = new Section("flatOnes", null);
		
		assertEquals(flatListOnes1, flatListOnes2);
		assertEquals(struct1, struct2);
		
		// completely different
		assertNotEquals(flatListOnes1, flatListTwos);
		// same name different children
		assertNotEquals(flatListOnes1, flatListOnesFake);
		assertNotEquals(flatListOnes1, flatNull);
		// same children different name
		assertNotEquals(flatListOnesFake, flatListTwos);
		// different structured 
		assertNotEquals(struct1, structDif);
		// different object type
		assertNotEquals(struct1, new Object());
		assertNotEquals(struct1, null);
	}

}
