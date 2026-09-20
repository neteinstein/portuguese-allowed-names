// Compose Multiplatform's skiko.mjs builds its exports dynamically, so webpack's static ESM
// analysis reports `export 'skikoApi' was not found` for the import Kotlin/Wasm generates. That
// is only a false positive, but webpack treats a missing export as an *error* (failing the
// build) as soon as another real ESM package - pdf.js, in core:parser - is in the bundle.
// Downgrading exportsPresence to a warning keeps that check on for genuinely missing exports
// while letting skiko's dynamic ones through.
config.module = config.module || {};
config.module.parser = config.module.parser || {};
config.module.parser.javascript = Object.assign({}, config.module.parser.javascript, {
    exportsPresence: "warn",
});
