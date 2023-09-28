# react-native-pdf

[![NPM version][npm-image]][npm-url]

## This is a fork which I made because original project seems unmaintained and I needed it to work with newer React-Native versions. There are also quite a few improvements, such as performance increase, bug fixes, stability improvement and new-functionality

## Purpose

This library's purpose is to fill the gap that currently exists in the React Native ecosystem for PDF creation and editing. It aims to provide an easy, simple, and consistent API for both **creating** new and **editing** existing PDF documents in React Native.

### Compatibility matrix

| pdf-lib | React Native | Expo  | Hermes | npm  | node  |
| ------- | ------------ | ----- | ------ | ---- | ----- |
| 1.1.5   | >= 0.69.0    | >= 45 | ✅     | >= 7 | >= 13 |
| 1.1.4   | >= 0.66.0    | >= 45 | ✅     | >= 7 | >= 13 |

## Thanks

This library would not be possible without the following projects:

- https://github.com/galkahana/PDF-Writer is used for handling PDFs on iOS. (The static binaries for v3.6 are built with [hummus-ios-build](https://github.com/Hopding/hummus-ios-build)).
- https://github.com/TomRoush/PdfBox-Android is used for handling PDFs on Android.

## Alternatives

Create PDFs from HTML: https://github.com/christopherdro/react-native-html-to-pdf

## Requirements

### React Native Version

This library supports React Native versions greater or equal to 0.60.0
Older may work, but I do not aim to support them.
If you see any issue and you're using an old React Native version, plase upgrade and try again.

### Adnroid API and iOS version

This library supports Android devices >= API 31, and iOS devices >= iOS 8.0.
Latest version that supported Android API 18 was 1.0.3

## Mostly automatic installation

