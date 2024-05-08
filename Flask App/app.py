from flask import Flask
from image2text.app import image_recognition_bp
from search_engine.search_engine import search_engine_bp
#from user_post_recommendation.recommendation import recommendation_bp

app = Flask(__name__)

# Register blueprints with db passed as argument
app.register_blueprint(image_recognition_bp)
app.register_blueprint(search_engine_bp)
#app.register_blueprint(recommendation_bp)

if __name__ == "__main__":
    app.run(host='192.168.1.42', port=5000)