package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.model.matchingrules.Category
import au.com.dius.pact.model.matchingrules.DateMatcher
import au.com.dius.pact.model.matchingrules.EqualsMatcher
import au.com.dius.pact.model.matchingrules.MaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinMaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinTypeMatcher
import au.com.dius.pact.model.matchingrules.NullMatcher
import au.com.dius.pact.model.matchingrules.RegexMatcher
import au.com.dius.pact.model.matchingrules.TimeMatcher
import au.com.dius.pact.model.matchingrules.TimestampMatcher
import au.com.dius.pact.model.matchingrules.TypeMatcher
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.MatchingType

/**
 * @author Tim Ysewyn
 * @Since 2.0.0
 */
@CompileStatic
@PackageScope
class MatchingRulesConverter {

	static Category matchingRulesForBody(BodyMatchers bodyMatchers) {
		return matchingRulesFor("body", bodyMatchers)
	}

	private static Category matchingRulesFor(String categoryName, BodyMatchers bodyMatchers) {
		Category category = new Category(categoryName)
		bodyMatchers.jsonPathMatchers().forEach({ BodyMatcher it ->
			String key = getMatcherKey(it.path())
			MatchingType matchingType = it.matchingType()
			switch (matchingType) {
				case MatchingType.NULL:
					category.setRule(key, NullMatcher.INSTANCE)
					break
				case MatchingType.EQUALITY:
					category.setRule(key, EqualsMatcher.INSTANCE)
					break
				case MatchingType.TYPE:
					if (it.minTypeOccurrence() && it.maxTypeOccurrence()) {
						category.setRule(key, new MinMaxTypeMatcher(it.minTypeOccurrence(), it.maxTypeOccurrence()))
					} else if (it.minTypeOccurrence()) {
						category.setRule(key, new MinTypeMatcher(it.minTypeOccurrence()))
					} else if (it.maxTypeOccurrence()) {
						category.setRule(key, new MaxTypeMatcher(it.maxTypeOccurrence()))
					} else {
						category.setRule(key, TypeMatcher.INSTANCE)
					}
					break
				case MatchingType.DATE:
					category.setRule(key, new DateMatcher())
					break
				case MatchingType.TIME:
					category.setRule(key, new TimeMatcher())
					break
				case MatchingType.TIMESTAMP:
					category.setRule(key, new TimestampMatcher())
					break
				case MatchingType.REGEX:
					category.setRule(key, new RegexMatcher(it.value().toString()))
					break
				default:
					break
			}
		})
		return category
	}

	private static String getMatcherKey(String path) {
		return "${path.startsWith('$') ? path.substring(1) : path}"
	}

}
