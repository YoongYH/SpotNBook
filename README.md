# SpotNBook Flask API

This Flask API serves as the backend for the SpotNBook mobile application, providing endpoints for image recognition, search engine and recommendation.

When hosting Flask locally, it relies on the network connection of your device. Therefore, after deploying the Flask app, it's necessary to manually update the target request IP address in the Android Studio project file.

Due to the large file size, the word2vec model can download from here: https://huggingface.co/fse/word2vec-google-news-300/tree/main
Usage: Download "word2vec-google-news-300.model" and "word2vec-google-news-300.model.vectors.npy" and place inside ./FlaskApp/search_engine

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
