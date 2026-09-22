// AppRuntimeSmokeTest starts the whole app - Koin graph, navigation, resources - inside the
// browser, and the start destination does real work before it settles. Mocha's 2s default is a
// unit-test budget, not enough for that on a cold CI runner, and the test then fails as a bare
// timeout that says nothing about the app. 30s is long enough to never be the reason it fails,
// and short enough that a genuine hang still ends the build rather than the job's own limit.
//
// captureConsole forwards the browser's own console into the Gradle log. Without it a failure in
// here arrives as a bare "Error" with no message, which is the least useful thing a test that
// exists to catch runtime breakage could say.
config.set({
    client: {
        captureConsole: true,
        mocha: {
            timeout: 30000
        }
    }
});
