# Desktop for Android
Desktop for Android is a multiwindow manager which will allow Android to provide a desktop-like experience (like MS Windows or Ubuntu). This application has been built as a showcase for the the Android multiwindow API introduced in the [Jabol](https://github.com/tieto/multiwindow_for_android/wiki/Jabol) project.

## How to build
* Build [Jabol](https://github.com/tieto/multiwindow_for_android/wiki/Jabol)
* Connect device via usb
* In Jabol source code execute:
```
mmm frameworks/base/libs/multiwindow
adb remount
adb sync
```
* In .classpath file modify path to multiwindow.jar (only put path to Jabol project)
* Import into Eclipse and build

## Features
* Expandable menu with applications
* Wallpaper
* Application icons on desktop
* Creating shortcut of application on desktop via drag and drop
* Menu bar with launched applications
* Mouse friendly
* Favourites and Frequently used application category

## Screenshots
Regular Desktop
![Regular Desktop](https://raw.githubusercontent.com/wiki/tieto/multiwindow_for_android/desktop_for_android/screen1.png?raw=true)
Expanded Menu
![Expanded menu](https://raw.githubusercontent.com/wiki/tieto/multiwindow_for_android/desktop_for_android/screen2.png?raw=true)
About Pop-up
![About popup](https://raw.githubusercontent.com/wiki/tieto/multiwindow_for_android/desktop_for_android/screen3.png?raw=true)

## Credits
Thanks to Tieto development team members:
* Wojciech Pawlica
* Krzysztof Wrotkowski

## Licence
The Project is released under the [GPLv3 Licence](https://gnu.org/licenses/gpl-3.0.txt)

## Community
You can ask questions on the mailing group: [Tieto multiwindow for android](https://groups.google.com/forum/#!forum/tieto-multiwindow-for-android)

## About Tieto
Tieto’s Product Development Services is the unique telecom expert with global presence. For 30 years we have been in the forefront of mobile and telecom technology. When you think of all the mobile calls in the world – more than half of them are possible because of us! It is Tieto – the product development services – that keeps the telecom technology in motion.

