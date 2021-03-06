/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.osgi.obr;

import java.text.ParseException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.ivy.osgi.obr.filter.AndFilter;
import org.apache.ivy.osgi.obr.filter.CompareFilter;
import org.apache.ivy.osgi.obr.filter.CompareFilter.Operator;
import org.apache.ivy.osgi.obr.filter.NotFilter;
import org.apache.ivy.osgi.obr.filter.RequirementFilterParser;
import org.apache.ivy.osgi.obr.xml.RequirementFilter;

public class RequirementFilterTest extends TestCase {

    public void testParser() throws Exception {
        assertParseFail("c>2");
        assertParseFail("");
        assertParseFail(")");
        RequirementFilter cgt2 = new CompareFilter("c", Operator.GREATER_THAN, "2");
        checkParse(cgt2, "(c>2)");
        RequirementFilter twoeqd = new CompareFilter("2", Operator.EQUALS, "d");
        checkParse(twoeqd, "(2=d)");
        RequirementFilter foodotbarge0dot0 = new CompareFilter("foo.bar",
                Operator.GREATER_OR_EQUAL, "0.0");
        checkParse(foodotbarge0dot0, "(foo.bar>=0.0)");
        RequirementFilter and = new AndFilter(new RequirementFilter[] {foodotbarge0dot0});
        checkParse(and, "(&(foo.bar>=0.0))");
        RequirementFilter and2 = new AndFilter(new RequirementFilter[] {cgt2, twoeqd,
                foodotbarge0dot0});
        checkParse(and2, "(&(c>2)(2=d)(foo.bar>=0.0))");
        RequirementFilter spaceAfterAnd = new AndFilter(new RequirementFilter[] {twoeqd});
        checkParse(spaceAfterAnd, "(& (2=d))");

        RequirementFilter version350 = new CompareFilter("version", Operator.GREATER_OR_EQUAL,
                "3.5.0");
        RequirementFilter version400 = new CompareFilter("version", Operator.GREATER_OR_EQUAL,
                "4.0.0");
        RequirementFilter notVersion400 = new NotFilter(version400);
        RequirementFilter bundle = new CompareFilter("bundle", Operator.EQUALS,
                "org.eclipse.core.runtime");
        RequirementFilter andEverythingWithSpace = new AndFilter(new RequirementFilter[] {
                version350, notVersion400, bundle});
        checkParse(andEverythingWithSpace,
            "(&     (version>=3.5.0)     (!(version>=4.0.0))     (bundle=org.eclipse.core.runtime)    )");
    }

    private void assertParseFail(String toParse) {
        try {
            RequirementFilterParser.parse(toParse);
            Assert.fail("Expecting a ParseException");
        } catch (ParseException e) {
            // OK
        }
    }

    private void checkParse(RequirementFilter expected, String toParse) throws ParseException {
        RequirementFilter parsed = RequirementFilterParser.parse(toParse);
        Assert.assertEquals(expected, parsed);
    }
}
