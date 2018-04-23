
package com.openexchange.ajax.mail.filter.tests.api;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Keep;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.comparison.ContainsComparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.MatchesComparison;
import com.openexchange.ajax.mail.filter.api.dao.test.AddressTest;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;

public class UpdateTest extends AbstractMailFilterTest {

    private Rule rule;

    public UpdateTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        rule = new Rule();
        rule.setName("testUpdate");
        rule.addAction(new Keep());

        final IsComparison isComp = new IsComparison();
        rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue" }));

        // Create the rule
        final int id = mailFilterAPI.createRule(rule);
        rememberRule(id);
        rule.setId(id);
    }

    /**
     * Tests a simple update name of the rule
     */
    @Test
    public void testUpdate() throws Exception {
        rule.setName("testUpdate - 2");

        // Update the rule
        mailFilterAPI.updateRule(rule);
        rule.setPosition(0);

        // Assert
        getAndAssert(Collections.singletonList(rule));
    }

    /**
     * Tests a condition update of the rule
     */
    @Test
    public void testUpdateCondition() throws Exception {
        // update condition
        rule.setTest(new HeaderTest(new ContainsComparison(), new String[] { "updatedHeader" }, new String[] { "updatedValue" }));
        mailFilterAPI.updateRule(rule);
        rule.setPosition(0);

        // assert
        getAndAssert(Collections.singletonList(rule));
    }

    /**
     * Test add an action
     */
    @Test
    public void testUpdateAddActionCommand() throws Exception {
        rule.addAction(new Stop());
        rule.setPosition(0);

        // Update
        mailFilterAPI.updateRule(rule);

        // Assert
        getAndAssert(Collections.singletonList(rule));
    }

    /**
     * Test update test command
     */
    @Test
    public void testUpdateTestCommand() throws Exception {
        AddressTest addressTest = new AddressTest(new MatchesComparison(), new String[] { "matchesHeader" }, new String[] { "matchesValue1", "matchesValue2" });
        rule.setTest(addressTest);

        // Update
        mailFilterAPI.updateRule(rule);
        rule.setPosition(0);

        // Assert
        getAndAssert(Collections.singletonList(rule));
    }
}
