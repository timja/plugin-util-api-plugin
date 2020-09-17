package io.jenkins.plugins.util;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import jenkins.model.Jenkins;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Defines several architecture rules that should be enforced for every Jenkins plugin.
 *
 * @author Ullrich Hafner
 */
public final class PluginArchitectureRules {
    /**
     * Direct calls to {@link Jenkins#getInstance()} or {@link Jenkins#getInstanceOrNull()}} are prohibited since these
     * methods require a running Jenkins instance. Otherwise the accessor of this method cannot be unit tested. Create a
     * new {@link JenkinsFacade} object to access the running Jenkins instance. If your required method is missing you
     * need to add it to {@link JenkinsFacade}.
     */
    public static final ArchRule NO_JENKINS_INSTANCE_CALL =
            noClasses().that().doNotHaveSimpleName("JenkinsFacade")
                    .should().callMethod(Jenkins.class, "getInstance")
                    .orShould().callMethod(Jenkins.class, "getInstanceOrNull")
                    .orShould().callMethod(Jenkins.class, "getActiveInstance")
                    .orShould().callMethod(Jenkins.class, "get");

    /** Junit 5 test classes should not be public. */
    public static final ArchRule NO_PUBLIC_TEST_CLASSES =
            noClasses().that().haveSimpleNameEndingWith("Test")
                    .and().haveSimpleNameNotContaining("_jmh")
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotEndingWith("ITest")
                    .should().bePublic();

    /** Some packages that are transitive dependencies of Jenkins should not be used at all. */
    public static final ArchRule NO_FORBIDDEN_PACKAGE_ACCESSED
            = noClasses().should().dependOnClassesThat(resideInAnyPackage(
            "org.apache.commons.lang..",
            "org.joda.time..",
            "javax.xml.bind..",
            "net.jcip.annotations..",
            "javax.annotation..",
            "junit..",
            "org.hamcrest..",
            "com.google.common.."));

    /**
     * Methods that are used as AJAX end points must be in public classes.
     */
    public static final ArchRule AJAX_PROXY_METHOD_MUST_BE_IN_PUBLIC_CLASS =
            methods().that().areAnnotatedWith(JavaScriptMethod.class)
                    .should().bePublic()
                    .andShould().beDeclaredInClassesThat().arePublic();

    /**
     * Methods that use data binding must be in public classes.
     */
    public static final ArchRule DATA_BOUND_CONSTRUCTOR_MUST_BE_IN_PUBLIC_CLASS =
            constructors().that().areAnnotatedWith(DataBoundConstructor.class)
                    .should().beDeclaredInClassesThat().arePublic();

    /**
     * Methods that use data binding must be in public classes.
     */
    public static final ArchRule DATA_BOUND_SETTER_MUST_BE_IN_PUBLIC_CLASS =
            methods().that().areAnnotatedWith(DataBoundSetter.class)
                    .should().beDeclaredInClassesThat().arePublic();

    /** Ensures that the {@code readResolve} methods are protected so sub classes can call the parent method. */
    @ArchTest
    public static final ArchRule READ_RESOLVE_SHOULD_BE_PROTECTED =
            methods().that().haveName("readResolve").and().haveRawReturnType(Object.class)
                    .should().beProtected();

    private PluginArchitectureRules() {
        // prevents instantiation
    }
}
