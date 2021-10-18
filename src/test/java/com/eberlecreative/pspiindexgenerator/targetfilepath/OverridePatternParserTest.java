package com.eberlecreative.pspiindexgenerator.targetfilepath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.eberlecreative.pspiindexgenerator.outputfilenameresolver.Appender;
import com.eberlecreative.pspiindexgenerator.outputfilenameresolver.OverridePatternParser;
import com.eberlecreative.pspiindexgenerator.util.FieldValueRepository;
import com.eberlecreative.pspiindexgenerator.util.HashMapFieldValueRepository;

public class OverridePatternParserTest {

    private OverridePatternParser testObj;
    private FieldValueRepository fieldValueRepository;
    private StringBuilder builder;
    private List<Appender> appenders;
    private String overridePattern;
    private String actualResult;
    private Exception thrownException;
    
    @Before
    public void init() {
        testObj = new OverridePatternParser();
        fieldValueRepository = new HashMapFieldValueRepository();
        fieldValueRepository.put("firstName", "John");
        fieldValueRepository.put("lastName", "Doe");
        fieldValueRepository.put("id", "000123");
        builder = new StringBuilder();
        thrownException = null;
    }

	@Test
    public void specifyReplacementPatternWorks() {
        givenOverridePattern("<firstName>_<lastName>_<id>.jpg");
        whenParseOverridePatternIsExecuted();
        thenActualResultEquals("John_Doe_000123.jpg");
    }
    
    @Test
    public void specifyReplacementPatternWorksWithHeadAndTail() {
        givenOverridePattern("_<firstName>_<lastName>_<id>_.jpg");
        whenParseOverridePatternIsExecuted();
        thenActualResultEquals("_John_Doe_000123_.jpg");
    }
    
    @Test
    public void specifyExceptionThrownWhenTokenUnknown() {
        givenOverridePattern("_<firstName>_<lastName>_<id>_<bananas>_.jpg");
        whenParseOverridePatternIsExecuted();
        thenExceptionIsThrown(RuntimeException.class, "Unable to find value for field name \"bananas\" to insert into file pattern: _<firstName>_<lastName>_<id>_<bananas>_.jpg");
    }

    private void thenExceptionIsThrown(Class<? extends Throwable> expectedType, String expectedMessage) {
        assertNotNull(thrownException);
        assertEquals(expectedType, thrownException.getClass());
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    private void thenActualResultEquals(final String expected) {
        if(thrownException != null) {
            thrownException.printStackTrace();
            assertNull(thrownException);
        }
        assertEquals(expected, actualResult);
    }

    private void whenParseOverridePatternIsExecuted() {
        try {
            appenders = testObj.parseOverridePattern(overridePattern);
            appenders.forEach(appender -> appender.append(builder, fieldValueRepository));
            actualResult = builder.toString();
        } catch(Exception e) {
            thrownException = e;
        }
    }
    
    private void givenOverridePattern(String overridePattern) {
        this.overridePattern = overridePattern;
    }
    
}
