from flask import Flask, Blueprint, request, jsonify

# For Calculate Levenshtein Distance (Similarity of Text)
import Levenshtein

# For Word to Vector (Check similarity between words)
from gensim.models import KeyedVectors, Word2Vec

# Firebase Dependencies
import firebase_admin
from firebase_admin import credentials, firestore

import time

# Firebase Dependencies
import firebase_admin
from firebase_admin import credentials, firestore

# Initialize Firebase
cred = credentials.Certificate("spotnbook-b562b-firebase-adminsdk-aqrm1-e242bf6727.json")
firebase_admin.initialize_app(cred)

# Retrieve Firestore Instances
db = firestore.client()

# Load local file
stopwords_file = 'search_engine/stopwords.txt'
    
#----- Load Word2Vec model -----#
# Start time
start_time = time.time()

# Load Word2Vec model
print(f"Initiating Word2Vec Model...")
model = KeyedVectors.load("search_engine/word2vec-google-news-300.model")

# End time
end_time = time.time()
loading_time = end_time - start_time
print(f"Model Initiated, Loading time: {loading_time} seconds")

#-----User Searching-----#
def search_users(keyword):
    # User Collection and Merchant Collection
    users_ref = db.collection("users")
    merchants_ref = db.collection("merchants")
    
    # Retrieve username and merchantname
    usernames = {doc.id: doc.to_dict()["username"].lower() for doc in users_ref.get()}
    merchant_names = {doc.id: doc.to_dict()["merchantName"].lower() for doc in merchants_ref.get()}
    
    similar_results = []
    
    # Search among users
    for user_id, username in usernames.items():
        ld_score = ld_similarity(keyword, username)
        ngram_score = ngram_similarity(keyword, username)
        
        # Calculate combined similarity score
        combined_similarity = (ld_score + ngram_score) / 2
        
        similar_results.append((user_id, combined_similarity, 'user'))
    
    # Search among merchants
    for merchant_id, merchant_name in merchant_names.items():
        ld_score = ld_similarity(keyword, merchant_name)
        ngram_score = ngram_similarity(keyword, merchant_name)
        
        # Calculate combined similarity score
        combined_similarity = (ld_score + ngram_score) / 2
        
        similar_results.append((merchant_id, combined_similarity, 'merchant'))
    
    # Sort from High to Low with Combined Similarity
    similar_results.sort(key=lambda x: x[1], reverse=True)
    
    return similar_results

def ld_similarity(keyword, text):
    # Calculate Lavenshtein Distance
    distance = Levenshtein.distance(keyword, text)
    
    # Calculate Similarity using = 1 - (distance / max_length)
    similarity = 1 - (distance / max(len(keyword), len(text)))
    return similarity

def ngram_similarity(keyword, text):
    n = 2
    keyword_ngrams = set([keyword[i:i+n] for i in range(len(keyword)-n+1)])
    text_ngrams = set([text[i:i+n] for i in range(len(text)-n+1)])
    common_ngrams = keyword_ngrams.intersection(text_ngrams)
    similarity = len(common_ngrams) / max(len(keyword_ngrams), len(text_ngrams))
    return similarity

#-----Post Searching-----#
def load_stopwords(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        stopwords = [line.strip() for line in file]
    return stopwords

def search_posts(keyword):
    posts_ref = db.collection("posts")
    posts_caption = {doc.id: doc.to_dict()["postCaption"].lower() for doc in posts_ref.get()}
    
    similar_posts = []
    
    stopwords = load_stopwords(stopwords_file)
    
    for post_id, caption in posts_caption.items():
        if (keyword == caption):
            similar_posts.append((post_id, caption, 1))
        else:
            keyword_chunks = keyword.split()
            words = caption.split()
            unique_words = set(words)
            
            filtered_words = [word for word in unique_words if word not in stopwords]
            
            total_similarity = 0
            num_words = len(filtered_words)
            
            for word in filtered_words:
                for chunk in keyword_chunks:
                    try:
                        word_similarity = model.similarity(chunk, word)
                        if word_similarity < 0.5: #If similarity less than 0.5, ignore it
                            continue
                        
                        total_similarity += word_similarity
                        
                    except KeyError: #Keyword not recognized, skipped
                        continue
            
            # Normalize similarity with (Total Similarity / Num of words (stopword excluded))
            normalized_similarity = total_similarity / num_words
            
            similar_posts.append((post_id, caption, normalized_similarity))
    
    # Sort from highest to lowest
    sorted_similar_posts = sorted(similar_posts, key=lambda x: x[2], reverse=True)
    return sorted_similar_posts

def calculate_similarity(keyword, words):
    total_similarity = 0
    for word in words:
        try:
            word_similarity = model.similarity(keyword, word)
            total_similarity += word_similarity
        except KeyError:
            continue
    return total_similarity

search_engine_bp = Blueprint('search_engine_bp', __name__)
@search_engine_bp.route('/search', methods=['POST'])
def search():
    keyword = request.json.get('keyword')
    
    search_users_result = search_users(keyword)
    search_posts_result = search_posts(keyword)
    
    # Combine Results
    combined_results = {
        "users": search_users_result,
        "posts": search_posts_result
    }
        
    # Return the combined results as JSON
    return jsonify(combined_results)