<h1 align="center">
  <a href="https://github.com/thanhcuong1990/react-native-photo-view-next">
    View and Zoom images for React Native
  </a>
</h1>

<p align="center">
  <a href="https://github.com/thanhcuong1990/react-native-photo-view-next/blob/master/LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="React Native SDWebImage is released under the MIT license." />
  </a>
  <a href="https://www.npmjs.com/package/react-native-photo-view-next">
    <img src="https://badge.fury.io/js/react-native-photo-view-next.svg" alt="Current npm package version." />
  </a>
    <a href="https://npm.im/react-native-photo-view-next">
    <img src="https://img.shields.io/npm/dm/react-native-photo-view-next.svg" alt="Current npm package download in last month." />
  </a>
  <a href="https://github.com/thanhcuong1990/react-native-photo-view-next/pulls">
    <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg" alt="PRs welcome!" />
  </a>
</p>


This project is the next generate of [react-native-photo-view](https://github.com/alwx/react-native-photo-view).
Provides custom Image view for React Native that allows to perform
pinch-to-zoom on images. Works on both iOS and Android.

This component uses [PhotoDraweeView](https://github.com/ongakuer/PhotoDraweeView) for Android and [MWPhotobrowser](https://github.com/mwaterfall/MWPhotoBrowser) on iOS.

## Installation

With npm:

```console
npm install react-native-photo-view-next
```

With Yarn:
```console
yarn add react-native-photo-view-next
```

## Usage

```javascript
import PhotoView from 'react-native-photo-view-next';
```

Basics:
```javascript
<PhotoView
  source={{uri: 'https://reactjs.org/logo-og.png'}}
  minimumZoomScale={0.5}
  maximumZoomScale={3}
  androidScaleType="center"
  onLoad={() => console.log("Image loaded!")}
  style={{width: 300, height: 300}} />
```

## Properties

| Property | Type | Description |
|-----------------|----------|--------------------------------------------------------------|
| source | Object | same as source for other React images |
| loadingIndicatorSource | Object | source for loading indicator |
| fadeDuration | int | duration of image fade (in ms) |
| minimumZoomScale | float | The minimum allowed zoom scale. The default value is 1.0 |
| maximumZoomScale | float | The maximum allowed zoom scale. The default value is 3.0 |
| showsHorizontalScrollIndicator | bool | **iOS only**: When true, shows a horizontal scroll indicator. The default value is true. |
| showsVerticalScrollIndicator | bool | **iOS only**: When true, shows a vertical scroll indicator. The default value is true. |
| scale | float | Set zoom scale programmatically |
androidZoomTransitionDuration | int | **Android only**: Double-tap zoom transition duration |
| androidScaleType | String | **Android only**: One of the default *Android* scale types: "center", "centerCrop", "centerInside", "fitCenter", "fitStart", "fitEnd", "fitXY" |
| onLoadStart | func | Callback function |
| onLoad | func | Callback function |
| onLoadEnd | func | Callback function |
| onProgress | func | **iOS only**: Callback function, invoked on download progress with {nativeEvent: {loaded, total}}. |
| onTap | func | Callback function (called on image tap) |
| onViewTap | func | Callback function (called on tap outside of image) |
| onScale | func | Callback function |
