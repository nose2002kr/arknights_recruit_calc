# arknights_calc

Arknights, Recruitment calculator

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Lab: Write your first Flutter app](https://docs.flutter.dev/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://docs.flutter.dev/cookbook)

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev/), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

# Requirements.
You need Flutter for Android and Rust, Flutter Rust Bridge.
You can install them as follows.
 - Flutter: https://docs.flutter.dev/get-started/install/windows/mobile
   You need to be able to run `flutter --version` and `flutter doctor` says all is ok.
 - Android SDK, I recommend AndroidStudio: https://developer.android.com/studio/install?hl=ko#windows
 - Rust: https://www.rust-lang.org/tools/install
   You need to be able to run `cargo --version`.
 - Flutter Rust Bridge: https://cjycode.com/flutter_rust_bridge/quickstart
   You need to be able to run `flutter_rust_bridge_codegen --version`.
   To update function symbol `flutter_rust_bridge_codegen generate -r rust/src/api/**/*.rs -d lib/src/rust/`

