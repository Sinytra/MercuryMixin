/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.cadixdev.mercury.mixin.annotation;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A container for data held in the {@code @At} annotation.
 *
 * @author Jadon Fowler
 * @since 0.1.0
 */
public class AtData {
    private static final Pattern DOT_REF_PATTERN = Pattern.compile("([\\w_$/]+)\\.(.*\\(.*?\\).+)");
    private static final Pattern METHOD_REF_PATTERN = Pattern.compile("^(\\(.*?\\).+)$");

    // @At(value = "", target = "")
    public static AtData from(final IAnnotationBinding binding) {
        String injectionPoint = null;
        String className = null;
        InjectTarget target = null;

        for (final IMemberValuePairBinding pair : binding.getDeclaredMemberValuePairs()) {
            if (Objects.equals("value", pair.getName())) {
                injectionPoint = (String) pair.getValue();
            }
            else if (Objects.equals("target", pair.getName())) {
                final String combined = (String) pair.getValue();

                Matcher methodDescMatcher = METHOD_REF_PATTERN.matcher(combined);
                if (methodDescMatcher.matches()) {
                    className = null;
                    target = InjectTarget.of(combined);
                    break;
                }

                final int semiIndex = combined.indexOf(';');
                if (semiIndex >= 0) {
                    className = combined.substring(1, semiIndex);
                    target = InjectTarget.of(combined.substring(semiIndex + 1));
                }
                else {
                    Matcher matcher = DOT_REF_PATTERN.matcher(combined);
                    if (matcher.matches()) {
                        className = matcher.group(1);
                        target = InjectTarget.of(matcher.group(2));
                    } else {
                        // it's just the class name, probably a NEW
                        className = combined;   
                    }
                }
            }
        }

        return new AtData(injectionPoint, className, target);
    }

    private final String injectionPoint;
    private final String className;
    private final InjectTarget target;

    public AtData(final String injectionPoint, final String className, final InjectTarget target) {
        this.injectionPoint = injectionPoint;
        this.className = className;
        this.target = target;
    }

    public String getInjectionPoint() {
        return this.injectionPoint;
    }

    public Optional<String> getClassName() {
        return Optional.ofNullable(this.className);
    }

    public Optional<InjectTarget> getTarget() {
        return Optional.ofNullable(this.target);
    }

    @Override
    public String toString() {
        return "AtData{" +
                "injectionPoint='" + this.injectionPoint + '\'' +
                ", className='" + this.className + '\'' +
                ", target=" + this.target +
                '}';
    }

}