See [here](#manual-installation) for manual installation instructions (manual installation should not be necessary).

1. `$ npm install @shogobg/react-native-pdf --save`
2. `$ react-native link @shogobg/react-native-pdf`
3. For Android, add the following to your app's `build.gradle` file:
   ```
   android {
     ...
     dexOptions {
         jumboMode = true
     }
     ...
   }
   ```

## Getting started

### Create a New PDF Document

```javascript
import PDFLib, { PDFDocument, PDFPage } from '@shogobg/react-native-pdf';

// Create a PDF page with text and rectangles
const page1 = PDFPage
  .create()
  .setMediaBox(200, 200)
  .drawText('You can add text and rectangles to the PDF!', {
    x: 5,
    y: 235,
    color: '#007386',
  })
  .drawRectangle({
    x: 25,
    y: 25,
    width: 150,
    height: 150,
    color: '#FF99CC',
  })
  .drawRectangle({
    x: 75,
    y: 75,
    width: 50,
    height: 50,
    color: '#99FFCC',
  });

// Create a PDF page with text and images
const jpgPath = // Path to a JPG image on the file system...
const pngPath = // Path to a PNG image on the file system...
const page2 = PDFPage
  .create()
  .setMediaBox(250, 250)
  .drawText('You can add JPG images too!')
  .drawImage(jpgPath, 'jpg', {
     x: 5,
     y: 125,
     width: 200,
     height: 100,
  })
  .drawImage(pngPath, 'png', {
     x: 5,
     y: 25,
     width: 200,
     height: 100,
  });

// Create a new PDF in your app's private Documents directory
const docsDir = await PDFLib.getDocumentsDirectory();
const pdfPath = `${docsDir}/sample.pdf`;
PDFDocument
  .create(pdfPath)
  .addPages(page1, page2)
  .write() // Returns a promise that resolves with the PDF's path
  .then(path => {
    console.log('PDF created at: ' + path);
    // Do stuff with your shiny new PDF!
  });
```

### Modify an Existing PDF Document

```javascript
import PDFLib, { PDFDocument, PDFPage } from '@shogobg/react-native-pdf';

// Modify first page in document
const page1 = PDFPage
  .modify(0)
  .drawText('This is a modification on the first page!', {
    x: 5,
    y: 235,
    color: '#F62727',
  })
  .drawRectangle({
    x: 150,
    y: 150,
    width: 50,
    height: 50,
    color: '#81C744',
  });

// Modify second page in document
const jpgPath = // in iOS Path to a JPG image on the file system... in Android path to the assert
const pngPath = // in iOS Path to a PNG image on the file system... in Android path to the assert
const page2 = PDFPage
  .modify(1)
  .drawText('You can add images to modified pages too!')
  .drawImage(
    jpgPath,
    'jpg',
    {
      x: 5,
      y: 125,
      width: 200,
      height: 100,
      source: 'assets' // 'assets' to get image from Android assets 'path' to get image from imagePath
    }
  )
  .drawImage(
    pngPath,
    'png',
    {
      x: 5,
      y: 25,
      width: 200,
      height: 100,
      source: 'path' // 'assets' to get image from Android assets 'path' to get image from imagePath
    }
   );

// Create a PDF page to add to document
const page3 = PDFPage
  .create()
  .setMediaBox(200, 200)
  .drawText('You can add new pages to a modified PDF as well!', {
    x: 5,
    y: 235,
    color: '#007386',
  });

const existingPDF = 'path/to/existing.pdf';
PDFDocument
  .modify(existingPDF)
  .modifyPages(page1, page2)
  .addPage(page3)
  .write() // Returns a promise that resolves with the PDF's path
  .then(path => {
    console.log('PDF modified at: ' + path);
  });
```

### Using custom fonts

The library includes Times New Roman as the default font. For using other fonts, you must include any of them like this.

1. If you dont already have some folder for your assets, create one ('./assets/fonts' for example)
2. Ensure your TTF file is named the same as the internal font "Full Name" so it works both on iOS and Android (more on this here https://medium.com/react-native-training/react-native-custom-fonts-ccc9aacf9e5e)
3. Copy the TTF file on the ./assets/fonts folder
4. Edit your package.json and tell react-native about your new assets folder like this:

```json
  "rnpm": {
    "assets": [
      "./assets/fonts"
    ]
  }
```

5. Run `react-native link` (so the font will be bundled with your app's assets).

This way, you could start using your shiny custom font on your PDF's like this:

```javascript
const page1 = PDFPage.create()
  .setMediaBox(200, 200)
  .drawText("This text is using the font Franklin Gothic Medium!", {
    x: 5,
    y: 235,
    color: "#F62727",
    fontName: "Franklin Gothic Medium",
  });
```

### Centering text

```JavaScript
const page1 = PDFPage.create()
  .setMediaBox(200, 200)
  .drawText("This text is using the font Franklin Gothic Medium!", {
      x: 5,
      y: 235,
      fieldSize: 240,
      textAlign: "center",
      color: textColor,
      fontName: 'MSMincho',
    })
```

```JavaScript
const page1 = PDFPage.create()
  .setMediaBox(200, 200)
  .drawText("This text is using the font Franklin Gothic Medium!", {
      x: 5,
      y: 235,
      fieldSize: 240,
      textAlign: "right",
      color: textColor,
      fontName: 'MSMincho',
    })
```

### Measuring text

If you need to know the text's size for some reason.

```javascript
return PDFLib.measureText(
  "My Centered Title",
  "Franklin Gothic Medium",
  14
).then((result) => {
  console.log("The text size is: ", result);
});
```

### Copying page from one (or multiple) PDF into another

```javascript
const filePath1 = "./myPDF1.pdf";
const filePath2 = "./myPDF2.pdf";

const pageNumber = 0;

const copiedPage = PDFPage.loadFromFile(filePath1, pageNumber);
const copiedPage2 = PDFPage.loadFromFile(filePath2, 2);

const finalPath = await PDFDocument.create(
  `${RNFS.TemporaryDirectoryPath}/Final.pdf`
)
  .loadPages(copiedPage, copiedPage2)
  .write();
```

## Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `@shogobg/react-native-pdf` and add `RNPdfLib.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNPdfLib.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`

- Add `import com.reactlibrary.PdfLibPackage;` to the imports at the top of the file
- Add `new PdfLibPackage()` to the list returned by the `getPackages()` method

2. Append the following lines to `android/settings.gradle`:
   ```
   include ':react-native-pdf-lib'
   project(':react-native-pdf-lib').projectDir = new File(rootProject.projectDir, 	'../node_modules/@shogobg/react-native-pdf/android')
   ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
   ```
     compile project(':react-native-pdf-lib')
   ```
4. For Android, add the following to your app's `build.gradle` file:
   ```
   android {
     ...
     // Add this section:
     dexOptions {
         jumboMode = true
     }
     ...
   }
   ```

## Development

It is not possible to test changes to a native library independently of a runnable project.
This library (and any react-native library with native code) depends on modules that will only exist if the library is built in the context of a full, runnable project.
To test any changes, create a new react native project, and then clone the repository for @shogobg/react-native-pdf into that project's node_modules directory. Next run react-native link on that project. Then, when you make changes to the library's code, you should be able to rebuild the runnable project to see the results.

[npm-image]: https://img.shields.io/npm/v/@shogobg/react-native-pdf.svg?style=flat-square
[npm-url]: https://www.npmjs.com/package/@shogobg/react-native-pdf
