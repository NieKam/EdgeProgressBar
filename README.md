# EdgeProgressBar

[![GitHub license](https://img.shields.io/badge/License-Apache-green.svg)](https://github.com/NieKam/EdgeProgressBar/blob/master/LICENSE)

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

### Attributes

| Attribute name    | Type | Description |
| -------------   | ------------- | -------------|
| progress_color  |  color  | Content Cell  |
| tint_color  | color  | Content Cell  |
| line_width  | dimmension  | Content Cell  |
| indeterminate  | boolean  | Content Cell  |
| max  | integer  | Content Cell  |
| start_progress  | float  | Content Cell  |
| progress_anim_duration  | integer  | Content Cell  |
| indeterminate_type  | enum  | Content Cell  |


in progress..
