# Problem To Solve
* The clock only show up in the Gravity_Bottom Video Case, while work prefect in the ImageView.
* Small Memory Leak. 
```Kotlin
// Have found the real reason that cause 'A resource failed to call release. '
// I don't know why this will cause MemoryLeak, but I think there is no need to fix it
val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

pickerLauncher.launch(intent)
```
* Add interesting element and control method

# NewFeature
* add settingFragment and easy to append new settingItem
* add EnableClock setting
* add ClockColor setting
* adjust the layout in recyclerView.itemView


# Basic Info
* This is a project for android developing
* To build a app that I'm interested
* Can run on API 31 and above

# Notice
* this more like a learning note, and I do wish free to use But I used the font from
```
Sizenko Alexander
Style-7
http://www.styleseven.com
Created: October 7 2008
```
Please use this project only in educational way.

# Author
```
LinMuBinary 13/9/2024
```