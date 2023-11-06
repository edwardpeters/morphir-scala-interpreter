### Unit Testing Framework

Beneath this directory are two morphir-elm projects; test-framework, which provides a type (and eventually, helper functions) used to mark top-level definitions which should be treated as unit tests. unit-test-examples shows what a project using this framework could look like.

There remains a significant amount of work to be done to make this usable, organized and robust. Note that not all of this work is immediately necessary for a working release.

* The entry point (which currently just lives on the MorphirRuntime API) should be moved somewhere more in line with Damian's vision
* Test result output should be better-formatted for readability:
    * Failed/passed tests should be color-coded as red/green
    * Nested test suites should indent properly
    * Summaries - # of tests run/passed/ignored - should be printed at the end of each suite, or maybe just at the end of the test process overall.
    * Output values should be as human-readable as possible, rather than showing the very bulky RTValue format
        * This might be cause to write a "PrintIR" variant for RTValues specifically. 
    * Introspection should be better-organized with good extractors for identifying "Assert (<actual> == <expected>)" cases
    * Additional cases for introspection/improved printing should be covered
* Additional "Test" variants should be exposed from test-framework:
    * Some sort of "Suite" construct should be available
    * Some sort of "Ignore" construct should be available, for tests users want to keep but temporarily disable
    * Users should be able to attach custom failure messages to tests, when they don't want the introspection to provide the description
    * Users should be able to manually name tests rather than having the system always default to the FQName. (Tests within a suite might require this)
    * Helper functions might be wanted, rather than directly calling the Test constructors
    * NOTE: All of the above will require additional introspection code to support them
* (Reach goal) the test-framework project should be removed, and folded into native morphir-elm so taht people do not need to have a local copy if they want to write tests.