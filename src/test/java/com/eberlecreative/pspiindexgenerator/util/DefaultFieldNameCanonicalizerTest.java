package com.eberlecreative.pspiindexgenerator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class DefaultFieldNameCanonicalizerTest {

	private static final String EXPECTED_VALUE = "firstname";

	private DefaultFieldNameCanonicalizer testObj;

	@Before
	public void init() {
		testObj = new DefaultFieldNameCanonicalizer();
	}

	@Test
	public void specifyCanonicalizedValueEqualsExpectedValue() {
		thenResultEqualsExpectedValue(EXPECTED_VALUE);
	}

	@Test
	public void specifySpaceSeparatedValueEqualsExpectedValue() {
		thenResultEqualsExpectedValue("first Name");
	}

	@Test
	public void specifyUnderscoreSeparatedValueEqualsExpectedValue() {
		thenResultEqualsExpectedValue("first_Name");
	}

	@Test
	public void specifyDashSeparatedValueEqualsExpectedValue() {
		thenResultEqualsExpectedValue("first-Name");
	}

	@Test
	public void specifySpaceSeparatedLowerCaseValueEqualsExpectedValue() {
		thenResultEqualsExpectedValue("first name");
	}

	@Test
	public void specifyUnderscoreSeparatedLowerCaseValueEqualsExpectedValue() {
		thenResultEqualsExpectedValue("first_name");
	}

	@Test
	public void specifyDashSeparatedValueLowerCaseEqualsExpectedValue() {
		thenResultEqualsExpectedValue("first-name");
	}

	@Test
	public void specifyMizedSeparatedValueEqualsExpectedValue() {
		thenResultEqualsExpectedValue("first  --- ___ --- ___ ---   Name");
	}

	@Test
	public void specifyInvalidFieldNameExceptionThrownIfInputValueIsNotEmptyButHasInvalidCharacters() {
		thenInvalidFieldNameExceptionThrown("!@#$%^&*()");
	}

	@Test
	public void specifyInvalidFieldNameExceptionThrownIfInputValueIsEmpty() {
		thenInvalidFieldNameExceptionThrown("");
	}

	@Test
	public void specifyInvalidFieldNameExceptionThrownIfInputValueIsNull() {
		thenInvalidFieldNameExceptionThrown(null);
	}

	private void thenInvalidFieldNameExceptionThrown(String inputValue) {
		try {
			getResult(inputValue);
			fail(String.format("Expected InvalidFieldNameException to be thrown for value \"%s\"", inputValue));
		} catch (InvalidFieldNameException e) {
			assertEquals(String.format("Field name \"%s\" is invalid!", inputValue), e.getMessage());
		}
	}

	private void thenResultEqualsExpectedValue(String inputValue) {
		thenResultEquals(inputValue, EXPECTED_VALUE);
	}

	private void thenResultEquals(String inputValue, String expectedValue) {
		assertEquals(expectedValue, getResult(inputValue));
	}

	private String getResult(String inputValue) {
		return testObj.canonicalizeFieldName(inputValue);
	}

}
