# SpotNBook Flask API

This Flask API serves as the backend for the SpotNBook mobile application, providing endpoints for image recognition, search engine and recommendation.

When hosting Flask locally, it relies on the network connection of your device. Therefore, after deploying the Flask app, it's necessary to manually update the target request IP address in the Android Studio project file.

## Endpoints

/extract_text       : Image Processing
/search             : Search Engine
--/recommendation   : Recommendation

## Dependencies

- Python 3.12
- Flask
- Levenshtein
- gensim
- firebase_admin
- requests
- PIL
- pytesseract
- re