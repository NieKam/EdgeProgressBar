# EdgeProgressBar

[![GitHub license](https://img.shields.io/badge/License-Apache-green.svg)](https://github.com/NieKam/EdgeProgressBar/blob/master/LICENSE)

[![Version](https://img.shields.io/badge/Version-1.0-blue.svg)](https://jitpack.io/#NieKam/EdgeProgressBar/1.0.0)

# Gradle Dependency

### Repository

The Gradle dependency is available via JitPack.

The minimum API level supported by this library is API 17.

```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
```gradle
dependencies {
  implementation 'com.github.NieKam:EdgeProgressBar:1.0.0'
}
```
# Info

EdgeProgressBar can work in two modes:
- #### Normal progress bar

This mode is default one. Progress will be drawn around the screen edges. You use following attributes:

| Attribute name    | Type | Description |
| -------------   | ------------- | -------------|
| progress_color  |  color  | Content Cell  |
| tint_color  | color  | Content Cell  |
| line_width  | dimmension  | Content Cell  |
| max  | integer  | Content Cell  |
| start_progress  | float  | Content Cell  |
| progress_anim_duration  | integer  | Content Cell  |


<img src="https://github.com/NieKam/EdgeProgressBar/blob/master/screenshots/Screenshot_1526048947.png" width="25%" height="25%">


`progress_color` is orange 
`tint_color` is yellow

- #### Indeterminate

To use this mode you need to add attribute 

`indeterminate="true"`

In indeterminate mode you can choose between following effects:

1. ZizZag

<img src="https://github.com/NieKam/EdgeProgressBar/blob/master/screenshots/zigzag.gif" width="25%" height="25%">

2. Dash

<img src="https://github.com/NieKam/EdgeProgressBar/blob/master/screenshots/dash.gif" width="25%" height="25%">

3. Snake

<img src="https://github.com/NieKam/EdgeProgressBar/blob/master/screenshots/snake.gif" width="25%" height="25%">

4. Glow

<img src="https://github.com/NieKam/EdgeProgressBar/blob/master/screenshots/glow.gif" width="25%" height="25%">


Indeterminate progress could use following attributes:

| Attribute name    | Type | Description |
| -------------   | ------------- | -------------|
| progress_color  |  color  | Content Cell  |
| tint_color  | color  | Content Cell  |
| line_width  | dimmension  | Content Cell  |
| indeterminate  | boolean  | Content Cell  |
| indeterminate_type  | enum  | Content Cell  |

# Usage

### XML layout

```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >
  ...
  
  <com.niekam.edgeprogressbar.EdgeProgressBar
      android:id="@+id/edgeProgress"
      android:layout_width="0dp"
      android:layout_height="0dp"
      app:layout_constraintStart_toStartOf="parent"
      app:layout_constraintEnd_toEndOf="parent"
      app:layout_constraintTop_toTopOf="parent"
      app:layout_constraintBottom_toBottomOf="parent"
      />
  
  </android.support.constraint.ConstraintLayout>
```

in progress..
