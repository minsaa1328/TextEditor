Android Kotlin Text Editor
Project Overview

This project is an Android Text Editor designed for the development and testing of Kotlin code directly on Android devices. The application includes core text editor features like file management, basic text editing, and syntax highlighting for Kotlin code. It also integrates with a Kotlin compiler using ADB for real-time code compilation and error reporting.

Features
1. Basic Editor Functionality

File Operations:

Open, Save, and New file functionality.

Supports file extensions: .txt, .kt, .java.

Auto-save feature to prevent data loss.

Text Editing:

Copy, Paste, Cut, Undo, Redo operations.

Character & Word Counting:

Display character and word count in the status bar.

Find and Replace:

Supports whole word and case-sensitive search options.

2. Syntax Highlighting

Default Kotlin Syntax Highlighting:

Syntax highlighting for keywords, comments, strings, and other Kotlin code elements.

Configurable Syntax Highlighting:

Supports syntax highlighting for multiple languages (e.g., Java, Python).

Users can define language-specific keywords, comments, and strings via configuration files (JSON/XML).

3. Compiler Integration

Kotlin Compiler via ADB:

Compile Kotlin code directly within the editor.

Connects to a Kotlin compiler running on a desktop machine using ADB.

Displays compilation errors in real-time with highlighted errors in the editor.

4. Error Handling

Displays clear error messages for syntax and compilation issues.

Compilation status indicators to show success or failure.

Technologies Used

Programming Language: Kotlin

ADB (Android Debug Bridge): Used for compiler integration.

Syntax Highlighting: Custom implementation for Kotlin and other languages.
