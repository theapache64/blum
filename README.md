# blum

A simple java program to extract package name from an android project directory

# Why did i make it ?

Because, typing the package name of an APK in the `adb` console was waste of time. I mean, for example, if i wanted to uninstall my project's application from my android device, i had to type `adb uninstall com.this.is.my.package.name`. But with `blum`, i can turn `adb uninstall com.this.is.my.package.name` to `purge`. 

# How does it work ?

Well, when you type `purge` or any of your custom command, `blum` checks if the current directory is an android project dir, and if yes, it'll go for the `build.gradle`, and will fetch the package name automatically and will merge with your custom command. Here, purge is just one of my custom commands. 

#more-docs-coming-soon
