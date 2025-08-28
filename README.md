#Android Kotlin Text Editor
##Project Overview

This project is an Android Text Editor designed for the development and testing of Kotlin code directly on Android devices. The application includes core text editor features like file management, basic text editing, and syntax highlighting for Kotlin code. It also integrates with a Kotlin compiler using ADB for real-time code compilation and error reporting.

###Features
##1. Basic Editor Functionality

###File Operations:

Open, Save, and New file functionality.

Supports file extensions: .txt, .kt, .java.

Auto-save feature to prevent data loss.

###Text Editing:

Copy, Paste, Cut, Undo, Redo operations.

###Character & Word Counting:

Display character and word count in the status bar.

###Find and Replace:

Supports whole word and case-sensitive search options.

##2. Syntax Highlighting

###Default Kotlin Syntax Highlighting:

Syntax highlighting for keywords, comments, strings, and other Kotlin code elements.

###Configurable Syntax Highlighting:

Supports syntax highlighting for multiple languages (e.g., Java, Python).

Users can define language-specific keywords, comments, and strings via configuration files (JSON/XML).

##3. Compiler Integration

###Kotlin Compiler via ADB:

Compile Kotlin code directly within the editor.

Connects to a Kotlin compiler running on a desktop machine using ADB.

Displays compilation errors in real-time with highlighted errors in the editor.

##4. Error Handling

Displays clear error messages for syntax and compilation issues.

Compilation status indicators to show success or failure.

###Technologies Used

Programming Language: Kotlin

ADB (Android Debug Bridge): Used for compiler integration.

Syntax Highlighting: Custom implementation for Kotlin and other languages.


Installation
Prerequisites

Before running the app and setting up the Kotlin compiler integration, make sure the following tools are installed:

Android Studio (or any other Android development environment).

Android device or emulator running Android 9 (Pie) or above.

Kotlin compiler installed on the desktop machine.

Java 8 installed on your system.

ADB (Android Debug Bridge) installed.

You will need to specify the paths to these tools in the compile_listener.bat file, as outlined below.

Steps
1. Clone the Repository:

First, clone this repository to your local machine:

git clone https://github.com/your-username/android-kotlin-text-editor.git

2. Open the Project in Android Studio:

Open the project folder in Android Studio.

3. Connect Your Device:

Connect your Android device via USB or use an Android emulator.

4. Specify Paths in compile_listener.bat:

The compile_listener.bat file in the ADB_config folder needs the paths to ADB, Kotlin Compiler, and Java to be correctly set.

Navigate to the ADB_config folder inside the root directory of this project:

cd android-kotlin-text-editor/ADB_config


Open the compile_listener.bat file in a text editor and update the following paths:

ADB Path:
Set the ADB path to your local installation of Android SDK. For example:

set "ADB_EXE=C:\Users\<Your-Username>\AppData\Local\Android\Sdk\platform-tools\adb.exe"


Kotlin Compiler Path:
Set the Kotlin Compiler path to where kotlinc.bat is installed. For example:

set "KOTLINC_EXE=C:\kotlinc\bin\kotlinc.bat"


Java Path:
Set the Java path to your Java installation. For example:

set "JAVA_EXE=C:\Program Files (x86)\Common Files\Oracle\Java\java8path\java.exe"


Device Directory:
The directory where the app will save files on the device. This is typically the path for your app's internal storage:

set "DEVICE_DIR=/sdcard/Android/data/com.example.kotlintexteditor/files"

5. Set Environment Variables:

You will need to set the environment variables to ensure the Kotlin and Java executables can be accessed globally from the terminal.

For Windows:

Right-click This PC or Computer and select Properties.

Click Advanced system settings.

Click Environment Variables.

Under System variables, find Path and click Edit.

Add the following paths (replace with your actual paths):

For Kotlin:

C:\kotlinc\bin


For Java:

C:\Program Files (x86)\Common Files\Oracle\Java\java8path


For ADB (optional):

C:\Users\<Your-Username>\AppData\Local\Android\Sdk\platform-tools


For Mac/Linux:

Open a terminal and edit the .bash_profile or .bashrc (for bash) or .zshrc (for zsh) file:

nano ~/.bash_profile  # or ~/.bashrc or ~/.zshrc depending on the shell


Add the following lines:

export KOTLINC_HOME="/path/to/kotlinc"
export JAVA_HOME="/path/to/java"
export PATH=$PATH:$KOTLINC_HOME/bin:$JAVA_HOME/bin


Save and close the file, then run:

source ~/.bash_profile  # or ~/.bashrc or ~/.zshrc

6. Run the Kotlin Compiler Listener:

Once you've set the paths and environment variables, you can run the compile_listener.bat file:

On Windows:

In the ADB_config folder, double-click the compile_listener.bat file to start the Kotlin compiler listener. This will allow the app to communicate with the compiler on your desktop machine via ADB.

On Mac/Linux:

Run the following command in the terminal:

./compile_listener.sh


(Ensure the script is executable. If not, run chmod +x compile_listener.sh to make it executable.)

7. Build and Run the App:

After starting the Kotlin compiler listener, go back to Android Studio and click Run to build and launch the app on your Android device or emulator.

8. Compile Code from the App:

Once the app is running, you can write Kotlin code and press the Compile button. The app will trigger the Kotlin compiler on your desktop machine through the ADB connection, and the output will be displayed in the app.

Troubleshooting

Error: ADB Connection Failed:

Ensure that your Android device is connected via USB and USB debugging is enabled in the developer options.

Check if the compile_listener.bat or compile_listener.sh is running properly on your desktop machine.

Error: Compilation Failed:

Check if there are any issues with the Kotlin code you are trying to compile. The error messages will be shown in the app’s editor with line numbers.
