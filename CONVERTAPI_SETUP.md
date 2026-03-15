# ConvertAPI Integration Setup

## Overview
The OmniConverter app now includes ConvertAPI integration as a fallback for conversions that cannot be performed locally. This allows the app to convert Word documents, Excel files, and other complex formats to PDF using cloud processing.

## Setup Instructions

### 1. Get ConvertAPI Credentials
1. Visit [ConvertAPI](https://www.convertapi.com/)
2. Sign up for a free account
3. Get your **Secret Key** from the dashboard
4. Copy the secret key

### 2. Configure the App
1. Open `app/src/main/java/com/omniconverter/app/core/AppConfig.java`
2. Replace `"your_convertapi_secret_here"` with your actual secret key:
   ```java
   public static final String CONVERT_API_SECRET = "your_actual_secret_key_here";
   ```

### 3. Test the Integration
1. Build and install the app
2. Select "Word to PDF" converter
3. Pick a Word document (.docx, .doc)
4. The app will upload to ConvertAPI and download the converted PDF
5. Check History tab for the result

## Supported Conversions

### Local Conversions (No API Key Required)
- Image format conversion (JPG ↔ PNG ↔ WebP ↔ BMP)
- MP4 to MP3 audio extraction
- PDF merging (basic concatenation)

### Cloud Conversions (ConvertAPI Required)
- Word to PDF (.docx, .doc)
- Excel to PDF (.xlsx, .xls)
- PowerPoint to PDF (.pptx, .ppt)
- HTML to PDF
- And 100+ other formats (see ConvertAPI docs)

## How It Works

### Fallback Flow
```
1. User selects converter
2. App tries local conversion first
3. If local fails → automatically uses ConvertAPI
4. File uploaded to ConvertAPI servers
5. Cloud conversion performed
6. Converted file downloaded
7. Saved to device storage
8. Notification shown to user
```

### API Usage
- **Upload**: Files are uploaded to ConvertAPI's secure storage
- **Convert**: Cloud processing converts to target format
- **Download**: Converted files are downloaded and saved locally
- **Cost**: Each conversion consumes credits from your ConvertAPI account

## Error Handling

### Common Issues
1. **"Please check your ConvertAPI secret key"**
   - Solution: Verify the secret key in AppConfig.java

2. **"Conversion failed with code: 401"**
   - Solution: Check if secret key is correct and account is active

3. **"No internet connection"**
   - Solution: Ensure device has internet access

4. **"File type not supported"**
   - Solution: Check ConvertAPI documentation for supported formats

## Security Notes

- Files are uploaded to ConvertAPI's encrypted servers
- Files are automatically deleted after 3 hours
- No files are stored permanently on external servers
- All API calls use HTTPS encryption

## Cost Information

- ConvertAPI offers a free tier with limited conversions
- Paid plans available for higher usage
- Each conversion consumes credits based on file size and complexity
- Check [ConvertAPI pricing](https://www.convertapi.com/pricing) for details

## Alternative Setup (Optional)

If you prefer not to use ConvertAPI, you can:
1. Comment out the ConvertAPI calls in WordToPDFConverter
2. The app will show "conversion not supported" messages instead
3. All local conversions will continue to work normally

## Troubleshooting

### Build Issues
- Ensure INTERNET permission is in AndroidManifest.xml
- Check that all imports are correct

### Runtime Issues
- Enable internet permission in device settings
- Check Logcat for detailed error messages
- Verify ConvertAPI account status

### API Issues
- Test your secret key with ConvertAPI's web interface first
- Check ConvertAPI status page for service availability
- Ensure your account has sufficient credits

## Support

For ConvertAPI-specific issues:
- Visit [ConvertAPI Support](https://www.convertapi.com/support)
- Check [API Documentation](https://www.convertapi.com/doc)

For app-specific issues:
- Check Android Logcat for error messages
- Verify file permissions and storage access
